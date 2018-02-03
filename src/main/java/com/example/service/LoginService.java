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
    public void validateUser(String username, String password, HttpServletResponse response) throws Exception {
        // 这里的username其实是userId
        try {
            String userId = username;
            User user = identityService.createUserQuery().userId(userId).singleResult();

            if(user.getPassword().equals(password)) {
                response.addCookie(new Cookie("userId", userId));
                response.sendRedirect("/model-list.html");
// TODO: 2018/1/21  返回值调整
                return;
            }else {
                response.sendRedirect("/");
                return ;
            }
        }catch (Exception e) {
            response.sendRedirect("/");
            System.out.println(e);
            return;
        }


    }
}
