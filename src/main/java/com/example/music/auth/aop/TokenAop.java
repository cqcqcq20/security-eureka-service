package com.example.music.auth.aop;

import com.alibaba.fastjson.JSONObject;
import com.example.common.exception.BasicErrorCode;
import com.example.common.exception.ErrorCode;
import com.example.common.rep.HttpResponse;
import com.example.common.utils.Md5Utils;
import com.example.log.LogRecordBuilder;
import com.example.log.interceptor.Interceptor;
import com.example.log.interceptor.UidInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 用于基于oauth2运行时出现的错误
 */
@Aspect
@Component
public class TokenAop extends LogRecordBuilder {

    public static final String execution = "execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UidInterceptor uidInterceptor;

    @Before(value = execution)
    public void doBefore() {
        setStartAt();
    }

    @Around(value = execution)
    public ResponseEntity<?> doSomething(ProceedingJoinPoint joinPoint) throws Throwable {
        ResponseEntity<?> proceed = (ResponseEntity<?>) joinPoint.proceed();
        if (proceed.getBody() instanceof OAuth2AccessToken) {
            HttpResponse<?> success = HttpResponse.success(proceed.getBody());
            proceed = ResponseEntity.ok(success);
        }
        return proceed;
    }

    @AfterReturning(value = execution,returning = "response")
    public void doAfterReturning(JoinPoint joinPoint,ResponseEntity<?> response) throws Throwable {
        JSONObject message = connectMessage(joinPoint,"auth", "获取accessToken");
        if (response.getBody() instanceof OAuth2AccessToken) {
            HttpResponse<?> success = HttpResponse.success(response.getBody());
            message.put("code", success.getCode());
            message.put("msg", success.getMsg());
        }
        kafkaTemplate.send("service-request-log",message.toString());
    }

    @Override
    public List<Interceptor> getInterceptors() {
        return Arrays.asList(uidInterceptor);
    }

    @AfterThrowing(throwing = "ex" ,pointcut = execution)
    public void doAfterThrowing(JoinPoint joinPoint,Throwable ex) throws Throwable {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        JSONObject message = connectMessage(joinPoint, "auth", "获取accessToken");
        HttpResponse<Object> failure = HttpResponse.failure(BasicErrorCode.ERROR_CODE_SERVER_500);
        if (ex instanceof ErrorCode) {
            ErrorCode errorCode = (ErrorCode) ex;
            message.put("code", errorCode.getCode());
            message.put("msg", errorCode.getMsg());
            failure = HttpResponse.failure(errorCode);
        } else if (ex instanceof OAuth2Exception) {
            OAuth2Exception oAuth2Exception = (OAuth2Exception) ex;
            int code = BasicErrorCode.VALIDATOR_FAILURE_ERROR.getCode();
            message.put("code", code);
            message.put("msg", oAuth2Exception.getMessage());
            failure = HttpResponse.failure(code,oAuth2Exception.getMessage());
        } else {
            message.put("code", BasicErrorCode.ERROR_CODE_SERVER_500.getCode());
            message.put("msg", BasicErrorCode.ERROR_CODE_SERVER_500.getMsg());
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), failure);
        } catch (Exception e) {
            throw new ServletException();
        }
        kafkaTemplate.send("service-request-log",message.toString());
    }

    private String getExStack(Throwable ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            return String.format("%s(%s)[%s]",ex.getMessage(),stackTrace[0].getClassName(),stackTrace[0].getLineNumber());
        }
        return ex.getMessage();
    }
}
