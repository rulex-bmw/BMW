package com.eroc.bmw.dao;

import com.eroc.bmw.pojo.DataBean;
import org.iq80.leveldb.DB;

import java.io.IOException;

public interface LevelDBDao {


    void set(DataBean.Data param) throws IOException;

    void origin();

    void verifyHeaderData() throws IOException;
}
