package com.eroc.bmw.dao;

import com.eroc.bmw.pojo.ParamBean;
import org.iq80.leveldb.DB;

import java.io.IOException;

public interface LevelDBDao {


    void set(ParamBean.Param param) throws IOException;

    DB getDb(String fileName);

    void verifyHeaderData() throws IOException;
}
