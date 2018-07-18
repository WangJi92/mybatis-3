/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.session.defaults;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private final Configuration configuration;

  /**
   * 穿件这个DefaultSqlSessionFactory 最需要的就是配置文件中的信息
   * @param configuration
   */
  public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  //region 各种图条件下的创建 SqlSession
  @Override
  public SqlSession openSession() {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
  }

  @Override
  public SqlSession openSession(boolean autoCommit) {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
  }

  @Override
  public SqlSession openSession(ExecutorType execType) {
    return openSessionFromDataSource(execType, null, false);
  }

  @Override
  public SqlSession openSession(TransactionIsolationLevel level) {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
  }

  @Override
  public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
    return openSessionFromDataSource(execType, level, false);
  }

  @Override
  public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
    return openSessionFromDataSource(execType, null, autoCommit);
  }

  @Override
  public SqlSession openSession(Connection connection) {
    return openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
  }
  //endregion

  @Override
  public SqlSession openSession(ExecutorType execType, Connection connection) {
    return openSessionFromConnection(execType, connection);
  }

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * 根据配置信息 创建SqlSession 链接处理
   * @param execType
   * @param level
   * @param autoCommit
   * @return
   */
  private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Transaction tx = null;
    try {
      //// 获取配置环境中的环境信息，保护环境id、环境连接池、环境数据源等信息
      final Environment environment = configuration.getEnvironment();

      // 添加默认的事务信息属性
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);

      //根据事务隔离类型、数据源、自动提交属性创建一个事务属性
      tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);

      //创建一个执行器哦，从配置文件中，根据当前的Transaction实例
      final Executor executor = configuration.newExecutor(tx, execType);

      //通过配置信息、执行器、自动提交类型创建一个SqlSession
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      //可能会获取一个连接我们调用关闭()
      closeTransaction(tx);
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }

  /**
   *
   * @param execType  执行器的类型
   * @param connection  数据库链接
   * @return
   */
  private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
    try {
      boolean autoCommit;
      try {
        autoCommit = connection.getAutoCommit();
      } catch (SQLException e) {
        // Failover to true, as most poor drivers 故障转移为真,因为大多数可怜的drivers
        // or databases won't support transactions 或数据库不会支持事务
        autoCommit = true;
      }
      // 获取配置环境中的环境信息，保护环境id、环境连接池、环境数据源等信息
      final Environment environment = configuration.getEnvironment();

      //添加默认的事务信息属性
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);

      //从现有的链接中穿件一个事务封装的实例
      final Transaction tx = transactionFactory.newTransaction(connection);
      final Executor executor = configuration.newExecutor(tx, execType);

      //通过配置信息、执行器、自动提交类型创建一个SqlSession
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      //包装错误日志信息
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      //上面如果有异常，将规范化日志信息重置
      ErrorContext.instance().reset();
    }
  }

  /**
   * 如果环境中没有配置事务信息，使用默认的ManagedTransactionFactory，忽略commit and rollback 处理
   * @param environment
   * @return
   */
  private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
    if (environment == null || environment.getTransactionFactory() == null) {
      return new ManagedTransactionFactory();
    }
    return environment.getTransactionFactory();
  }

  /**
   * 关闭事务，关闭连接
   * @param tx
   */
  private void closeTransaction(Transaction tx) {
    if (tx != null) {
      try {
        //关闭事务
        tx.close();
      } catch (SQLException ignore) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

}
