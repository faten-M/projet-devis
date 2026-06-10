package com.projetdevis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/{path:[^\\.]*}",
        "/{path:[^\\.]*}/{subpath:[^\\.]*}"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
