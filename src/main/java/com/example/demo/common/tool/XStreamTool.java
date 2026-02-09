package com.example.demo.common.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Map;
import java.util.Set;

/**
 * XStream XML/JSON 转换工具，包含安全类型白名单与名称编码处理。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class XStreamTool {
    private static final XStream xstream;
    private static final String DOLLAR_TOKEN = "__DOLLAR__";

    static {
        // 自定义 NameCoder，禁止将 "_" 转为 "$"
        NameCoder nc = new NameCoder() {
            /**
             * 编码节点名称，转义 "$" 以规避 XStream 默认行为。
             *
             * @param name 节点名
             * @return 编码后的节点名
             * @author GPT-5.2-codex(high)
             * @date 2026/2/9
             */
            @Override
            public String encodeNode(String name) {
                return name == null ? null : name.replace("$", DOLLAR_TOKEN);
            }

            /**
             * 编码属性名称，转义 "$" 以规避 XStream 默认行为。
             *
             * @param name 属性名
             * @return 编码后的属性名
             * @author GPT-5.2-codex(high)
             * @date 2026/2/9
             */
            @Override
            public String encodeAttribute(String name) {
                return name == null ? null : name.replace("$", DOLLAR_TOKEN);
            }

            /**
             * 解码节点名称，还原 "$" 字符。
             *
             * @param nodeName 节点名
             * @return 解码后的节点名
             * @author GPT-5.2-codex(high)
             * @date 2026/2/9
             */
            @Override
            public String decodeNode(String nodeName) {
                return nodeName == null ? null : nodeName.replace(DOLLAR_TOKEN, "$");
            }

            /**
             * 解码属性名称，还原 "$" 字符。
             *
             * @param attributeName 属性名
             * @return 解码后的属性名
             * @author GPT-5.2-codex(high)
             * @date 2026/2/9
             */
            @Override
            public String decodeAttribute(String attributeName) {
                return attributeName == null ? null : attributeName.replace(DOLLAR_TOKEN, "$");
            }
        };

        xstream = new XStream(new DomDriver("UTF-8", nc));
        xstream.autodetectAnnotations(true);
        xstream.ignoreUnknownElements();
    }

    /**
     * 将 XML 反序列化为指定类型对象（白名单限制）。
     *
     * @param xml   XML 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> T fromXML(String xml, Class<T> clazz) {
        xstream.allowTypes(new Class[]{clazz});
        xstream.setClassLoader(clazz.getClassLoader());
        xstream.processAnnotations(clazz); // 如果类上有注解
        Object o = xstream.fromXML(xml);
        return (T) o;
    }

    /**
     * 将 Java 对象序列化为 XML 字符串。
     *
     * @param obj Java 对象
     * @return XML 字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static String toXML(Object obj) {
        Class<?> clazz = obj.getClass();
        xstream.allowTypes(new Class[]{clazz});
        xstream.processAnnotations(clazz); // 如果类中有注解
        return xstream.toXML(obj);
    }

    /**
     * 将 JSON 字符串转换为扁平化 XML 片段。
     *
     * @param json 输入的JSON字符串
     * @return 转换后的XML字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static String jsonToXML(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON字符串不能为null或空");
        }

        try {
            JSONObject jsonObject = JSON.parseObject(json);
            StringBuilder xmlBuilder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String tag = key.toUpperCase();
                xmlBuilder.append("<").append(tag).append(">");
                xmlBuilder.append(value);
                xmlBuilder.append("</").append(tag).append(">\n");
            }

            return xmlBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON转换为XML失败: " + e.getMessage(), e);
        }
    }

}
