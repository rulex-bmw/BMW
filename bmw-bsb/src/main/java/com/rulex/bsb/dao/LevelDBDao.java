package com.rulex.bsb.dao;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.DataException;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.bsb.utils.SHA256;
import org.iq80.leveldb.DB;
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
public class LevelDBDao {

    public static final byte[] HEADER_KEY = bytes("000000");
    public static final byte[] WRITEPOSITION = bytes("writePosition");
    public static final byte[] READPOSITION = bytes("readPosition");
    public static final byte[] CREATION_DATA = bytes("Rulex BMW (Blockchain Middleware) is accelerating the landing of blockchain technology by migrating existed app ecosystem to public blockchains");


    /**
     * 设置创世区块
     */
    public static void origin() {
        if (null != LevelDBUtil.getDataDB().get(HEADER_KEY)) {
            return;
        }
        //生成创世数据
        try {
            DataBean.Data.Builder origin = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(CREATION_DATA))
                    .setTs(new DateTime().toString("yyyyMMddHHmmssSSSS"))
                    .setSerial(0);
            byte[] data = origin.build().toByteArray();
            byte[] key = SHA256.getSHA256Bytes(data);
            //生成标签
            DataBean.Position position = DataBean.Position.newBuilder().setDataKey(ByteString.copyFrom(key)).setSerial(0).build();
            LevelDBUtil.getMataDB().put(WRITEPOSITION, position.toByteArray());
            LevelDBDao.setHeaderData(origin, LevelDBUtil.getDataDB());
            LevelDBUtil.getDataDB().put(key, data);
            LevelDBUtil.getMataDB().put(READPOSITION, position.toByteArray());
        } finally {
            LevelDBUtil.closeDB();
        }
    }


    /**
     * 1、Calculate headerValue and save key='000000', headerValue=CryptoUtils (record, Vn-1).
     * 2、Protocal buffer serializer value, calculate key=HASH (value), save records, value=param|Ts|prev_hash
     * 3、save flag
     *
     * @param param
     * @throws IOException
     */
    public static synchronized void set(DataBean.Data param) throws IOException {
        if (null == param.getPayload() && !(param.getPayload().toByteArray().length <= 256)) {
            return;
        }
        try {
            //generation timestamp
            String timestamp = new DateTime().toString("yyyyMMddHHmmssSSSS");
            ByteString p = param.getPayload();
            //获取上一个hash
            DataBean.Position writeposition = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION));
            Long s = writeposition.getSerial();
            s++;
            //Set up herderVelue
            DataBean.Data.Builder record = DataBean.Data.newBuilder().setPayload(p).setTs(timestamp).setSerial(s);
            setHeaderData(record, LevelDBUtil.getDataDB());
            //seek key=HASH(payload,ts,serial,prevHash)
            // Take a hash of data
            record.setPrevHash(writeposition.getDataKey());
            byte[] hashkey = SHA256.getSHA256Bytes(record.build().toByteArray());
            //从signature获取签名
            ByteString sign = ByteString.copyFrom(bytes("1"));
            //Save a data , data = payload, ts, prevhash, serial, sign, flag
            record.setSign(sign).setFalg(false);
            LevelDBUtil.getDataDB().put(hashkey, record.build().toByteArray());
            LevelDBUtil.getMataDB().put(WRITEPOSITION, DataBean.Position.newBuilder().setDataKey(ByteString.copyFrom(hashkey)).setSerial(s).build().toByteArray());
        } finally {
            LevelDBUtil.closeDB();
        }
    }

    /**
     * Preserving partial order structure head
     *
     * @param record
     * @param db
     */

    public static void setHeaderData(DataBean.Data.Builder record, DB db) {
        byte[] vn_1 = db.get(HEADER_KEY);
        if (vn_1 != null) {
            record.setPrevHash(ByteString.copyFrom(vn_1)).build();
        }
        db.put(HEADER_KEY, SHA256.getSHA256Bytes(record.build().toByteArray()));
    }

    /**
     * 获取readposition
     *
     * @return
     */
    public static DataBean.Position getReadposition() {
        DataBean.Position data = null;
        try {
            byte[] readposition = LevelDBUtil.getMataDB().get(READPOSITION);
            data = DataBean.Position.parseFrom(readposition);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LevelDBUtil.closeDB();
        }
        return data;
    }


    /**
     * 根据key查询出当前值，并将记录加入区块链
     *
     * @param key leveldb数据的hashkey
     * @return 是否上链成功
     */
    public static boolean setStatus(byte[] key) {
        try {
            byte[] bytes = LevelDBUtil.getDataDB().get(key);
            DataBean.Data data = DataBean.Data.parseFrom(bytes);
            int edit = BlockChainDao.putStatus(key, data.getPayload().toByteArray());
            if (edit == 1) {
                //修改readposition
                DataBean.Position readposition = DataBean.Position.newBuilder().setDataKey(ByteString.copyFrom(key)).setSerial(data.getSerial()).build();
                LevelDBUtil.getMataDB().put(READPOSITION, readposition.toByteArray());
                //修改flag
                LevelDBUtil.getDataDB().put(key, data.toBuilder().setFalg(true).build().toByteArray());
                return true;
            } else {
                throw new DataException("Data write blockchain failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } finally {
            LevelDBUtil.closeDB();
        }
        return false;
    }

    /**
     * Verify the offset structure header
     *
     * @return Map<String, byte[]> levelDB数据库保存的上链数据的key.
     * map的value为levelDB数据库数据的key,map的key为上一条levelDB数据库数据的key
     * The value of the key.
     * map of the data base is the key of the database data, and the key of the map is the key of the last database data
     * @throws IOException
     */
    public Map<String,  byte[]> verifyHeaderData() throws IOException {

        String startValue = null;
        String headerValue = null;
        Map<String, byte[]> map = new HashMap<>();
        try {

            //从数据库读取最后一条记录的key
            // Read the key of the last record from the database
            DataBean.Data hash = DataBean.Data.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION));
            byte[] lastKey = hash.getPrevHash().toByteArray();

            //从数据库读取readPosition的key
            // Read the key for readPosition from the database
            byte[] readKey = null;
            if (LevelDBUtil.getMataDB().get(READPOSITION) != null) {
                DataBean.Data hash2 = DataBean.Data.parseFrom(LevelDBUtil.getMataDB().get(READPOSITION));
                readKey = hash2.getPrevHash().toByteArray();
            }
            byte[] prveKey = lastKey;
            Stack<byte[]> stack = new Stack<byte[]>();
            stack.push(lastKey);

            //从数据库读取所需的记录，并保存所有的key
            //Read the required records from the database and save all the keys
            boolean flag = true;
            if (asString(prveKey).equals(asString(readKey))) {
                flag = false;
            }
            while (true) {
                //mapValue为map的vale值,map的value为数据库数据的key,map的key为上一条数据库数据的key
                //The value of the map
                byte[] mapValue = prveKey;
                byte[] value = LevelDBUtil.getDataDB().get(prveKey);
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
                DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(prveKey));
                prveKey = data.getPrevHash().toByteArray();
                if (prveKey.length == 0 || prveKey == null) {
                    break;
                }
                stack.push(prveKey);
                //将LevelDB数据库的key保存进map里
                //Save the key of LevelDB database into map
                if (flag) {
                    map.put(asString(prveKey), mapValue);
                }
                //排除掉已上区块链的数据
                //Exclude data that is already on the blockChain
                if (asString(prveKey).equals(asString(readKey))) {
                    flag = false;
                }
            }

            //从数据库读取header的值
            //Read the value of the header from the database
            headerValue = asString(LevelDBUtil.getDataDB().get(HEADER_KEY));

            //验算header值
            //Check the header values
            while (!stack.isEmpty()) {
                byte[] value = stack.pop();
                DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(value));
                if (startValue == null) {
                    DataBean.Header header = DataBean.Header.newBuilder().setPayload(data.getPayload()).setTs(data.getTs()).setSerial(data.getSerial()).build();

                    startValue = SHA256.getSHA256(header.toString());
                } else {

                    DataBean.Header header = DataBean.Header.newBuilder().setPayload(data.getPayload()).setTs(data.getTs()).setSerial(data.getSerial()).setPrevHeader(ByteString.copyFrom(bytes(startValue))).build();

                    startValue = SHA256.getSHA256(header.toString());
                }
            }
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
     * Get the hash map to make it easier to find the next key
     *
     * @return Map<String, byte[]> data库的key.
     * map的value为数据库数据的key,map的key为上一条数据库数据的key
     * The value of the key.
     * map of the data base is the key of the database data, and the key of the map is the key of the last database data
     * @throws IOException
     */
    public static Map<String, byte[]> getHashMap() throws IOException {

        Map<String, byte[]> map = new HashMap<>();
        try {
            //从数据库读取最后一条记录的key
            // Read the key of the last record from the database
            DataBean.Data hash = DataBean.Data.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION));
            byte[] lastKey = hash.getPrevHash().toByteArray();
            //从数据库读取readPosition的key
            // Read the key for readPosition from the database
            byte[] readKey = null;
            if (LevelDBUtil.getMataDB().get(READPOSITION) != null) {
                DataBean.Data hash2 = DataBean.Data.parseFrom(LevelDBUtil.getMataDB().get(READPOSITION));
                readKey = hash2.getPrevHash().toByteArray();
            }

            byte[] prveKey = lastKey;

            //从数据库读取所需的记录，并保存所有的key
            //Read the required records from the database and save all the keys
            boolean flag = true;
            if (asString(readKey).equals(asString(prveKey))) {
                flag = false;
            }
            while (flag) {
                //mapValue为map的vale值,map的value为数据库数据的key,map的key为上一条数据库数据的key
                //The value of the map
                byte[] mapValue = prveKey;
                DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(prveKey));
                prveKey = data.getPrevHash().toByteArray();

                if (prveKey.length == 0 || prveKey == null) {
                    break;
                }

                //将LevelDB数据库的key保存进map里
                //Save the key of LevelDB database into map
                if (flag) {
                    map.put(asString(prveKey), mapValue);
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
