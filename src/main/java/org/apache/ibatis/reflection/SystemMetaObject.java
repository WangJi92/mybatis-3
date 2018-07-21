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

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * Mybatis 系统中元数据处理对象类，包含的很多的默认的方法
 * @author Clinton Begin
 */
public final class SystemMetaObject {
  /**
   * 默认对象创建器
   */
  public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

  /**
   * 默认工厂包装器、感觉没有进行什么处理啊，其实就是为了包装类扩展的处理
   */
  public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

  /**
   * 空对象元数据信息
   */
  public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());

  private SystemMetaObject() {
    // Prevent Instantiation of Static Class
  }

  private static class NullObject {
  }

  /**
   * 使用默认的配置创建元数据对象信息
   * @param object
   * @return
   */
  public static MetaObject forObject(Object object) {
    return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
  }

}
