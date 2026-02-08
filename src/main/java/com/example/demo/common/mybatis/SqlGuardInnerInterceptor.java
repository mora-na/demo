package com.example.demo.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;

import java.sql.Connection;
import java.util.Locale;

public class SqlGuardInnerInterceptor implements InnerInterceptor {

    private final SqlGuardProperties properties;

    public SqlGuardInnerInterceptor(SqlGuardProperties properties) {
        this.properties = properties;
    }

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

    private boolean isUpdateOrDelete(String sql) {
        String stripped = stripLeadingComments(sql);
        String lower = stripped.toLowerCase(Locale.ROOT);
        return startsWithKeyword(lower, "update") || startsWithKeyword(lower, "delete");
    }

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

    private boolean startsWithKeyword(String sql, String keyword) {
        if (!sql.startsWith(keyword)) {
            return false;
        }
        if (sql.length() == keyword.length()) {
            return true;
        }
        return !isIdentifierChar(sql.charAt(keyword.length()));
    }

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

    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static final class ScanState {
        private final String source;
        private int index;
        private int depth;
        private boolean inSingleQuote;
        private boolean inDoubleQuote;
        private boolean inBacktick;
        private boolean inLineComment;
        private boolean inBlockComment;

        private ScanState(String source) {
            this.source = source;
        }

        private boolean hasNext() {
            return index < source.length();
        }

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

        private boolean isInLiteralOrComment() {
            return inSingleQuote || inDoubleQuote || inBacktick || inLineComment || inBlockComment;
        }
    }
}
