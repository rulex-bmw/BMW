package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.SqliteUtils;
import com.rulex.dsm.bean.Source;
import net.sf.jsqlparser.statement.delete.Delete;
import org.apache.ibatis.plugin.Invocation;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DelService {

    private DelService() {
    }


    /**
     * 标记数据已删除
     *
     * @param delete
     * @param invocation
     * @param sourceList
     */
    public static void credibleDel(Delete delete, Invocation invocation, List<Source> sourceList) {
        try {
            String table = delete.getTable().getName();
            // 找到拦截目标
            for(Source source : sourceList) {
                if (source.getTable().equalsIgnoreCase(table)) {

                    List<Map<String, Object>> keys = UpdateService.executerSql(invocation, source, table, " " + delete.getWhere());// 所有主键key, key is column

                    for(Map<String, Object> key : keys) {

                        Map<String, String> orgHash = UpdateService.getOrgHash(UpdateService.getPrimayKey(key, source));// 获取 orgPKHash 和 typeHash

                        if (null == orgHash) continue;// 未找到原始hash，不执行上链

                        delIndexes(orgHash.get("orgPKHash"));// 删除索引

                        // 生成payload
                        byte[] payload = DataBean.Alteration.newBuilder().setOperation(DataBean.Operation.DELETE)
                                .setOrgHashKey(ByteString.copyFrom(Base64.getDecoder().decode(orgHash.get("typeHash"))))
                                .setRecordid(source.getId())
                                .build().toByteArray();

                        // 执行上链
                        BSBService.producer(DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build(), null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断是否需要拦截
     *
     * @param delete
     * @param sources
     * @return
     */
    public static boolean decideTointerceptor(Delete delete, List<Source> sources) {
        String name = delete.getTable().getName();

        for(Source source : sources) {
            if (source.getTable().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 删除数据索引
     *
     * @param orgPKHash
     */
    public static void delIndexes(String orgPKHash) {
        String sql = "delete from key_indexes where orgPKHash = ?";
        Object[] obj = {orgPKHash};
        SqliteUtils.edit(obj, sql);
    }


}
