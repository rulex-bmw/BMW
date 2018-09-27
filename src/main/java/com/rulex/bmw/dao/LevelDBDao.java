package com.rulex.bmw.dao;

import com.rulex.bmw.pojo.DataBean;

import java.io.IOException;

public interface LevelDBDao {


    void set(DataBean.Data param) throws IOException;

    void origin();

    void verifyHeaderData() throws IOException;
}
