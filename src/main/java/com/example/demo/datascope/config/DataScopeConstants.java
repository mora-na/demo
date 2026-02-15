package com.example.demo.datascope.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Datascope 模块常量配置，支持配置覆盖并提供默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "datascope.constants")
public class DataScopeConstants {

    private Scope scope = new Scope();
    private Layer layer = new Layer();
    private Label label = new Label();
    private Rule rule = new Rule();
    private Sql sql = new Sql();
    private Status status = new Status();
    private Controller controller = new Controller();
    private Filter filter = new Filter();
    private Parser parser = new Parser();

    @Data
    public static class Scope {
        public static final String DEFAULT_GLOBAL_SCOPE_KEY = "*";
        public static final String DEFAULT_GLOBAL_SCOPE_MENU_NAME = "全局覆盖";
        public static final String DEFAULT_GLOBAL_SCOPE_PERMISSION = "*";

        /**
         * 用户覆盖中的全局 scopeKey。
         */
        private String globalScopeKey = DEFAULT_GLOBAL_SCOPE_KEY;
        /**
         * 全局覆盖在列表展示中的菜单名。
         */
        private String globalScopeMenuName = DEFAULT_GLOBAL_SCOPE_MENU_NAME;
        /**
         * 全局覆盖在列表展示中的权限标识。
         */
        private String globalScopePermission = DEFAULT_GLOBAL_SCOPE_PERMISSION;
    }

    @Data
    public static class Layer {
        public static final String DEFAULT_SOURCE_LAYER_3 = "LAYER3";
        public static final String DEFAULT_SOURCE_LAYER_2 = "LAYER2";
        public static final String DEFAULT_SOURCE_LAYER_1 = "LAYER1";

        private String sourceLayer3 = DEFAULT_SOURCE_LAYER_3;
        private String sourceLayer2 = DEFAULT_SOURCE_LAYER_2;
        private String sourceLayer1 = DEFAULT_SOURCE_LAYER_1;
    }

    @Data
    public static class Label {
        public static final String DEFAULT_FINAL_ALL = "ALL";
        public static final String DEFAULT_FINAL_NONE = "NONE";
        public static final String DEFAULT_FINAL_SELF = "SELF";
        public static final String DEFAULT_FINAL_DEPT = "DEPT";
        public static final String DEFAULT_FINAL_DEPT_AND_SELF = "DEPT+SELF";

        private String finalAll = DEFAULT_FINAL_ALL;
        private String finalNone = DEFAULT_FINAL_NONE;
        private String finalSelf = DEFAULT_FINAL_SELF;
        private String finalDept = DEFAULT_FINAL_DEPT;
        private String finalDeptAndSelf = DEFAULT_FINAL_DEPT_AND_SELF;
    }

    @Data
    public static class Rule {
        public static final String DEFAULT_SOURCE_DEFAULT = "DEFAULT";
        public static final String DEFAULT_SOURCE_MAPPING = "MAPPING";
        public static final String DEFAULT_DEPT_COLUMN = "create_dept";
        public static final String DEFAULT_USER_COLUMN = "create_by";

        private String sourceDefault = DEFAULT_SOURCE_DEFAULT;
        private String sourceMapping = DEFAULT_SOURCE_MAPPING;
        private String defaultDeptColumn = DEFAULT_DEPT_COLUMN;
        private String defaultUserColumn = DEFAULT_USER_COLUMN;
    }

    @Data
    public static class Sql {
        public static final String DEFAULT_NO_FILTER_TEXT = "无过滤";
        public static final String DEFAULT_NONE_CONDITION = "1 = 0";
        public static final String DEFAULT_IN_OPERATOR = " IN ";
        public static final String DEFAULT_EQUALS_OPERATOR = " = ";
        public static final String DEFAULT_SELF_USER_PARAM = ":userId";
        public static final String DEFAULT_OR_OPERATOR = " OR ";
        public static final String DEFAULT_DOT = ".";
        public static final String DEFAULT_LEFT_BRACKET = "(";
        public static final String DEFAULT_RIGHT_BRACKET = ")";

        private String noFilterText = DEFAULT_NO_FILTER_TEXT;
        private String noneCondition = DEFAULT_NONE_CONDITION;
        private String inOperator = DEFAULT_IN_OPERATOR;
        private String equalsOperator = DEFAULT_EQUALS_OPERATOR;
        private String selfUserParam = DEFAULT_SELF_USER_PARAM;
        private String orOperator = DEFAULT_OR_OPERATOR;
        private String dot = DEFAULT_DOT;
        private String leftBracket = DEFAULT_LEFT_BRACKET;
        private String rightBracket = DEFAULT_RIGHT_BRACKET;
    }

    @Data
    public static class Status {
        public static final int DEFAULT_ENABLED = 1;
        public static final int DEFAULT_DISABLED = 0;
        public static final int DEFAULT_ROLE_ACTIVE = 1;

        private int enabled = DEFAULT_ENABLED;
        private int disabled = DEFAULT_DISABLED;
        private int roleActive = DEFAULT_ROLE_ACTIVE;
    }

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Filter {
        public static final int DEFAULT_TYPE = 1;
        public static final int DEFAULT_TYPE_MIN = 1;
        public static final int DEFAULT_TYPE_MAX = 3;

        private int defaultType = DEFAULT_TYPE;
        private int typeMin = DEFAULT_TYPE_MIN;
        private int typeMax = DEFAULT_TYPE_MAX;
    }

    @Data
    public static class Parser {
        public static final String DEFAULT_DEPT_ID_SEPARATOR = ",";

        private String deptIdSeparator = DEFAULT_DEPT_ID_SEPARATOR;
    }
}
