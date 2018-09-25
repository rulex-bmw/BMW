package com.eroc.bmw.dao;

import com.eroc.bmw.pojo.DataBean;
import com.eroc.bmw.pojo.HeaderBean;
import com.eroc.bmw.pojo.ParamBean;
import com.eroc.bmw.pojo.RecordBean;
import com.eroc.bmw.util.DataException;
import com.eroc.bmw.util.SHA256;
import com.google.protobuf.ByteString;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteOptions;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

import static org.fusesource.leveldbjni.JniDBFactory.*;

/**
 * Data persistence layer
 *
 * @author admin
 */
public class LevelDBDaoImpl implements LevelDBDao {

    public static final String DATA_PATH = "data";
    public static final String FLAG_PATH = "flag";
    public static final byte[] HEADER_KEY = bytes("000000");
    private byte[] prev_hash = null;

    /**
     * setup connection
     */
    private Options getOptions() {
        Options options = new Options();
        options.createIfMissing(true);
        return options;
    }


    /**
     * 1、Calculate headerValue and save key='000000', headerValue=SHA256 (record, Vn-1).
     * 2、Protocal buffer serializer value, calculate key=HASH (value), save records, value=param|Ts|prev_hash
     * 3、save flag
     *
     * @param param
     * @throws IOException
     */
    public synchronized void set(ParamBean.Param param) throws IOException {
        Options options = null;
        options = getOptions();
        DB dataDB = null;
        DB flagDB = null;
        DBIterator iterator = null;
        try {
            dataDB = factory.open(new File(DATA_PATH), options);
            flagDB = factory.open(new File(FLAG_PATH), options);
            //generation timestamp
            byte[] ts = bytes(new DateTime().toString("yyyyMMddHHmmssSSSS"));
            ByteString timestamp = ByteString.copyFrom(ts);
            ByteString p = param.getParam();
            ByteString f = param.getFlag();
            //Set up herderVelue
            RecordBean.Record record = RecordBean.Record.newBuilder().setParam(p).setTs(timestamp).build();
            setHeaderData(record, dataDB);
            //seek key=HASH(value)
            // Take a hash of data
            DataBean.Data data = null;
            if (prev_hash == null) {
                iterator = dataDB.iterator();
                for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    if (!asString(iterator.peekNext().getKey()).equalsIgnoreCase(asString(HEADER_KEY))) {
                        prev_hash = iterator.peekNext().getKey();
                    }
                }
                iterator.close();
            }
            //Save a data
            if (prev_hash == null) {
                prev_hash = bytes(SHA256.getSHA256(record.toString()));
                dataDB.put(prev_hash, record.toByteArray());
            } else {
                data = DataBean.Data.newBuilder().setParam(p).setTs(timestamp).setPrevHash(ByteString.copyFrom(prev_hash)).build();
                prev_hash = bytes(SHA256.getSHA256(data.toString()));
                dataDB.put(prev_hash, data.toByteArray());
            }
            //save flag
            flagDB.put(prev_hash, f.toByteArray());
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

    public void setHeaderData(RecordBean.Record record, DB db) {
        byte[] vn_1 = db.get(HEADER_KEY);
        if (vn_1 != null) {
            HeaderBean.Header build = HeaderBean.Header.newBuilder().setParam(record.getParam()).setTs(record.getTs()).setVn1(ByteString.copyFrom(vn_1)).build();
            db.put(HEADER_KEY, bytes(SHA256.getSHA256(build.toString())));
        } else {
            String sha256 = SHA256.getSHA256(record.toString());
            db.put(HEADER_KEY, bytes(sha256));
        }
    }


    /**
     * Connect to the database
     */
    public DB getDb(String fileName) {

        Options options = getOptions();
        DB db = null;
        try {
            db = factory.open(new File(fileName), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    /**
     * Verify the offset structure header
     *
     * @throws IOException
     */
    public void verifyHeaderData() throws IOException {

        Options options = null;
        options = getOptions();
        DB db = null;
        DBIterator iterator = null;
        String vi = null;
        String value = null;
        try {
            db = factory.open(new File(DATA_PATH), options);
            iterator = db.iterator();
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {

                //get the first record from the database with the key of "000000".
                if (asString(iterator.peekNext().getKey()).equals(asString(HEADER_KEY))) {

                    vi = asString(iterator.peekNext().getValue());

                } else {

                    DataBean.Data data = DataBean.Data.parseFrom(iterator.peekNext().getValue());

                    RecordBean.Record.Builder recordBuilder = RecordBean.Record.newBuilder();
                    recordBuilder.setParam(data.getParam());
                    recordBuilder.setTs(data.getTs());

                    RecordBean.Record record = recordBuilder.build();
                    //Superposition record value.
                    if (value == null) {

                        value = SHA256.getSHA256(record.toString());
                    } else {
                        value = SHA256.getSHA256(record.toString() + value);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Make sure you close the db.
            iterator.close();
            db.close();
        }
        // Compare the calculated headerValue with the one in the database. If not,the data is modified and an exception is thrown
        if (!value.equals(vi)) {
            try {
                throw new DataException("Sorry, the database data is abnormal, please check whether the data has been tampered！");
            } catch (DataException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Verify that headerValue is correct.");
        }
    }
}
