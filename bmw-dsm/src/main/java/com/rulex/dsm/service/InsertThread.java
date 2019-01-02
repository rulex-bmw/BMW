package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.ConnectionProperties;
import com.rulex.dsm.bean.Source;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class InsertThread extends Thread {

    //上链信息
    public byte[] payload;
    //拦截的数据库表
    public String tableName;
    //拦截条件
    public Source source;
    //查询自增主键where条件
    public List<String> whereValues;

    public InsertThread(Source source, byte[] payload, String tableName, List<String> whereValues) {
        this.source = source;
        this.payload = payload;
        this.tableName = tableName;
        this.whereValues = whereValues;

    }

    @Override
    public synchronized void  run() {
        Object priKey = null;
        try {
            Map<String, String> map = source.getConProperties().getField();

            //创建数据库连接
            Class.forName(map.get("driver"));
            String url = map.get("url");
            String user = map.get("username");
            String password = map.get("password");
            Connection con = DriverManager.getConnection(url, user, password);

            //获得新增数据的自增主键
            String primaryKey = source.getKeys().get(0).getColumn();

            StringBuilder select = new StringBuilder("SELECT " + primaryKey + " FROM " + tableName + " WHERE ");
            for (String app : whereValues) {

                if (whereValues.get(0).equals(app)) {
                    select.append(app);
                } else {
                    select.append(" and " + app);
                }
            }
            select.append(" ORDER BY " + primaryKey + " ASC;");
            PreparedStatement pps = con.prepareStatement(select.toString());
            ResultSet rs = pps.executeQuery();

            while (rs.next()) {
                priKey = rs.getObject(primaryKey);
            }

            //base64处理主键
            String orgPKHash = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(TypeUtils.objectToByte(priKey)));

            con.close();
            if (payload != null) {
                // 调用bsb执行上链，建立主键索引表
                DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                BSBService.producer(data, orgPKHash);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
