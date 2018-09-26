package com.eroc.bmw;


import com.eroc.bmw.dao.LevelDBDao;
import com.eroc.bmw.dao.LevelDBDaoImpl;
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

import static org.fusesource.leveldbjni.JniDBFactory.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JfTest {

    /**
     * 验证数据完整性
     */

    @Test
    public void contextLoads() throws IOException {
        Options options = new Options();
        factory.destroy(new File("data"), options);
        factory.destroy(new File("flag"), options);


        LevelDBDao levelDBDao = new LevelDBDaoImpl();

        ByteString flag = ByteString.copyFrom(bytes("11"));
        ByteString param = ByteString.copyFrom(bytes("1234567890"));
        ParamBean.Param build = ParamBean.Param.newBuilder().setFlag(flag).setParam(param).build();
        levelDBDao.set(build);


    }



    @Test
    public void xtLoads() throws IOException {
        Options options = new Options();
        options.createIfMissing(true);
        DB dataDB = null;
        dataDB = factory.open(new File("data"), options);
        DBIterator iterator = dataDB.iterator();
        for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            String s = asString(iterator.peekNext().getKey());
            String asString = asString(iterator.peekNext().getValue());
            System.out.println(s + "--------" + asString);

        }

    }

}
