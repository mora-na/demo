package com.example.demo.common.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * XStream XML/JSON 转换工具，使用包白名单并复用线程内实例兼顾安全与性能。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class XStreamTool {

    private static final ThreadLocal<XStream> XSTREAM_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<Set<Class<?>>> PROCESSED_TYPES =
            ThreadLocal.withInitial(HashSet::new);
    private static final String[] ALLOWED_PACKAGE_WILDCARDS = {
            "com.example.demo.**",
            "java.lang.**",
            "java.util.**"
    };

    private XStreamTool() {
    }

    /**
     * 将 XML 反序列化为指定类型对象。
     *
     * @param xml   XML 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化结果
     */
    public static <T> T fromXML(String xml, Class<T> clazz) {
        return xmlToBean(xml, clazz);
    }

    /**
     * 将 XML 反序列化为指定类型对象。
     *
     * @param xml   XML 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化结果
     */
    public static <T> T xmlToBean(String xml, Class<T> clazz) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("xml content is blank");
        }
        XStream xstream = prepareXStream(clazz);
        Object o = xstream.fromXML(xml);
        return clazz.cast(o);
    }

    /**
     * 将 Java 对象序列化为 XML 字符串。
     *
     * @param obj Java 对象
     * @return XML 字符串
     */
    public static String toXML(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("object to serialize cannot be null");
        }
        Class<?> clazz = obj.getClass();
        XStream xstream = prepareXStream(clazz);
        return xstream.toXML(obj);
    }

    /**
     * 将 JSON 字符串转换为扁平化 XML 片段。
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
            Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
            StringBuilder xmlBuilder = new StringBuilder(entrySet.size() * 32);
            for (Map.Entry<String, Object> entry : entrySet) {
                String tag = entry.getKey().toUpperCase();
                xmlBuilder.append("<").append(tag).append(">");
                xmlBuilder.append(escapeXmlValue(entry.getValue()));
                xmlBuilder.append("</").append(tag).append(">\n");
            }

            return xmlBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON转换为XML失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取配置好的 XStream 实例，并按传入类型补充注解与类加载器；线程内复用实例避免重复构建。
     */
    private static XStream prepareXStream(Class<?> boundType) {
        XStream xstream = XSTREAM_CACHE.get();
        if (xstream == null) {
            xstream = createXStream();
            XSTREAM_CACHE.set(xstream);
            PROCESSED_TYPES.set(new HashSet<>());
        }
        Set<Class<?>> processed = PROCESSED_TYPES.get();
        if (!processed.contains(boundType)) {
            xstream.processAnnotations(boundType);
            processed.add(boundType);
        }
        xstream.setClassLoader(boundType.getClassLoader());
        return xstream;
    }

    private static XStream createXStream() {
        XStream xstream = new XStream(new StaxDriver(new XmlFriendlyNameCoder("_-", "_")));
        // 新版安全配置：默认拒绝所有类型，仅白名单包和基础类型允许
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.allowTypesByWildcard(ALLOWED_PACKAGE_WILDCARDS);
        xstream.autodetectAnnotations(true);
        xstream.ignoreUnknownElements();
        return xstream;
    }

    private static String escapeXmlValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
