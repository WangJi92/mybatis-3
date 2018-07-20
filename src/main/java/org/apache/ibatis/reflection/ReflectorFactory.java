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
package org.apache.ibatis.reflection;

public interface ReflectorFactory {

  /**
   * 是否允许缓存保存的Class中的属性的信息
   * [Mybatis3.3.x技术内幕（七）：Mybatis初始化之六个工具](https://my.oschina.net/zudajun/blog/668596)
   * @return
   */
  boolean isClassCacheEnabled();

  void setClassCacheEnabled(boolean classCacheEnabled);

  /**
   * 找到当前类中的Reflector 信息，这个类代表一组缓存的类定义的信息,允许简单的属性名之间的映射和getter / setter方法
   * 通过封装 反射，方便调用属性的字段、方法等等，通过反射进行调用
   * @param type
   * @return
   */
  Reflector findForClass(Class<?> type);
}