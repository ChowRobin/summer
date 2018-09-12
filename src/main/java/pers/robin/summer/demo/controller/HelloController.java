package pers.robin.summer.demo.controller;

import pers.robin.summer.web.anotation.Controller;
import pers.robin.summer.web.anotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/sayhello")
    public void hello() {
        System.out.println("hello");
    }
}
