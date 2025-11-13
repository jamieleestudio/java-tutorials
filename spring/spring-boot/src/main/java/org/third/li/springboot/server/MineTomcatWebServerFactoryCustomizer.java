package org.third.li.springboot.server;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 自定义 tomcat server 属性
 */
@Component
public class MineTomcatWebServerFactoryCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setBaseDirectory(new File("C:\\Users\\lixiaofeng\\server"));
        factory.setPort(8888);
    }

}
