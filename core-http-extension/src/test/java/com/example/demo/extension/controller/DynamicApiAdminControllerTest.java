package com.example.demo.extension.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.model.CommonResult;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.dto.DynamicApiCreateRequest;
import com.example.demo.extension.dto.DynamicApiQuery;
import com.example.demo.extension.dto.DynamicApiUpdateRequest;
import com.example.demo.extension.manager.DynamicApiService;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.support.DynamicApiException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicApiAdminControllerTest {

    @Test
    void createReturnsValidationDetails() {
        DynamicApiConstants constants = new DynamicApiConstants();
        Map<String, String> details = Collections.singletonMap("config", "invalid");
        DynamicApiException exception = new DynamicApiException(
                constants.getController().getBadRequestCode(),
                constants.getMessage().getConfigInvalid(),
                details
        );
        DynamicApiService service = new FailingService(exception);
        DynamicApiAdminController controller = new DynamicApiAdminController(service, constants);

        CommonResult<Object> result = controller.create(new DynamicApiCreateRequest());
        assertEquals(constants.getController().getBadRequestCode(), result.getCode());
        assertEquals(constants.getMessage().getConfigInvalid(), result.getMessage());
        assertEquals(details, result.getData());
    }

    private static class FailingService implements DynamicApiService {
        private final DynamicApiException exception;

        private FailingService(DynamicApiException exception) {
            this.exception = exception;
        }

        @Override
        public DynamicApi createApi(DynamicApiCreateRequest request) {
            throw exception;
        }

        @Override
        public DynamicApi updateApi(Long id, DynamicApiUpdateRequest request) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean enableApi(Long id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean disableApi(Long id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean deleteApi(Long id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean reloadAll() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public DynamicApi getApi(Long id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public IPage<DynamicApi> page(Page<DynamicApi> page, DynamicApiQuery query) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<DynamicApi> listEnabled() {
            throw new UnsupportedOperationException("not used");
        }
    }
}
