/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.rest.editor.model;

import com.example.util.Status;
import com.example.util.ToWeb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.catalina.util.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("service")
public class ModelSaveRestResource implements ModelDataJsonConstants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelSaveRestResource.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private IdentityService identityService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId
            , String name, String description
            , String json_xml, String svg_xml) {
        try {
            Model model = repositoryService.getModel(modelId);

            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);

            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();

        } catch (Exception e) {
            LOGGER.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }

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
        return new ResponseEntity<byte[]>(xmlBytys,
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
                Map<String, Object> variables = new HashMap<String, Object>();
                FormData formData = formService.getTaskFormData(task.getId());
                for (FormProperty formProperty : formData.getFormProperties()) {
                    if (LongFormType.class.isInstance(formProperty.getType())) {
                        System.out.println("请输入"+formProperty.getName() + ":");
                        Long value =  new Long(2);
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
