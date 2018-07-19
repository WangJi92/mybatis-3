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
package org.apache.ibatis.mapping;

/**
 * 代表的内容映射语句读取XML文件或一个注解。它创建的SQL将被传递到数据库收到用户的输入参数
 * Represents the content of a mapped statement read from an XML file or an annotation. 
 * It creates the SQL that will be passed to the database out of the input parameter received from the user.
 *
 * @author Clinton Begin
 */
public interface SqlSource {

  /**
   *  这里获取BoundSQL 需要对于 ONGL的数据进行解析，还需要讲SQL中预处理的参数类型进行处理
   *  返回的 BoundSql 已经是处理好的 ？？？，然后又参数的MapperMapping等等数据
   * @param parameterObject  调用数据库时候传递的参数
   * @return
   */
  BoundSql getBoundSql(Object parameterObject);

}
