package com.kafka.api.admin;

import com.google.common.collect.Maps;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AdminClientFactory {

    /**
     * 创建AdminClient客户端对象
     * 配置项参考：http://www.manongjc.com/article/26490.html
     */
    public static AdminClient createAdminClientByProperties() {

        Properties prop = new Properties();

        // 配置Kafka服务的访问地址及端口号
        prop.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.24.128:9093,192.168.24.129:9094,192.168.24.130:9095");
        prop.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,"20000");
        prop.setProperty(AdminClientConfig.RETRIES_CONFIG,"3");

        // 创建AdminClient实例
        return KafkaAdminClient.create(prop);
    }

    /**
     * 创建AdminClient的第二种方式
     */
    public static AdminClient createAdminClientByMap() {

        Map<String, Object> conf = new HashMap<>();

        // 配置Kafka服务的访问地址及端口号
        conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.24.128:9093,192.168.24.129:9094,192.168.24.130:9095");

        // 创建AdminClient实例
        return AdminClient.create(conf);
    }
}
