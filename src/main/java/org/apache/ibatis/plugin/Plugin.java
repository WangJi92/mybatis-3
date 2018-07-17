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
package org.apache.ibatis.plugin;

import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * [MyBatis 教程 - MyBatis插件（Plugins）开发](https://blog.csdn.net/top_code/article/details/55520948)
 * [MyBatis源码剖析 - MyBatis 插件之拦截器（Interceptor）实现原理](https://blog.csdn.net/top_code/article/details/55657776)
 *
 * 其实这个类承担了相当多的责任；
 * 第一个就是包装target，通过动态代理，处理一些代理过滤的逻辑；
 * 第二个就是实现InvocationHandler，可以通过动态代理进行调用处理逻辑；
 *
 * @author Clinton Begin
 */
public class Plugin implements InvocationHandler {

  /**
   * 目标类
   */
  private final Object target;

  /**
   * 过滤拦截器处理
   */
  private final Interceptor interceptor;

  /**
   * 标注要被拦截的接口的方法签名
   */
  private final Map<Class<?>, Set<Method>> signatureMap;

  private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
    this.target = target;
    this.interceptor = interceptor;
    this.signatureMap = signatureMap;
  }

  public static Object wrap(Object target, Interceptor interceptor) {
    //获取注解方法上的所有的Class 对应的方法，这些方法是需要被拦截的方法
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);

    //目标的类
    Class<?> type = target.getClass();

    //过滤目标类的接口信息，必须包含在特定注解上的类信息才进行代理当前的类信息
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      //动态代理目标的类的信息
      return Proxy.newProxyInstance(
          type.getClassLoader(),
          interfaces,
          new Plugin(target, interceptor, signatureMap));
    }
    return target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      //查看当前调用的方法是否需要代理
      Set<Method> methods = signatureMap.get(method.getDeclaringClass());
      if (methods != null && methods.contains(method)) {
        //通过代理类去处理数据
        return interceptor.intercept(new Invocation(target, method, args));
      }

      //否则直接通过方法进行反射调用
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  /**
   * 查找调用Interceptor 上的注解，通过注解中的参数对于特定的方法进行拦截哦，具体可以通过PluginTest，跟踪进行简单的了解处理的过程信息
   * @param interceptor
   * @return
   */
  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    // issue #251
    if (interceptsAnnotation == null) {
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());      
    }
    Signature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
    for (Signature sig : sigs) {
      //如果不存在 就创建一个HasHset...
      Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }
    return signatureMap;
  }

  /**
   * 只进行对于Signature 中定义的接口类进行拦截，其他的接口不进行拦截哦，这里主要是起了一个过滤的作用
   * @param type
   * @param signatureMap
   * @return
   */
  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[interfaces.size()]);
  }

}
