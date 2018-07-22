/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.parsing;

import java.util.Properties;

/**
 * PropertyParserTest中详细展示啦测试用例
 *
 * ${a:b}
 *
 * SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}
 *
 * 哈哈 ！你是否还记得 #{} 是预处理变量  ${} 直接替换字符串哦，在mybatis 中的处理细节就是在这里哦
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * 是否启用默认值哦
   * The special property key that indicate whether enable a default value on placeholder.
   * <p>
   *   The default value is {@code false} (indicate disable a default value on placeholder)
   *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * 默认值分割符
   * The special property key that specify a separator for key and default value on placeholder.
   * <p>
   *   The default separator is {@code ":"}.
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  /**
   * 默认不启用默认值
   */
  private static final String ENABLE_DEFAULT_VALUE = "false";

  /**
   * 默认值分割符 :
   */
  private static final String DEFAULT_VALUE_SEPARATOR = ":";

  private PropertyParser() {
    // Prevent Instantiation
  }

  public static String parse(String string, Properties variables) {
    //含有特殊键值对变量的替换处理
    VariableTokenHandler handler = new VariableTokenHandler(variables);

    //解析字符串中的 ${} 开始结束 符 ，然后通过 键值处理程序返回的值进行替换哦
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    return parser.parse(string);
  }


  /**
   * ${a:1} 被GenericTokenParser 解析其中的 ${} 变为啦  a:1
   * 这里主要是根据配置中的键值对信息，查看是否替换这个变量的信息，如果不存在使用默认值
   */
  private static class VariableTokenHandler implements TokenHandler {
    /**
     * 当前配置文件中包含的数据的信息  key:value 键值对的信息
     */
    private final Properties variables;
    /**
     * 启用默认值
     */
    private final boolean enableDefaultValue;
    /**
     * 默认值分割符哦 ${a:1}  分割符 ：
     */
    private final String defaultValueSeparator;

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
    }

    @Override
    public String handleToken(String content) {
      //这里的Content 已经处理过啦 ${} 这个 只剩下 a:1
      if (variables != null) {
        String key = content;
        if (enableDefaultValue) {
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          if (separatorIndex >= 0) {
            key = content.substring(0, separatorIndex);
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          //设置啦，默认值的处理！这里就这样直接的处理默认值
          if (defaultValue != null) {
            return variables.getProperty(key, defaultValue);
          }
        }
        //存在这个key没有使用默认值
        if (variables.containsKey(key)) {
          return variables.getProperty(key);
        }
      }
      //否则原数据返回哦
      return "${" + content + "}";
    }
  }

}
