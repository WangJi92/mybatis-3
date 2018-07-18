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
package org.apache.ibatis.binding;

import org.apache.ibatis.lang.UsesJava7;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 *  [Mybatis3.3.x技术内幕（二）：动态代理之投鞭断流（自动映射器Mapper的底层实现原理](https://my.oschina.net/zudajun/blog/666223)
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  /**
   * Sql链接信息
   */
  private final SqlSession sqlSession;
  /**
   * 被调用的Mapper的类信息
   */
  private final Class<T> mapperInterface;
  /**
   * 要被调用的方法 对应的Mapper的缓存
   */
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      //意思就是如果你执行的是Mapper接口中的 的hashcode()、toString()、equals()等Object对象的方法时，就调用代理对象proxy的这些方法
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        //Java8默认方法执行
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    //这里被动态代理调用，执行调用逻辑处理
    final MapperMethod mapperMethod = cachedMapperMethod(method);

    //对于返回值进行处理、方法参数处理、返回值处理哦
    return mapperMethod.execute(sqlSession, args);
  }

  /**
   * 是否已经保存啦，MapperMethod否则构造一个哦
   * @param method
   * @return
   */
  private MapperMethod cachedMapperMethod(Method method) {
    return methodCache.computeIfAbsent(method, k -> new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
  }

  @UsesJava7
  private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
      throws Throwable {
    final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
        .getDeclaredConstructor(Class.class, int.class);
    if (!constructor.isAccessible()) {
      constructor.setAccessible(true);
    }
    final Class<?> declaringClass = method.getDeclaringClass();
    return constructor
        .newInstance(declaringClass,
            MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
        .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
  }

  /**
   * java8的很多特性是java虚拟机层面实现的，比如lamda表达式是由新版本的JVM支持的，而不是编译器是实现。
   * 但是default修饰符,其实更准确的定义是“在接口定义的，非抽象的，public的修饰符”，在编译为字节码后应该没有该标识符。
   *
   * 检测是否为Java8中的默认方法[java8接口中的默认方法](https://www.cnblogs.com/luozhiyun/p/7999705.html)
   * [如何获得方法的default修饰符？JVM规范并没有java8的defalut修饰符](https://blog.csdn.net/kkgbn/article/details/71272688)
   * Backport of java.lang.reflect.Method#isDefault()
   */
  private boolean isDefaultMethod(Method method) {
    return (method.getModifiers()
        & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
        && method.getDeclaringClass().isInterface();
  }
}
