package com.rulex.dsm;

import com.rulex.bsb.dao.LevelDBDao;
import com.rulex.bsb.utils.SqliteUtils;
import com.rulex.dsm.bean.TestDao;
import com.rulex.dsm.bean.UserDao;
import com.rulex.dsm.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rulex.bsb.dao.LevelDBDao.getHashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.rulex.dsm.bean")
@ComponentScan("com.rulex")
public class BmwDsmApplicationTests {

    @Resource
    private UserDao userDao;

    @Resource
    private TestDao testDao;

    @Test
    public void contextLoads() {
//        List<User> objects = new ArrayList<>();
        User user1 = new User();
        user1.setAge(20);
        user1.setName("张三");

//        User user2 = new User();
//        user2.setAge(20);
//        user2.setName("李四");

//        objects.add(user1);
//        objects.add(user2);
//        userDao.batchCreateUser(objects);
        userDao.insertUser(user1);
//
        userDao.insertUser(user1);

//        Map map=new HashMap();
//        map.put("name","xiaohong");
//        map.put("sex",20);
//        userDao.insertMapUser(map);
//        int j = userDao.updateUser("test01", 10);
//        System.out.println(j);
//        int i = userDao.editUser(user1);
//        System.out.println(i);

//        int i = userDao.editUser(user1);
//        System.out.println(i);

//        int j = userDao.updateUser("test01", 10);
//        System.out.println(j);
//
//        List<User> users = userDao.selectAll();
//        System.out.println(users);
//        userDao.dropTable("test");

//        userDao.delUser(9);
//        System.out.println();

    }


    @Test
    public void test() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setWallet(300.00);
        test.setAge(20);
        int i = testDao.editTest(test);
        System.out.println(i);


    }


    @Test
    public void test2() {
//        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
//        test.setPhone(13222222332l);
//        test.setWallet(12.11);
//        test.setUsername("zhangsan");
//        test.setAge(23);
//        test.setTall(170);
//        int i = testDao.insertTest(test);

        Map map=new HashMap();

        map.put("phone2",13222222322l);
        map.put("wallet",12.10);
        map.put("username","zhangsan2");
        map.put("age2",15);
        map.put("tall2",170);

      int i = testDao.insertMapTest(map);


        List<Map<String, Object>> maps = SqliteUtils.query("select * from key_indexes", null);
        System.out.println("索引信息条数" + maps.size());
        System.out.println("最近新增索引信息" + maps.get(maps.size() - 1));
        System.out.println("上条索引信息" + maps.get(maps.size() - 2));
    }
}
