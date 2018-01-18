package com.example.service;

import org.activiti.engine.*;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.impl.util.json.JSONString;
import org.activiti.rest.editor.model.ModelSaveRestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

/**
 * Created by Hevin on 2018/1/5.
 */
@RestController
@RequestMapping("user")
public class UserService {

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value="/new",method =  RequestMethod.PUT)
    public Object newUser(String userId,String password) throws Exception {

        User user = identityService.newUser(userId);
        user.setPassword(password);
        identityService.saveUser(user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", "true");

        return jsonObject.toString();


    }

    @RequestMapping("{userId}")
    public Object getUser(@PathVariable String userId) throws Exception {

        User user = identityService.createUserQuery().userId(userId).singleResult();
        return user;

    }


}
