package com.example.demo.common.web;

import com.example.demo.user.dto.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseControllerTest {

    @Test
    void exportExcel_writesResponseWithSuffix() throws Exception {
        TestController controller = new TestController();
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.doExport(response, Collections.singletonList(new UserVO()), "report");

        String header = response.getHeader("Content-Disposition");
        assertNotNull(header);
        assertTrue(header.contains("report.xlsx"));
        assertTrue(response.getContentType().startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(response.getContentAsByteArray().length > 0);
    }

    private static class TestController extends BaseController {
        void doExport(MockHttpServletResponse response, java.util.List<UserVO> data, String name) {
            exportExcel(response, data, UserVO.class, name);
        }
    }
}
