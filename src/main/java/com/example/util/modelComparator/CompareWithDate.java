package com.example.util.modelComparator;

import org.activiti.engine.repository.Model;

import java.util.Comparator;

/**
 * Created by Hevin on 2017/12/24.
 */
public class CompareWithDate implements Comparator<Model> {


    @Override
    public int compare(Model o1, Model o2) {

        if(o1.getCreateTime().after(o2.getCreateTime())) {
            return -1;
        }else if(o1.getCreateTime().before(o2.getCreateTime())) {
            return 1;
        }
        return 0;

    }
}