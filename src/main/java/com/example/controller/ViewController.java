package com.example.controller;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Created by Hevin on 2018/1/21.
 */
@Controller
public class ViewController {
    @Autowired
    private FormService formService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;



    @GetMapping("/form/{taskId}")
    public String getForm(@PathVariable String taskId, Model model, @CookieValue String userId) {
        model.addAttribute("data",formService.getTaskFormData(taskId).getFormProperties());

        model.addAttribute("taskId",taskId);
        model.addAttribute("userId",userId);
        return "report";
    }

    @GetMapping("/model-list.html")
    public String getModelList(Model model) {
        List<org.activiti.engine.repository.Model> list = repositoryService
                .createModelQuery().orderByCreateTime().desc().list();
        model.addAttribute("models",list);
        return "model-list";
    }
}
