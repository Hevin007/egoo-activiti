package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.example.util.Status;
import com.example.util.ToWeb;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.POJONode;
import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;

import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.TaskFormDataImpl;
import org.activiti.engine.impl.json.JsonListConverter;
import org.activiti.engine.impl.persistence.entity.AttachmentEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.catalina.util.URLEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonSimpleJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;

/**
 * Created by Hevin on 2018/1/19.
 */

@RestController
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    @Autowired
    private FormService formService;
    @RequestMapping("tasks")
    public Object getList(@CookieValue String userId) throws Exception {

        List<Task> list = taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId).list();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success",true);
        jsonObject.put("data", list);
        return JSON.parse(jsonObject.toString());

    }


    @PostMapping("submitTaskForm")
    public Object submitTaskForm(String taskId, HttpServletRequest request, MultipartFile attachment) throws Exception  {
        // TODO: 2018/2/3 身份验证机制，会未添加useId
        Map formValues = new HashMap<>();
        List<FormProperty> formProperties = formService.getTaskFormData(taskId).getFormProperties();
        for(FormProperty formProperty: formProperties) {
            String value = request.getParameter(formProperty.getId());
            formValues.put(formProperty.getId(), value);
        }
        if(!attachment.isEmpty()) {

            String attachmentType = attachment.getContentType();
            String attachmentName = attachment.getOriginalFilename();
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            String processInstanceId = task.getProcessInstanceId();
            taskService.createAttachment(attachmentType, taskId,
                    processInstanceId, attachmentName, "", attachment.getInputStream());
        }
        taskService.complete(taskId,formValues);

        Map Hashmap = new HashMap();
        Hashmap.put("success",true);

        return Hashmap;
    }

    /*
    返回的json格式
{
  "variables":{
    "userText":"userText"
  },
  "tasks": [
    {
      "taskId":"taskId",
      "taskName": "taskName",
      "attachments": [
        {
          "attachmentId":"attachmentId",
          "attachmentName": "attachmentName",
        },
      ]
    },
  ]
}
     */
    @GetMapping("task/{taskId}/variables")
    public Object getVariables(@PathVariable String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        // 结果集合中加入variables
        Map resultMap = new HashMap();
        resultMap.put("variables", variables);


        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .finished().processInstanceId(processInstanceId).list();

        List taskList = new ArrayList();

        for(HistoricTaskInstance taskInstance: list) {
            Map taskMap = new HashMap();
            taskMap.put("taskId", taskInstance.getId());
            taskMap.put("taskName", taskInstance.getName());
            List attachments = new ArrayList();
            List<Attachment> taskAttachments = taskService.getTaskAttachments(taskInstance.getId());
            for(Attachment attachment: taskAttachments) {
                Map attachmentMap = new HashMap();
                attachmentMap.put("attachmentId", attachment.getId());
                attachmentMap.put("attachmentName", attachment.getName());
                attachments.add(attachmentMap);
            }
            taskMap.put("attachments", attachments);

            taskList.add(taskMap);
        }

        resultMap.put("tasks",taskList);

        return resultMap;
    }




    @GetMapping("/task/{taskId}/attachments")
    public List<Map> getAttachments(@PathVariable String taskId) throws Exception {
        List<Attachment> taskAttachments = taskService.getTaskAttachments(taskId);
        List<Map> list = new ArrayList<Map>();
        for(Attachment attachment : taskAttachments) {
            HashMap map = new HashMap();
            InputStream attachmentContent = taskService.getAttachmentContent(attachment.getId());
            StringBuilder sb = new StringBuilder();
            String line;

            BufferedReader br = new BufferedReader(new InputStreamReader(attachmentContent));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String str = sb.toString();
            map.put("attachmentId", attachment.getId());
            map.put("data", str);
            list.add(map);
        }

        return list;
    }

    @GetMapping("test/{processInstanceId}")
    public Object getTest(@PathVariable String processInstanceId) {
        List<HistoricTaskInstance> list1 = historyService.createHistoricTaskInstanceQuery().finished().processInstanceId(processInstanceId).list();
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        JSONObject jsonObject = new JSONObject();
        for(HistoricTaskInstance task : list1) {

        }

        return JSON.parse(jsonObject.toString());
    }




}
