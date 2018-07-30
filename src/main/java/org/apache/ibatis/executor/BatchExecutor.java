/**
 *    Copyright 2009-2018 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * [Mybatis3.3.x技术内幕（四）：五鼠闹东京之执行器Executor设计原本](https://my.oschina.net/zudajun/blog/667214)
 *
 * @author Jeff Butler 
 */
public class BatchExecutor extends BaseExecutor {

  /**
   * 批量更新返回值
   */
  public static final int BATCH_UPDATE_RETURN_VALUE = Integer.MIN_VALUE + 1002;

  /**
   * 批量更新的StateMent
   */
  private final List<Statement> statementList = new ArrayList<>();

  /**
   *  对应的结果集（主要保存了update结果的count数量）
   */
  private final List<BatchResult> batchResultList = new ArrayList<>();

  /**
   *  当前保存的sql，即上次执行的sql
   */
  private String currentSql;

  /**
   * 当前处理的MapperStateMent
   */
  private MappedStatement currentStatement;

  public BatchExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  /**
   * 需要注意的是sql.equals(currentSql)和statementList.get(last)，充分说明了其有序逻辑：AABB，将生成2个Statement对象；
   * AABBAA，将生成3个Statement对象，而不是2个。因为，只要sql有变化，将导致生成新的Statement对象。
   *
   * 缓存了这么多Statement批处理对象，何时执行它们？在doFlushStatements()方法中完成执行stmt.executeBatch()，随即关闭这些Statement对象。读者可自行查看。
   *这里所说的Statement，可以是Statement或Preparestatement。
   *注：对于批处理来说，JDBC只支持update操作（update、insert、delete等），不支持select查询操作。
   *
   * [使用JDBC进行批处理](https://blog.csdn.net/yerenyuan_pku/article/details/52304317)
   *
   采用PreparedStatement.addBatch()方式实现批处理的优缺点
   采用PreparedStatement.addBatch()实现批处理
   优点：发送的是预编译后的SQL语句，执行效率高。
   缺点：只能应用在SQL语句相同，但参数不同的批处理中。因此此种形式的批处理经常用于在同一个表中批量插入数据，或批量更新表的数据。
   *
   *
   * @param ms
   * @param parameterObject
   * @return
   * @throws SQLException
   */
  @Override
  public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
    final Configuration configuration = ms.getConfiguration();
    final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
    final BoundSql boundSql = handler.getBoundSql();

    //本次执行更新的SQL
    final String sql = boundSql.getSql();
    final Statement stmt;

    //要求当前的sql和上一次的currentSql相同，同时MappedStatement也必须相同
    if (sql.equals(currentSql) && ms.equals(currentStatement)) {
      int last = statementList.size() - 1;

      //已经存在Statement，取出最后一个Statement，有序
      stmt = statementList.get(last);
      applyTransactionTimeout(stmt);

      //设置StateMent的参数信息
      //fix Issues 322
      handler.parameterize(stmt);
      BatchResult batchResult = batchResultList.get(last);

      //添加一个不同的参数信息
      batchResult.addParameterObject(parameterObject);
    } else {
      // 尚不存在，新建Statement
      Connection connection = getConnection(ms.getStatementLog());

      //创建一个新的StateMent对象信息
      stmt = handler.prepare(connection, transaction.getTimeout());

      //设置StateMent的参数信息
      //fix Issues 322
      handler.parameterize(stmt);

      //上一次执行的SQL信息
      currentSql = sql;
      currentStatement = ms;
      statementList.add(stmt);
      batchResultList.add(new BatchResult(ms, sql, parameterObject));
    }
  // handler.parameterize(stmt);

    /**
     * 将当前的Statement添加到批处理中去
     * [使用JDBC进行批处理](https://blog.csdn.net/yerenyuan_pku/article/details/52304317)
     */
    handler.batch(stmt);
    return BATCH_UPDATE_RETURN_VALUE;
  }

  @Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
      throws SQLException {
    Statement stmt = null;
    try {
      //执行查询的时候回自动更新掉批处理的数据信息
      flushStatements();
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameterObject, rowBounds, resultHandler, boundSql);
      Connection connection = getConnection(ms.getStatementLog());
      stmt = handler.prepare(connection, transaction.getTimeout());
      handler.parameterize(stmt);
      return handler.<E>query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }

  @Override
  protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
    flushStatements();
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
    Connection connection = getConnection(ms.getStatementLog());
    Statement stmt = handler.prepare(connection, transaction.getTimeout());
    handler.parameterize(stmt);
    return handler.<E>queryCursor(stmt);
  }


  /**
   *  缓存了这么多Statement批处理对象，何时执行它们？在doFlushStatements()方法中完成执行stmt.executeBatch()，随即关闭这些Statement对象。读者可自行查看。
   *  这里所说的Statement，可以是Statement或Preparestatement。
   *  注：对于批处理来说，JDBC只支持update操作（update、insert、delete等），不支持select查询操作。
   *
   采用PreparedStatement.addBatch()方式实现批处理的优缺点
   采用PreparedStatement.addBatch()实现批处理
   优点：发送的是预编译后的SQL语句，执行效率高。
   缺点：只能应用在SQL语句相同，但参数不同的批处理中。因此此种形式的批处理经常用于在同一个表中批量插入数据，或批量更新表的数据。
   * @param isRollback
   * @return
   * @throws SQLException
   */
  @Override
  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    try {
      List<BatchResult> results = new ArrayList<>();

      //回滚的化就不执行啦~
      if (isRollback) {
        return Collections.emptyList();
      }
      for (int i = 0, n = statementList.size(); i < n; i++) {
        Statement stmt = statementList.get(i);
        applyTransactionTimeout(stmt);
        BatchResult batchResult = batchResultList.get(i);
        try {
          //获取批量更新后，更新影响的行数量
          batchResult.setUpdateCounts(stmt.executeBatch());
          MappedStatement ms = batchResult.getMappedStatement();
          List<Object> parameterObjects = batchResult.getParameterObjects();
          KeyGenerator keyGenerator = ms.getKeyGenerator();
          if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
            Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
            jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
          } else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) { //issue #141
            for (Object parameter : parameterObjects) {
              keyGenerator.processAfter(this, ms, stmt, parameter);
            }
          }
          // Close statement to close cursor #1109
          closeStatement(stmt);
        } catch (BatchUpdateException e) {
          StringBuilder message = new StringBuilder();
          message.append(batchResult.getMappedStatement().getId())
              .append(" (batch index #")
              .append(i + 1)
              .append(")")
              .append(" failed.");
          if (i > 0) {
            message.append(" ")
                .append(i)
                .append(" prior sub executor(s) completed successfully, but will be rolled back.");
          }
          throw new BatchExecutorException(message.toString(), e, results, batchResult);
        }
        results.add(batchResult);
      }
      return results;
    } finally {
      for (Statement stmt : statementList) {
        closeStatement(stmt);
      }
      currentSql = null;
      statementList.clear();
      batchResultList.clear();
    }
  }

}
