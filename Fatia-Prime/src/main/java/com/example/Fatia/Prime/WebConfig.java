package com.example.Fatia.Prime;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/")
            .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        // CSS e JS sao versionados por ?v=... no index.html; trocar a versao invalida o cache.
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
