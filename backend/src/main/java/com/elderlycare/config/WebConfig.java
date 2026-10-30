package com.elderlycare.config;

import filter.AuthFilter;
import filter.SessionPathParamFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<SessionPathParamFilter> sessionPathParamFilterRegistration() {
        FilterRegistrationBean<SessionPathParamFilter> registration =
                new FilterRegistrationBean<>(new SessionPathParamFilter());
        registration.setName("sessionPathParamFilter");
        registration.addUrlPatterns("/api/*");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>(new AuthFilter());
        registration.setName("authFilter");
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void configurePathMatch(PathMatchConfigurer configurer) {

        configurer.setUseTrailingSlashMatch(true);
    }
}
