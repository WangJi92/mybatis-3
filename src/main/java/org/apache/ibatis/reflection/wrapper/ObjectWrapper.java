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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 */
public interface ObjectWrapper {

  /**
   * 根据分词器获取当前对象的具体的属性的值的信息
   * @param prop
   * @return
   */
  Object get(PropertyTokenizer prop);

  /**
   * 根据分词器 设置当前对象的属性的值的信息
   * @param prop
   * @param value
   */
  void set(PropertyTokenizer prop, Object value);

  /**
   * 找到当前对象的具体的属性值的名称
   * @param name
   * @param useCamelCaseMapping 罗峰命名的方法 不是使用 _ 都会替换掉哦
   * @return
   */
  String findProperty(String name, boolean useCamelCaseMapping);

  /**
   * 获取所有的可读的字段
   * @return
   */
  String[] getGetterNames();

  /**
   * 获取所有的可写的字段
   * @return
   */
  String[] getSetterNames();

  /**
   * 获取 可写的字段的类型
   * @param name
   * @return
   */
  Class<?> getSetterType(String name);

  /**
   * 获取可读的字段的类型
   * @param name
   * @return
   */
  Class<?> getGetterType(String name);

  /**
   * 当前字段时候有set方法
   * @param name
   * @return
   */
  boolean hasSetter(String name);

  /**
   * 当前字段是否有get方法
   * @param name
   * @return
   */
  boolean hasGetter(String name);

  /**
   * 根据当前的分词器实例化属性的值
   * @param name  属性名称（这个只是打印日志用用）
   * @param prop  属性分词器
   * @param objectFactory   对象创建工厂 {@linkplain org.apache.ibatis.reflection.factory.DefaultObjectFactory 默认的对象创建神器}
   * @return
   */
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

  /**
   * 是否为集合？
   * @return
   */
  boolean isCollection();

  /**
   * 添加数据
   * @param element
   */
  void add(Object element);

  /**
   * 添加所有的数据
   * @param element
   * @param <E>
   */
  <E> void addAll(List<E> element);

}
