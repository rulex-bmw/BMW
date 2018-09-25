package com.eroc.bmw;

import com.eroc.bmw.dao.LevelDBDao;
import com.eroc.bmw.dao.LevelDBDaoImpl;
import com.eroc.bmw.pojo.DataBean;
import com.eroc.bmw.pojo.ParamBean;
import com.eroc.bmw.thread.MyRunnable;
import com.google.protobuf.ByteString;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BmwApplicationTests {

//    public String a = null;

    /**
     * 验证数据完整性
     */
//    {
//        a = "1";
//        System.out.println(a+"22222");
//        System.exit(1);
//    }
    @Test
    public void contextLoads() throws IOException {
//        Options options = new Options();
//        factory.destroy(new File("data"), options);
//        factory.destroy(new File("flag"), options);
//        LevelDBDao levelDBDao = new LevelDBDaoImpl();
//        ByteString flag = ByteString.copyFrom(bytes("1"));
//        ByteString param = ByteString.copyFrom(bytes("2234b4323334fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
//        ParamBean.Param build = ParamBean.Param.newBuilder().setFlag(flag).setParam(param).build();
//        levelDBDao.set(build);
////
        Options options = new Options();
        options.createIfMissing(true);
        DB dataDB = null;
        dataDB = factory.open(new File("data"), options);
        DBIterator iterator = dataDB.iterator();
        String s = null;
        String asString = null;
        for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            s = asString(iterator.peekNext().getKey());
            if (asString(iterator.peekNext().getKey()).equalsIgnoreCase(asString(LevelDBDaoImpl.HEADER_KEY))) {
                asString = asString(iterator.peekNext().getValue());
            } else {
                DataBean.Data data = DataBean.Data.parseFrom(iterator.peekNext().getValue());
                asString = data.toString();
            }
            System.out.println(s + "--------" + asString);
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
        }
        iterator.close();
        dataDB.close();




//		try {
////			LevelDBDaoImpl.setMsg();
//			levelDBDao.getList();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//        byte[] param = {'2','3','4'};
//        byte[] flag = {'a','b','c'};
//        ByteString bytes = ByteString.copyFrom(param);
//        ByteString flags = ByteString.copyFrom(flag);
//        ParamBean.Param build = ParamBean.Param.newBuilder().setParam(bytes).setFlag(flags).build();
////        System.out.println(build.toByteArray().length);
//        System.out.println(build.toString());
////        System.out.println(SHA256.getSHA256("1"));
//        byte[] bytes1 = build.toByteArray();
////        ByteString bytes2 = ByteString.copyFrom(bytes1);
//        ParamBean.Param param1 = ParamBean.Param.parseFrom(bytes1);
//        System.out.println(param1.toString());

//        6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b

//        DBIterator iterator = db.iterator();
//        try {
//            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
//                String key = asString(iterator.peekNext().getKey());
//                String value = asString(iterator.peekNext().getValue());
//                System.out.println(key+" = "+value);
//            }
//        } finally {
//            // Make sure you close the iterator to avoid resource leaks.
//            iterator.close();
//        }

    }


    @Test
    public void test() {
        MyRunnable myRunnable = new MyRunnable("1号run");
        Thread thread = new Thread(myRunnable);
        for(int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " " + i);
            if (i == 30) {
//                Thread thread2 = new Thread(myRunnable);
                thread.start();
//                thread2.start();
            }
            if (i == 50) {
                myRunnable.stopThread();
            }

//                Thread myThread1 = new MyThread("一号");     // 创建一个新的线程  myThread1  此线程进入新建状态
//                Thread myThread2 = new MyThread("二号");     // 创建一个新的线程 myThread2 此线程进入新建状态
//                myThread1.start();                     // 调用start()方法使得线程进入就绪状态
//                myThread2.start();                     // 调用start()方法使得线程进入就绪状态


//            System.out.println(Thread.currentThread().getName() + " " + i);
//            if (i == 30) {
//                Runnable in_my_runnable = new MyRunnable("in my runnable");
//                Thread in_my_thread = new MyThread(in_my_runnable, "in my thread");
//                in_my_thread.start();
//            }


        }
    }


}
