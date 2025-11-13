package org.third.li.servlet.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class DynamicServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        System.out.println("dynamic servlet context init");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        System.out.println("dynamic servlet context destroy");
    }

}
