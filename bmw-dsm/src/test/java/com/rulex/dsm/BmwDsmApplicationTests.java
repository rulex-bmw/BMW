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
import java.util.ArrayList;
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
//        List<User> objects = new ArrayList<>();
//        User user1 = new User();
//        user1.setAge(20);
//        user1.setName("王liu");
//        User user2 = new User();
//        user2.setAge(20);
//        user2.setName("王liu");
//        User user3 = new User();
//        user3.setAge(20);
//        user3.setName("王liu");
//        objects.add(user1);
//        objects.add(user2);
//        objects.add(user3);
//        userDao.batchCreateUser(objects);
//
        int j = userDao.updateUser();
        System.out.println(j);
//
//        List<User> users = userDao.selectAll();
//        System.out.println(users);
//        userDao.dropTable("test");
    }

}
