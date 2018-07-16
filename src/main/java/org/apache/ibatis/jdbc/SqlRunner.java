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
package org.apache.ibatis.jdbc;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.*;
import java.util.*;

/**
 * @author Clinton Begin
 */
public class SqlRunner {

  public static final int NO_GENERATED_KEY = Integer.MIN_VALUE + 1001;

  /**
   * 数据库连接
   */
  private final Connection connection;

  /**
   * 类型转换工厂
   */
  private final TypeHandlerRegistry typeHandlerRegistry;

  /**
   * <P>主键自动增长策略</P>
   * <P>
   *   [mybatis 中哪些数据库支持 useGeneratedKeys="true"](https://blog.csdn.net/sinat_30474567/article/details/75221516)
   *   [深入浅出mybatis之useGeneratedKeys参数用法](https://www.cnblogs.com/nuccch/p/9069644.html)
   * </P>
   *
   */
  private boolean useGeneratedKeySupport;

  public SqlRunner(Connection connection) {
    this.connection = connection;
    this.typeHandlerRegistry = new TypeHandlerRegistry();
  }

  public void setUseGeneratedKeySupport(boolean useGeneratedKeySupport) {
    this.useGeneratedKeySupport = useGeneratedKeySupport;
  }

  /**
   * 执行返回一行的SELECT语句
   * Executes a SELECT statement that returns one row.
   *
   * @param sql  The SQL 需要执行的SQL的语句
   * @param args The arguments to be set on the statement. 要在声明中设置的参数
   * @return The row expected.期待的数据的信息
   * @throws SQLException If less or more than one row is returned 如果返回少于或多于一行 返回异常信息
   */
  public Map<String, Object> selectOne(String sql, Object... args) throws SQLException {
    List<Map<String, Object>> results = selectAll(sql, args);
    if (results.size() != 1) {
      throw new SQLException("Statement returned " + results.size() + " results where exactly one (1) was expected.");
    }
    return results.get(0);
  }

  /**
   * Executes a SELECT statement that returns multiple rows.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The list of rows expected.
   * @throws SQLException If statement preparation or execution fails
   */
  public List<Map<String, Object>> selectAll(String sql, Object... args) throws SQLException {
    PreparedStatement ps = connection.prepareStatement(sql);
    try {
      //设置预处理参数值
      setParameters(ps, args);

      //获取返回的结果
      ResultSet rs = ps.executeQuery();

      //处理转换结果
      return getResults(rs);
    } finally {
      try {
        ps.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  /**
   * <p>
   *  [通过getGeneratedKeys()获取主键](https://blog.csdn.net/qq_39147516/article/details/78439780)
   *  [java getGeneratedKeys(获取自动递增主键)一个小问题](https://www.cnblogs.com/wumian/articles/2012-10-20-1204.html)
   * </p>
   * Executes an INSERT statement.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched. 影响的行数或批处理结果,如果批处理语句
   * @throws SQLException If statement preparation or execution fails
   */
  public int insert(String sql, Object... args) throws SQLException {
    PreparedStatement ps;

    //自动生成主键策略,返回自动增加的主键的信息
    if (useGeneratedKeySupport) {
      ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    } else {
      ps = connection.prepareStatement(sql);
    }

    try {
      setParameters(ps, args);
      ps.executeUpdate();
      if (useGeneratedKeySupport) {
        List<Map<String, Object>> keys = getResults(ps.getGeneratedKeys());
        if (keys.size() == 1) {
          Map<String, Object> key = keys.get(0);
          Iterator<Object> i = key.values().iterator();
          if (i.hasNext()) {
            Object genkey = i.next();
            if (genkey != null) {
              try {
                return Integer.parseInt(genkey.toString());
              } catch (NumberFormatException e) {
                //ignore, no numeric key support
              }
            }
          }
        }
      }
      return NO_GENERATED_KEY;
    } finally {
      try {
        ps.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  /**
   * 更新参数一样的，设置参数也是一样的哦
   * Executes an UPDATE statement.
   *
   * @param sql  The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
   * @throws SQLException If statement preparation or execution fails
   */
  public int update(String sql, Object... args) throws SQLException {
    PreparedStatement ps = connection.prepareStatement(sql);
    try {
      setParameters(ps, args);
      return ps.executeUpdate();
    } finally {
      try {
        ps.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  /**
   * Executes a DELETE statement.
   *
   * @param sql  The SQL  SQL参数
   * @param args The arguments to be set on the statement. 设置的参数声明
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched. 影响的行数或批处理结果,如果批处理语句
   * @throws SQLException If statement preparation or execution fails
   */
  public int delete(String sql, Object... args) throws SQLException {
    return update(sql, args);
  }

  /**
   * Executes any string as a JDBC Statement. 执行任何字符串作为JDBC语句。
   * Good for DDL
   *
   * @param sql The SQL
   * @throws SQLException If statement preparation or execution fails
   */
  public void run(String sql) throws SQLException {
    Statement stmt = connection.createStatement();
    try {
      stmt.execute(sql);
    } finally {
      try {
        stmt.close();
      } catch (SQLException e) {
        //ignore
      }
    }
  }

  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      //ignore
    }
  }

  /**
   * <P>Null 是Mybatis 中自己定义的几种常见的类型转换枚举信息</P>
   * 1、首先看看是否为默认的枚举信息
   * 2、不是的话看看是否为自定义的类型转换TypeHandler 处理器
   * 3、如果都没有那就异常处理啦，通过调用类型转换函数进行设置SQL预处理的参数的类型的值
   * @param ps 这个底层SQL，防止SQL注入
   * @param args  需要设置在SQL中的每一个数据的信息
   * @throws SQLException
   */
  private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
    for (int i = 0, n = args.length; i < n; i++) {
      if (args[i] == null) {
        throw new SQLException("SqlRunner requires an instance of Null to represent typed null values for JDBC compatibility");
      } else if (args[i] instanceof Null) {
        ((Null) args[i]).getTypeHandler().setParameter(ps, i + 1, null, ((Null) args[i]).getJdbcType());
      } else {
        TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(args[i].getClass());
        if (typeHandler == null) {
          throw new SQLException("SqlRunner could not find a TypeHandler instance for " + args[i].getClass());
        } else {
          typeHandler.setParameter(ps, i + 1, args[i], null);
        }
      }
    }
  }

  /**
   * <p>[利用JDBC ResultSetMetaData 将数据反射到实体类中](https://blog.csdn.net/u010018421/article/details/53435728)</p>
   * @param rs
   * @return
   * @throws SQLException
   */
  private List<Map<String, Object>> getResults(ResultSet rs) throws SQLException {
    try {
      List<Map<String, Object>> list = new ArrayList<>();
      List<String> columns = new ArrayList<>();
      List<TypeHandler<?>> typeHandlers = new ArrayList<>();
      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
        //获取数据库中 as信息字段名称
        columns.add(rsmd.getColumnLabel(i + 1));
        try {
          //rsmd.getColumnClassName 获取到当前类类型的全限定Java类型名称 java.lang.String
          Class<?> type = Resources.classForName(rsmd.getColumnClassName(i + 1));
          TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(type);
          if (typeHandler == null) {
            typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
          }
          typeHandlers.add(typeHandler);
        } catch (Exception e) {
          typeHandlers.add(typeHandlerRegistry.getTypeHandler(Object.class));
        }
      }
      while (rs.next()) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 0, n = columns.size(); i < n; i++) {
          String name = columns.get(i);
          TypeHandler<?> handler = typeHandlers.get(i);

          //将字段的名称，还有结果放置在map中处理
          row.put(name.toUpperCase(Locale.ENGLISH), handler.getResult(rs, name));
        }
        list.add(row);
      }
      return list;
    } finally {
      if (rs != null) {
        try {
            rs.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }

}
