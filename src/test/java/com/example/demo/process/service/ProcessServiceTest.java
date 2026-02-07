package com.example.demo.process.service;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProcessServiceTest {

    @Test
    void startProcess_returnsInstanceId() {
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        TaskService taskService = Mockito.mock(TaskService.class);
        RepositoryService repositoryService = Mockito.mock(RepositoryService.class);
        HistoryService historyService = Mockito.mock(HistoryService.class);
        ProcessService service = new ProcessService(runtimeService, taskService, repositoryService, historyService);

        ProcessInstance instance = Mockito.mock(ProcessInstance.class);
        when(instance.getId()).thenReturn("pid-1");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), anyString())).thenReturn(instance);

        String id = service.startProcess("key", "biz", new HashMap<>());
        assertEquals("pid-1", id);
    }

    @Test
    void taskQueries_delegateToEngine() {
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        TaskService taskService = Mockito.mock(TaskService.class);
        RepositoryService repositoryService = Mockito.mock(RepositoryService.class);
        HistoryService historyService = Mockito.mock(HistoryService.class);
        ProcessService service = new ProcessService(runtimeService, taskService, repositoryService, historyService);

        TaskQuery taskQuery = Mockito.mock(TaskQuery.class, Mockito.RETURNS_SELF);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(Collections.singletonList(Mockito.mock(Task.class)));

        List<Task> tasks = service.getMyTasks("user");
        assertEquals(1, tasks.size());

        List<Task> candidateTasks = service.getCandidateTasks("user", Collections.singletonList("group"));
        assertEquals(1, candidateTasks.size());
    }

    @Test
    void historyQueries_delegateToEngine() {
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        TaskService taskService = Mockito.mock(TaskService.class);
        RepositoryService repositoryService = Mockito.mock(RepositoryService.class);
        HistoryService historyService = Mockito.mock(HistoryService.class);
        ProcessService service = new ProcessService(runtimeService, taskService, repositoryService, historyService);

        HistoricActivityInstanceQuery query = Mockito.mock(HistoricActivityInstanceQuery.class, Mockito.RETURNS_SELF);
        when(historyService.createHistoricActivityInstanceQuery()).thenReturn(query);
        when(query.list()).thenReturn(Collections.singletonList(Mockito.mock(HistoricActivityInstance.class)));

        List<HistoricActivityInstance> history = service.getProcessHistory("pid");
        assertEquals(1, history.size());
    }
}
