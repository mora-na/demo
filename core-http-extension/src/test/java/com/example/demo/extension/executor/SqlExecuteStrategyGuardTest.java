package com.example.demo.extension.executor;

import com.example.demo.common.mybatis.SqlGuardProperties;
import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.model.SqlExecuteConfig;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SqlExecuteStrategyGuardTest {

    @Test
    void unionIsRejectedBySqlGuard() {
        DynamicApiConstants constants = new DynamicApiConstants();
        SqlGuardProperties guard = new SqlGuardProperties();
        guard.setEnabled(true);
        guard.setBlockUnion(true);
        DataSource dataSource = new FailingDataSource();
        SqlExecuteStrategy strategy = new SqlExecuteStrategy(dataSource, guard, constants);

        SqlExecuteConfig config = new SqlExecuteConfig();
        config.setSql("select 1 union select 2");
        DynamicApiContext context = buildContext(config);
        DynamicApiExecuteResult result = strategy.execute(context);

        assertFalse(result.isSuccess());
        assertEquals(constants.getMessage().getSqlInvalid(), result.getMessage());
    }

    private DynamicApiContext buildContext(SqlExecuteConfig config) {
        DynamicApi api = new DynamicApi();
        api.setId(1L);
        api.setPath("/ext/sql");
        api.setMethod("GET");
        api.setType("SQL");
        DynamicApiMeta meta = new DynamicApiMeta(api, "SQL", DynamicApiAuthMode.INHERIT, config, null);
        DynamicApiRequest request = DynamicApiRequest.builder()
                .path("/ext/sql")
                .method("GET")
                .build();
        return new DynamicApiContext(meta, request, 1000L, null, null, null, null);
    }

    private static class FailingDataSource implements DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("Unexpected connection usage");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("Unexpected connection usage");
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("Not supported");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
    }
}
