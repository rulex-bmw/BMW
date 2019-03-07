package com.rulex.bsb.service;

import com.rulex.bsb.dao.LevelDBDao;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * BSB Total thread of business processing
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
     * readposition: Linked data key     writeposition：set levelDB new data
     * get m from readpositionm and search m+1 from map
     * m:finish on blockchain，m+1: unblockchain
     * get DATA from levelDB,Storing immutable information in a database
     */
    public synchronized static Integer Consumer() {
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
            //Chain from the m+1 term
            byte[] prevHash = readposition.getDataKey().toByteArray();
            for(int i = 0; i < size; i++) {
                byte[] currentHash = keyMap.get(Base64.getEncoder().encodeToString(prevHash));
                if (currentHash == null || currentHash.length == 0) {
                    break;
                }
                if (LevelDBDao.setStatus(currentHash)) {
                    //Invokes the message notification mechanism to prompt other threads to complete the chain
                    prevHash = currentHash;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


}