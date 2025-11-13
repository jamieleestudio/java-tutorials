package org.third.li.servlet.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter(filterName = "annotationFilter", urlPatterns="/*")
public class AnnotationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        //request url =  contextPath + servletPath + pathInfo

        /**
         * /catalog/lawn/index.html ContextPath: /catalog
         *                          ServletPath: /lawn
         *                          PathInfo: /index.html
         * /catalog/garden/implements/ ContextPath: /catalog
         *                             ServletPath: /garden
         *                             PathInfo: /implements/
         * /catalog/help/feedback.jsp ContextPath: /catalog
         *                            ServletPath: /help/feedback.jsp
         *                            PathInfo: null
         */
        System.out.println("annotationFilter -> request url:" + httpServletRequest.getRequestURI());
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

}
