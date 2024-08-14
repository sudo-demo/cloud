package com.example.demo.service.impl;

import com.example.demo.service.DemoService;
import org.springframework.stereotype.Service;


/**
 * @author huanghongjia
 */
@Service
public class Demo2ServiceImpl implements DemoService {

    @Override
    public void demo1() {

    }

    @Override
    public void demo2() {
        System.out.println("Demo2ServiceImpl");
    }

    @Override
    public void demo3() {

    }
}
