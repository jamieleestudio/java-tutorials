package org.third.li.spring.integration.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

/**
 * 示例 1：连接工厂（全部示例共用）。
 *
 * <p>{@code DefaultMqttPahoClientFactory} 封装 Eclipse Paho 客户端的连接选项：
 * broker 地址、凭据、自动重连、会话保持等。所有入站/出站适配器共用一个工厂，
 * 保证客户端 ID 前缀统一、连接参数一致。
 */
@Configuration
public class MqttClientFactoryConfig {

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { brokerUrl });
        if (!username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }
        // 断线自动重连
        options.setAutomaticReconnect(true);
        // 清除会话：重连后不恢复上次的订阅状态（按需调整）
        options.setCleanSession(true);
        // 连接超时与心跳
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }
}
