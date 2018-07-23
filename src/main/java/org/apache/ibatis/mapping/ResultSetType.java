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
package org.apache.ibatis.mapping;

import java.sql.ResultSet;

/**
 * [java 中的resultset的类型](https://www.cnblogs.com/tianguook/p/3383045.html)
 * [java大量数据加载时resultSetType参数的设置 TYPE_FORWARD_ONLY](https://blog.csdn.net/10km/article/details/50404694)
 *
 * 感觉又学到啦~~~ 构建参数的过程中 内存问题
 *
 * 如果PreparedStatement对象初始化时resultSetType参数设置为TYPE_FORWARD_ONLY，
 * 在从ResultSet（结果集）中读取记录的时，对于访问过的记录就自动释放了内存。
 *  而设置为TYPE_SCROLL_INSENSITIVE或TYPE_SCROLL_SENSITIVE时为了保证能游标能向上移动到任意位置
 * ，已经访问过的所有都保留在内存中不能释放。所以大量数据加载的时候，就OOM了。
 * @author Clinton Begin
 */
public enum ResultSetType {
  /**
   * 只能向下移动
   */
  FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
  SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
  SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);

  private final int value;

  ResultSetType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
