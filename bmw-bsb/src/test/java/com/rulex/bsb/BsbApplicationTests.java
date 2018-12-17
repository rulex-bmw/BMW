package com.rulex.bsb;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BsbApplicationTests {

//    @Resource
//    private BSBService bsbService;
//
//    @Test
//    public void levelDB() throws IOException {
//        Verify verify = new Verify();
//        //存入levelDB
//        ByteString param = ByteString.copyFrom(bytes("13afds255sgds522987eff54325747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
//        DataBean.Data build = DataBean.Data.newBuilder().setParam(param).build();
////        bsbService.producer(build);
//        //查询数据
//        DB dataDB = null;
//        if (LevelDBDao.dataDB == null) {
//            dataDB = LevelDBUtil.getDb("data");
//        } else {
//            dataDB = LevelDBDao.dataDB;
//        }
////        dataDB = LevelDBUtil.getDb("data");
//        DBIterator iterator = dataDB.iterator();
//        String s = null;
//        String asString = null;
//        int i = 0;
//        for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
//            s = asString(iterator.peekNext().getKey());
//            if (asString(iterator.peekNext().getKey()).equalsIgnoreCase(asString(LevelDBDao.HEADER_KEY))) {
//                asString = asString(iterator.peekNext().getValue());
//            } else {
//                DataBean.Data data = DataBean.Data.parseFrom(iterator.peekNext().getValue());
//                asString = data.toString();
//            }
//            System.out.println(s + "--------\n" + asString);
//            i++;
//            System.out.println("----------------------------------------------------");
//        }
//        System.out.println("当前共有" + i + "条payload");
//        iterator.close();
//        dataDB.close();
//        if (LevelDBDao.mataDB != null) {
//            LevelDBDao.mataDB.close();
//        }
//        if (LevelDBDao.dataDB != null) {
//            LevelDBDao.dataDB.close();
//        }
//    }


//    @Test
//    public void blockChain() {
//        Verify verify = new Verify();
//        try {
//            bsbService.customer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    class Producer implements Callable<Integer> {
//        @Override
//        public Integer call() throws Exception {
//            Verify verify = new Verify();
//            System.out.println("producer的线程=" + Thread.currentThread().getId());
//            //存入levelDB
//            ByteString param = ByteString.copyFrom(bytes("4543afds255sgds522987eff54325747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
//            DataBean.Data build = DataBean.Data.newBuilder().setParam(param).build();
//            bsbService.producer(build);
//            Thread.sleep(1000);
//            return 1;
//        }
//    }

//    class Customer implements Callable<Integer> {
//        @Override
//        public Integer call() throws Exception {
//            Verify verify = new Verify();
//            System.out.println("customer的线程为=" + Thread.currentThread().getId());
//            Integer customer = bsbService.customer();
//            return customer;
//        }
//    }


//    @Test
//    public void jdbcTest() {
//        for(int i = 0; i < 2; i++) {
//            ExecutorService ES = Executors.newCachedThreadPool();
//            Callable<Integer> customer = new Customer();
//            Callable<Integer> producer = new Producer();
//            Future<Integer> customerResult = ES.submit(customer);
//            Future<Integer> producerResult = ES.submit(producer);
//            ES.shutdown();
//            System.out.println("当前主线程id=" + Thread.currentThread().getId());
//            try {
//                Integer c = customerResult.get();
//                Integer p = producerResult.get();
//                System.out.println(p + "---------------------------" + c);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            if (LevelDBDao.mataDB != null) {
//                LevelDBDao.mataDB.close();
//            }
//            if (LevelDBDao.dataDB != null) {
//                LevelDBDao.dataDB.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    @Test
//    public void exceptions() {
//        try {
//            DB data = LevelDBUtil.getDb("data");
//            DB data1 = LevelDBUtil.getDb("mata");
//            data.close();
//            data1.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }


}
