package com.example.service;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hevin on 2018/2/3.
 */
@RestController
public class AttachmentService {
    @Autowired
    private TaskService taskService;

    @DeleteMapping("attachment/{attachmentId}")
    public Object deleteAttachment(@PathVariable String attachmentId) throws Exception{
        taskService.deleteAttachment(attachmentId);
        Map Hashmap = new HashMap();
        Hashmap.put("success",true);

        return Hashmap;
    }

    @GetMapping("attachment/{attachmentId}")
    public Object getAttachment(@PathVariable String attachmentId) throws Exception {
        InputStream attachmentContent = taskService.getAttachmentContent(attachmentId);
        HttpHeaders headers = new HttpHeaders();
        byte[] byt = new byte[attachmentContent.available()];


        attachmentContent.read(byt);
        Attachment attachment = taskService.getAttachment(attachmentId);
        String attachmentName = attachment.getName();
        String attachmentType = attachment.getType();
        if(attachmentType.startsWith("image")) {

            headers.setContentType(MediaType.IMAGE_JPEG);
        }else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 设置这一行会触发浏览器下载
            headers.setContentDispositionFormData("attachment", attachmentName);
        }
        return new ResponseEntity<>(byt,
                headers,HttpStatus.OK);
    }


}
