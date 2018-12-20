package com.rulex.dsm.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.bsb.utils.SqliteUtils;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.utils.XmlUtil;
import com.sun.javafx.collections.MappingChange;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryService {

    /**
     * 查询写入区块链的信息
     *
     * @param hashKey 上链信息key
     * @return 该key对应的最新上链信息
     */
    public static void queryInfo(byte[] hashKey) {
        try {

            DataBean.Data data = DataBean.Data.parseFrom( LevelDBUtil.getDataDB().get(hashKey));

            byte[] payload = data.getPayload().toByteArray();







//
//            //按时间倒序查出该主键的所有上链信息
//            String sql = "select * from key_indexes where pri_key_hash=? order by ts desc";
//            List<Map<String, Object>> mapList = SqliteUtils.query(sql, new Object[]{primaryId});
//
//            //获取上链数据规则
//            List<Source> sourceList = XmlUtil.parseXML();
//
//            Source  source =new Source();
//            for (Source sour : sourceList) {
//                if (sour.getTable().equals(tableName)) {
//                    source=sour;
//                }
//            }
//
//            //获取insert时的数据
//            if (mapList != null && mapList.size() >= 1) {
//                //获取insert时的payload
//                byte[] hash_key = (byte[]) mapList.get(0).get("hash_key");
//
//
//
//                Class clazz = Class.forName("com.rulex.tools.pojo.RulexBean");
//                //反射获取RulexBean的内部类
//                Class innerClazz[] = clazz.getDeclaredClasses();
//                for (Class Class : innerClazz) {
//                    //获取表对应的内部类
//                    if (Class.getSimpleName().equals(TypeUtils.InitialsLow2Up(source.getName()))) {
//
//                        //执行当前message类的parseFrom方法
//                        Method parseFrom = Class.getMethod("parseFrom", byte[].class);
//                        Constructor con = Class.getDeclaredConstructor();
//                        con.setAccessible(true);
//                        Object message = parseFrom.invoke(con.newInstance(), playload);
//
//                    }
//                }
//            }
//
//            DataBean.Alteration alteration =null;
//            Map<String,String> retMap=new HashMap();
//            //获取update后的数据
//            for (Field field:source.getFields()){
//                for (Map<String, Object> map: mapList) {
//
//
//                    for (Map.Entry<String, Object> entry : map.entrySet()) {
//
//                        alteration = DataBean.Alteration.parseFrom(TypeUtils.objectToByte(entry.getValue()));
//
//                        if (alteration.getOperation().getNumber() == 0) {
//
//                            map.put("0", "该查询对象已被删除");
//
//                        }else {






//                        }
//
//
//                    }
//                }}

//
//                field.getFieldId
//
//
//                if (alteration.getOperation().getNumber() == 0) {
//
//                    map.put("0","该查询对象已被删除");
//
//            }
//                List<DataBean.FieldValue> values = alteration.getFieldsList();



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LevelDBUtil.closeDB();
        }







                }




        //处理查出来的信息，得到最新的上链信息



//        }





}


