package com.example.music.auth.config.handler;

import com.example.music.auth.exception.CustomOAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component("customWebResponseExceptionTranslator")
public class OAuth2ExceptionWebResponseExceptionTranslator implements WebResponseExceptionTranslator<OAuth2Exception> {

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
        OAuth2Exception oAuth2Exception;
        if (e instanceof OAuth2Exception) {
             oAuth2Exception = (OAuth2Exception) e;
        } else {
            throw e;
        }
        return ResponseEntity
                .status(HttpServletResponse.SC_OK)
                .body(new CustomOAuthException(oAuth2Exception.getMessage()));
    }
}
