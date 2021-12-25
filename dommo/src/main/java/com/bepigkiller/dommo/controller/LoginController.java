package com.bepigkiller.dommo.controller;

import com.bepigkiller.dommo.vo.R;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class LoginController {
    @PostMapping("login")
    public R login(HashMap<String, String> map){
        String name = map.get("user");
        String password = map.get("password");
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, password);
        subject.login(token);
    }
}
