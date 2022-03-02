package com.cainiao.crud.controller;
// author： 浩子
// time：   2022/2/28，16:12

import com.cainiao.crud.bean.Employee;
import com.cainiao.crud.bean.Msg;
import com.cainiao.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工 CRUD 请求
 */
@Controller
public class EmployeeController {
    // 在web层一定会有service层的对象
    @Autowired
    EmployeeService employeeService;

    /**
     * 单个批量二合一
     * 批量删除：1-2-3
     * 单个删除：1
     */
    @ResponseBody
    @RequestMapping(value = "/emp/{ids}", method = RequestMethod.DELETE)
    public Msg deleteEmp(@PathVariable("ids") String ids) {
        // 批量删除
        if (ids.contains("-")) {
            List<Integer> del_ids = new ArrayList<>();
            String[] str_ids = ids.split("-");
            // 组装id的集合
            for (String string : str_ids) {
                del_ids.add(Integer.parseInt(string));
            }
            employeeService.deleteBatch(del_ids);
        } else {
            Integer id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return Msg.success();
    }


    /**
     * 如果直接发送ajax=PUT形式的请求
     * 封装的数据
     * Employee   * [empId=1014, empName=null, gender=null, email=null, dId=null]
     * <p>
     * 问题：
     *      请求体中有数据；但是Employee对象封装不上；
     *      update tbl_emp  where emp_id = 1014;
     * 原因：
     * Tomcat：
     *      1、将请求体中的数据，封装一个map。
     *      2、request.getParameter("empName")就会从这个map中取值。
     *      3、SpringMVC封装POJO对象的时候。
     *          会把POJO中每个属性的值，request.getParamter("email");
     * AJAX发送PUT请求引发的血案：
     *      PUT请求，请求体中的数据，request.getParameter("empName")拿不到
     *      Tomcat一看是PUT不会封装请求体中的数据为map，只有POST形式的请求才封装请求体为map
     * org.apache.catalina.connector.Request--parseParameters() (3111);
     * <p>
     * protected String parseBodyMethods = "POST";
     * if( !getConnector().isParseBodyMethod(getMethod()) ) {
     * success = true;
     * return;
     * }
     * <p>
     * 解决方案；
     * 我们要能支持直接发送PUT之类的请求还要封装请求体中的数据
     *      1、配置上 FormContentFilter
     *      2、他的作用；将请求体中的数据解析包装成一个map。
     *      3、request被重新包装，request.getParameter()被重写，就会从自己封装的map中取数据
     * 员工更新方法
     */
    @RequestMapping(value = "/emp/{empId}", method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee) {
//        System.out.println(employee);
        employeeService.updateEmp(employee);
        return Msg.success();
    }

    /**
     * 根据id查询员工
     */
    @RequestMapping(value = "/emp/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getEmp(id);
        return Msg.success().add("emp", employee);
    }

    /**
     * 检查用户名是否可用
     */
    @RequestMapping(value = "/checkUser")
    @ResponseBody
    public Msg checkUser(@RequestParam("empName") String empName) {
        //先判断用户名是否是合法的表达式;
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
        if (!empName.matches(regx)) {
            return Msg.fail().add("va_msg", "用户名必须是6-16位数字和字母的组合或者2-5位中文");
        }
        boolean b = employeeService.checkUser(empName);
        if (b) {
            return Msg.success();
        } else {
            return Msg.fail();
        }
    }

    /**
     * 保存员工的方法
     */
    @RequestMapping(value = "/emp", method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            // 校验失败，应该返回失败，在模态框中显示校验失败的错误信息
            Map<String, Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError fieldError : errors) {
    //                System.out.println("错误的字段名：" + fieldError.getField());
    //                System.out.println("错误信息：" + fieldError.getDefaultMessage());
                map.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields", map);
        } else {
            employeeService.sevaEmp(employee);
            return Msg.success();
        }
    }


    /**
     * 查询员工数据（分页查询）
     * ResponseBody 注解会将返回的对象 直接转换为json字符串
     */
    @RequestMapping(value = "/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 引入分页插件
        // 调用时，只需要调用，传入页码，以及每页的大小
        PageHelper.startPage(pageNum, 5);
        // startPage 后面紧跟的查询就是分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用pageInfo包装查询后的结果,只需要将pageInfo交给页面就可以了
        // 封装了详细的分页信息，包括有我们查出来的数据。后面的数据为要连续显示的页数。
        // return new PageInfo<>(emps, 5);
        return Msg.success().add("pageInfo", new PageInfo<>(emps, 5));
    }

    /**
     * 查询员工数据（分页查询）(弃用了)
     */
    // @RequestMapping(value = "/emps")
    public String getEmps(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model) {
        // 引入分页插件
        // 调用时，只需要调用，传入页码，以及每页的大小
        PageHelper.startPage(pageNum, 5);
        // startPage 后面紧跟的查询就是分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用pageInfo包装查询后的结果,只需要将pageInfo交给页面就可以了
        // 封装了详细的分页信息，包括有我们查出来的数据。后面的数据为要连续显示的页数。
        PageInfo pageInfo = new PageInfo<>(emps, 5);
        model.addAttribute("pageInfo", pageInfo);

        return "list";
    }
}
