package com.yineng.bpe.log.logging.config;

import java.util.Objects;
import java.util.logging.Logger;

public class JdkLoggingConfigApp {


    private static Logger LOG = null;


    private void log(){

        /*
         * 日志底层获取配置文件是通过FileInputStream来获取，所以这里需要配置文件的绝对路径
         *
         * 如果没有配置是获取的 java.home下的 conf/logging.properties 文件 （默认配置）
         * 参数->FileHandler
         */

        System.out.println(System.getProperty("java.home"));

        //获取文件路径
        String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("logging.properties")).getPath();
        System.setProperty("java.util.logging.config.file",path);


        LOG = Logger.getLogger("com.lixf");
        LOG.warning("测试信息");
        LOG.info("hello");
    }


    public static void main(String[] args) {

        new JdkLoggingConfigApp().log();

    }

}
