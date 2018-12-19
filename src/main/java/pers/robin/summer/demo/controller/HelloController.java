package pers.robin.summer.demo.controller;

import pers.robin.summer.web.annotation.Controller;
import pers.robin.summer.web.annotation.RequestMapping;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/sayhello")
    public void hello() {
        System.out.println("hello");
    }
}
