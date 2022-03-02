package com.cainiao.crud.test;
// author： 浩子
// time：   2022/2/28，14:15


import com.cainiao.crud.bean.Department;
import com.cainiao.crud.bean.Employee;
import com.cainiao.crud.dao.DepartmentMapper;
import com.cainiao.crud.dao.EmployeeMapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * 测试 dao 层的工作
 * 推荐Spring的项目就可以使用Spring的单元测试，可以自动注入我们需要的组件
 * 1. 导入SpringTest模块
 * 2. @ContextConfiguration 指定Spring配置文件的位置
 * 3. @RunWith 指定用哪个单元测试模块，这里指定用spring的单元测试
 * 4. @Autowired 要使用的组件
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class MapperTest {
    @Autowired
    DepartmentMapper departmentMapper;
    @Autowired
    EmployeeMapper employeeMapper;
    @Autowired
    SqlSession sqlSession;

    @Test
    public void testCRUD() {

        //原生测试方法
        /*//1,创建springIOC容器
        ApplicationContext ioc = new ClassPathXmlApplicationContext("applicationContext.xml");
        //2,从容器中获取mapper
        DepartmentMapper ben = ioc.getBean(DepartmentMapper.class);*/

        System.out.println(departmentMapper);

        // 1. 插入几个部门
        departmentMapper.insertSelective(new Department(null, "开发部"));
        departmentMapper.insertSelective(new Department(null, "测试部"));
        // 2. 生成员工数据，并插入
         employeeMapper.insertSelective(new Employee(null, "Jerry", "M", "111111@qq.com", 1));

        // 3. 批量插入员工数据。
/*        for () {
            employeeMapper.insertSelective(new Employee(null, "uuid", "M", "111111@qq.com", 1));
        }*/
        EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
        for (int i = 0; i < 100; i++) {
            String name = UUID.randomUUID().toString().substring(0, 5) + i;
            mapper.insertSelective(new Employee(null, name, "M", name + "@qq.com", 1));
        }
        System.out.println("添加完成");
    }
}
