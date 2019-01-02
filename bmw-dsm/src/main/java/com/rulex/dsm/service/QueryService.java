package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import com.rulex.dsm.utils.XmlUtil;

import java.util.*;

public class QueryService {

    /**
     * 查询写入区块链的最新状态信息
     *
     * @param hashKey 上链信息key
     * @return Map<String, Object> 该hashKey对应的上链信息，其中key为allBlockChainValues的value为该块的所有相关信息
     */
    public static Map<String, Object> queryInfo(byte[] hashKey) {

        Map<String, Object> returnMap = new HashMap<>();
        try {

            // 获取该块的相关首块的hashKey
            DataBean.Data data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(hashKey));
            DataBean.Alteration alteration = DataBean.Alteration.parseFrom(data.getPayload().toByteArray());
            ByteString orgHashKey = alteration.getOrgHashKey();
            int recordid = alteration.getRecordid();

            //获取所有与该块相关的payload
            List<byte[]> payloads = new ArrayList();
            Stack<byte[]> payloadStack = new Stack();
            byte[] preHash = data.getPrevHash().toByteArray();

            //该块为新增块，直接添加，无修改记录
            if (orgHashKey.size() == 0) {

                payloads.add(data.getPayload().toByteArray());
                payloadStack.add(data.getPayload().toByteArray());
            } else {

                payloads.add(data.getPayload().toByteArray());
                payloadStack.add(data.getPayload().toByteArray());

                boolean flag = true;
                while (flag) {
                    //此为新增块，添加到payloads，停止循环
                    if (orgHashKey.equals(ByteString.copyFrom(preHash))) {
                        payloads.add(DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(preHash)).getPayload().toByteArray());
                        payloadStack.add(DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(preHash)).getPayload().toByteArray());

                        flag = false;
                    } else {
                        data = DataBean.Data.parseFrom(LevelDBUtil.getDataDB().get(preHash));
                        preHash = data.getPrevHash().toByteArray();

                        // 对比当前数据与所查块的首块hashKey是否相等,若相等保存在payloads中
                        if (orgHashKey.equals(DataBean.Alteration.parseFrom(data.getPayload().toByteArray()).getOrgHashKey())) {

                            payloads.add(data.getPayload().toByteArray());
                            payloadStack.add(data.getPayload().toByteArray());
                        }
                    }
                }

            }

            // 获取上链数据规则
            List<Source> sourceList = XmlUtil.parseXML();

            // 获取所需的上链数据
            sourceOk:
            for (Source source : sourceList) {

                if (source.getId() == recordid) {

                    fieldOk:
                    for (Field field : source.getFields()) {

                        for (byte[] payload : payloads) {

                            DataBean.Alteration altera = DataBean.Alteration.parseFrom(payload);

                            // 如果该对象信息被删除，直接返回
                            if (altera.getOperation().getNumber() == 0) {

                                returnMap.put("0", "该查询对象已被删除");
                                break sourceOk;

                            } else {

                                // 将最新的fieldValue返回
                                for (DataBean.FieldValue fieldValue : altera.getFieldsList()) {

                                    if (fieldValue.getField() == field.getFieldId()) {

                                        returnMap.put(field.getName(), typeHandle(field.getType(), fieldValue));

                                        continue fieldOk;
                                    }
                                }
                            }
                        }
                    }

                }
            }

            // 获取该块所有相关的上链数据
            List<Map<String, Object>> allValues = new ArrayList<>();

            for (Source source : sourceList) {

                if (source.getId() == recordid) {

                    //获取每条上链信息的信息
                    List<byte[]> payList = new ArrayList<>();
                    while (!payloadStack.isEmpty()) {
                        payList.add(payloadStack.pop());
                    }
                    allValues = parsePayload(payList, source);
                }
            }
            returnMap.put("allBlockChainValues", allValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LevelDBUtil.closeDB();
        }

        return returnMap;
    }


    /**
     * 解析payload
     *
     * @param payloads 待解析数据
     * @param source   拦截信息
     * @return List<Map<String, Object>> 解析后的值
     */
    public static List<Map<String, Object>> parsePayload(List<byte[]> payloads, Source source) {
        List<Map<String, Object>> allValues = new ArrayList<>();
        try {
            for (byte[] payload : payloads) {

                DataBean.Alteration altera = DataBean.Alteration.parseFrom(payload);


                Map<String, Object> map = new HashMap<>();

                // 如果该对象信息被删除
                if (altera.getOperation().getNumber() == 0) {

                    map.put("0", "该查询对象已被删除");
                } else {

                    for (DataBean.FieldValue fieldValue : altera.getFieldsList()) {
                        for (Field field : source.getFields()) {

                            if (fieldValue.getField() == field.getFieldId()) {
                                map.put(field.getName(), typeHandle(field.getType(), fieldValue));
                            }
                        }
                    }
                }

                allValues.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allValues;
    }


    /**
     * 获取fieldValue的值
     *
     * @param fieldType  field的类型
     * @param fieldValue 上链信息
     * @return fieldValue的值
     */
    public static Object typeHandle(String fieldType, DataBean.FieldValue fieldValue) {

        if (DataTypes.wrapper_Int.getName().equals(fieldType) || DataTypes.primeval_int.getName().equals(fieldType)) {
            return fieldValue.getIntValue();
        } else if (DataTypes.wrapper_Long.getName().equals(fieldType) || DataTypes.primeval_long.getName().equals(fieldType)) {

            return fieldValue.getLongValue();
        } else if (DataTypes.wrapper_Double.getName().equals(fieldType) || DataTypes.primeval_double.getName().equals(fieldType)) {

            return fieldValue.getDoubleValue();
        } else if (DataTypes.wrapper_Float.getName().equals(fieldType) || DataTypes.primeval_float.getName().equals(fieldType)) {

            return fieldValue.getFloatValue();
        } else if (DataTypes.primeval_string.getName().equals(fieldType)) {

            return fieldValue.getStringValue();
        } else if (DataTypes.primeval_boolean.getName().equals(fieldType)) {

            return fieldValue.getBooleanValue();
        } else if (DataTypes.primeval_ByteString.getName().equals(fieldType)) {

            return fieldValue.getBytesValue();
        }
        return null;
    }


}