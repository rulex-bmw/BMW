package com.rulex.bmw.dao;

import com.rulex.bmw.pojo.DataBean;

import java.io.IOException;
import java.util.Map;

public interface LevelDBDao {


    void set(DataBean.Data param) throws IOException;

    void origin();

    Map<byte[], byte[]> verifyHeaderData() throws IOException;
}
