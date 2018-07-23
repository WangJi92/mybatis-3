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
package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * [Mybatis3源码分析(12)-Sql解析执行-MetaObject](https://blog.csdn.net/ashan_li/article/details/50375466)
 * [Mybatis——MetaObject学习](https://blog.csdn.net/u013769320/article/details/50492965)
 * 简介：MetaObject是Mybatis提供的一个用于方便、优雅访问对象属性的对象，通过它可以简化代码、不需要try/catch各种reflect异常，同时它支持对JavaBean、Collection、Map三种类型对象的操作
 * @author Clinton Begin
 */
public class MetaObject {

  /**
   * 原始需要创建元数据对象的实例
   */
  private final Object originalObject;

  /**
   * 当前对象的包装对象 包装为MapWrapper、BeanWrapper、CollectionWrapper
   *
   * 包装类代理了当前MetaObject对象中好多的处理方法
   */
  private final ObjectWrapper objectWrapper;

  /**
   * 对象生成器 通过反射构造对象
   */
  private final ObjectFactory objectFactory;

  /**
   * 对象包装的扩展工厂，方便集成其他的包装类，目前是没有看到在用哦，除了测试类中
   */
  private final ObjectWrapperFactory objectWrapperFactory;

  /**
   * 反射工厂，缓存对象的Reflector 数据信息，员数据对象的所有的员数据都是由此提供
   */
  private final ReflectorFactory reflectorFactory;

  private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;

    if (object instanceof ObjectWrapper) {
      //是否当前已经被包装啦
      this.objectWrapper = (ObjectWrapper) object;
    } else if (objectWrapperFactory.hasWrapperFor(object)) {
      //方便扩展目前是没有什么用
      this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    } else if (object instanceof Map) {
      //Map的包装
      this.objectWrapper = new MapWrapper(this, (Map) object);
    } else if (object instanceof Collection) {
      //集合数据的包装
      this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    } else {
      //Bean 数据的包装
      this.objectWrapper = new BeanWrapper(this, object);
    }
  }

  public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      // 如果当前的对象为空，所以为了不返回NULL，构造一个什么都没有的空元数据~~哈哈 厉害啦，使用默认的工厂、默认的实现类
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public ReflectorFactory getReflectorFactory() {
	return reflectorFactory;
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  public String findProperty(String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  public String[] getGetterNames() {
    return objectWrapper.getGetterNames();
  }

  public String[] getSetterNames() {
    return objectWrapper.getSetterNames();
  }

  public Class<?> getSetterType(String name) {
    return objectWrapper.getSetterType(name);
  }

  public Class<?> getGetterType(String name) {
    return objectWrapper.getGetterType(name);
  }

  public boolean hasSetter(String name) {
    return objectWrapper.hasSetter(name);
  }

  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  /**
   * 有分词的获取 层次中数据的值的信息
   * @param name
   * @return
   */
  public Object getValue(String name) {
    //处理分词数据
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      //递归的调用 处理寻找当前对象的元数据 xx[0]
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        //对象值为空只能返回NULL啦
        return null;
      } else {
        //继续递归获取子数据的信息
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      //非嵌套的数据直接获取啦啦~
      return objectWrapper.get(prop);
    }
  }

  /**
   * 这里设置数据的值也是嵌套的处理，递归哦
   * @param name
   * @param value
   */
  public void setValue(String name, Object value) {
    //分词处理
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      //递归的调用 处理寻找当前对象的元数据 xx[0]
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());

      //数据实例为空
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        if (value == null && prop.getChildren() != null) {
          // don't instantiate child path if value is null 如果value为null，则不实例化子路径
          return;
        } else {
          //创建一个当前数据的MetaObject的实例
          metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
        }
      }

      //然后在设置子路径中的数据的值
      metaValue.setValue(prop.getChildren(), value);
    } else {
      //没有子路径直接处理就好啦
      objectWrapper.set(prop, value);
    }
  }

  /**
   * 找到最后一层数据的元数据对象信息
   * @param name
   * @return
   */
  public MetaObject metaObjectForProperty(String name) {
    //获取当前对象的数据值信息
    Object value = getValue(name);

    //然后获取员数据信息
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  /**
   * 获取包装数据
   * @return
   */
  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  /**
   * 当前数据是否为集合数据哦
   * @return
   */
  public boolean isCollection() {
    //和包装类中的方法很像啊，分流给包装类去处理
    return objectWrapper.isCollection();
  }

  /**
   * 集合对象中添加数据
   * @param element
   */
  public void add(Object element) {
    objectWrapper.add(element);
  }

  public <E> void addAll(List<E> list) {
    //和包装类中的方法很像啊，分流给包装类去处理
    objectWrapper.addAll(list);
  }

}
