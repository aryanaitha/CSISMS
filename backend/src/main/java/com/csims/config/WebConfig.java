package com.csims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Enable CORS for Vite dev server on localhost:5173
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * SPA Client route forwarder for HTML5 client-side routing.
     * Routes React URLs to index.html when refreshed or directly accessed on port 8080.
     */
    @Controller
    public static class SpaForwardController {
        @GetMapping(value = {
            "/login",
            "/dashboard",
            "/users",
            "/roles",
            "/account-requests",
            "/audit-logs",
            "/projects",
            "/workforce",
            "/tasks",
            "/attendance",
            "/inventory",
            "/equipment",
            "/maintenance",
            "/safety",
            "/incidents",
            "/reports"
        })
        public String forwardSpa() {
            return "forward:/index.html";
        }
    }
}
