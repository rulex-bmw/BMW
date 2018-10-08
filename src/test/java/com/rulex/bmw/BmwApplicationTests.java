package com.rulex.bmw;

import com.google.protobuf.ByteString;
import com.rulex.bmw.dao.LevelDBDaoImpl;
import com.rulex.bmw.pojo.DataBean;
import com.rulex.bmw.service.BSBService;
import com.rulex.bmw.service.Verify;
import com.rulex.bmw.util.LevelDBUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.*;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BmwApplicationTests {

    @Resource
    private BSBService bsbService;

    @Test
    public void levelDB() throws IOException {
        Verify verify = new Verify();
        //存入levelDB
        ByteString param = ByteString.copyFrom(bytes("13afds255sgds522987eff54325747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
        DataBean.Data build = DataBean.Data.newBuilder().setParam(param).build();
//        bsbService.producer(build);
        //查询数据
        DB dataDB = null;
        dataDB = LevelDBUtil.getDb("data");
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
            System.out.println(s + "--------\n" + asString);
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
        }
        iterator.close();
        dataDB.close();

    }


    @Test
    public void blockChain() {
        Verify verify = new Verify();
        bsbService.customer();
    }


    class Producer implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            int i;
            Verify verify = new Verify();
            //存入levelDB
            for(i = 1; i <= 3; i++) {
                ByteString param = ByteString.copyFrom(bytes(i + "4543afds255sgds522987eff54325747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
                DataBean.Data build = DataBean.Data.newBuilder().setParam(param).build();
                bsbService.producer(build);
                System.out.println("producer run with "+i);
            }
            //查询数据
//            DB dataDB = null;
//            dataDB = LevelDBUtil.getDb("data");
//            DBIterator iterator = dataDB.iterator();
//            String s = null;
//            String asString = null;
//            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
//                s = asString(iterator.peekNext().getKey());
//                if (asString(iterator.peekNext().getKey()).equalsIgnoreCase(asString(LevelDBDaoImpl.HEADER_KEY))) {
//                    asString = asString(iterator.peekNext().getValue());
//                } else {
//                    DataBean.Data data = DataBean.Data.parseFrom(iterator.peekNext().getValue());
//                    asString = data.toString();
//                }
//                System.out.println(s + "--------\n" + asString);
//                System.out.println("--------------------------------------------------------------------------------------------------------------------");
//            }
//            iterator.close();
//            dataDB.close();
            i--;
            return i;
        }
    }

    class Customer implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            Verify verify = new Verify();
            Integer customer = bsbService.customer();
            return customer;
        }
    }


    @Test
    public void jdbcTest() {
        ExecutorService ES = Executors.newCachedThreadPool();
        Callable<Integer> customer = new Customer();
        Callable<Integer> producer = new Producer();
        Future<Integer> producerResult = ES.submit(producer);
        Future<Integer> customerResult = ES.submit(customer);
        ES.shutdown();
        try {
            Integer p = producerResult.get();
            Integer c = customerResult.get();
            System.out.println(p + "---------------------------" + c);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }


    @Test
    public void exceptions(){
        try {
            DB data = LevelDBUtil.getDb("data");
            DB data1 = LevelDBUtil.getDb("mata");
            data.close();
            data1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
