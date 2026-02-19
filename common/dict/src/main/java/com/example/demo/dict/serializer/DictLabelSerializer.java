package com.example.demo.dict.serializer;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.dict.annotation.DictLabel;
import com.example.demo.dict.config.DictConstants;
import com.example.demo.dict.support.DictTool;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;

/**
 * 字典翻译序列化器，自动追加 Label 字段。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public class DictLabelSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private final String dictType;
    private final String targetField;

    public DictLabelSerializer() {
        this(null, null);
    }

    public DictLabelSerializer(String dictType, String targetField) {
        this.dictType = dictType;
        this.targetField = targetField;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value);
        if (StringUtils.isBlank(dictType)) {
            return;
        }
        String currentName = gen.getOutputContext().getCurrentName();
        String targetName = StringUtils.isNotBlank(targetField)
                ? targetField
                : (currentName == null ? null : currentName + resolveLabelFieldSuffix());
        if (StringUtils.isBlank(targetName) || Strings.CS.equals(targetName, currentName)) {
            return;
        }
        String label = DictTool.getLabel(dictType, value);
        if (label == null) {
            gen.writeNullField(targetName);
        } else {
            gen.writeStringField(targetName, label);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return this;
        }
        DictLabel annotation = property.getAnnotation(DictLabel.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(DictLabel.class);
        }
        if (annotation == null) {
            return this;
        }
        return new DictLabelSerializer(annotation.value(), annotation.target());
    }

    private String resolveLabelFieldSuffix() {
        DictConstants constants = SpringContextHolder.getBean(DictConstants.class);
        if (constants == null || constants.getSerializer() == null
                || StringUtils.isBlank(constants.getSerializer().getLabelFieldSuffix())) {
            return DictConstants.Serializer.DEFAULT_LABEL_FIELD_SUFFIX;
        }
        return constants.getSerializer().getLabelFieldSuffix();
    }
}
