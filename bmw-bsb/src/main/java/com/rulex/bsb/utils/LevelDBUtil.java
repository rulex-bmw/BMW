package com.rulex.bsb.utils;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

@Repository
public class LevelDBUtil {

    @Resource(name = "jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Connect to the LevelDB database
     */
    public static  DB getDb(String fileName) throws IOException {

        Options options = new Options();
        options.createIfMissing(true);
        DB db = factory.open(new File(fileName), options);

        return db;
    }


    public int setstatus(byte[] key, byte[] value) {
        InputStream hash = TypeUtils.byte2Input(key);
        InputStream payload = TypeUtils.byte2Input(value);
        String sql = "insert into bmw_chain (key_hash,payload)values (?,?);";
        int row = jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setBlob(1, hash);
                preparedStatement.setBlob(2, payload);
            }
        });
        return row;
    }


}
