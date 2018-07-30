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
package org.apache.ibatis.session;

/**
 * <P>[MyBatis插件原理第五篇——ParameterHandler 和 ResultSetHandler](https://blog.csdn.net/ykzhen2015/article/details/51690672)</P>
 * @author Clinton Begin
 */
public interface ResultHandler<T> {

  /**
   * ResultContext MayBe  是当前数据中的一项，通过对于每一个数据项进行转换获取到最终需要的数据的类型信息
   * {@linkplain org.apache.ibatis.session.defaults.DefaultSqlSession#selectMap(String, Object, String) 使用看这里哦}
   * @param resultContext
   */
  void handleResult(ResultContext<? extends T> resultContext);

}
