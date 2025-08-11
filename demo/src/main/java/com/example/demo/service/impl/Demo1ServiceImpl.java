package com.example.demo.service.impl;

import com.example.demo.service.DemoService;
import org.springframework.stereotype.Service;


/**
 * 
 */
@Service
public class Demo1ServiceImpl implements DemoService {

    @Override
    public void demo1() {

    }

    @Override
    public void demo2(String name,Integer age) {
        System.out.println("Demo1ServiceImpl"+name+age);
    }

    @Override
    public void demo3() {

    }
}
