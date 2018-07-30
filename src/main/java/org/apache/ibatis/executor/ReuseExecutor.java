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
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [Mybatis3.3.x技术内幕（四）：五鼠闹东京之执行器Executor设计原本](https://my.oschina.net/zudajun/blog/667214)
 * ReuseExecutor：执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后，不关闭Statement对象，
 * 而是放置于Map<String, Statement>内，供下一次使用。（可以是Statement或PrepareStatement对象）
 *
 * @author Clinton Begin
 */
public class ReuseExecutor extends BaseExecutor {

  /**
   * 缓存一下StateMent
   */
  private final Map<String, Statement> statementMap = new HashMap<>();

  public ReuseExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  @Override
  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Configuration configuration = ms.getConfiguration();

    //转手交给StatementHandler~
    StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);

    //从缓存中获取或者从新创建一个新的StateMent
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    return handler.update(stmt);
  }

  @Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);

    //从缓存中获取或者从新创建一个新的StateMent
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    return handler.<E>query(stmt, resultHandler);
  }

  @Override
  protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);

    //从缓存中获取或者从新创建一个新的StateMent
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    return handler.<E>queryCursor(stmt);
  }

  /**
   *ReuseExecutor就是依赖Map<String, Statement>来完成对Statement的重用的（用完不关）。
   *总不能一直不关吧？到底什么时候关闭这些Statement对象的？问的非常好。
   *方法flushStatements()就是用来处理这些Statement对象的。
   *在执行commit、rollback等动作前，将会执行flushStatements()方法，将Statement对象逐一关闭。读者可参看BaseExecutor源码。
   * @param isRollback
   * @return
   * @throws SQLException
   */
  @Override
  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    for (Statement stmt : statementMap.values()) {
      closeStatement(stmt);
    }
    statementMap.clear();
    return Collections.emptyList();
  }

  /**
   * 执行update或select，以sql作为key查找Statement对象，存在就使用，不存在就创建，用完后
   * ，不关闭Statement对象，而是放置于Map<String, Statement>内，供下一次使用。（可以是Statement或PrepareStatement对象）
   * 获取StateMent，如果存在就不在重新
   * @param handler
   * @param statementLog
   * @return
   * @throws SQLException
   */
  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    BoundSql boundSql = handler.getBoundSql();

    //sql是key，不同的sql，将产生不同的Statement
    String sql = boundSql.getSql();
    if (hasStatementFor(sql)) {

      //当前存在哦~
      stmt = getStatement(sql);

      //从获取中处理一个事务超时
      applyTransactionTimeout(stmt);
    } else {
      Connection connection = getConnection(statementLog);

      //如果不存在，根据StatementHandler，创建一个新的StateMent
      stmt = handler.prepare(connection, transaction.getTimeout());
      putStatement(sql, stmt);
    }
    handler.parameterize(stmt);
    return stmt;
  }

  /**
   * 查看当前缓存中是否存在 StateMent
   * @param sql
   * @return
   */
  private boolean hasStatementFor(String sql) {
    try {
      return statementMap.keySet().contains(sql) && !statementMap.get(sql).getConnection().isClosed();
    } catch (SQLException e) {
      return false;
    }
  }

  /**
   * 获取StateMent对象信息
   * @param s
   * @return
   */
  private Statement getStatement(String s) {
    return statementMap.get(s);
  }

  /**
   * 缓存StateMent对象信息
   * @param sql
   * @param stmt
   */
  private void putStatement(String sql, Statement stmt) {
    statementMap.put(sql, stmt);
  }

}
