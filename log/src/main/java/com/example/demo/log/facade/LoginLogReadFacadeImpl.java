package com.example.demo.log.facade;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.log.api.dto.LoginLogRecordDTO;
import com.example.demo.log.api.dto.UserAgentInfoDTO;
import com.example.demo.log.api.facade.LoginLogReadFacade;
import com.example.demo.log.entity.SysLoginLog;
import com.example.demo.log.service.SysLoginLogService;
import com.example.demo.log.support.UserAgentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录日志读接口实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class LoginLogReadFacadeImpl implements LoginLogReadFacade {

    private final SysLoginLogService loginLogService;

    @Override
    public LoginLogRecordDTO getLatestByUserAndStatus(Long userId, Integer loginType, Integer status) {
        if (userId == null || loginType == null || status == null) {
            return null;
        }
        SysLoginLog record = loginLogService.getOne(Wrappers.lambdaQuery(SysLoginLog.class)
                .eq(SysLoginLog::getUserId, userId)
                .eq(SysLoginLog::getLoginType, loginType)
                .eq(SysLoginLog::getStatus, status)
                .orderByDesc(SysLoginLog::getLoginTime)
                .orderByDesc(SysLoginLog::getId)
                .last("limit 1"));
        return toRecordDTO(record);
    }

    @Override
    public UserAgentInfoDTO parseUserAgent(String userAgent) {
        UserAgentUtils.UserAgentInfo parsed = UserAgentUtils.parse(userAgent);
        UserAgentInfoDTO dto = new UserAgentInfoDTO();
        if (parsed == null) {
            return dto;
        }
        dto.setBrowser(parsed.getBrowser());
        dto.setOs(parsed.getOs());
        dto.setDeviceType(parsed.getDeviceType());
        return dto;
    }

    private LoginLogRecordDTO toRecordDTO(SysLoginLog record) {
        if (record == null) {
            return null;
        }
        LoginLogRecordDTO dto = new LoginLogRecordDTO();
        dto.setLoginIp(record.getLoginIp());
        dto.setBrowser(record.getBrowser());
        dto.setOs(record.getOs());
        dto.setDeviceType(record.getDeviceType());
        dto.setLoginTime(record.getLoginTime());
        return dto;
    }
}
