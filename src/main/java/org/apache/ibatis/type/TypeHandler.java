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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *<p>[MyBatis源码解析（十）——Type类型模块之类型处理器TypeHandler](https://www.cnblogs.com/V1haoge/p/6715063.html)</p>
 * @author Clinton Begin
 * 第一类是设置参数的方法setParameter()，这个方法是用于设置数据库操作的参数，例如查询参数、删除参数、更新参数等
 * 另一类是用于取得结果的方法，这一类方法又细分为两大种，第一种是从结果集中获取结果，按照获取的方式分为两种：
 * 一种是通过列名（columnName）来获取，另一种是通过列下标（columnIndex）来获取，
 * 这两种获取方式正对应我们直接使用JDBC进行数据库查询结果中获取数据的两种方式，
 * 第二种是针对存储过程而设，通过列下标的方式来获取存储过程输出结果中的数据
 *
 * 总的来说类型处理器就是两方面的作用，一方面将Java类型的参数（T prarameter）设置到数据库操作脚本中（匹配数据库类型jdbcType）
 * ，另一种是获取操作结果到Java类型（T）中
 */
public interface TypeHandler<T> {

  /**
   * 设置参数的类型哦
   * @param ps  预处理参数
   * @param i
   * @param parameter
   * @param jdbcType
   * @throws SQLException
   */
  void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

  /**
   * 根据列的名称获取结果
   * @param rs
   * @param columnName
   * @return
   * @throws SQLException
   */
  T getResult(ResultSet rs, String columnName) throws SQLException;

  /**
   * 根据列的顺序获取结果
   * @param rs
   * @param columnIndex
   * @return
   * @throws SQLException
   */
  T getResult(ResultSet rs, int columnIndex) throws SQLException;

  /**
   * 存储过程 根据列的名称获取结果
   * @param cs
   * @param columnIndex
   * @return
   * @throws SQLException
   */
  T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
