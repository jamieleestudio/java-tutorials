package org.third.li.servlet.filter;

import jakarta.servlet.*;

import java.io.IOException;

public class DynamicFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("this is dynamic filter.");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }


}
