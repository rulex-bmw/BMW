package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.mapping.BoundSql;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InsertService {


    /**
     * 拦截新增信息并写入区块链
     *
     * @param insert     拦截的insert对象
     * @param boundSql   获取sql参数
     * @param sourceList xml解析拦截规则
     */
    public static void credibleInsert(Insert insert, BoundSql boundSql, List<Source> sourceList) {

        try {
            String tableName = insert.getTable().getName();
            List<String> column = new ArrayList<>();
            for (Column c : insert.getColumns()) {
                column.add(c.getColumnName());
            }
            for (Source source : sourceList) {
                if (source.getTable().equalsIgnoreCase(tableName)) {
                    //获取payload
                    byte[] payload = createPayload(source, boundSql, column);
                    //获取PrimaryId
                    Object PrimaryId = null;
                    byte[] hashPrimaryId = SHA256.getSHA256Bytes(TypeUtils.objectToByte(PrimaryId));

                    if (payload != null) {
                        //调用bsb执行上链
                        DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                        BSBService.producer(data, hashPrimaryId);
                        BSBService.Consumer();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成payload
     *
     * @param source      xml解析拦截对象
     * @param boundSql    获取参数
     * @param columnNames 数据库字段名
     * @return byte[] 返回payload值
     */
    public static byte[] createPayload(Source source, BoundSql boundSql, List<String> columnNames) throws Exception {

        //生成payload
        DataBean.Alteration.Builder alteration = DataBean.Alteration.newBuilder();
        alteration.setOperationValue(1);
        alteration.setRecordid(source.getId());

        //处理拦截的数据
        Object parameter = boundSql.getParameterObject();
        List<DataBean.FieldValue> fieldValues = processParam(parameter, columnNames, source);

        alteration.addAllFields(fieldValues);

        return alteration.build().toByteArray();

    }

    /**
     * 处理Parameter值
     *
     * @param parameter     sql参数
     * @param columnNames  数据库字段名
     * @param source       xml解析拦截对象
     * @return Object 处理后的Builder对象
     */
    public static List<DataBean.FieldValue> processParam(Object parameter, List<String> columnNames, Source source) throws Exception {

        List<DataBean.FieldValue> fieldValues = new ArrayList<>();

        //如果执行sql的方法参数类型是对象
        if (Class.forName(source.getPojo()) == parameter.getClass()) {

            for (Field field : source.getFields()) {
                for (String columnName : columnNames) {


                    if (field.getColumn().equalsIgnoreCase(columnName)) {


                        DataBean.FieldValue fieldValue = null;

                        fieldValues.add(fieldValue);

                    }
                }
            }

            return fieldValues;
            //如果执行sql的方法参数类型是map
        } else if (parameter instanceof java.util.HashMap) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * 反射设置Builder对象的属性
     *
     * @param field        Field对象
     * @param fieldName    field的name值
     * @param value        Builder对象的属性值
     * @param builderClass Builder类
     * @param builder      Builder对象
     * @return Object 处理后的Builder对象
     */
    public static DataBean.FieldValue typeHandle(String fieldType, Object value) {

        DataBean.FieldValue.Builder fieldValue = DataBean.FieldValue.newBuilder();
        if (DataTypes.wrapper_Int.getName().equals(fieldType) || DataTypes.primeval_int.getName().equals(fieldType)) {
            return fieldValue.setIntValue((int) value).build();
        } else if (DataTypes.wrapper_Long.getName().equals(fieldType) || DataTypes.primeval_long.getName().equals(fieldType)) {

            return fieldValue.setLongValue((long) value).build();
        } else if (DataTypes.wrapper_Double.getName().equals(fieldType) || DataTypes.primeval_double.getName().equals(fieldType)) {

            return fieldValue.setDoubleValue((double) value).build();
        } else if (DataTypes.wrapper_Float.getName().equals(fieldType) || DataTypes.primeval_float.getName().equals(fieldType)) {

            return fieldValue.setFloatValue((float) value).build();
        } else if (DataTypes.primeval_string.getName().equals(fieldType)) {

            return fieldValue.setStrValue((String) value).build();
        } else if (DataTypes.primeval_boolean.getName().equals(fieldType)) {

            return fieldValue.setBoolValue((boolean) value).build();
        } else if (DataTypes.primeval_ByteString.getName().equals(fieldType)) {

            return fieldValue.setBytesValue(ByteString.copyFrom((byte[]) value)).build();
        }
        return null;
    }


}
