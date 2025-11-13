package org.third.li.springframework.beans;

import org.springframework.context.annotation.Bean;

public class UserManager {

    @Bean({"lixiaofeng","engineer"})
    public User engineer(){
        return new User(1L,"lixiaofeng");
    }

}
