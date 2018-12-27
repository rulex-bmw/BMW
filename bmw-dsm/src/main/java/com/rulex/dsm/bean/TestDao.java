package com.rulex.dsm.bean;

import com.rulex.dsm.pojo.Test;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface TestDao {

    int editWithEntity(Test test);

    int editPhone(Test test);

    int modifyTall(Test test);

    int modifyUsername(Test test);

    int modifyMoreParam(Test test);

    int editWithMap(Map map);

    int editWithParam(@Param("wallet") Double wallet, @Param("age") Integer age);

    int del(Test test);

    int insertTest(Test test);

    int editUsername(Test test);

    int insertMapTest(Map map);
}
