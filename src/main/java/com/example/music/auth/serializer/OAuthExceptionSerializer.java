package com.example.music.auth.serializer;

import com.example.music.auth.exception.CustomOAuthException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class OAuthExceptionSerializer extends StdSerializer<CustomOAuthException> {

    public OAuthExceptionSerializer() {
        super(CustomOAuthException.class);
    }

    @Override
    public void serialize(CustomOAuthException value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        gen.writeStartObject();
        gen.writeNumberField("code", value.getHttpErrorCode());
        gen.writeStringField("msg", String.format("%s %s", request.getServletPath(), value.getMessage()));

        if (value.getAdditionalInformation() != null) {
            for (Map.Entry<String, String> entry : value.getAdditionalInformation().entrySet()) {
                String key = entry.getKey();
                String add = entry.getValue();
                gen.writeStringField(key, add);
            }
        }
        gen.writeEndObject();
    }
}
