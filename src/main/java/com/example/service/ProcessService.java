package com.example.service;

import com.alibaba.fastjson.JSON;
import com.example.controller.TaskController;
import com.example.util.ToWeb;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hevin on 2018/1/19.
 */
@RestController
public class ProcessService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskController taskController;
    @Autowired
    private HistoryService historyService;

    @RequestMapping("/service/process/{deploymentId}/startInstance")
    public Object startInstance(@PathVariable String deploymentId) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(processDefinition.getKey());
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("success", "true");

        return jsonObject.toString();
    }
    //TODO 流程实例的结束接口

    @RequestMapping("instances")
    public Object getList() throws Exception {
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()
                .orderByProcessDefinitionId().desc().list();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data",list);
        jsonObject.put("success","true");
        return JSON.parse(jsonObject.toString());
    }
/*
返回的json格式
{
   variables:{
      userText:"userText",
   },
  [
    {
      taskId:"taskId",

      attachments:[
        {
          attachmentId:"attachmentId",
          data: ""
        },
      ]
    },
    {

    }
  ]
}
 */
    @GetMapping("process-instance/{processInstanceId}/variables")
    public Object getVariables(@PathVariable String processInstanceId) throws Exception {

        List<HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()
                .finished().processInstanceId(processInstanceId).list();


        List taskList = new ArrayList();
        for (HistoricTaskInstance task: list) {
            Map taskMap = new HashMap();

            List<Map> attachments = taskController.getAttachments(task.getId());

            taskMap.put("taskId", task.getId());
            taskMap.put("taskName",task.getName());
            taskMap.put("variables", new HashMap());
            taskMap.put("attachments",attachments);
            taskList.add(taskMap);

        }

        return taskList;
    }
}
