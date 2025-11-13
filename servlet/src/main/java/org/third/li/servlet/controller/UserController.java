package org.third.li.servlet.controller;

import org.third.li.servlet.web.annotation.Controller;

import javax.ws.rs.Path;
import java.util.logging.Logger;

@Path("/user")
@Controller
public class UserController {

    @Path("/list")
    public String listPage(){
        System.out.println("user list");
        return "login.jsp";
    }

}
