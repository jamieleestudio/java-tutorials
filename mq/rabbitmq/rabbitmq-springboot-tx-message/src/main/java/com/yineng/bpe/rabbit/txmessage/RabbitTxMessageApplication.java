package com.yineng.bpe.rabbit.txmessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RabbitTxMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitTxMessageApplication.class, args);
    }
}
