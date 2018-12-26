package com.rulex.dsm.bean;

import com.rulex.dsm.pojo.Test;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface TestDao {

    int editTest(Test test);

    int insertTest(Test test);

    int insertMapTest(Map map);
}
