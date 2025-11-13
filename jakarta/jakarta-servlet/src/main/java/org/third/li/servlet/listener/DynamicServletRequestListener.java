package org.third.li.servlet.listener;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

public class DynamicServletRequestListener implements ServletRequestListener {

    /**
     * Receives notification that a ServletRequest is about to go out of scope of the web application.
     *
     * @param sre the ServletRequestEvent containing the ServletRequest and the ServletContext representing the web
     * application
     *
     * @implSpec The default implementation takes no action.
     */
    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("dynamic servlet requestDestroyed");
    }

    /**
     * Receives notification that a ServletRequest is about to come into scope of the web application.
     *
     * @param sre the ServletRequestEvent containing the ServletRequest and the ServletContext representing the web
     * application
     *
     * @implSpec The default implementation takes no action.
     */
    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("dynamic servlet requestInitialized");
    }


}
