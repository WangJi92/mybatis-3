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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 *
 * 具体的查看例子 MetaObjectTest 这个类，对于这种数据的处理哦【  richType.richList[0] 类中的richType字段 的richList 的第一个数据   】
 * 属性分词器 将属性中的 逗号 [] 等进行拆分能够区分父类和子类的信息
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
  /**
   * 第一个逗号之前的数据，如果有 数组类型的数据[]，是去掉后面的 []之后的名称
   */
  private String name;

  /**
   * 当前数据的全限定名称 比如：xxxx[0]
   */
  private final String indexedName;

  /**
   * 当前是否存在集合数据的索引
   */
  private String index;

  /**
   * 第一个逗号后面的都是孩子，没有逗号证明没有下面的属性啦
   */
  private final String children;

  public PropertyTokenizer(String fullname) {
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      name = fullname.substring(0, delim);
      children = fullname.substring(delim + 1);
    } else {
      name = fullname;
      children = null;
    }
    indexedName = name;
    delim = name.indexOf('[');
    if (delim > -1) {
      index = name.substring(delim + 1, name.length() - 1);
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  @Override
  public boolean hasNext() {
    return children != null;
  }

  @Override
  public PropertyTokenizer next() {
    //进行下一个数据的处理
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
