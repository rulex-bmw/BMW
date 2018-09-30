package com.rulex.bmw.dao;

import com.google.protobuf.ByteString;
import com.rulex.bmw.pojo.DataBean;
import com.rulex.bmw.util.*;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static org.fusesource.leveldbjni.JniDBFactory.asString;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

/**
 * Data persistence layer
 *
 * @author admin
 */
public class LevelDBDaoImpl implements LevelDBDao {

    public static final String DATA_PATH = "data";
    public static final String FLAG_PATH = "mata";
    public static final byte[] HEADER_KEY = bytes("000000");
    private static final byte[] WRITEPOSITION = bytes("writePosition");
    private static final byte[] READPOSITION = bytes("readPosition");
    private static final byte[] CREATION_DATA = bytes("Rulex BMW (Blockchain Middleware) is accelerating the landing of blockchain technology by migrating existed app ecosystem to public blockchains");

    /**
     * 设置创世区块
     */
    public void origin() {
        double i = 0;
        DB dataDB = null;
        DB flagDB = null;
        try {
            dataDB = LevelDBUtil.getDb(DATA_PATH);
            flagDB = LevelDBUtil.getDb(FLAG_PATH);
            if (dataDB.get(HEADER_KEY) != null) {
                return;
            }
            //generation timestamp
            byte[] ts = bytes(new DateTime().toString("yyyyMMddHHmmssSSSS"));
            ByteString timestamp = ByteString.copyFrom(ts);
            ByteString serial = ByteString.copyFrom(bytes(TypeUtils.doubleToString(i)));
            DataBean.Data origin = DataBean.Data.newBuilder().setParam(ByteString.copyFrom(CREATION_DATA)).setTs(timestamp).setSerial(serial).build();
            String key = SHA256.getSHA256(origin.toString());
            DataBean.Data mata = DataBean.Data.newBuilder().setPrevHash(ByteString.copyFrom(bytes(key))).setSerial(serial).build();
            flagDB.put(WRITEPOSITION, mata.toByteArray());
            LevelDBDaoImpl levelDBDao = new LevelDBDaoImpl();
            levelDBDao.setHeaderData(origin, dataDB);
            dataDB.put(bytes(key), origin.toByteArray());
            //将创世数据加入区块链
            String sql = "insert into bmw_chain (key,value) values (*,*)";
            Object[] obj = {bytes(key), origin.toByteArray()};
//            int edit = JDBCUtils.edit(sql, obj);
            int edit = 1;
            if (edit == 1) {
                flagDB.put(READPOSITION, mata.toByteArray());
            } else {
                throw new DataException("Data write blockchain failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } finally {
            try {
                dataDB.close();
                flagDB.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 1、Calculate headerValue and save key='000000', headerValue=SHA256 (record, Vn-1).
     * 2、Protocal buffer serializer value, calculate key=HASH (value), save records, value=param|Ts|prev_hash
     * 3、save flag
     *
     * @param param
     * @throws IOException
     */
    public synchronized void set(DataBean.Data param) throws IOException {
        if (param.getParam() == null && !(param.getParam().toByteArray().length <= 256)) {
            return;
        }
        DB dataDB = null;
        DB flagDB = null;
        DBIterator iterator = null;
        try {
            dataDB = LevelDBUtil.getDb(DATA_PATH);
            flagDB = LevelDBUtil.getDb(FLAG_PATH);
            //generation timestamp
            byte[] ts = bytes(new DateTime().toString("yyyyMMddHHmmssSSSS"));
            ByteString timestamp = ByteString.copyFrom(ts);
            ByteString p = param.getParam();
            //获取上一个hash
            DataBean.Data writeposition = DataBean.Data.parseFrom(flagDB.get(WRITEPOSITION));
            String s = writeposition.getSerial().toStringUtf8();
            Double i = Double.valueOf(s);
            i++;
            ByteString serial = ByteString.copyFrom(bytes(TypeUtils.doubleToString(i)));
            //Set up herderVelue
            DataBean.Data record = DataBean.Data.newBuilder().setParam(p).setTs(timestamp).setSerial(serial).build();
            setHeaderData(record, dataDB);
            //seek key=HASH(value)
            // Take a hash of data
            DataBean.Data hash = DataBean.Data.newBuilder().setParam(p).setTs(timestamp).setSerial(serial).setPrevHash(writeposition.getPrevHash()).build();
            byte[] hashkey = bytes(SHA256.getSHA256(hash.toString()));
            //从signature获取签名
            ByteString sign = ByteString.copyFrom(bytes("1"));
            //Save a data
            DataBean.Data data = DataBean.Data.newBuilder().setParam(p).setTs(timestamp).setPrevHash(writeposition.getPrevHash()).setSerial(serial).setSign(sign).setFlag(param.getFlag()).build();
            dataDB.put(hashkey, data.toByteArray());
            DataBean.Data mata = DataBean.Data.newBuilder().setPrevHash(ByteString.copyFrom(hashkey)).setSerial(serial).build();
            flagDB.put(WRITEPOSITION, mata.toByteArray());
        } finally {
            // Make sure you close the db to shutdown the
            // database and avoid resource leaks.
            dataDB.close();
            flagDB.close();
        }
    }

    /**
     * Preserving partial order structure head
     *
     * @param record
     * @param db
     */

    public void setHeaderData(DataBean.Data record, DB db) {
        byte[] vn_1 = db.get(HEADER_KEY);
        if (vn_1 != null) {
            DataBean.Data build = DataBean.Data.newBuilder().setParam(record.getParam()).setTs(record.getTs()).setSerial(record.getSerial()).setVn1(ByteString.copyFrom(vn_1)).build();
            db.put(HEADER_KEY, bytes(SHA256.getSHA256(build.toString())));
        } else {
            String sha256 = SHA256.getSHA256(record.toString());
            db.put(HEADER_KEY, bytes(sha256));
        }
    }

    /**
     * 获取readposition
     *
     * @return
     */
    @Override
    public DataBean.Data getReadposition() {
        DB mataDB = null;
        DataBean.Data data = null;
        try {
            mataDB = LevelDBUtil.getDb(FLAG_PATH);
            byte[] readposition = mataDB.get(READPOSITION);
            data = DataBean.Data.parseFrom(readposition);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 根据key查询出当前值，并将记录加入区块链
     *
     * @param hashey
     */
    public boolean setStatus(String hashey) {
        DB dataDB = null;
        DB flagDB = null;
        try {
            dataDB = LevelDBUtil.getDb(DATA_PATH);
            byte[] bytes = dataDB.get(bytes(hashey));
            DataBean.Data data = DataBean.Data.parseFrom(bytes);
            DataBean.Data chainData = DataBean.Data.newBuilder().setParam(data.getParam()).setSerial(data.getSerial()).setTs(data.getTs()).setPrevHash(data.getPrevHash()).build();
            String sql = "insert into bmw_chain (key,value) values (*,*)";
            Object[] obj = {bytes(hashey), chainData.toByteArray()};
//            int edit = JDBCUtils.edit(sql, obj);
            int edit = 1;
            if (edit == 1) {
                //修改readposition
                DataBean.Data readposition = DataBean.Data.newBuilder().setPrevHash(ByteString.copyFrom(bytes(hashey))).setSerial(data.getSerial()).build();
                flagDB.put(READPOSITION,readposition.toByteArray());
                return true;
            } else {
                throw new DataException("Data write blockchain failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 暂时无用
     *
     *
     * 修改readposition
     *
     * @param currentKey
     */
//    @Override
//    public void editReadposition(String currentKey) {
//        DB db = null;
//        try {
//            db = LevelDBUtil.getDb(DATA_PATH);
//            byte[] bytes = db.get(bytes(currentKey));
//            DataBean.Data data = DataBean.Data.parseFrom(bytes);
//            DataBean.Data mata = DataBean.Data.newBuilder().setPrevHash(ByteString.copyFrom(bytes(currentKey))).setSerial(data.getSerial()).build();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Verify the offset structure header
     *
     * @return Map<byte[], byte[]> data库的key.
     * map的value为数据库数据的key,map的key为上一条数据库数据的key
     * The value of the key.
     * map of the data base is the key of the database data, and the key of the map is the key of the last database data
     * @throws IOException
     */
    public Map<String, String> verifyHeaderData() throws IOException {

        DB db = null;
        String startValue = null;
        String headerValue = null;
        Map<String, String> map = new HashMap<String, String>();
        try {
            //从数据库读取最后一条记录的key
            // Read the key of the last record from the database
            db = LevelDBUtil.getDb(FLAG_PATH);
            DataBean.Data hash = DataBean.Data.parseFrom(db.get(WRITEPOSITION));
            byte[] lastKey = hash.getPrevHash().toByteArray();
            //从数据库读取readPosition的key
            // Read the key for readPosition from the database
            byte[] readKey = null;
            if (db.get(READPOSITION) != null) {
                DataBean.Data hash2 = DataBean.Data.parseFrom(db.get(READPOSITION));
                readKey = hash2.getPrevHash().toByteArray();
            }
            db.close();

            byte[] prveKey = lastKey;
            Stack<byte[]> stack = new Stack<byte[]>();
            stack.push(lastKey);
            //从数据库读取所需的记录，并保存所有的key
            //Read the required records from the database and save all the keys
            db = LevelDBUtil.getDb(DATA_PATH);
            boolean flag = true;
            if (asString(prveKey).equals(asString(readKey))) {
                flag = false;
            }
            while (true) {
                //mapValue为map的vale值,map的value为数据库数据的key,map的key为上一条数据库数据的key
                //The value of the map
                byte[] mapValue = prveKey;
                byte[] value = db.get(prveKey);
                //value为null说明数据被篡改，抛出错误
                // A value of null indicates that the data has been tampered with and an error has been thrown
                if (value == null) {
                    try {
                        throw new DataException("Sorry, the database data is abnormal, please check whether the data has been tampered！");
                    } catch (DataException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                DataBean.Data data = DataBean.Data.parseFrom(db.get(prveKey));
                prveKey = data.getPrevHash().toByteArray();
                if (prveKey.length == 0 || prveKey == null) {
                    break;
                }
                stack.push(prveKey);
                //将LevelDB数据库的key保存进map里
                //Save the key of LevelDB database into map
                if (flag) {
                    map.put(asString(prveKey), asString(mapValue));
                }
                //排除掉已上区块链的数据
                //Exclude data that is already on the blockChain
                if (asString(prveKey).equals(asString(readKey))) {
                    flag = false;
                }
            }

            //从数据库读取header的值
            //Read the value of the header from the database
            headerValue = asString(db.get(HEADER_KEY));

            //验算header值
            //Check the header values
            while (!stack.isEmpty()) {
                byte[] value = stack.pop();
                DataBean.Data data = DataBean.Data.parseFrom(db.get(value));
                if (startValue == null) {
                    DataBean.Data record = DataBean.Data.newBuilder().setParam(data.getParam()).setTs(data.getTs()).setSerial(data.getSerial()).build();

                    startValue = SHA256.getSHA256(record.toString());
                } else {

                    DataBean.Data record = DataBean.Data.newBuilder().setParam(data.getParam()).setTs(data.getTs()).setSerial(data.getSerial()).setVn1(ByteString.copyFrom(bytes(startValue))).build();

                    startValue = SHA256.getSHA256(record.toString());
                }
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //如果算出来的header值与数据库中保存的不一样，说明数据被篡改，抛出错误
        //If the calculated header value is different from what is stored in the database, the data is tampered with and an error is thrown
        if (!headerValue.equals(startValue)) {

            try {
                throw new DataException("Sorry, the database data is abnormal, please check whether the data has been tampered！");
            } catch (DataException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Verify that headerValue is correct.");
        }
        return map;
    }


    /**
     * Get the hash map
     *
     * @return Map<byte[], byte[]> data库的key.
     * map的value为数据库数据的key,map的key为上一条数据库数据的key
     * The value of the key.
     * map of the data base is the key of the database data, and the key of the map is the key of the last database data
     * @throws IOException
     */
    public static Map<String, String> getHashMap() throws IOException {

        DB db = null;
        Map<String, String> map = new HashMap<String, String>();
        try {
            //从数据库读取最后一条记录的key
            // Read the key of the last record from the database
            db = LevelDBUtil.getDb(FLAG_PATH);
            DataBean.Data hash = DataBean.Data.parseFrom(db.get(WRITEPOSITION));
            byte[] lastKey = hash.getPrevHash().toByteArray();
            //从数据库读取readPosition的key
            // Read the key for readPosition from the database
            byte[] readKey = null;
            if (db.get(READPOSITION) != null) {
                DataBean.Data hash2 = DataBean.Data.parseFrom(db.get(READPOSITION));
                readKey = hash2.getPrevHash().toByteArray();
            }
            db.close();

            byte[] prveKey = lastKey;
            //从数据库读取所需的记录，并保存所有的key
            //Read the required records from the database and save all the keys
            db = LevelDBUtil.getDb(DATA_PATH);
            boolean flag = true;
            if (asString(readKey).equals(asString(prveKey))) {
                flag = false;
            }
            while (flag) {
                //mapValue为map的vale值,map的value为数据库数据的key,map的key为上一条数据库数据的key
                //The value of the map
                byte[] mapValue = prveKey;
                DataBean.Data data = DataBean.Data.parseFrom(db.get(prveKey));
                prveKey = data.getPrevHash().toByteArray();

                if (prveKey.length == 0 || prveKey == null) {
                    break;
                }

                //将LevelDB数据库的key保存进map里
                //Save the key of LevelDB database into map
                if (flag) {
                    map.put(asString(prveKey), asString(mapValue));
                }
                //排除掉已上区块链的数据
                //Exclude data that is already on the blockChain
                if (asString(prveKey).equals(asString(readKey))) {
                    flag = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
