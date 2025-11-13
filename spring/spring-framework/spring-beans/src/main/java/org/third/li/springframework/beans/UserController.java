package org.third.li.springframework.beans;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserController {

    @Autowired
    private BizService<User> bizService;

    public BizService<User> getBizService() {
        return bizService;
    }
}
