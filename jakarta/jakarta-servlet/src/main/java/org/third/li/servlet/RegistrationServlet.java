package org.third.li.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 无法在过程中进行添加 servlet
 * 原因需要分析
 */
@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {

    private ServletConfig servletConfig;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.servletConfig = config;
    }
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        System.out.println("registration servlet");

        var servletName = request.getParameter("servletName");
        if(servletName == null || servletName.isEmpty()){
            servletName = "dynamicServlet";
        }
        var registration = request.getServletContext().getServletRegistration(servletName);
        if(registration != null){
            System.out.println(servletName + "servlet is exists.");
        }else{
            var re = servletConfig.getServletContext().addServlet(servletName,new DynamicServlet());
            re.addMapping("/dynamic");
            System.out.println("add servlet is success. name's " + servletName);
        }
    }

}
