/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL处理
 * @author Clinton Begin
 */
public class StaticSqlSource implements SqlSource {

  /**
   * SQL语句
   */
  private final String sql;

  /**
   * 预处理参数
   */
  private final List<ParameterMapping> parameterMappings;

  /**
   * mybatis核心配置哦
   */
  private final Configuration configuration;

  public StaticSqlSource(Configuration configuration, String sql) {
    this(configuration, sql, null);
  }

  /**
   * 将已经通过 解析完成的 SQL数据进行包装，只含有 ？？ ？？ ？？ 这种类型的数据
   * @param configuration
   * @param sql  ？？？ 类型的数据
   * @param parameterMappings  这些问号对应的参数数据的类型，parameterMappings不是实际的数据，只是对于数据的类型格式进行简单的数据结构描述性封装
   */
  public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.configuration = configuration;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return new BoundSql(configuration, sql, parameterMappings, parameterObject);
  }

}
