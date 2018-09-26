package com.eroc.bmw.service;

import com.eroc.bmw.dao.LevelDBDao;
import com.eroc.bmw.dao.LevelDBDaoImpl;
import com.eroc.bmw.pojo.DataBean;

import java.io.IOException;

public class BSBServiceImpl implements BSBService {

    @Override
    public void producer(DataBean.Data data) {
        try {
            LevelDBDao levelDBDao = new LevelDBDaoImpl();
            levelDBDao.origin();
            levelDBDao.set(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void customer() {


    }
}
