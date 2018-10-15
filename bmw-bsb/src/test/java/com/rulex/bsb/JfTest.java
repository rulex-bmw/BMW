package com.rulex.bsb;


import com.google.protobuf.ByteString;
import com.rulex.bsb.dao.LevelDBDaoImpl;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.service.BSBServiceImpl;
import com.rulex.bsb.utils.LevelDBUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JfTest {

    /**
     * 验证数据完整性
     */

    @Test
    public void contextLoads() throws IOException {
//        Options options = new Options();
//        factory.destroy(new File("data"), options);
//        factory.destroy(new File("mata"), options);


        BSBService bsbService = new BSBServiceImpl();
        ByteString flag = ByteString.copyFrom(bytes("4"));
        ByteString param = ByteString.copyFrom(bytes("eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
        DataBean.Data build = DataBean.Data.newBuilder().setFlag(flag).setParam(param).build();
        bsbService.producer(build);


    }


    @Test
    public void xtLoads() throws IOException {
        //查询数据
        Options options = new Options();
        options.createIfMissing(true);
        DB dataDB = null;
        dataDB = factory.open(new File("data"), options);
        DBIterator iterator = dataDB.iterator();
        String s = null;
        String asString = null;
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            s = asString(iterator.peekNext().getKey());
            if (asString(iterator.peekNext().getKey()).equalsIgnoreCase(asString(LevelDBDaoImpl.HEADER_KEY))) {
                asString = asString(iterator.peekNext().getValue());
            } else {
                DataBean.Data data = DataBean.Data.parseFrom(iterator.peekNext().getValue());
                asString = data.toString();
            }
            System.out.println(s);
            System.out.println("--------------------------------------------------------------------------------------------------------------------");
        }
        iterator.close();
        dataDB.close();

    }


    @Test
    public void contextds() throws IOException {

        DB db = LevelDBUtil.getDb("mata");
        DataBean.Data build = DataBean.Data.newBuilder().setPrevHash(ByteString.copyFrom(bytes("da29cd7d95e264d9da2bc8dec0c12d14f734465b9c022858066008eb214ebc60"))).build();

        db.put(bytes("readPosition"), build.toByteArray());
        DataBean.Data hash = DataBean.Data.parseFrom(db.get(bytes("readPosition")));
        System.out.println(hash.toString());

        db.close();


    }

    @Test
    public void tds() throws IOException {
        Iterator<Map.Entry<String, String>> entries = LevelDBDaoImpl.getHashMap().entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();

            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

        }
    }
}
