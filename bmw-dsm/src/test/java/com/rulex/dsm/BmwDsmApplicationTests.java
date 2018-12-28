package com.rulex.dsm;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.bsb.utils.SqliteUtils;
import com.rulex.dsm.bean.CurriculumDao;
import com.rulex.dsm.bean.TestDao;
import com.rulex.dsm.bean.UserDao;
import com.rulex.dsm.pojo.Curriculum;
import com.rulex.dsm.pojo.User;
import com.rulex.dsm.service.QueryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rulex.bsb.dao.LevelDBDao.WRITEPOSITION;


@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.rulex.dsm.bean")
@ComponentScan("com.rulex")
public class BmwDsmApplicationTests {

    @Resource
    private UserDao userDao;

    @Resource
    private TestDao testDao;

    @Resource
    private CurriculumDao curriculumDao;

    // 一次修改,影响多条数据测试,参数为entity类型，修改double类型
    @Test
    public void testEntity() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setWallet(7000.00);
        test.setAge(20);
        for(int i = 0; i < 10; i++) {
            System.out.println("受影响的条数： " + testDao.editWithEntity(test));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // 一次修改,影响多条数据测试,参数为map类型，修改double类型
    @Test
    public void testMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wallet", 200.00);
        map.put("age", 20);
        int i = testDao.editWithMap(map);
        System.out.println("受影响的条数： " + i);
    }


    // 一次修改,影响多条数据测试,直接传参数，修改double类型
    @Test
    public void testParam() {
        int i = testDao.editWithParam(200.00, 20);
        System.out.println("受影响的条数： " + i);
    }

    // 修改long类型测试
    @Test
    public void updatePhone() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setUsername("张三");
        test.setPhone(15111123321L);
        int i = testDao.editPhone(test);
        System.out.println("受影响的条数： " + i);
    }


    // 修改非上链字段测试
    @Test
    public void modifyTall() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setId(1);
        test.setTall(195);
        int i = testDao.modifyTall(test);
        System.out.println("受影响的条数： " + i);
    }

    // 修改用户名测试
    @Test
    public void modifyUsername() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setId(1);
        test.setUsername("小强");
        int i = testDao.modifyUsername(test);
        System.out.println("受影响的条数： " + i);
    }

    // 修改多个上链参数
    @Test
    public void modifyMoreParam() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setId(1);
        test.setUsername("小新");
        test.setWallet(100000.00);
        int i = testDao.modifyMoreParam(test);
        System.out.println("受影响的条数： " + i);
    }


    // autoincrement删除数据
    @Test
    public void delAutoIncrement() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setId(2);
        int i = testDao.del(test);
        System.out.println("受影响的条数： " + i);
    }


    // 联合主键修改上链参数
    @Test
    public void modifyParam() {
        Curriculum curriculum = new Curriculum();
        curriculum.setClassroom(101);
        curriculum.setTeacher("张三");
        curriculum.setProject("地理");
        curriculum.setCredit(10);
        curriculum.setStuNum(1000);
        int i = curriculumDao.modifyParam(curriculum);
        System.out.println("受影响的条数： " + i);
    }

    // 联合主键修改主键及参数
    @Test
    public void modifyPrimary() {
        Curriculum curriculum = new Curriculum();
        curriculum.setClassroom(101);
        curriculum.setTeacher("老刘");
        curriculum.setCredit(100);
        curriculum.setStuNum(600);
        int i = curriculumDao.modifyPrimary(curriculum);
        System.out.println("受影响的条数： " + i);
    }


    // 联合主键删除数据
    @Test
    public void delUnite() {
        Curriculum curriculum = new Curriculum();
        curriculum.setClassroom(101);
        curriculum.setTeacher("老刘");
        curriculum.setProject("地理");
        int i = curriculumDao.delProject(curriculum);
        System.out.println("受影响的条数： " + i);
    }


    @Test
    public void sqliteTest() {
        String sql = "SELECT * FROM key_indexes";

        List<Map<String, Object>> query = SqliteUtils.query(sql, null);
        for (Map<String, Object> map : query) {
            System.out.println(map);
        }
        System.out.println("索引总条数： " + query.size());

      /*  String sql = "insert into key_indexes (orgPKHash,typeHash,type,ts) values(?,?,?,?);";
        Object[] obj = new Object[4];
        byte[] key = TypeUtils.concatByteArrays(new byte[][]{TypeUtils.objectToByte("张三"), TypeUtils.objectToByte(101), TypeUtils.objectToByte("地理")});
        obj[0] = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(key));
        obj[1] = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(TypeUtils.objectToByte("unint02")));
        obj[2] = 1;
        obj[3] = System.currentTimeMillis();
        SqliteUtils.edit(obj, sql);*/
    }




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


    /*@Test
    public void test() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setWallet(300.00);
        test.setAge(20);
        int i = testDao.editTest(test);
        System.out.println(i);


    }*/


    //主键自增
    @Test
    public void insert() {
        Map map=new HashMap();

        map.put("phone",15133118672l);
        map.put("wallet",11.88);
        map.put("username","zj");
        map.put("age",18);
        map.put("tall",180);

        int i = testDao.insertMapTest(map);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> maps = SqliteUtils.query("select * from key_indexes", null);
        System.out.println("索引信息条数" + maps.size());
        System.out.println("最近新增索引信息" + maps.get(maps.size() - 1));
    }

    //主键自增
    @Test
    public void query() {

        try {
            byte[] writeKey = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION)).getDataKey().toByteArray();
            System.out.println(Base64.getEncoder().encodeToString(writeKey));
            QueryService.queryInfo(writeKey);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    //主键自增
    @Test
    public void editTest() {
        com.rulex.dsm.pojo.Test test = new com.rulex.dsm.pojo.Test();
        test.setUsername("qq");
        test.setWallet(13.00);
        test.setId(349);
        test.setTall(189);
        test.setAge(21);

        int i = testDao.editById(test);

    }

    //复合自增
    @Test
    public void insertTest() {
        Curriculum curriculum = new Curriculum();
        curriculum.setClassroom(102);
        curriculum.setTeacher("张三2");
        curriculum.setProject("地理2");
        curriculum.setCredit(102);
        curriculum.setStuNum(10002);

        int i = curriculumDao.insertProject(curriculum);


        List<Map<String, Object>> maps = SqliteUtils.query("select * from key_indexes", null);
        System.out.println("索引信息条数" + maps.size());
        System.out.println("最近新增索引信息" + maps.get(maps.size() - 1));
    }


    // 复合自增修改
    @Test
    public void modifyTest() {
        Curriculum curriculum = new Curriculum();
        curriculum.setClassroom(102);
        curriculum.setTeacher("张三2");
        curriculum.setProject("地理2");
        curriculum.setCredit(124);
        curriculum.setStuNum(144);
        int i = curriculumDao.modifyParam(curriculum);
        System.out.println("受影响的条数： " + i);
    }

}
