package com.rulex.bmw.service;

import com.rulex.bmw.dao.LevelDBDao;
import com.rulex.bmw.dao.LevelDBDaoImpl;
import com.rulex.bmw.pojo.DataBean;

import javax.security.auth.callback.Callback;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

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
        Map<byte[], byte[]> map = new HashMap<>();


    }
}


class customer implements Callable<DataBean.Data> {
    @Override
    public DataBean.Data call() throws Exception {
        //将数据保存至区块链
        return null;
    }
}

class producer implements Callable<DataBean.Data> {
    @Override
    public DataBean.Data call() throws Exception {
        //本地数据持久化
        return null;
    }
}