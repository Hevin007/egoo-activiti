package com.example.service;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Hevin on 2018/1/15.
 */
@Controller
@RequestMapping(value = "loginValidate")
public class LoginService {
    @Autowired
    private IdentityService identityService;
    @RequestMapping( method = RequestMethod.POST)
    public String validateUser(String username, String password, HttpServletResponse response) {
        try {
            User user = identityService.createUserQuery().userId(username).singleResult();

            if(user.getPassword().equals(password)) {
                response.addCookie(new Cookie("username", username));
                response.sendRedirect("/model-list.html");

                return "success";
            }else {

                return "password error";
            }
        }catch (Exception e) {
            System.out.println(e);
            return "username error";
        }


    }
}
