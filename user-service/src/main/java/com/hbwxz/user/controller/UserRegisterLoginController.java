package com.hbwxz.user.controller;

import com.hbwxz.common.response.CommonResponse;
import com.hbwxz.user.pojo.User;
import com.hbwxz.user.service.UserRegisterLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/register")
public class UserRegisterLoginController {

    @Autowired
    private UserRegisterLoginService userRegisterLoginService;

    @PostMapping("/name-password")
    public CommonResponse namePasswordRegister (@RequestBody User user) {
        return userRegisterLoginService.namePasswordRegister(user);
    }
}
