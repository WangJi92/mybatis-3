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
package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

import java.util.List;

/**
 * <resultMap id="selectAuthor" type="org.apache.ibatis.domain.blog.Author" >
   <id column="id" property="id" />
   <result property="username" column="username" />
   <result property="password" column="password" />
   <result property="email" column="email" />
   <result property="bio" column="bio" />
   <result property="favouriteSection" column="favourite_section" />
 </resultMap>
 *
 * 结果映射解析器
 * @author Eduardo Macarron
 */
public class ResultMapResolver {
  /**
   * Mapper构建助手
   */
  private final MapperBuilderAssistant assistant;

  /**
   * 对应 selectAuthor
   */
  private final String id;
  /**
   * 当前type的类型
   */
  private final Class<?> type;

  /**
   * 是否继承  http://www.mybatis.org/mybatis-3/zh/sqlmap-xml.html
   */
  private final String extend;

  /**
   * 鉴别器
   */
  private final Discriminator discriminator;

  /**
   * 映射文件的每一项信息
   */
  private final List<ResultMapping> resultMappings;

  /**
   * 是否自动映射
   */
  private final Boolean autoMapping;

  public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, String extend, Discriminator discriminator, List<ResultMapping> resultMappings, Boolean autoMapping) {
    this.assistant = assistant;
    this.id = id;
    this.type = type;
    this.extend = extend;
    this.discriminator = discriminator;
    this.resultMappings = resultMappings;
    this.autoMapping = autoMapping;
  }

  public ResultMap resolve() {
    return assistant.addResultMap(this.id, this.type, this.extend, this.discriminator, this.resultMappings, this.autoMapping);
  }

}