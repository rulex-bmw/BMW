package com.rulex.dsm;

import com.rulex.dsm.dao.UserDao;
import com.rulex.dsm.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.rulex.dsm.dao")
public class BmwDsmApplicationTests {

    @Resource
    private UserDao userDao;

    @Test
    public void contextLoads() {
        User user = new User();
        user.setAge(2);
        user.setName("张三");
        int i = userDao.insertUser(user);
        System.out.println(i);
//
//        String name = "李四";
//        int id = 3;
//        Integer age = 44;
//        int i = userDao.updateUsername(name, id, age);
//        System.out.println(i);

//        int i = userDao.delUser(2);
//        System.out.println(i);
    }

}
