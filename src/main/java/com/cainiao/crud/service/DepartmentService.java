package com.cainiao.crud.service;
// author： 浩子
// time：   2022/3/1，13:36

import com.cainiao.crud.bean.Department;
import com.cainiao.crud.dao.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    public List<Department> getDepts() {
        return departmentMapper.selectByExample(null);
    }
}
