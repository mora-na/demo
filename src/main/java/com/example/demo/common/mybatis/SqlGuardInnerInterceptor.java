package com.example.demo.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;

import java.sql.Connection;
import java.util.Locale;

/**
 * SQL 安全防护拦截器，用于阻断多语句执行与全表更新/删除。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class SqlGuardInnerInterceptor implements InnerInterceptor {

    private final SqlGuardProperties properties;

    /**
     * 构建 SQL 防护拦截器。
     *
     * @param properties 防护配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public SqlGuardInnerInterceptor(SqlGuardProperties properties) {
        this.properties = properties;
    }

    /**
     * 在 SQL 预编译前执行防护判断。
     *
     * @param statementHandler StatementHandler
     * @param connection       数据库连接
     * @param transactionTimeout 事务超时（秒）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public void beforePrepare(StatementHandler statementHandler, Connection connection, Integer transactionTimeout) {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        BoundSql boundSql = statementHandler.getBoundSql();
        if (boundSql == null) {
            return;
        }
        String sql = boundSql.getSql();
        if (sql == null) {
            return;
        }
        String trimmed = sql.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (properties.isBlockMultiStatement() && hasMultipleStatements(trimmed)) {
            throw new IllegalStateException("SQL guard blocked multiple statements");
        }
        if (properties.isBlockFullTable() && isUpdateOrDelete(trimmed) && !hasTopLevelWhere(trimmed)) {
            throw new IllegalStateException("SQL guard blocked full table update/delete");
        }
    }

    /**
     * 判断 SQL 是否为 UPDATE/DELETE。
     *
     * @param sql SQL 文本
     * @return true 表示 UPDATE/DELETE
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isUpdateOrDelete(String sql) {
        String stripped = stripLeadingComments(sql);
        String lower = stripped.toLowerCase(Locale.ROOT);
        return startsWithKeyword(lower, "update") || startsWithKeyword(lower, "delete");
    }

    /**
     * 判断 SQL 顶层是否存在多个语句。
     *
     * @param sql SQL 文本
     * @return true 表示包含多个语句
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean hasMultipleStatements(String sql) {
        int semicolonIndex = findTopLevelSemicolon(sql);
        if (semicolonIndex < 0) {
            return false;
        }
        for (int i = semicolonIndex + 1; i < sql.length(); i++) {
            if (!Character.isWhitespace(sql.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找顶层分号位置，忽略字符串、注释、括号嵌套。
     *
     * @param sql SQL 文本
     * @return 分号索引，未找到返回 -1
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int findTopLevelSemicolon(String sql) {
        ScanState state = new ScanState(sql);
        while (state.hasNext()) {
            char c = state.next();
            if (state.isInLiteralOrComment()) {
                continue;
            }
            if (c == '(') {
                state.depth++;
                continue;
            }
            if (c == ')') {
                if (state.depth > 0) {
                    state.depth--;
                }
                continue;
            }
            if (state.depth == 0 && c == ';') {
                return state.index - 1;
            }
        }
        return -1;
    }

    /**
     * 判断 SQL 顶层是否包含 WHERE 子句。
     *
     * @param sql SQL 文本
     * @return true 表示存在顶层 WHERE
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean hasTopLevelWhere(String sql) {
        ScanState state = new ScanState(sql.toLowerCase(Locale.ROOT));
        while (state.hasNext()) {
            char c = state.next();
            if (state.isInLiteralOrComment()) {
                continue;
            }
            if (c == '(') {
                state.depth++;
                continue;
            }
            if (c == ')') {
                if (state.depth > 0) {
                    state.depth--;
                }
                continue;
            }
            if (state.depth == 0 && matchesKeywordAt(state.source, state.index - 1, "where")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移除 SQL 前置注释，便于关键字判断。
     *
     * @param sql SQL 文本
     * @return 去除前置注释后的 SQL
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String stripLeadingComments(String sql) {
        int i = 0;
        int length = sql.length();
        while (i < length) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '-' && i + 1 < length && sql.charAt(i + 1) == '-') {
                i = skipLine(sql, i + 2);
                continue;
            }
            if (c == '/' && i + 1 < length && sql.charAt(i + 1) == '*') {
                i = skipBlock(sql, i + 2);
                continue;
            }
            break;
        }
        return sql.substring(i);
    }

    /**
     * 跳过单行注释。
     *
     * @param sql   SQL 文本
     * @param start 起始索引
     * @return 跳过后的索引
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int skipLine(String sql, int start) {
        int i = start;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\n' || c == '\r') {
                return i + 1;
            }
            i++;
        }
        return i;
    }

    /**
     * 跳过块注释。
     *
     * @param sql   SQL 文本
     * @param start 起始索引
     * @return 跳过后的索引
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int skipBlock(String sql, int start) {
        int i = start;
        while (i + 1 < sql.length()) {
            if (sql.charAt(i) == '*' && sql.charAt(i + 1) == '/') {
                return i + 2;
            }
            i++;
        }
        return sql.length();
    }

    /**
     * 判断字符串是否以关键字起始，并校验边界字符。
     *
     * @param sql     SQL 文本（已小写）
     * @param keyword 关键字
     * @return true 表示匹配关键字
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean startsWithKeyword(String sql, String keyword) {
        if (!sql.startsWith(keyword)) {
            return false;
        }
        if (sql.length() == keyword.length()) {
            return true;
        }
        return !isIdentifierChar(sql.charAt(keyword.length()));
    }

    /**
     * 在指定位置判断是否匹配关键字（含边界判断）。
     *
     * @param sql     SQL 文本（已小写）
     * @param index   关键字起始索引
     * @param keyword 关键字
     * @return true 表示匹配
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean matchesKeywordAt(String sql, int index, String keyword) {
        if (index < 0 || index + keyword.length() > sql.length()) {
            return false;
        }
        for (int i = 0; i < keyword.length(); i++) {
            if (sql.charAt(index + i) != keyword.charAt(i)) {
                return false;
            }
        }
        int before = index - 1;
        int after = index + keyword.length();
        if (before >= 0 && isIdentifierChar(sql.charAt(before))) {
            return false;
        }
        return after >= sql.length() || !isIdentifierChar(sql.charAt(after));
    }

    /**
     * 判断字符是否为 SQL 标识符组成字符。
     *
     * @param c 字符
     * @return true 表示可作为标识符
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * SQL 扫描状态机，用于跳过字面量/注释并跟踪括号层级。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static final class ScanState {
        private final String source;
        private int index;
        private int depth;
        private boolean inSingleQuote;
        private boolean inDoubleQuote;
        private boolean inBacktick;
        private boolean inLineComment;
        private boolean inBlockComment;

        /**
         * 构建扫描状态。
         *
         * @param source SQL 文本
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private ScanState(String source) {
            this.source = source;
        }

        /**
         * 是否还有可扫描字符。
         *
         * @return true 表示未扫描完
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private boolean hasNext() {
            return index < source.length();
        }

        /**
         * 读取下一个字符并更新状态机。
         *
         * @return 当前字符
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private char next() {
            char c = source.charAt(index++);
            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                return c;
            }
            if (inBlockComment) {
                if (c == '*' && index < source.length() && source.charAt(index) == '/') {
                    inBlockComment = false;
                    index++;
                }
                return c;
            }
            if (inSingleQuote) {
                if (c == '\'') {
                    if (index < source.length() && source.charAt(index) == '\'') {
                        index++;
                    } else {
                        inSingleQuote = false;
                    }
                }
                return c;
            }
            if (inDoubleQuote) {
                if (c == '"') {
                    if (index < source.length() && source.charAt(index) == '"') {
                        index++;
                    } else {
                        inDoubleQuote = false;
                    }
                }
                return c;
            }
            if (inBacktick) {
                if (c == '`') {
                    inBacktick = false;
                }
                return c;
            }
            if (c == '-' && index < source.length() && source.charAt(index) == '-') {
                inLineComment = true;
                index++;
                return c;
            }
            if (c == '/' && index < source.length() && source.charAt(index) == '*') {
                inBlockComment = true;
                index++;
                return c;
            }
            if (c == '\'') {
                inSingleQuote = true;
            } else if (c == '"') {
                inDoubleQuote = true;
            } else if (c == '`') {
                inBacktick = true;
            }
            return c;
        }

        /**
         * 判断是否处于字面量或注释中。
         *
         * @return true 表示处于字面量或注释
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private boolean isInLiteralOrComment() {
            return inSingleQuote || inDoubleQuote || inBacktick || inLineComment || inBlockComment;
        }
    }
}
