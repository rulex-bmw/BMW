package com.rulex.dsm.dao;

import com.rulex.dsm.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao {

    int insertUser(User user);

    int updateUsername(@Param("name") String username, @Param("idcard") Integer id, @Param("age") Integer age);

    int delUser(@Param("idcard") Integer id);

}
