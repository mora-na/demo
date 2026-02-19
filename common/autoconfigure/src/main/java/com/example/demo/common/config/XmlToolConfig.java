package com.example.demo.common.config;

import com.example.demo.common.tool.XMLTool;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.stream.XMLInputFactory;

/**
 * XMLTool 统一配置，复用全局 ObjectMapper，并关闭外部实体。
 */
@Configuration
public class XmlToolConfig {

    @Bean
    @ConditionalOnMissingBean(XmlMapper.class)
    public XmlMapper xmlMapper() {
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        disableXmlExternalEntities(inputFactory);
        XmlMapper xmlMapper = new XmlMapper(new XmlFactory(inputFactory));
        xmlMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return xmlMapper;
    }

    @Bean
    public XmlToolInitializer xmlToolInitializer(ObjectMapper objectMapper,
                                                 ObjectProvider<XmlMapper> xmlMapperProvider) {
        XmlMapper xmlMapper = xmlMapperProvider.getIfAvailable();
        return new XmlToolInitializer(objectMapper, xmlMapper);
    }

    private void disableXmlExternalEntities(XMLInputFactory factory) {
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

    public static final class XmlToolInitializer {
        private XmlToolInitializer(ObjectMapper objectMapper, XmlMapper xmlMapper) {
            XMLTool.setObjectMapper(objectMapper);
            if (xmlMapper != null) {
                XMLTool.setXmlMapper(xmlMapper);
            }
        }
    }
}
