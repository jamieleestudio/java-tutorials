package org.third.li.servlet.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.third.li.servlet.web.annotation.Controller;

import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.substringAfter;

public class DispatcherServlet extends HttpServlet {

    private Map<String, HandlerMethodInfo> handleMethodInfoMapping = new HashMap<>();

    public void init(ServletConfig config) throws ServletException{
        registerResources();
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        // requestURI = /a/hello/world
        String requestURI = request.getRequestURI();
        // contextPath  = /a or "/" or ""
        String prefixPath = request.getContextPath();
        // 映射路径（子路径）
        String requestMappingPath = substringAfter(requestURI,
                StringUtils.replace(prefixPath, "//", "/"));

        var methodInfo = handleMethodInfoMapping.get(requestMappingPath);
        if(methodInfo != null){
            var method = methodInfo.getHandlerMethod();
            Object result = null;
            try {
                //这里每次都是 new instance 效率低，应该留一个instance 就行了
                result = method.invoke(method.getDeclaringClass().newInstance(),null);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }
            if(result != null){
                var viewPath = result.toString();
                ServletContext servletContext = request.getServletContext();
                if (!viewPath.startsWith("/")) {
                    viewPath = "/" + viewPath;
                }
                RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(viewPath);
                requestDispatcher.forward(request, response);
            }
        }
    }

    private void registerResources(){
        //还可以jdk 的spi机制
        Reflections reflections = new Reflections("org.third.li.servlet.controller");
        var controllerClazz =  reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> clazz : controllerClazz) {
            var pathAnnotation = clazz.getAnnotation(Path.class);
            var path = "";
            if(pathAnnotation != null && pathAnnotation.value() != null){
                path+=pathAnnotation.value();
            }
            for (Method method : clazz.getMethods()) {
                var methodPath = method.getAnnotation(Path.class);
                if(methodPath != null){
                    path+=methodPath.value();
                    var methodInfo = new HandlerMethodInfo(path,method, Set.of());
                    handleMethodInfoMapping.put(path,methodInfo);
                }

            }
        }
    }

}
