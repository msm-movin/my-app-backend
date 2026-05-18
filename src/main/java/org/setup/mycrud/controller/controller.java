package org.setup.mycrud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api")
@RestController
public class controller {
@GetMapping("/home")
public String home(){
    return "Hello World";
}
}
