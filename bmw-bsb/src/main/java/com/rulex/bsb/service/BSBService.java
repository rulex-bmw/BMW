package com.rulex.bsb.service;

import com.rulex.bsb.dao.LevelDBDao;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;

import java.io.IOException;
import java.util.Map;

/**
 * BSB业务处理总线程
 *
 * @author admin
 */
public class BSBService {


    public static void producer(DataBean.Data data) {
        try {
            LevelDBDao.origin();
            LevelDBDao.set(data);
            System.out.println("producer thread id " + Thread.currentThread().getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * readposition:已上链数据key     writeposition：存入levelDB最新数据
     * 从readposition中读取m并去map中寻找m+1
     * m:已经上链，m+1:未上链
     * 从levelDB中取出DATA,将不可变信息存入数据库
     */
    public static Integer Consumer() throws IOException {
        try {
            if (null == LevelDBUtil.getMataDB().get(LevelDBDao.WRITEPOSITION)) {
                return -1;
            }
            Map<byte[], byte[]> keyMap = LevelDBDao.getHashMap();
            int size = keyMap.size();
            if (size == 0) {
                return 0;
            }
            //get readposition
            DataBean.Position readposition = LevelDBDao.getReadposition();
            //从第m+1项上链
            byte[] prevHash = readposition.getDataKey().toByteArray();
            int i;
            for(i = 0; i < size; i++) {
                byte[] currentHash = keyMap.get(prevHash);
                if (currentHash == null || currentHash.length == 0) {
                    break;
                }
                if (LevelDBDao.setStatus(currentHash)) {
                    //调用消息通知机制，提示其他线程完成上链
                    prevHash = currentHash;
                    System.out.println("customer thread id: " + Thread.currentThread().getId() + "\ncustomer run with " + i);
                }
            }
            return i;
        } finally {
            LevelDBUtil.closeDB();
        }
    }


}