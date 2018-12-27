package com.rulex.bsb.service;

import com.rulex.bsb.dao.LevelDBDao;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * BSB业务处理总线程
 *
 * @author admin
 */
public class BSBService {

    private static boolean openThread = true;


    public static void producer(DataBean.Data data, String orgPKHash) {
        try {
            LevelDBDao.origin();
            LevelDBDao.set(data, orgPKHash);
            if (openThread) {
                new CunsumerThread().start();
                openThread = false;
            }

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
    public static Integer Consumer() {
        try {
            if (null == LevelDBUtil.getMataDB().get(LevelDBDao.WRITEPOSITION)) {
                return -1;
            }
            Map<String, byte[]> keyMap = LevelDBDao.getHashMap();
            int size = keyMap.size();
            if (size == 0) {
                return 0;
            }
            //get readposition
            DataBean.Position readposition = LevelDBDao.getReadposition();
            //从第m+1项上链
            byte[] prevHash = readposition.getDataKey().toByteArray();
            for(int i = 0; i < size; i++) {
                byte[] currentHash = keyMap.get(Base64.getEncoder().encodeToString(prevHash));
                if (currentHash == null || currentHash.length == 0) {
                    break;
                }
                if (LevelDBDao.setStatus(currentHash)) {
                    //调用消息通知机制，提示其他线程完成上链
                    prevHash = currentHash;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


}