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
package org.apache.ibatis.transaction;

import org.apache.ibatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * 创建事物实例
 * Creates {@link Transaction} instances.
 * [Mybatis3.3.x技术内幕（三）：Mybatis事务管理（将颠覆你心中目前对事务的理解）](https://my.oschina.net/zudajun/blog/666764)
 *
 * @author Clinton Begin
 */
public interface TransactionFactory {

  /**
   * 自定义属性集事务工厂。
   * Sets transaction factory custom properties.
   * @param props
   */
  void setProperties(Properties props);

  /**
   * 创建一个事务从一个现有的连接
   * Creates a {@link Transaction} out of an existing connection.
   * @param conn Existing database connection
   * @return Transaction
   * @since 3.1.0
   */
  Transaction newTransaction(Connection conn);
  
  /**
   * 创建一个事务从一个. 数据源
   * Creates a {@link Transaction} out of a datasource.
   * @param dataSource DataSource to take the connection from 数据源连接
   * @param level Desired isolation level 所需的隔离级别
   * @param autoCommit Desired autocommit 想要自动提交
   * @return Transaction
   * @since 3.1.0
   */
  Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
