package org.third.li.servlet.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.third.li.servlet.DynamicServlet;
import org.third.li.servlet.filter.DynamicFilter;

@WebListener
public class RegistrationListener implements ServletContextListener {

    @Override
    public  void contextInitialized(ServletContextEvent sce) {

        var servletContext = sce.getServletContext();
        //ServletContextListener 只能被调用一次，不能作为动态监听器再次添加
        //java.lang.IllegalArgumentException: Once the first ServletContextListener has been called, no more ServletContextListeners may be added
        servletContext.addListener(DynamicServletRequestListener.class);

        /**
         * 如果 ServletContext 传到了 ServletContainerInitializer 的 onStartup 方法，则给定名字的类可以实现除上面列
         * 出的接口之外的 javax.servlet.ServletContextListener
         */
        //servletContext.addListener("org.third.li.servlet.listener.DynamicServletContextListener");

        servletContext.addFilter("dynamicFilter", DynamicFilter.class);

        var dynamic1 = servletContext.addServlet("dynamicServlet1", "org.third.li.servlet.DynamicServlet");
        dynamic1.addMapping("/dynamic1");

        var dynamic2 = servletContext.addServlet("dynamicServlet2", DynamicServlet.class);
        dynamic2.addMapping("/dynamic2");

        var dynamic3 = servletContext.addServlet("dynamicServlet3",new DynamicServlet());
        dynamic3.addMapping("/dynamic3");

    }

}
