package com.example.music.auth.controller;

import com.example.common.rep.HttpResponse;
import com.example.log.ApiLog;
import com.example.music.auth.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController()
@RequestMapping("/")
public class UserController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @PostMapping("user")
    @ApiLog(module = "auth",desc = "用户信息")
    public HttpResponse<?> user(Principal principal) {
        return HttpResponse.success(customUserDetailsService.loadUserByUserId(principal.getName()));
    }

}
