package com.rulex.dsm.bean;

import com.rulex.dsm.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserDao {

    int updateUser(@Param("aaaa") String name, @Param("bbb") Integer age);

    int editUser(User user);

    int insertUser(User u);

    int insertMapUser(Map<String,Object>  map);

    List<User> selectAll();

    void dropTable(@Param("tableName") String tableName);

    void batchCreateUser(@Param("userList") List<User> userList);

    void alderTable();

    void delUser(Integer id);

}
