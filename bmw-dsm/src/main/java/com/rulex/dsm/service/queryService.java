package com.rulex.dsm.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.bsb.utils.SqliteUtils;
import com.rulex.dsm.bean.Source;

import java.util.List;
import java.util.Map;

public class queryService {


    /**
     * 查询写入区块链的信息
     *
     * @param primaryId 获取参数
     * @return 最新的上链信息
     */
    public static void queryInfo(Object primaryId, List<Source> sources) {

        //按时间倒序查出该主键的所有上链信息
        String sql = "select * from key_indexes where pri_key_hash=? order by ts desc";
        List<Map<String, Object>> mapList = SqliteUtils.query(sql, new Object[]{primaryId});

        //处理查出来的信息，得到最新的上链信息
        if (mapList != null && mapList.size() >= 1) {
            byte[] hash_key = (byte[]) mapList.get(0).get("hash_key");

            try {
                DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(hash_key));
                byte[] playload = data.getPayload().toByteArray();


                DataBean.Alteration alteration = DataBean.Alteration.parseFrom(LevelDBUtil.getDataDB().get(hash_key));
                if (alteration.getOperation().getNumber() == 0) {

                    List<DataBean.FieldValue> values = alteration.getFieldsList();
                }

            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            } finally {
                LevelDBUtil.closeDB();
            }


        }


    }


}
