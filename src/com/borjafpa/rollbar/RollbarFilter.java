package com.borjafpa.rollbar;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.MDC;

public class RollbarFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            MDC.put("request", httpRequest);

            HttpSession session = httpRequest.getSession(false);
            if (session != null) MDC.put("session", httpRequest);

            filterChain.doFilter(servletRequest, servletResponse);

        } finally {
            MDC.remove("request");
            MDC.remove("session");
        }

    }

    public void destroy() {}

}
