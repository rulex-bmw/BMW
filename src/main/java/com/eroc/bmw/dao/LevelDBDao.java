package com.eroc.bmw.dao;

import com.eroc.bmw.pojo.DataBean;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.Map;

public interface LevelDBDao {


    void set(DataBean.Data param) throws IOException;

    void origin();

    Map<byte[], byte[]> verifyHeaderData() throws IOException;
}
