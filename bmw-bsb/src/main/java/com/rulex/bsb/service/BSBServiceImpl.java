package com.rulex.bsb.service;

import com.rulex.bsb.dao.LevelDBDao;
import com.rulex.bsb.dao.LevelDBDaoImpl;
import com.rulex.bsb.pojo.DataBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.asString;

/**
 * BSB业务处理总线程
 *
 * @author admin
 */
@Service
public class BSBServiceImpl implements BSBService {

    @Resource
    private LevelDBDao levelDBDao;

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
    public Integer customer() throws IOException {
        int i = 0;
        String currentHash = null;
        String prevHash = null;
        if (null == LevelDBDaoImpl.mataDB.get(LevelDBDaoImpl.WRITEPOSITION)) {
            return -1;
        }
        Map<String, String> keyMap = LevelDBDaoImpl.getHashMap();
//        Map<String, String> keyMap = Verify.hashKey;
        if (keyMap.size() == 0) {
            return i;
        }
        //get readposition
        DataBean.Data readposition = levelDBDao.getReadposition();
        //从第m+1项上链
        prevHash = asString(readposition.getPrevHash().toByteArray());
        while (true) {
            currentHash = keyMap.get(prevHash);
            if (currentHash == null || currentHash.equals("")) {
                break;
            }
            if (levelDBDao.setStatus(currentHash)) {
                //调用消息通知机制，提示其他线程完成上链

                prevHash = currentHash;
                i++;
                System.out.println("customer run with " + i);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return i;
    }


}

//@Service
//class Producer implements Callable<Integer> {
//    @Resource
//    private LevelDBDao levelDBDao;
//
//    private DataBean.Data data;
//
//    public Producer(DataBean.Data data) {
//        this.data = data;
//    }
//
//    @Override
//    public Integer call() throws Exception {
//        //将数据保存至区块链
//        try {
//            levelDBDao.origin();
//            levelDBDao.set(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//}

//@Service
//class Customer implements Callable<Integer> {
//    @Resource
//    private LevelDBDao levelDBDao;
//
//    @Override
//    public Integer call() throws Exception {
//        int num = 0;
//        //本地数据持久化
//        String currentHash = null;
//        String prevHash = null;
//        Map<String, String> keyMap = Verify.hashKey;
//        if (keyMap.size() == 0) {
//            return null;
//        }
//        //get readposition
//        DataBean.Data readposition = levelDBDao.getReadposition();
//        //从第m+1项上链
//        prevHash = asString(readposition.getPrevHash().toByteArray());
//        while (true) {
//            currentHash = keyMap.get(prevHash);
//            if (currentHash == null || currentHash.equals("")) {
//                break;
//            }
//            if (levelDBDao.setStatus(currentHash)) {
//                //调用消息通知机制，提示其他线程完成上链
//
//                prevHash = currentHash;
//                num++;
//            }
//        }
//        return num;
//    }
//}