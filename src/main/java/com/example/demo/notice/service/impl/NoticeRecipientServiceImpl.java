package com.example.demo.notice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.notice.entity.NoticeRecipient;
import com.example.demo.notice.mapper.NoticeRecipientMapper;
import com.example.demo.notice.service.NoticeRecipientService;
import org.springframework.stereotype.Service;

/**
 * 通知接收记录服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Service
public class NoticeRecipientServiceImpl extends ServiceImpl<NoticeRecipientMapper, NoticeRecipient>
        implements NoticeRecipientService {
}
