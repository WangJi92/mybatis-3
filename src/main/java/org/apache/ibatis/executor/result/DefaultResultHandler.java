/**
 *    Copyright 2009-2018 the original author or authors.
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

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * [mybatis 中的 ResultHandler（传入map返回map）](https://www.cnblogs.com/LiuPan2016/p/8391753.html) 可以根据自己的需求定义返回值的信息
 * 默认的结果处理器，这里只是将返回的List想你想添加起来哦
 * {@linkplain org.apache.ibatis.session.defaults.DefaultSqlSession#selectMap(String, Object, String) 这里有使用到返回为Map的情况}
 * @author Clinton Begin
 */
public class DefaultResultHandler implements ResultHandler<Object> {

  private final List<Object> list;

  public DefaultResultHandler() {
    list = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public DefaultResultHandler(ObjectFactory objectFactory) {
    //穿创建一个数据List信息对象哦~
    list = objectFactory.create(List.class);
  }

  @Override
  public void handleResult(ResultContext<? extends Object> context) {
    //然后遍历每一项的数据信息，然后进行数据的处理哦
    list.add(context.getResultObject());
  }

  public List<Object> getResultList() {
    return list;
  }

}
