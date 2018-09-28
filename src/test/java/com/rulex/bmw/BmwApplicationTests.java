package com.rulex.bmw;

import com.rulex.bmw.dao.LevelDBDaoImpl;
import com.rulex.bmw.pojo.DataBean;
import com.rulex.bmw.service.BSBService;
import com.rulex.bmw.service.BSBServiceImpl;
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
public class BmwApplicationTests {

    @Test
    public void contextLoads() throws IOException {
//        Verify verify = new Verify();
        //存入levelDB
        BSBService bsbService = new BSBServiceImpl();
        ByteString flag = ByteString.copyFrom(bytes("1"));
        ByteString param = ByteString.copyFrom(bytes("6b4323334fce19d6b804eff54325747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
        DataBean.Data build = DataBean.Data.newBuilder().setFlag(flag).setParam(param).build();
        bsbService.producer(build);
        //查询数据
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

    }


}
