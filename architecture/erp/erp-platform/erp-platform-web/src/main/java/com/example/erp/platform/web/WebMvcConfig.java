package com.example.erp.platform.web;

import com.example.erp.platform.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final boolean securityEnabled;

    public WebMvcConfig(AuthInterceptor authInterceptor,
                        @Value("${erp.security.enabled:false}") boolean securityEnabled) {
        this.authInterceptor = authInterceptor;
        this.securityEnabled = securityEnabled;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (securityEnabled) {
            registry.addInterceptor(authInterceptor).addPathPatterns("/openapi/**");
        }
    }
}