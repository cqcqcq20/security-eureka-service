package com.example.music.auth.config.provider;

import com.example.music.auth.config.token.SmsCodeAuthenticationToken;
import com.example.music.auth.service.CustomUserDetailsService;
import com.example.music.auth.service.RedisTokenService;
import com.example.music.auth.exception.AuthVerifyCodeException;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class SmsCodeAuthenticationProvider implements AuthenticationProvider {

    private CustomUserDetailsService userServiceDetail;

    private RedisTokenService redisTokenService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //这个authentication就是SmsCodeAuthenticationToken
        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;

        String phone = (String) authenticationToken.getPrincipal();
        String area = authenticationToken.getArea();
        String type = authenticationToken.getType();
        // 验证验证码
        String smsCodeCached = redisTokenService.getVerificationCode(phone,area,type);

        String smscode = authenticationToken.getCode();
        if (StringUtils.isEmpty(smsCodeCached) || !smscode.equals(smsCodeCached)) {
            throw new AuthVerifyCodeException("验证码错误");
        } else {
            redisTokenService.removeVerificationCode(phone,area,type);
        }

        //校验手机号
        UserDetails user = userServiceDetail.loadUserByUsername(phone,area);
        //这时候已经认证成功了
        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(user,area,type, user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public CustomUserDetailsService getUserServiceDetail() {
        return userServiceDetail;
    }

    public void setUserServiceDetail(CustomUserDetailsService userServiceDetail) {
        this.userServiceDetail = userServiceDetail;
    }

    public RedisTokenService getRedisTokenService() {
        return redisTokenService;
    }

    public void setRedisTokenService(RedisTokenService redisTokenService) {
        this.redisTokenService = redisTokenService;
    }
}
