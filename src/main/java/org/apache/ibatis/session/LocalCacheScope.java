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
 * 本地缓存作用域
 *
 * 一级缓存是指SqlSession级别的缓存，当在同一个SqlSession中进行相同的SQL语句查询时，第二次以后的查询不会从数据库查询，而是直接从缓存中获取，一级缓存最多缓存1024条SQL。
 * 二级缓存是指可以跨SqlSession的缓存。
 *
 * <p>[Mybatis Local Cache陷阱](https://segmentfault.com/a/1190000008207977)
 * [MyBatis（3.4.2）的Cache机制完全解析](https://blog.csdn.net/realskyzou/article/details/54137051)
 *
 * [Mybatis缓存介绍-这个说得清楚一点](https://blog.csdn.net/u010643307/article/details/70148723)
 * </p>
 * @author Eduardo Macarron
 */
public enum LocalCacheScope {
  /**
   * 一级缓存保存在session中还是在一个查询语句中？当前是session
   */
  SESSION,
  /**
   * 一级缓存保存在STATEMENT中去处理哦
   */
  STATEMENT
}
