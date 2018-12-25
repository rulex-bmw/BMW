package com.rulex.dsm.bean;

import com.rulex.dsm.pojo.Test;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDao {

    int editTest(Test test);


}
