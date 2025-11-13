package org.third.li.servlet.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebFilter(filterName = "charsetEncodingFilter", urlPatterns="/*",initParams = @WebInitParam(name = "encoding",value = "GBK"))
public class CharsetEncodingFilter implements Filter {

    private String encoding;
    private ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.encoding = Optional.ofNullable(filterConfig.getInitParameter("encoding")).orElse("UTF-8");
        this.servletContext = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpRequest.setCharacterEncoding(encoding);
            httpResponse.setCharacterEncoding(encoding);
            System.out.println("character encoding is：" + encoding);
            // CharsetEncodingFilter -> FrontControllerServlet -> forward -> index.jsp
        }

        // 执行过滤链
        chain.doFilter(request,response);
    }


}
