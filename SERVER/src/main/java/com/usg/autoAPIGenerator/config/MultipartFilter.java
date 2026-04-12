package com.usg.autoAPIGenerator.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.IOException;

@Component
public class MultipartFilter implements Filter {

    private final MultipartResolver multipartResolver = new StandardServletMultipartResolver();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Check if it's a multipart request and wrap it
        if (multipartResolver.isMultipart(httpRequest)) {
            MultipartHttpServletRequest wrappedRequest = multipartResolver.resolveMultipart(httpRequest);
            try {
                chain.doFilter(wrappedRequest, response);
            } finally {
                multipartResolver.cleanupMultipart(wrappedRequest);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}