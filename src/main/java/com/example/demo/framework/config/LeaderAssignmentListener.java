package com.example.demo.framework.config;

import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("leaderAssignmentListener")
public class LeaderAssignmentListener implements TaskListener {

    private final UserService userService;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 获取申请人
        String applicant = (String) delegateTask.getVariable("applicant");
        // 查询申请人的直属领导
        String leader = userService.getBaseMapper().selectById(applicant).getName();
        // 自动分配给领导
        delegateTask.setAssignee(leader);
    }

}
