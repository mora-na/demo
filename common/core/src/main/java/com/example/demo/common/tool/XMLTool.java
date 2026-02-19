package com.example.demo.common.tool;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.XMLInputFactory;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * XML/JSON 转换工具，基于 Jackson XmlMapper 实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class XMLTool {

    private static volatile ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static volatile XmlMapper XML_MAPPER = createSafeXmlMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    static {
        XML_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private XMLTool() {
    }

    /**
     * 允许外部注入统一的 ObjectMapper（如 Spring 全局配置）。
     */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        if (objectMapper != null) {
            OBJECT_MAPPER = objectMapper;
        }
    }

    /**
     * 允许外部注入统一的 XmlMapper（如 Spring 全局配置）。
     */
    public static void setXmlMapper(XmlMapper xmlMapper) {
        if (xmlMapper != null) {
            XML_MAPPER = xmlMapper;
        }
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
        try {
            return XML_MAPPER.readValue(xml, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("xml parse failed", e);
        }
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
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("xml serialize failed", e);
        }
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
            Map<String, Object> jsonObject = OBJECT_MAPPER.readValue(json, MAP_TYPE);
            if (jsonObject == null || jsonObject.isEmpty()) {
                return "";
            }
            Set<Map.Entry<String, Object>> entrySet = jsonObject.entrySet();
            StringBuilder xmlBuilder = new StringBuilder(entrySet.size() * 32);
            for (Map.Entry<String, Object> entry : entrySet) {
                String tag = entry.getKey().toUpperCase(Locale.ROOT);
                xmlBuilder.append("<").append(tag).append(">");
                xmlBuilder.append(escapeXmlValue(entry.getValue()));
                xmlBuilder.append("</").append(tag).append(">\n");
            }

            return xmlBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSON转换为XML失败: " + e.getMessage(), e);
        }
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

    private static XmlMapper createSafeXmlMapper() {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        disableXmlExternalEntities(inputFactory);
        XmlMapper xmlMapper = new XmlMapper(new XmlFactory(inputFactory));
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return xmlMapper;
    }

    private static void disableXmlExternalEntities(XMLInputFactory factory) {
        if (factory == null) {
            return;
        }
        try {
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        } catch (Exception ignored) {
            // ignore
        }
        try {
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        } catch (Exception ignored) {
            // ignore
        }
        try {
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        } catch (Exception ignored) {
            // ignore
        }
    }
}
