package com.example.demo.config.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * 简化 JSON Schema 校验器。
 */
@Component
public class ConfigSchemaValidator {

    private final ObjectMapper objectMapper;

    public ConfigSchemaValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ValidationResult validate(String schemaJson, JsonNode value) {
        if (StringUtils.isBlank(schemaJson) || value == null) {
            return ValidationResult.ok();
        }
        JsonNode schemaNode;
        try {
            schemaNode = objectMapper.readTree(schemaJson);
        } catch (Exception ex) {
            return ValidationResult.failed("schema.invalid");
        }
        return validateNode(schemaNode, value, "$");
    }

    private ValidationResult validateNode(JsonNode schema, JsonNode value, String path) {
        if (schema == null || schema.isNull()) {
            return ValidationResult.ok();
        }
        String type = schema.path("type").asText(null);
        if (type != null && !type.isEmpty()) {
            if (!matchType(type, value)) {
                return ValidationResult.failed(path + ": type mismatch");
            }
        }
        JsonNode required = schema.get("required");
        JsonNode properties = schema.get("properties");
        if (required != null || properties != null) {
            if (!value.isObject()) {
                return ValidationResult.failed(path + ": expected object");
            }
            if (required != null && required.isArray()) {
                for (JsonNode item : required) {
                    if (item == null || !item.isTextual()) {
                        continue;
                    }
                    String name = item.asText();
                    if (!value.has(name)) {
                        return ValidationResult.failed(path + ": missing required field " + name);
                    }
                }
            }
            if (properties != null && properties.isObject()) {
                Iterator<String> fields = properties.fieldNames();
                while (fields.hasNext()) {
                    String name = fields.next();
                    JsonNode propertySchema = properties.get(name);
                    if (value.has(name)) {
                        ValidationResult result = validateNode(propertySchema, value.get(name), path + "." + name);
                        if (!result.isValid()) {
                            return result;
                        }
                    }
                }
            }
        }
        JsonNode items = schema.get("items");
        if (items != null) {
            if (!value.isArray()) {
                return ValidationResult.failed(path + ": expected array");
            }
            int index = 0;
            for (JsonNode element : value) {
                ValidationResult result = validateNode(items, element, path + "[" + index + "]");
                if (!result.isValid()) {
                    return result;
                }
                index++;
            }
        }
        return ValidationResult.ok();
    }

    private boolean matchType(String type, JsonNode value) {
        switch (type) {
            case "string":
                return value.isTextual();
            case "number":
            case "integer":
                return value.isNumber();
            case "boolean":
                return value.isBoolean();
            case "object":
                return value.isObject();
            case "array":
                return value.isArray();
            case "null":
                return value.isNull();
            default:
                return true;
        }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failed(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
