package com.rulex.bsb.dao;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.*;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.util.*;

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
     * Creation block
     */
    public static void origin() {
        if (null != LevelDBUtil.getDataDB().get(HEADER_KEY)) {
            return;
        }
        // Generate creation data
        DataBean.Data.Builder origin = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(CREATION_DATA))
                .setTs(System.currentTimeMillis())
                .setSerial(0);
        DataBean.Header.Builder header = DataBean.Header.newBuilder().setPayload(origin.getPayload()).setSerial(origin.getSerial()).setTs(origin.getTs());
        byte[] data = origin.build().toByteArray();
        byte[] key = SHA256.getSHA256Bytes(data);
        // generate tag
        DataBean.Position position = DataBean.Position.newBuilder().setDataKey(ByteString.copyFrom(key)).setSerial(0).build();
        LevelDBUtil.getMataDB().put(WRITEPOSITION, position.toByteArray());
        LevelDBDao.setHeaderData(header, LevelDBUtil.getDataDB());
        LevelDBUtil.getDataDB().put(key, data);
        LevelDBUtil.getMataDB().put(READPOSITION, position.toByteArray());
    }


    /**
     * 1、Calculate headerValue and save key='000000', headerValue=CryptoUtils (record, Vn-1).
     * 2、Protocal buffer serializer value, calculate key=HASH (value), save records, value=param|Ts|prev_hash
     * 3、save flag
     *
     * @param param
     * @param orgPKHash
     * @throws IOException
     */
    public static synchronized void set(DataBean.Data param, String orgPKHash) throws IOException {
        if (null == param.getPayload() && !(param.getPayload().toByteArray().length <= 256)) {
            return;
        }
        // get prevhash
        DataBean.Position writeposition = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION));
        Long s = writeposition.getSerial();
        s++;
        // Set up herderVelue
        DataBean.Data.Builder record = DataBean.Data.newBuilder().setPayload(param.getPayload()).setTs(System.currentTimeMillis()).setSerial(s);
        DataBean.Header.Builder header = DataBean.Header.newBuilder().setTs(record.getTs()).setSerial(record.getSerial()).setPayload(record.getPayload());
        setHeaderData(header, LevelDBUtil.getDataDB());
        // seek key=HASH(payload,ts,serial,prevHash)
        // Take a hash of data
        record.setPrevHash(writeposition.getDataKey());
        byte[] hashkey = SHA256.getSHA256Bytes(record.build().toByteArray());
        // Get the signature from signature
        ByteString sign = ByteString.copyFrom(bytes("1"));
        // Save a data , data = payload, ts, prevhash, serial, sign, flag
        record.setSign(sign).setFalg(false);
        LevelDBUtil.getDataDB().put(hashkey, record.build().toByteArray());
        if (orgPKHash != null) {
            // PrimaryId and hashkey index information is stored in the Sqlite database
            SqliteUtils.edit(new Object[]{orgPKHash, Base64.getEncoder().encodeToString(hashkey), 1, System.currentTimeMillis()}, "insert into key_indexes (orgPKHash,typeHash,type,ts) values(?,?,?,?)");
        }
        LevelDBUtil.getMataDB().put(WRITEPOSITION, DataBean.Position.newBuilder().setDataKey(ByteString.copyFrom(hashkey)).setSerial(s).build().toByteArray());
    }


    /**
     * Preserving partial order structure head
     *
     * @param record
     * @param db
     */

    public static void setHeaderData(DataBean.Header.Builder record, DB db) {
        byte[] vn_1 = db.get(HEADER_KEY);
        if (vn_1 != null) {
            record.setPrevHeader(ByteString.copyFrom(vn_1));
        }
        db.put(HEADER_KEY, SHA256.getSHA256Bytes(record.build().toByteArray()));
    }

    /**
     * get readposition
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
        }
        return data;
    }


    /**
     * The current value is queried based on the key and records are added to the block chain
     *
     * @param key leveldb hashkey
     * @return Whether the chain is successful
     */
    public static boolean setStatus(byte[] key) {
        try {
            byte[] bytes = LevelDBUtil.getDataDB().get(key);
            DataBean.Data data = DataBean.Data.parseFrom(bytes);
            String id = BlockChainDao.postData(TypeUtils.bytesToHexString(data.getPayload().toByteArray()));

            if (id != null) {
                // Associate the block chain id with the orgPKHash of the data
                if (key.length != 0 && key != null) {
                    setIdIndex(key, bytes, id);
                }
                // modify readposition
                DataBean.Position readposition = DataBean.Position.newBuilder().setDataKey(ByteString.copyFrom(key)).setSerial(data.getSerial()).build();
                LevelDBUtil.getMataDB().put(READPOSITION, readposition.toByteArray());
                // modify flag
                LevelDBUtil.getDataDB().put(key, data.toBuilder().setFalg(true).build().toByteArray());
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
     * 将区块链的id与数据的orgPKHash关联起来，用以查询区块链id
     *
     * @param key 缓存的上链信息的key
     * @param id  区块链id
     */
    public static void setIdIndex(byte[] key, byte[] bytes, String id) {

        List<Map<String, Object>> mapList = SqliteUtils.query("select orgPKHash from key_indexes where typeHash = ?", new Object[]{Base64.getEncoder().encodeToString(key)});
        if (mapList.size() == 1) {
            String orgPKHash = (String) mapList.get(0).get("orgPKHash");
            SqliteUtils.edit(new Object[]{orgPKHash, id, System.currentTimeMillis()}, "insert into id_indexes (orgPKHash,blockChainId,ts) values(?,?,?)");

        } else {

            DataBean.Alteration alteration = null;
            try {
                alteration = DataBean.Alteration.parseFrom(DataBean.Data.parseFrom(bytes).getPayload().toByteArray());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            ByteString orgHashKey = alteration.getOrgHashKey();

            List<Map<String, Object>> mapsList = SqliteUtils.query("select orgPKHash from key_indexes where typeHash = ?", new Object[]{Base64.getEncoder().encodeToString(orgHashKey.toByteArray())});

            String orgPKHash = (String) mapsList.get(0).get("orgPKHash");
            SqliteUtils.edit(new Object[]{orgPKHash, id, System.currentTimeMillis()}, "insert into id_indexes (orgPKHash,blockChainId,ts) values(?,?,?)");

        }
    }

    /**
     * Verify the offset structure header
     *
     * @return Map<String, byte[]>
     * The value of the key.
     * map of the data base is the key of the database data, and the key of the map is the key of the last database data
     * @throws IOException
     */
    public Map<String, byte[]> verifyHeaderData() throws IOException {

        byte[] startValue = null;
        byte[] headerValue = null;
        Map<String, byte[]> map = new HashMap<>();
        try {

            // Read the key of the last record from the database
            DataBean.Position position = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION));
            byte[] lastKey = position.getDataKey().toByteArray();

            // Read the key for readPosition from the database
            byte[] readKey = null;
            if (LevelDBUtil.getMataDB().get(READPOSITION) != null) {

                readKey = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(READPOSITION)).getDataKey().toByteArray();
            }
            byte[] preKey = lastKey;
            Stack<byte[]> stack = new Stack<>();
            stack.push(lastKey);

            // Read the required records from the database and save all the keys
            boolean flag = true;
            if (asString(preKey).equals(asString(readKey))) {
                flag = false;
            }
            while (true) {

                // The value of the map
                byte[] mapValue = preKey;
                byte[] value = LevelDBUtil.getDataDB().get(preKey);

                // A value of null indicates that the data has been tampered with and an error has been thrown
                if (value == null) {
                    try {
                        throw new DataException("Sorry, the database data is abnormal, please check whether the data has been tampered！");
                    } catch (DataException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                DataBean.Data data = DataBean.Data.parseFrom(value);
                preKey = data.getPrevHash().toByteArray();
                if (preKey.length == 0 || preKey == null) {
                    break;
                }
                stack.push(preKey);

                // Save the key of LevelDB database into map
                if (flag) {
                    map.put(Base64.getEncoder().encodeToString(preKey), mapValue);
                }

                // Exclude data that is already on the blockChain
                if (asString(preKey).equals(asString(readKey))) {
                    flag = false;
                }
            }

            // Read the value of the header from the database
            headerValue = LevelDBUtil.getDataDB().get(HEADER_KEY);

            // Check the header values
            while (!stack.isEmpty()) {
                byte[] value = stack.pop();
                DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(value));
                if (startValue == null) {
                    DataBean.Header header = DataBean.Header.newBuilder().setPayload(data.getPayload()).setTs(data.getTs()).setSerial(data.getSerial()).build();

                    startValue = SHA256.getSHA256Bytes(header.toByteArray());
                } else {

                    DataBean.Header header = DataBean.Header.newBuilder().setPayload(data.getPayload()).setTs(data.getTs()).setSerial(data.getSerial()).setPrevHeader(ByteString.copyFrom(startValue)).build();

                    startValue = SHA256.getSHA256Bytes(header.toByteArray());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If the calculated header value is different from what is stored in the database, the data is tampered with and an error is thrown
        if (!Arrays.equals(headerValue, startValue)) {
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
     * @return Map<String, byte[]>
     * The value of the key.
     * map of the data base is the key of the database data, and the key of the map is the key of the last database data
     * @throws IOException
     */
    public static Map<String, byte[]> getHashMap() throws IOException {

        Map<String, byte[]> map = new HashMap<>();
        try {

            // Read the key of the last record from the database
            DataBean.Position position = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(WRITEPOSITION));
            byte[] lastKey = position.getDataKey().toByteArray();

            // Read the key for readPosition from the database
            byte[] readKey = null;
            if (LevelDBUtil.getMataDB().get(READPOSITION) != null) {
                readKey = DataBean.Position.parseFrom(LevelDBUtil.getMataDB().get(READPOSITION)).getDataKey().toByteArray();
            }

            byte[] preKey = lastKey;

            // Read the required records from the database and save all the keys
            boolean flag = true;
            if (asString(readKey).equals(asString(preKey))) {
                flag = false;
            }
            while (flag) {

                // The value of the map
                byte[] mapValue = preKey;
                DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(preKey));
                preKey = data.getPrevHash().toByteArray();

                if (preKey.length == 0 || preKey == null) {
                    break;
                }

                // Save the key of LevelDB database into map
                if (flag) {
                    map.put(Base64.getEncoder().encodeToString(preKey), mapValue);
                }

                // Exclude data that is already on the blockChain
                if (asString(preKey).equals(asString(readKey))) {
                    flag = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
