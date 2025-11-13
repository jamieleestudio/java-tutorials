package org.third.li.springframework.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TeacherController {

    @Autowired
    private BizService<Teacher> bizService;

    public BizService<Teacher> getBizService() {
        return bizService;
    }
}
