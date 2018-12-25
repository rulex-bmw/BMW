package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;

import java.sql.Connection;
import java.util.Base64;

public class InsertThread extends Thread {


    public Connection connection;
    public byte[] payload;

    public InsertThread(Connection connection, byte[] payload) {
        this.connection = connection;
        this.payload = payload;

    }

    @Override
    public void run() {
        // 编写查询语句
        try {
            //等待新增数据完成
            Thread.sleep(15);

//            //获得新增数据的自增主键
//            String select = "SELECT LAST_INSERT_ID();";
//            PreparedStatement pps = connection.prepareStatement(select);
//            ResultSet rs = pps.executeQuery();

            Object priKey = 12;

//            while (rs.next()) {
//                priKey = rs.getObject(1);
//            }
            System.out.println(priKey+"------------------------------");
            String orgPKHash = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(TypeUtils.objectToByte(priKey)));
            if (payload != null) {
                // 调用bsb执行上链
                DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                BSBService.producer(data, orgPKHash);
                BSBService.Consumer();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
