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

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;

import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Mybatis 中最主要的接口，通过这个接口可以执行命令,映射器和管理事务。
 * <P>[Mybatis源码阅读之SqlSession创建](https://blog.csdn.net/u012734723/article/details/78184866)</P>
 * The primary Java interface for working with MyBatis.
 * Through this interface you can execute commands, get mappers and manage transactions.
 *
 * @author Clinton Begin
 */
public interface SqlSession extends Closeable {

  /**
   * Retrieve a single row mapped from the statement key 从声明中检索一行映射键
   * @param <T> the returned object type
   * @param statement
   * @return Mapped object
   */
  <T> T selectOne(String statement);

  /**
   * Retrieve a single row mapped from the statement key and parameter. 从声明中检索一行映射键和参数
   * @param <T> the returned object type
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @return Mapped object
   */
  <T> T selectOne(String statement, Object parameter);

  /**
   * Retrieve a list of mapped objects from the statement key and parameter. 从声明中检索一个映射对象列表键和参数
   * @param <E> the returned list element type
   * @param statement Unique identifier matching the statement to use.
   * @return List of mapped object
   */
  <E> List<E> selectList(String statement);

  /**
   * Retrieve a list of mapped objects from the statement key and parameter. 从声明中检索一个映射对象列表键和参数
   * @param <E> the returned list element type
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @return List of mapped object
   */
  <E> List<E> selectList(String statement, Object parameter);

  /**
   * 返回List数据信息
   * Retrieve a list of mapped objects from the statement key and parameter,
   * within the specified row bounds.
   * @param <E> the returned list element type 返回的列表元素类型
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @param rowBounds  Bounds to limit object retrieval
   * @return List of mapped object
   */
  <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

  /**
   * The selectMap is a special case in that it is designed to convert a list
   * of results into a Map based on one of the properties in the resulting
   * objects.
   * Eg. Return a of Map[Integer,Author] for selectMap("selectAuthors","id")
   * @param <K> the returned Map keys type
   * @param <V> the returned Map values type
   * @param statement Unique identifier matching the statement to use.
   * @param mapKey The property to use as key for each value in the list.
   * @return Map containing key pair data.
   */
  <K, V> Map<K, V> selectMap(String statement, String mapKey);

  /**
   * List 数据转为Map
   * The selectMap is a special case in that it is designed to convert a list
   * of results into a Map based on one of the properties in the resulting
   * objects.
   * @param <K> the returned Map keys type
   * @param <V> the returned Map values type
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @param mapKey The property to use as key for each value in the list.
   * @return Map containing key pair data.
   */
  <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);

  /**
   * 选择映射是一个特例,它被设计成一个结果列表转换成基于地图的一个属性在结果对象。
   * The selectMap is a special case in that it is designed to convert a list
   * of results into a Map based on one of the properties in the resulting
   * objects.
   * @param <K> the returned Map keys type 返回的键映射类型
   * @param <V> the returned Map values type 地图返回值类型
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @param mapKey The property to use as key for each value in the list. 作为列表中的每个值的键
   * @param rowBounds  Bounds to limit object retrieval
   * @return Map containing key pair data.
   */
  <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);

  /**
   * 游标提供相同的结果作为一个列表,除了它懒洋洋地获取数据使用一个迭代器。
   * A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.
   * @param <T> the returned cursor element type.
   * @param statement Unique identifier matching the statement to use.
   * @return Cursor of mapped objects
   */
  <T> Cursor<T> selectCursor(String statement);

  /**
   * A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.
   * @param <T> the returned cursor element type.
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @return Cursor of mapped objects
   */
  <T> Cursor<T> selectCursor(String statement, Object parameter);

  /**
   * 游标提供相同的结果作为一个列表,除了它懒洋洋地获取数据使用一个迭代器
   * A Cursor offers the same results as a List, except it fetches data lazily using an Iterator.
   * @param <T> the returned cursor element type.返回的指针元素类型。
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @param rowBounds  Bounds to limit object retrieval 范围限制对象检索
   * @return Cursor of mapped objects
   */
  <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds);

  /**
   * 从声明中检索一行映射键和参数 using a {@code ResultHandler}.
   * Retrieve a single row mapped from the statement key and parameter
   * using a {@code ResultHandler}.
   * @param statement Unique identifier matching the statement to use.
   * @param parameter A parameter object to pass to the statement.
   * @param handler ResultHandler that will handle each retrieved row
   */
  void select(String statement, Object parameter, ResultHandler handler);

  /**
   * 从声明中检索一行映射 using a {@code ResultHandler}.
   * Retrieve a single row mapped from the statement
   * using a {@code ResultHandler}.
   * @param statement Unique identifier matching the statement to use.
   * @param handler ResultHandler that will handle each retrieved row
   */
  void select(String statement, ResultHandler handler);

  /**
   * 从声明中检索一行映射键和参数使用 using a {@code ResultHandler} and {@code RowBounds}
   * Retrieve a single row mapped from the statement key and parameter
   * using a {@code ResultHandler} and {@code RowBounds}
   * @param statement Unique identifier matching the statement to use.
   * @param rowBounds RowBound instance to limit the query results 行限制查询结果绑定实例
   * @param handler ResultHandler that will handle each retrieved row 检索结果处理程序将处理每一行
   */
  void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);

  /**
   * 执行insert语句。
   * Execute an insert statement.
   * @param statement Unique identifier matching the statement to execute. 唯一标识符匹配语句来执行
   * @return int The number of rows affected by the insert. int影响插入的行数
   */
  int insert(String statement);

  /**
   * 用给定的参数对象执行insert语句。任何生成的自动增量值或选择关键条目将修改给定的参数对象属性。只有将返回受影响的行数。
   * Execute an insert statement with the given parameter object. Any generated
   * autoincrement values or selectKey entries will modify the given parameter
   * object properties. Only the number of rows affected will be returned.
   * @param statement Unique identifier matching the statement to execute.
   * @param parameter A parameter object to pass to the statement.
   * @return int The number of rows affected by the insert.
   */
  int insert(String statement, Object parameter);

  /**
   * 执行一个更新语句。将返回受影响的行数
   * Execute an update statement. The number of rows affected will be returned.
   * @param statement Unique identifier matching the statement to execute.
   * @return int The number of rows affected by the update.
   */
  int update(String statement);

  /**
   * 执行一个更新语句。将返回受影响的行数。
   * Execute an update statement. The number of rows affected will be returned.
   * @param statement Unique identifier matching the statement to execute.
   * @param parameter A parameter object to pass to the statement.
   * @return int The number of rows affected by the update.
   */
  int update(String statement, Object parameter);

  /**
   * 执行delete语句。将返回受影响的行数。
   * Execute a delete statement. The number of rows affected will be returned.
   * @param statement Unique identifier matching the statement to execute.
   * @return int The number of rows affected by the delete.
   */
  int delete(String statement);

  /**
   * 执行delete语句。将返回受影响的行数
   * Execute a delete statement. The number of rows affected will be returned.
   * @param statement Unique identifier matching the statement to execute. 唯一标识符匹配语句来执行。
   * @param parameter A parameter object to pass to the statement.
   * @return int The number of rows affected by the delete.
   */
  int delete(String statement, Object parameter);

  /**
   * 将批处理语句和提交数据库连接；注意,数据库连接将不会承诺如果没有更新/删除/插入
   * Flushes batch statements and commits database connection.
   * Note that database connection will not be committed if no updates/deletes/inserts were called.
   * To force the commit call {@link SqlSession#commit(boolean)}
   */
  void commit();

  /**
   * 将批处理语句和提交数据库连接
   * Flushes batch statements and commits database connection.
   * @param force forces connection commit 强制提交
   */
  void commit(boolean force);

  /**
   * 丢弃等待批处理语句和回滚数据库连接；注意,数据库连接将不会回滚,如果没有更新/删除/插入。
   * Discards pending batch statements and rolls database connection back.
   * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
   * To force the rollback call {@link SqlSession#rollback(boolean)}
   */
  void rollback();

  /**
   *
   丢弃等待批处理语句和回滚数据库连接；注意,数据库连接将不会回滚,如果没有更新/删除/插入。
   * Discards pending batch statements and rolls database connection back.
   * Note that database connection will not be rolled back if no updates/deletes/inserts were called.
   * @param force forces connection rollback 强制连接回滚
   */
  void rollback(boolean force);

  /**
   * 将批处理语句
   * Flushes batch statements.
   * @return BatchResult list of updated records 批处理结果列表的更新记录
   * @since 3.0.6
   */
  List<BatchResult> flushStatements();

  /**
   * Closes the session 关闭会话
   */
  @Override
  void close();

  /**
   * Clears local session cache 清除本地会话缓存
   */
  void clearCache();

  /**
   * 检索当前配置
   * Retrieves current configuration
   * @return Configuration
   */
  Configuration getConfiguration();

  /**
   * 检索一个Mapper
   * Retrieves a mapper.
   * @param <T> the mapper type 当前mapper的类型
   * @param type Mapper interface class mapper的接口
   * @return a mapper bound to this SqlSession 映射绑定到这个Sql会话
   */
  <T> T getMapper(Class<T> type);

  /**
   * 获取内部数据库连接
   * Retrieves inner database connection
   * @return Connection
   */
  Connection getConnection();
}
