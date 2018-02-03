package com.example;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Hevin on 2018/1/19.
 */
@RequestMapping("test")
public class testService {
    public String test() {
        return "test";
    }
}
