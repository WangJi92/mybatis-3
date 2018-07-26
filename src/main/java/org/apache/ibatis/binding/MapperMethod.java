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

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.Jdk;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.OptionalUtil;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * <P>
 *     [Mybatis3.3.x技术内幕（二）：动态代理之投鞭断流（自动映射器Mapper的底层实现原理](https://my.oschina.net/zudajun/blog/666223)
 *
 * </P>
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 * @author Kazuki Shimizu
 */
public class MapperMethod {

  private final SqlCommand command;
  /**
   * 解析方法参数 比如返回值还有一些特殊的属性 有没有@param参数等等
   */
  private final MethodSignature method;

  /***
   * 通过这些数据构造一个需要被调用的Mapper的方法
   * @param mapperInterface   mapper接口类的信息
   * @param method  mapper的方法
   * @param config   配置信息哦
   */
  public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
    //处理SQL命令的类型和唯一标识信息
    this.command = new SqlCommand(config, mapperInterface, method);

    //解析方法中的一些特殊的标识
    this.method = new MethodSignature(config, mapperInterface, method);
  }

  /**
   * [MyBatis Java API](http://www.mybatis.org/mybatis-3/zh/java-api.html)
   * @param sqlSession
   * @param args
   * @return
   */
  public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    switch (command.getType()) {
      case INSERT: {
        //解析参数，然后进行处理哦
    	Object param = method.convertArgsToSqlCommandParam(args);

    	//然后调用sqlSession 进行处理哦
        result = rowCountResult(sqlSession.insert(command.getName(), param));
        break;
      }
      case UPDATE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.update(command.getName(), param));
        break;
      }
      case DELETE: {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = rowCountResult(sqlSession.delete(command.getName(), param));
        break;
      }
      case SELECT:
        //根据MethodSignature 方法参数解析中获取的数据信息进行处理哦
        if (method.returnsVoid() && method.hasResultHandler()) {
          executeWithResultHandler(sqlSession, args);
          result = null;
        } else if (method.returnsMany()) {
          //通过Sqlsession 获取list数据转换为 各种的array 或者 集合等等
          result = executeForMany(sqlSession, args);
        } else if (method.returnsMap()) {
          //返回文档数据为Map的类型
          result = executeForMap(sqlSession, args);
        } else if (method.returnsCursor()) {
          //对于游标的支持哦，不一次性加载所有的数据信息
          result = executeForCursor(sqlSession, args);
        } else {
          //返回一个的情况、同事对于Java8的支持哦
          Object param = method.convertArgsToSqlCommandParam(args);
          result = sqlSession.selectOne(command.getName(), param);
          if (method.returnsOptional() &&
              (result == null || !method.getReturnType().equals(result.getClass()))) {
            result = OptionalUtil.ofNullable(result);
          }
        }
        break;
      case FLUSH:
        //执行批处理？？ 这里不是很理解 TODO 不是很理解
        result = sqlSession.flushStatements();
        break;
      default:
        throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }

  /**
   * 返回的数据的行数处理哦
   * @param rowCount
   * @return
   */
  private Object rowCountResult(int rowCount) {
    final Object result;
    if (method.returnsVoid()) {
      result = null;
    } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
      result = rowCount;
    } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
      result = (long)rowCount;
    } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
      result = rowCount > 0;
    } else {
      throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
    }
    return result;
  }

  private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
    MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
    if (!StatementType.CALLABLE.equals(ms.getStatementType())
        && void.class.equals(ms.getResultMaps().get(0).getType())) {
      throw new BindingException("method " + command.getName() 
          + " needs either a @ResultMap annotation, a @ResultType annotation," 
          + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
    }
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
    } else {
      sqlSession.select(command.getName(), param, method.extractResultHandler(args));
    }
  }

  /**
   * 将执行的数据转换为需要的类型哦
   * @param sqlSession
   * @param args
   * @param <E>
   * @return
   */
  private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
    List<E> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      //获取排序的参数哦
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.<E>selectList(command.getName(), param, rowBounds);
    } else {
      result = sqlSession.<E>selectList(command.getName(), param);
    }
    // issue #510 Collections & arrays support 如果不是list哦
    if (!method.getReturnType().isAssignableFrom(result.getClass())) {
      if (method.getReturnType().isArray()) {
        return convertToArray(result);
      } else {
        //转换为需要的集合
        return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
      }
    }
    return result;
  }

  /**
   * 对于获取游标类型的数据进行处理哦
   * @param sqlSession
   * @param args
   * @param <T>
   * @return
   */
  private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
    Cursor<T> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.<T>selectCursor(command.getName(), param, rowBounds);
    } else {
      result = sqlSession.<T>selectCursor(command.getName(), param);
    }
    return result;
  }

  /**
   * 将数据处理转换为集合 ，这个 DefaultObjectFactory 没有仔细去看哦
   * @param config
   * @param list
   * @param <E>
   * @return
   */
  private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
    Object collection = config.getObjectFactory().create(method.getReturnType());
    MetaObject metaObject = config.newMetaObject(collection);
    metaObject.addAll(list);
    return collection;
  }

  /**
   * 将返回值转换为数据进行处理
   * @param list
   * @param <E>
   * @return
   */
  @SuppressWarnings("unchecked")
  private <E> Object convertToArray(List<E> list) {
    Class<?> arrayComponentType = method.getReturnType().getComponentType();
    Object array = Array.newInstance(arrayComponentType, list.size());
    if (arrayComponentType.isPrimitive()) {
      for (int i = 0; i < list.size(); i++) {
        Array.set(array, i, list.get(i));
      }
    return array;
    } else {
      return list.toArray((E[])array);
    }
  }

  /**
   * 将数据转换为Map处理
   * @param sqlSession
   * @param args
   * @param <K>
   * @param <V>
   * @return
   */
  private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
    Map<K, V> result;
    Object param = method.convertArgsToSqlCommandParam(args);
    if (method.hasRowBounds()) {
      RowBounds rowBounds = method.extractRowBounds(args);
      result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey(), rowBounds);
    } else {
      result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey());
    }
    return result;
  }

  public static class ParamMap<V> extends HashMap<String, V> {

    private static final long serialVersionUID = -2212268410512043556L;

    @Override
    public V get(Object key) {
      if (!super.containsKey(key)) {
        throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
      }
      return super.get(key);
    }

  }

  /**
   * [Mybatis源码分析之Mapper执行SQL过程（三）](https://www.cnblogs.com/jeffen/p/6277696.html?utm_source=itdadao&utm_medium=referral)
   */
  public static class SqlCommand {
    /**
     * Mybatis使用package+Mapper+method全限名作为key，去xml内寻找唯一sql来执行的。
     */
    private final String name;

    /**
     * sql命令执行的类型，这个是干啥的啊
     */
    private final SqlCommandType type;

    public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
      //方法的名称
      final String methodName = method.getName();

      //定义class
      final Class<?> declaringClass = method.getDeclaringClass();

      //查看是否配置中已经定义啦~ MappedStatement 相当于一条SQL语句
      MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
          configuration);
      if (ms == null) {
        if(method.getAnnotation(Flush.class) != null){
          name = null;
          type = SqlCommandType.FLUSH;
        } else {
          throw new BindingException("Invalid bound statement (not found): "
              + mapperInterface.getName() + "." + methodName);
        }
      } else {
        name = ms.getId();
        type = ms.getSqlCommandType();
        if (type == SqlCommandType.UNKNOWN) {
          throw new BindingException("Unknown execution method for: " + name);
        }
      }
    }

    public String getName() {
      return name;
    }

    public SqlCommandType getType() {
      return type;
    }

    /**
     * 解决映射语句（个MappedStatement对象对应Mapper配置文件中的一个select/update/insert/delete节点，主要描述的是一条SQL语句）
     *
     * @param mapperInterface  映射接口
     * @param methodName  方法名称
     * @param declaringClass 定义的方法的class
     * @param configuration  核心配置信息
     * @return
     */
    private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
        Class<?> declaringClass, Configuration configuration) {
      //mapper接口的名称+方法的名称对应一条MappedStatement 【Mybatis3.3.x技术内幕（二）：动态代理之投鞭断流（自动映射器Mapper的底层实现原理）】(https://my.oschina.net/zudajun/blog/666223)
      String statementId = mapperInterface.getName() + "." + methodName;
      if (configuration.hasStatement(statementId)) {
        return configuration.getMappedStatement(statementId);
      } else if (mapperInterface.equals(declaringClass)) {
        return null;
      }
      for (Class<?> superInterface : mapperInterface.getInterfaces()) {
        if (declaringClass.isAssignableFrom(superInterface)) {
          MappedStatement ms = resolveMappedStatement(superInterface, methodName,
              declaringClass, configuration);
          if (ms != null) {
            return ms;
          }
        }
      }
      return null;
    }
  }

  /**
   * 方法签名
   */
  public static class MethodSignature {
    /**
     * 返回多个数据？
     */
    private final boolean returnsMany;
    private final boolean returnsMap;
    private final boolean returnsVoid;
    private final boolean returnsCursor;
    private final boolean returnsOptional;
    private final Class<?> returnType;
    private final String mapKey;
    private final Integer resultHandlerIndex;
    private final Integer rowBoundsIndex;
    private final ParamNameResolver paramNameResolver;

    public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {
      // 解析方法返回值类型、处理泛型
      Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
      if (resolvedReturnType instanceof Class<?>) {
        this.returnType = (Class<?>) resolvedReturnType;
      } else if (resolvedReturnType instanceof ParameterizedType) {
        this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
      } else {
        this.returnType = method.getReturnType();
      }
      this.returnsVoid = void.class.equals(this.returnType);

      //是否返回多个值
      this.returnsMany = configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray();

      /**
       *  是否返回游标[Mybatis 3.4.0 Cursor的使用](https://www.jianshu.com/p/97d96201295b)
       *  [MyBatis 3.4.0 版本功能介绍](https://blog.csdn.net/isea533/article/details/51533296?readlog)
       */

      this.returnsCursor = Cursor.class.equals(this.returnType);

      //是否返回Java 8 可选参数
      this.returnsOptional = Jdk.optionalExists && Optional.class.equals(this.returnType);

      /**
       * [Mybatis @MapKey注解的使用](https://blog.csdn.net/weixin_41023230/article/details/80103990)
       */
      this.mapKey = getMapKey(method);
      this.returnsMap = this.mapKey != null;

      /**
       * 找到分页参数的位置信息
       */
      this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);

      /**
       * 找到ResultHandler的位置信息
       */
      this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);

      /**
       * 查找方法参数中 @param注解信息
       */
      this.paramNameResolver = new ParamNameResolver(configuration, method);
    }

    /**
     * 解析参数中的@param 然后返回一个参数 或者多个参数 Hashmap
     * @param args
     * @return
     */
    public Object convertArgsToSqlCommandParam(Object[] args) {
      return paramNameResolver.getNamedParams(args);
    }

    public boolean hasRowBounds() {
      return rowBoundsIndex != null;
    }

    public RowBounds extractRowBounds(Object[] args) {
      return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
    }

    public boolean hasResultHandler() {
      return resultHandlerIndex != null;
    }

    public ResultHandler extractResultHandler(Object[] args) {
      return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
    }

    public String getMapKey() {
      return mapKey;
    }

    public Class<?> getReturnType() {
      return returnType;
    }

    public boolean returnsMany() {
      return returnsMany;
    }

    public boolean returnsMap() {
      return returnsMap;
    }

    public boolean returnsVoid() {
      return returnsVoid;
    }

    public boolean returnsCursor() {
      return returnsCursor;
    }

    /**
     * return whether return type is {@code java.util.Optional}
     * @return return {@code true}, if return type is {@code java.util.Optional}
     * @since 3.5.0
     */
    public boolean returnsOptional() {
      return returnsOptional;
    }

    /**
     * 找到方法参数中，某种数据类型的位置信息
     * @param method
     * @param paramType
     * @return
     */
    private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
      Integer index = null;
      final Class<?>[] argTypes = method.getParameterTypes();
      for (int i = 0; i < argTypes.length; i++) {
        if (paramType.isAssignableFrom(argTypes[i])) {
          if (index == null) {
            index = i;
          } else {
            throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
          }
        }
      }
      return index;
    }

    private String getMapKey(Method method) {
      String mapKey = null;
      if (Map.class.isAssignableFrom(method.getReturnType())) {
        final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
        if (mapKeyAnnotation != null) {
          mapKey = mapKeyAnnotation.value();
        }
      }
      return mapKey;
    }
  }

}
