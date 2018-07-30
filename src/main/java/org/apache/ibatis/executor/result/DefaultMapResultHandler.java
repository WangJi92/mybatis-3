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
package org.apache.ibatis.executor.result;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.Map;

/**
 * {@linkplain org.apache.ibatis.session.defaults.DefaultSqlSession#selectMap(String, Object, String) 这里有使用到返回为Map的情况}
 * @author Clinton Begin
 */
public class DefaultMapResultHandler<K, V> implements ResultHandler<V> {

  /**
   * 返回的Map 结果 {@linkplain org.apache.ibatis.session.defaults.DefaultSqlSession#selectMap(String, Object, String) 这里有使用到返回为Map的情况}
   */
  private final Map<K, V> mappedResults;

  /**
   * 返回行中对应的结果的列的信息
   */
  private final String mapKey;

  /**
   * 对象工厂创建器哦~
   */
  private final ObjectFactory objectFactory;

  /**
   * 包装器 包装JavaBean Map Collection
   */
  private final ObjectWrapperFactory objectWrapperFactory;

  /**
   * 反射器工厂
   */
  private final ReflectorFactory reflectorFactory;

  @SuppressWarnings("unchecked")
  public DefaultMapResultHandler(String mapKey, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;

    //创建一个Map的实例哦
    this.mappedResults = objectFactory.create(Map.class);

    //放置在Map中对应的行的key值的数据信息
    this.mapKey = mapKey;
  }

  @Override
  public void handleResult(ResultContext<? extends V> context) {
    //得到当前项的数据的信息
    final V value = context.getResultObject();

    //获取到该返回数据的元数据的信息，方便获取数据值信息，and 进行操作哦
    final MetaObject mo = MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
    // TODO is that assignment always true?
    final K key = (K) mo.getValue(mapKey);

    //然后将所有的数据信息放置到MapperResult中去处理
    mappedResults.put(key, value);
  }

  /**
   * 获取遍历完成之后的处理的结果嘻嘻
   * @return
   */
  public Map<K, V> getMappedResults() {
    return mappedResults;
  }
}
