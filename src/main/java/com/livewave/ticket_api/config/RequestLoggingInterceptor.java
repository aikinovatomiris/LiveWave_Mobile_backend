package com.livewave.ticket_api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        logger.info("Incoming Request: {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        long executionTime = System.currentTimeMillis() - startTime;

        logger.info("Request Completed: {} {} - Status: {} - Time: {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                executionTime);

        if (ex != null) {
            logger.error("Request failed with exception: ", ex);
        }
    }
}