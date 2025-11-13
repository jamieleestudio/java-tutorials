package org.third.li.servlet.initializer;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import org.third.li.servlet.HandlesTypeServlet;

import java.util.Set;

/**
 *
 * ServletContainerInitializer 类通过 jar services API 查找。对于每一个应用，应用启动时，由容器创建一个
 * ServletContainerInitializer 实 例 。 框 架 提 供 的 ServletContainerInitializer 实 现 必 须 绑 定 在 jar 包 的
 * META-INF/services 目录中的一个叫做 javax.servlet.ServletContainerInitializer 的文件，根据 jar services API，
 * 指定 ServletContainerInitializer 的实现
 *
 *
 */
@HandlesTypes(value = HandlesTypeServlet.class)
public class CustomServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("CustomServletContainerInitializer init "+c);
    }


}
