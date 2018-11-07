package com.rulex.bsb.dao;


import com.rulex.bsb.utils.DBUtils;
import org.springframework.stereotype.Repository;

@Repository
public class BlockChainDao {


    /**
     * 模拟写入区块链
     *
     * @param key
     * @param payload
     * @return
     */
    public Integer putStatus(byte[] key, byte[] payload) {
        String sql = "insert into bmw_chain (key_hash,payload)values (?,?);";
        Object[] objects = {key, payload};
        int edit = DBUtils.edit(sql, objects);
        return edit;
    }


}
