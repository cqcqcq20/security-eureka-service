package com.example.music.auth.config.granter;

import com.example.music.auth.config.token.SmsCodeAuthenticationToken;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证码登录
 */
public class SMSCodeTokenGranter extends AbstractTokenGranter {

    private static final String GRANT_TYPE = "sms_code";

    private final AuthenticationManager authenticationManager;

    public SMSCodeTokenGranter(AuthorizationServerTokenServices tokenServices,
                               ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, final AuthenticationManager authenticationManager) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<String, String>(tokenRequest.getRequestParameters());
        //客户端提交的用户名
        String phone = parameters.get("phone");
        //客户端提交的国际区号
        String area = parameters.get("area");
        String type = parameters.get("type");
//        if(!RegexUtils.regexPhoneNum(userMobileNo)){
//            throw new InvalidGrantException("手机号码格式错误");
//        }
        //客户端提交的验证码
        String smscode = parameters.get("code");
        if (StringUtils.isEmpty(smscode)) {
            throw new InvalidGrantException("验证码错误");
        } else if (StringUtils.isEmpty(area)) {
            throw new InvalidGrantException("请输入国际区号");
        }
        Authentication userAuth = new SmsCodeAuthenticationToken(phone, smscode,area,type);
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);

        try {
            userAuth = authenticationManager.authenticate(userAuth);//调用上面的provide 验证
        } catch (AccountStatusException | BadCredentialsException ase) {
            //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
            throw new InvalidGrantException(ase.getMessage());
        }

        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate user: " + phone);
        }
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

}
