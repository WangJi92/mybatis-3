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
package org.apache.ibatis.executor.resultset;

import org.apache.ibatis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * [MyBatis框架的使用及源码分析(十三) ResultSetHandler](https://www.cnblogs.com/zsg88/p/7572021.html)
 *
 * <p>在PreparedStatementHandler中的query()方法中，用ResultSetHandler来完成结果集的映射。</p>
 * ResultSetHandler负责处理两件事：
 （1）处理Statement执行后产生的结果集，生成结果列表
 （2）处理存储过程执行后的输出参数
 ResultSetHandler是一个接口，提供了两个函数分别用来处理普通操作和存储过程的结果
 *
 * @author Clinton Begin
 */
public interface ResultSetHandler {

  /**
   * 对普通查询到的结果转换
   * @param stmt
   * @param <E>
   * @return
   * @throws SQLException
   */
  <E> List<E> handleResultSets(Statement stmt) throws SQLException;

  /**
   * 对于查询游标的结果进行转换
   * @param stmt
   * @param <E>
   * @return
   * @throws SQLException
   */
  <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

  /**
   * 调用存储过程返回结果，将结果值放在参数中
   * @param cs
   * @throws SQLException
   */
  void handleOutputParameters(CallableStatement cs) throws SQLException;

}
