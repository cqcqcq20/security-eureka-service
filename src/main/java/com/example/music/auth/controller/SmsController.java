package com.example.music.auth.controller;

import com.example.log.ApiLog;
import com.example.music.auth.service.RedisTokenService;
import com.example.common.rep.HttpResponse;
import org.example.vlidator.annotation.CheckParam;
import org.example.vlidator.annotation.CheckParams;
import org.example.vlidator.utils.Validat;
import org.example.vlidator.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private RedisTokenService redisTokenService;

    @PostMapping("send")
    @ApiLog(module = "auth" ,desc = "发送验证码")
    @CheckParams({
            @CheckParam(value = Validat.NotNull, argName = "type" , msg = "手机号不能为空"),
            @CheckParam(value = Validat.NotNull, argName = "phone" , msg = "区号不能为空"),
            @CheckParam(value = Validat.NotNull, argName = "area" , msg = "验证码不能为空"),
    })
    public HttpResponse<?> sendCode(String type, String phone, String area) {
        return HttpResponse.success(redisTokenService.generateSmsCode(phone, area,type));
    }
}
