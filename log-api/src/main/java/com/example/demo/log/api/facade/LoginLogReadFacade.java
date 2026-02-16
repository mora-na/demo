package com.example.demo.log.api.facade;

import com.example.demo.log.api.dto.LoginLogRecordDTO;
import com.example.demo.log.api.dto.UserAgentInfoDTO;

/**
 * 登录日志读接口，供其他模块按契约读取登录审计信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public interface LoginLogReadFacade {

    LoginLogRecordDTO getLatestByUserAndStatus(Long userId, Integer loginType, Integer status);

    UserAgentInfoDTO parseUserAgent(String userAgent);
}
