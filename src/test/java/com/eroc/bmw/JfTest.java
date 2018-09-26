package com.eroc.bmw;


import com.eroc.bmw.dao.LevelDBDao;
import com.eroc.bmw.dao.LevelDBDaoImpl;
import com.google.protobuf.ByteString;
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

        ByteString flag = ByteString.copyFrom(bytes("1"));
        ByteString param = ByteString.copyFrom(bytes("1111b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b"));
        ParamBean.Param build = ParamBean.Param.newBuilder().setFlag(flag).setParam(param).build();
        levelDBDao.set(build);






    }

}
