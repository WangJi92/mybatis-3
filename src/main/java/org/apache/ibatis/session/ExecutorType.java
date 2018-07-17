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
 * 配置和设定执行器，SIMPLE 执行器执行其它语句。REUSE 执行器可能重复使用prepared statements 语句，BATCH执行器可以重复执行语句和批量更新。
 * [MyBatis配置详解](https://blog.csdn.net/bear_wr/article/details/52401881)
 * [mybatis使用的一点小结：session运行模式及批量提交](https://blog.csdn.net/meiwen1111/article/details/8260387)
 * @author Clinton Begin
 */
public enum ExecutorType {
  /**
   * 这个类型不做特殊的事情，它只为每个语句创建一个PreparedStatement
   */
  SIMPLE,
  /**
   * 这种类型将重复使用PreparedStatements
   */
  REUSE,
  /**
   * 这个类型批量更新，且必要地区别开其中的select 语句，确保动作易于理解
   */
  BATCH
}
