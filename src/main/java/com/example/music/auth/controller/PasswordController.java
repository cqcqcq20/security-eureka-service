package com.example.music.auth.controller;

import com.example.log.ApiLog;
import com.example.music.auth.config.token.SmsCodeAuthenticationToken;
import com.example.music.auth.service.CustomUserDetailsService;
import com.example.common.exception.BasicErrorCode;
import com.example.common.rep.HttpResponse;
import org.example.vlidator.annotation.CheckParam;
import org.example.vlidator.annotation.CheckParams;
import org.example.vlidator.utils.Validat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${app.password.matches}")
    public String matches;

    @PostMapping("forget")
    @ApiLog(module = "auth",desc = "忘记密码")
    @CheckParams({
            @CheckParam(value = Validat.NotNull, argName = "phone" , msg = "手机号不能为空"),
            @CheckParam(value = Validat.NotNull, argName = "area" , msg = "区号不能为空"),
            @CheckParam(value = Validat.NotNull, argName = "code" , msg = "验证码不能为空"),
            @CheckParam(value = Validat.Password, argName = "password" , msg = "密码不合法"),
    })
    public HttpResponse<?> forget(String phone, String area, String code, String password) {
        Authentication authentication = authenticationManager.authenticate(new SmsCodeAuthenticationToken(
                phone, code, area, "forget"
        ));
        User userPrincipal = (User) authentication.getPrincipal();
        if (!customUserDetailsService.updatePasswordById(userPrincipal.getUsername(), password)) {
            return HttpResponse.failure(BasicErrorCode.VALIDATOR_FAILURE_ERROR);
        }

        return HttpResponse.success();
    }
}
