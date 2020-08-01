package com.example.music.auth.exception;

import com.example.common.exception.BasicErrorCode;
import com.example.common.exception.ErrorCode;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

public class AuthVerifyCodeException extends OAuth2Exception implements ErrorCode {

    public AuthVerifyCodeException(String msg) {
        super(msg);
    }

    @Override
    public int getCode() {
        return BasicErrorCode.VALIDATOR_FAILURE_ERROR.getCode();
    }

    @Override
    public String getMsg() {
        return getMessage();
    }
}
