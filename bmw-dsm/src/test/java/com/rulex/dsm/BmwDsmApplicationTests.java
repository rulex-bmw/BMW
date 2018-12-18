package com.rulex.dsm;

import com.rulex.dsm.bean.UserDao;
import com.rulex.dsm.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.rulex.dsm.bean")
@ComponentScan("com.rulex")
public class BmwDsmApplicationTests {

    @Resource
    private UserDao userDao;

    @Test
    public void contextLoads() {
//        User user = new User();
//        user.setAge(20);
//        user.setName("王liu");
//        int i = userDao.insertUser(user);
//        System.out.println(i);
//
//        int j = userDao.updateUser();
//        System.out.println(j);
//
//        List<User> users = userDao.selectAll();
//        System.out.println(users);
        userDao.dropTable("test");
    }

}
