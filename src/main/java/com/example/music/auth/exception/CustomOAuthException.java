package com.example.music.auth.exception;

import com.example.music.auth.serializer.OAuthExceptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

@JsonSerialize(using = OAuthExceptionSerializer.class)
public class CustomOAuthException extends OAuth2Exception {

    public CustomOAuthException(String msg) {
        super(msg);
    }
}
