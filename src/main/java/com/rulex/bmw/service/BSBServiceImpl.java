package com.rulex.bmw.service;

import com.google.protobuf.ByteString;
import com.rulex.bmw.dao.LevelDBDao;
import com.rulex.bmw.dao.LevelDBDaoImpl;
import com.rulex.bmw.pojo.DataBean;

import java.io.IOException;
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
     * m:已经上链，m+1:未上链
     * 从levelDB中取出DATA,将不可变信息存入数据库
     */
    @Override
    public void customer() {
        String currentHash = null;
        String prevHash = null;
        Map<String, String> hashKey = Verify.hashKey;
        if (hashKey.size() == 0) {
            return;
        }
        //get readposition
        DataBean.Data readposition = levelDBDao.getReadposition();
        //从第m+1项上链
        prevHash = readposition.getPrevHash().toString();
        while (true) {
            currentHash = hashKey.get(prevHash.toString());
            if (currentHash == null) {
                break;
            }
            if (levelDBDao.setStatus(currentHash)) {
                prevHash = currentHash;
            }
        }
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