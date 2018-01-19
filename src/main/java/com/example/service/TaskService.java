package com.example.service;

import com.example.util.Status;
import com.example.util.ToWeb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.catalina.util.URLEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hevin on 2018/1/19.
 */
@RestController
@RequestMapping("service")
public class TaskService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private org.activiti.engine.TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/model/{modelId}/importXML", method = RequestMethod.GET)
    public Object download(@PathVariable String modelId, @RequestHeader HttpHeaders requestHeaders) throws Exception {


        Model modelData = repositoryService.getModel(modelId);
        if(modelData ==null){
            return ToWeb.buildResult().status(Status.FAIL).msg("系统错误");
        }
        byte[] bpmnBytes = repositoryService
                .getModelEditorSource(modelData.getId());
        JsonNode modelNode = new ObjectMapper().readTree(bpmnBytes);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return ToWeb.buildResult().status(Status.FAIL).msg("系统错误");
        }

        byte[] xmlBytys = new BpmnXMLConverter().convertToXML(model);

        // 封装输出流
        String filename = modelData.getName();
        if (requestHeaders.USER_AGENT.toLowerCase().contains("msie")) {
            // IE
            filename = new URLEncoder().encode(filename, "UTF-8");
        } else {
            // 非IE
            filename = new String(filename.getBytes("UTF-8"), "iso-8859-1");
        }

        filename = filename + ".bpmn20.xml";

        //封装http头部信息
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(xmlBytys,
                headers, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/model/{deploymentId}/run", method = RequestMethod.GET)
    public Object runProcess(@PathVariable String deploymentId) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(processDefinition.getKey());
        System.out.println(
                "Found process definition ["
                        + processDefinition.getName() + "] with id ["
                        + processDefinition.getId() + "]");
//        Scanner scanner = new Scanner(System.in);


        while (processInstance != null && !processInstance.isEnded()) {
            List<Task> tasks = taskService.createTaskQuery().list();

            System.out.println("Active outstanding tasks: [" + tasks.size() + "]");

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                System.out.println("Processing Task [" + task.getName() + "]");
                Map<String, Object> variables = new HashMap<>();
                FormData formData = formService.getTaskFormData(task.getId());
                for (FormProperty formProperty : formData.getFormProperties()) {
                    if (LongFormType.class.isInstance(formProperty.getType())) {
                        System.out.println("请输入"+formProperty.getName() + ":");
                        Long value =  2L;
                        variables.put(formProperty.getId(), value);
                        System.out.println("导入表单值"+formProperty.getName()+"="+value);
                    } else {
                        System.out.println("<form type not supported>");
                    }
                }

                taskService.complete(task.getId(), variables);

                HistoricActivityInstance endActivity = null;
                List<HistoricActivityInstance> activities =
                        historyService.createHistoricActivityInstanceQuery()
                                .processInstanceId(processInstance.getId()).finished()
                                .orderByHistoricActivityInstanceEndTime().asc()
                                .list();
                for (HistoricActivityInstance activity : activities) {
                    if (activity.getActivityType().equals("startEvent")) {

                        System.out.println("BEGIN " + processDefinition.getName()
                                + " [" + processInstance.getProcessDefinitionKey()
                                + "] " + activity.getStartTime());
                    }
                    if (activity.getActivityType().equals("endEvent")) {
                        // Handle edge case where end step happens so fast that the end step
                        // and previous step(s) are sorted the same. So, cache the end step
                        //and display it last to represent the logical sequence.
                        endActivity = activity;
                    } else {
                        System.out.println("-- " + activity.getActivityName()
                                + " [" + activity.getActivityId() + "] "
                                + activity.getDurationInMillis() + " ms");
                    }
                }

                if (endActivity != null) {
                    System.out.println("-- " + endActivity.getActivityName()
                            + " [" + endActivity.getActivityId() + "] "
                            + endActivity.getDurationInMillis() + " ms");
                    System.out.println("COMPLETE " + processDefinition.getName() + " ["
                            + processInstance.getProcessDefinitionKey() + "] "
                            + endActivity.getEndTime());
                }
            }

            processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstance.getId()).singleResult();

        }


        return null;
    }
}
