package com.example.service;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Hevin on 2018/1/18.
 */
@RestController
@RequestMapping("group")
public class GroupService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value="/new",method =  RequestMethod.PUT)
    public Object newGroup(String groupId,String name,String type) throws Exception {

        Group group = identityService.newGroup(groupId);
        group.setName(name);
        group.setType(type);
        identityService.saveGroup(group);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", "true");
        jsonObject.put("groupId", group.getId());
        return jsonObject.toString();

    }

    @RequestMapping("{groupId}")
    public Object getUser(@PathVariable String groupId) throws Exception {

        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        return group;

    }

    @RequestMapping("list")
    public Object getUser() throws Exception {
        List<Group> lists = identityService.createGroupQuery().list();
        return lists;
    }

    @RequestMapping(value = "/{groupId}/user", method = RequestMethod.POST)
    public Object groupAddUsers(@PathVariable String groupId, String userId) throws Exception {
        identityService.createMembership(userId, groupId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", "true");
        return jsonObject.toString();
    }


}
