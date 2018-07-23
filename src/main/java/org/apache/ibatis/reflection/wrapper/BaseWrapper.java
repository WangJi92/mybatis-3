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

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.List;
import java.util.Map;

/**
 * @author Clinton Begin
 */
public abstract class BaseWrapper implements ObjectWrapper {

  protected static final Object[] NO_ARGUMENTS = new Object[0];
  protected final MetaObject metaObject;

  protected BaseWrapper(MetaObject metaObject) {
    this.metaObject = metaObject;
  }

  protected Object resolveCollection(PropertyTokenizer prop, Object object) {
    if ("".equals(prop.getName())) {
      return object;
    } else {
      //获取当前数据中当前数据信息的值
      return metaObject.getValue(prop.getName());
    }
  }

  /**
   * 如果实体参数为集合类型，进行集合数据的获取，获取指定分词器上面的数据的信息
   * @param prop  分词器，当前分词的数据的处理
   * @param collection  集合数据对象
   * @return
   */
  protected Object getCollectionValue(PropertyTokenizer prop, Object collection) {
    if (collection instanceof Map) {
      //获取当前集合中对象的数据的值
      return ((Map) collection).get(prop.getIndex());
    } else {
      //分词器获取当前分词处理的是集合中的第几个数据
      int i = Integer.parseInt(prop.getIndex());

      //是List 还是 其他的基本数据类型数组哦
      if (collection instanceof List) {
        return ((List) collection).get(i);
      } else if (collection instanceof Object[]) {
        return ((Object[]) collection)[i];
      } else if (collection instanceof char[]) {
        return ((char[]) collection)[i];
      } else if (collection instanceof boolean[]) {
        return ((boolean[]) collection)[i];
      } else if (collection instanceof byte[]) {
        return ((byte[]) collection)[i];
      } else if (collection instanceof double[]) {
        return ((double[]) collection)[i];
      } else if (collection instanceof float[]) {
        return ((float[]) collection)[i];
      } else if (collection instanceof int[]) {
        return ((int[]) collection)[i];
      } else if (collection instanceof long[]) {
        return ((long[]) collection)[i];
      } else if (collection instanceof short[]) {
        return ((short[]) collection)[i];
      } else {
        throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
      }
    }
  }

  /**
   * 设定指定的分词器上 某个集合上的数据的值，与上面的数据类似哦
   * @param prop
   * @param collection
   * @param value
   */
  protected void setCollectionValue(PropertyTokenizer prop, Object collection, Object value) {
    if (collection instanceof Map) {
      ((Map) collection).put(prop.getIndex(), value);
    } else {
      int i = Integer.parseInt(prop.getIndex());
      if (collection instanceof List) {
        ((List) collection).set(i, value);
      } else if (collection instanceof Object[]) {
        ((Object[]) collection)[i] = value;
      } else if (collection instanceof char[]) {
        ((char[]) collection)[i] = (Character) value;
      } else if (collection instanceof boolean[]) {
        ((boolean[]) collection)[i] = (Boolean) value;
      } else if (collection instanceof byte[]) {
        ((byte[]) collection)[i] = (Byte) value;
      } else if (collection instanceof double[]) {
        ((double[]) collection)[i] = (Double) value;
      } else if (collection instanceof float[]) {
        ((float[]) collection)[i] = (Float) value;
      } else if (collection instanceof int[]) {
        ((int[]) collection)[i] = (Integer) value;
      } else if (collection instanceof long[]) {
        ((long[]) collection)[i] = (Long) value;
      } else if (collection instanceof short[]) {
        ((short[]) collection)[i] = (Short) value;
      } else {
        throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
      }
    }
  }

}