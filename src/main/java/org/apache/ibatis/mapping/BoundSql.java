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
package org.apache.ibatis.mapping;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 一个实际的SQL字符串从一个{@link SqlSource}后加工任何动态内容；
 QL可能SQL占位符“?”和一个列表(命令)的参数映射

 每个参数的附加信息(至少输入对象的属性名值)

 也可以有额外的参数创建的动态语言(for循环,bind……)
 * An actual SQL String got from an {@link SqlSource} after having processed any dynamic content.
 * The SQL may have SQL placeholders "?" and an list (ordered) of an parameter mappings 
 * with the additional information for each parameter (at least the property name of the input object to read 
 * the value from).
 * <p>
 * Can also have additional parameters that are created by the dynamic language (for loops, bind...).
 *
 * @author Clinton Begin
 *
 * <code>
      <select id="selectPostsForBlog" parameterType="int" resultType="Post">
       select * from Post where blog_id = #{blog_id}
</select>
 * </code>
 */
public class BoundSql {

  /**
   * 字符串SQL
   */
  private final String sql;
  /**
   * 参数映射List
   */
  private final List<ParameterMapping> parameterMappings;

  /**
   * 参数对象
   */
  private final Object parameterObject;

  /**
   * what？ 这个是在SQL预先处理的过程中，动态SQl解析过程中会解析的一些数据信息 DynamicContext# context.getBindings()中获取的数据添加到
   */
  private final Map<String, Object> additionalParameters;
  /**
   * what？[Mybatis——MetaObject学习](https://blog.csdn.net/u013769320/article/details/50492965)
   * 简介：MetaObject是Mybatis提供的一个用于方便、优雅访问对象属性的对象，通过它可以简化代码、不需要try/catch各种reflect异常，同时它支持对JavaBean、Collection、Map三种类型对象的操作。
   */
  private final MetaObject metaParameters;

  /**
   *
   * @param configuration  配置文件的信息
   * @param sql  ？？？ 带有预处理的SQL数据的信息
   * @param parameterMappings  SQL中 ？？？ 对应的参数的描述信息
   * @param parameterObject  传递的实际参数
   */
  public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
    this.parameterObject = parameterObject;
    this.additionalParameters = new HashMap<>();
    this.metaParameters = configuration.newMetaObject(additionalParameters);
  }

  public String getSql() {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public boolean hasAdditionalParameter(String name) {
    String paramName = new PropertyTokenizer(name).getName();
    return additionalParameters.containsKey(paramName);
  }

  /**
   * 这里的添加的数据 实际指向  additionalParameters
   * @param name
   * @param value
   */
  public void setAdditionalParameter(String name, Object value) {
    metaParameters.setValue(name, value);
  }

  public Object getAdditionalParameter(String name) {
    return metaParameters.getValue(name);
  }
}
