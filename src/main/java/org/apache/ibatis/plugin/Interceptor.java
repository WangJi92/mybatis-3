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
package org.apache.ibatis.plugin;

import java.util.Properties;

/**
 * PluginTest 中有非常详细的表述，自己可以手动的调试查看其中的源码的数据信息
 * [李红推荐技术大牛博客分析](https://my.oschina.net/zudajun)
 * @author Clinton Begin
 */
public interface Interceptor {

  /**
   * 调用实际方法的时候，真正调用进行拦截
   * @param invocation  封装被调用对象的方法和属性
   * @return
   * @throws Throwable
   */
  Object intercept(Invocation invocation) throws Throwable;

  /**
   * 对当前target 进行包装，返回比如通过动态代理代理后的对象
   * @param target
   * @return
   */
  Object plugin(Object target);


  /**
   * 设置一些属性？
   * @param properties
   */
  void setProperties(Properties properties);

}
