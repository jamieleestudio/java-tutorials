package org.third.li.springboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


//    @RequestMapping("/test/{name}")
    @RequestMapping("/test")
    @ResponseBody
    public ResponseEntity<Object> test(
//                                       @RequestBody String text,
//                                       String name,
                                       MultipartFile image
    ) {
//        System.out.println(queryDTO);
        return ResponseEntity.ok(new QueryDTO("test", 1));
    }

    @RequestMapping("/*")
    @ResponseBody
    public ResponseEntity<Object> test1(@RequestBody List<String> id, HttpServletRequest request) {
        System.out.println(id+"******");
        return ResponseEntity.ok("ok");
    }

}
