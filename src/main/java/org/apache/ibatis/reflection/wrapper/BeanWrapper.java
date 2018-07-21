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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 */
public class BeanWrapper extends BaseWrapper {

  /**
   * 当前处理的对象的类型哦
   */
  private final Object object;

  /**
   * 类上的元数据，方便通过反射处理
   */
  private final MetaClass metaClass;

  public BeanWrapper(MetaObject metaObject, Object object) {
    super(metaObject);
    this.object = object;
    this.metaClass = MetaClass.forClass(object.getClass(), metaObject.getReflectorFactory());
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    //分词器 有集合索引数据，证明当前分词的数据为一个集合哦
    if (prop.getIndex() != null) {
      //找到当前集合的数据 ，通过索引获取集合的值
      Object collection = resolveCollection(prop, object);
      return getCollectionValue(prop, collection);
    } else {
      //通过元数据获取数据的值的信息
      return getBeanProperty(prop, object);
    }
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, object);
      setCollectionValue(prop, collection, value);
    } else {
      setBeanProperty(prop, object, value);
    }
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    //根据元数据找到对象上的属性的方法
    return metaClass.findProperty(name, useCamelCaseMapping);
  }

  @Override
  public String[] getGetterNames() {
    return metaClass.getGetterNames();
  }

  @Override
  public String[] getSetterNames() {
    //直接被元数据Class 代理啦
    return metaClass.getSetterNames();
  }

  @Override
  public Class<?> getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return metaClass.getSetterType(name);
      } else {
        return metaValue.getSetterType(prop.getChildren());
      }
    } else {
      return metaClass.getSetterType(name);
    }
  }

  @Override
  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      // 通过元数据对象获取当前数据的类型，可能为因为当前数据没有值为空获取不到子数据MetaObject
      //所有可以通过元数据Class 获取子的类型信息
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        //直接根据类中的数据获取元数据的信息
        return metaClass.getGetterType(name);
      } else {
        return metaValue.getGetterType(prop.getChildren());
      }
    } else {
      return metaClass.getGetterType(name);
    }
  }

  @Override
  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      // 通过元数据对象获取当前数据的类型，可能为因为当前数据没有值为空获取不到子数据MetaObject
      //所有可以通过元数据Class 获取子的类型信息
      if (metaClass.hasSetter(prop.getIndexedName())) {
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
          //直接根据类中的数据获取元数据的信息
          return metaClass.hasSetter(name);
        } else {
          return metaValue.hasSetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return metaClass.hasSetter(name);
    }
  }

  @Override
  public boolean hasGetter(String name) {
    //这个属性可能为一个复合属性，需要分词器看看
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (metaClass.hasGetter(prop.getIndexedName())) {
        // 通过元数据对象获取当前数据的类型，可能为因为当前数据没有值为空获取不到子数据MetaObject
        //所有可以通过元数据Class 获取子的类型信息
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
          //直接根据类中的数据获取元数据的信息
          return metaClass.hasGetter(name);
        } else {
          return metaValue.hasGetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return metaClass.hasGetter(name);
    }
  }

  @Override
  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
   //创建一个员数据对象、方便处理对象的相关的属性的创建
    MetaObject metaValue;

    // 获取分词器中具体对象的类型
    Class<?> type = getSetterType(prop.getName());
    try {
      //创建一个对象的信息
      Object newObject = objectFactory.create(type);

      //创建元数据对象
      metaValue = MetaObject.forObject(newObject, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());

      //根据分词器设置对象属性的值的信息
      set(prop, newObject);
    } catch (Exception e) {
      throw new ReflectionException("Cannot set value of property '" + name + "' because '" + name + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
    }
    return metaValue;
  }

  /**
   * 获取Bean数据信息，通过 员数据调用反射的方式获取Filed 或者 Method的数据值
   * @param prop
   * @param object
   * @return
   */
  private Object getBeanProperty(PropertyTokenizer prop, Object object) {
    try {
      Invoker method = metaClass.getGetInvoker(prop.getName());
      try {
        return method.invoke(object, NO_ARGUMENTS);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      throw new ReflectionException("Could not get property '" + prop.getName() + "' from " + object.getClass() + ".  Cause: " + t.toString(), t);
    }
  }

  /**
   * 根据分词器设置 对象的属性值哦
   * @param prop
   * @param object
   * @param value
   */
  private void setBeanProperty(PropertyTokenizer prop, Object object, Object value) {
    try {
      Invoker method = metaClass.getSetInvoker(prop.getName());
      Object[] params = {value};
      try {
        method.invoke(object, params);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } catch (Throwable t) {
      throw new ReflectionException("Could not set property '" + prop.getName() + "' of '" + object.getClass() + "' with value '" + value + "' Cause: " + t.toString(), t);
    }
  }

  @Override
  public boolean isCollection() {
    // TODO  这里默认返回false 是为啥？
    return false;
  }

  @Override
  public void add(Object element) {
    // TODO 这里不支持集合 ？
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> void addAll(List<E> list) {
    //TODO 这里不支持List
    throw new UnsupportedOperationException();
  }

}
