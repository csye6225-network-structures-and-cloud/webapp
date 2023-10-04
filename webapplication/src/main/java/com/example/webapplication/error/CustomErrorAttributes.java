package com.example.webapplication.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

        // Remove unwanted attributes for 401 error
        if (errorAttributes.get("status").equals(401)) {
            errorAttributes.clear();
            errorAttributes.put("message", "You are not an authorized user to access this resource");
        }

        return errorAttributes;
    }
}
