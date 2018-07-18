/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.session;

import java.sql.Connection;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource
 * <P>
 *     从数据源中创建一个SQL回话信息
 *    [MyBatis配置详解](https://blog.csdn.net/bear_wr/article/details/52401881)
 *    [Mybatis3.3.x技术内幕（一）：SqlSession和SqlSessionFactory列传](https://my.oschina.net/zudajun/blog/665956)
 * </P>
 *
 * @author Clinton Begin
 */
public interface SqlSessionFactory {

  /**
   * 没有配置要求
   * @return
   */
  SqlSession openSession();

  /**
   * 自动提交事务
   * @param autoCommit
   * @return
   */
  SqlSession openSession(boolean autoCommit);

  /**
   * 使用之前的Connection
   * @param connection
   * @return
   */
  SqlSession openSession(Connection connection);

  /**
   * 设置事务的级别
   * @param level
   * @return
   */
  SqlSession openSession(TransactionIsolationLevel level);

  /**
   * 设置执行器类型，SIMPLE 执行器执行其它语句。REUSE 执行器可能重复使用prepared statements 语句，BATCH执行器可以重复执行语句和批量更新。
   * @param execType
   * @return
   */
  SqlSession openSession(ExecutorType execType);

  /**
   * 执行器类型和自动更新机制
   * @param execType
   * @param autoCommit
   * @return
   */
  SqlSession openSession(ExecutorType execType, boolean autoCommit);

  /**
   * 执行器类型 和 事务配置
   * @param execType
   * @param level
   * @return
   */
  SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level);

  /**
   * 在现有连接中使用执行器类型哦
   * @param execType
   * @param connection
   * @return
   */
  SqlSession openSession(ExecutorType execType, Connection connection);

  /**
   * 获取配置信息
   * @return
   */
  Configuration getConfiguration();

}
