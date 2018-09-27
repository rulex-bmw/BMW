package com.eroc.bmw.service;

import com.eroc.bmw.dao.LevelDBDao;
import com.eroc.bmw.dao.LevelDBDaoImpl;
import com.eroc.bmw.pojo.DataBean;

import java.io.IOException;

/**
 * BSB业务处理总线程
 *
 * @author admin
 */
public class BSBServiceImpl implements BSBService {


    private LevelDBDao levelDBDao = new LevelDBDaoImpl();

    @Override
    public void producer(DataBean.Data data) {
        try {
            levelDBDao.origin();
            levelDBDao.set(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从readposition中读取m并去map中寻找m+1
     * 从levelDB中取出DATA,将不可变信息存入数据库
     */
    @Override
    public void customer() {


    }
}
