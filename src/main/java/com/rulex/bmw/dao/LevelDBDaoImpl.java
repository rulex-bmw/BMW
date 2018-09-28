package com.rulex.bmw.dao;

import com.rulex.bmw.pojo.DataBean;
import com.rulex.bmw.util.DataException;
import com.rulex.bmw.util.LevelDBUtil;
import com.rulex.bmw.util.SHA256;
import com.rulex.bmw.util.TypeUtils;
import com.google.protobuf.ByteString;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static org.fusesource.leveldbjni.JniDBFactory.*;

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
            DataBean.Data build = DataBean.Data.newBuilder().setParam(ByteString.copyFrom(CREATION_DATA)).setTs(timestamp).setSerial(serial).build();
            String hash = SHA256.getSHA256(build.toString());
            DataBean.Data mata = DataBean.Data.newBuilder().setPrevHash(ByteString.copyFrom(bytes(hash))).setSerial(serial).build();
            flagDB.put(WRITEPOSITION, mata.toByteArray());
            flagDB.put(READPOSITION, mata.toByteArray());
            LevelDBDaoImpl levelDBDao = new LevelDBDaoImpl();
            levelDBDao.setHeaderData(build, dataDB);
            dataDB.put(bytes(hash), build.toByteArray());
        } catch (IOException e) {
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
}
