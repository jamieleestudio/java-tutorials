package com.yineng.bpe.log.logging;


import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

/**
 * SEVERE
 * WARNING
 * INFO
 * CONFIG
 * FINE
 * FINER
 * FINEST
 */
public class JdkLoggingApp {

    public static void main(String[] args) {

        //默认info级别
        Logger logger = Logger.getGlobal();
        //信息
        logger.info("start process...");
        //警告
        logger.warning("memory is running out...");
        logger.fine("ignored.");
        //严重
        logger.severe("process will be terminated...");


        Logger logger2 = Logger.getLogger(JdkLoggingApp.class.getName());
        logger2.info("Start process...");
        try {
            "".getBytes("invalidCharsetName");
        } catch (UnsupportedEncodingException e) {
            // TODO: 使用logger.severe()打印异常
            logger2.severe("不支持的charsetName!");
        }
        logger2.info("Process end.");


        Logger log = Logger.getLogger("lavasoft");

        log.info("aaa");
        log.info("bbb");

        Logger log2 = Logger.getLogger("lavasoft");
        log2.info("ccc");
    }

}
