package com.example.demo.common.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.Map;
import java.util.Set;

public class XStreamTool {
    private static final XStream xstream;
    private static final String DOLLAR_TOKEN = "__DOLLAR__";

    static {
        // 自定义 NameCoder，禁止将 "_" 转为 "$"
        NameCoder nc = new NameCoder() {
            @Override
            public String encodeNode(String name) {
                return name == null ? null : name.replace("$", DOLLAR_TOKEN);
            }

            @Override
            public String encodeAttribute(String name) {
                return name == null ? null : name.replace("$", DOLLAR_TOKEN);
            }

            @Override
            public String decodeNode(String nodeName) {
                return nodeName == null ? null : nodeName.replace(DOLLAR_TOKEN, "$");
            }

            @Override
            public String decodeAttribute(String attributeName) {
                return attributeName == null ? null : attributeName.replace(DOLLAR_TOKEN, "$");
            }
        };

        xstream = new XStream(new DomDriver("UTF-8", nc));
        xstream.autodetectAnnotations(true);
        xstream.ignoreUnknownElements();
    }

    public static <T> T fromXML(String xml, Class<T> clazz) {
        xstream.allowTypes(new Class[]{clazz});
        xstream.setClassLoader(clazz.getClassLoader());
        xstream.processAnnotations(clazz); // 如果类上有注解
        Object o = xstream.fromXML(xml);
        return (T) o;
    }

    /**
     * 将Java对象转换为XML字符串
     */
    public static String toXML(Object obj) {
        Class<?> clazz = obj.getClass();
        xstream.allowTypes(new Class[]{clazz});
        xstream.processAnnotations(clazz); // 如果类中有注解
        return xstream.toXML(obj);
    }

    /**
     * 将JSON字符串转换为简洁格式的XML字符串
     *
     * @param json 输入的JSON字符串
     * @return 转换后的XML字符串
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
