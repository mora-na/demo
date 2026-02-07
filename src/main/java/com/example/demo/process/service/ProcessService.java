package com.example.demo.process.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "camunda", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ProcessService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;

    /**
     * 启动流程实例
     */
    public String startProcess(String processKey, String businessKey,
                               Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                processKey,
                businessKey,  // 业务主键，关联你的业务表
                variables
        );
        log.info("启动流程: processKey={}, instanceId={}, businessKey={}",
                processKey, instance.getId(), businessKey);
        return instance.getId();
    }

    /**
     * 查询待办任务
     */
    public List<Task> getMyTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)       // 指定处理人
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 查询候选任务（组任务）
     */
    public List<Task> getCandidateTasks(String userId, List<String> groups) {
        return taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .taskCandidateGroupIn(groups)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
        log.info("完成任务: taskId={}", taskId);
    }

    /**
     * 签收任务（候选人 → 处理人）
     */
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * 转办任务
     */
    public void transferTask(String taskId, String toUserId) {
        taskService.setAssignee(taskId, toUserId);
    }

    /**
     * 添加审批意见
     */
    public void addComment(String taskId, String processInstanceId, String comment) {
        taskService.createComment(taskId, processInstanceId, comment);
    }

    /**
     * 查询流程历史
     */
    public List<HistoricActivityInstance> getProcessHistory(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
    }

    /**
     * 根据业务主键查询流程实例
     */
    public ProcessInstance getProcessByBusinessKey(String businessKey) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .singleResult();
    }

    /**
     * 终止/取消流程
     */
    public void cancelProcess(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }
}
