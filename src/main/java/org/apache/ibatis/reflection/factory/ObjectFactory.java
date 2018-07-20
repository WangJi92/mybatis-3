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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * 使用一个ObjectFactory来创建所有需要的新对象。【就是使用来简化通过反射创建的过程】
 * MyBatis uses an ObjectFactory to create all needed new Objects.
 * 
 * @author Clinton Begin
 */
public interface ObjectFactory {

  /**
   * 设置配置属性。
   * Sets configuration properties.
   * @param properties configuration properties
   */
  void setProperties(Properties properties);

  /**
   * 使用默认构造函数创建一个新的对象。
   * Creates a new object with default constructor. 
   * @param type Object type
   * @return
   */
  <T> T create(Class<T> type);

  /**
   * 创建一个新对象的构造函数和参数指定
   * Creates a new object with the specified constructor and params.
   * @param type Object type  需要创建对象的类型
   * @param constructorArgTypes Constructor argument types 构造函数参数类型
   * @param constructorArgs Constructor argument values  构造函数参数值
   * @return
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);
  
  /**
   * 返回true,如果这个对象可以有一组的其他对象
   * Returns true if this object can have a set of other objects.
   * 这个主要是为了支持非Java 数据 比如scala 的集合对象信息
   * It's main purpose is to support non-java.util.Collection objects like Scala collections.
   * 
   * @param type Object type 对象类型
   * @return whether it is a collection or not 是否它是一个集合
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);

}
