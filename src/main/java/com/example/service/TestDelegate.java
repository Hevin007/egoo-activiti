package com.example.service;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import java.util.Date;

/**
 * Created by Hevin on 2018/1/4.
 */
public class TestDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("TestDelegate "+execution.getVariable("year"));
        Date now = new Date();
        execution.setVariable("autoWelcomeTime", now);

    }
}
