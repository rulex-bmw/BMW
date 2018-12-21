package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.LevelDBUtil;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Primary;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.mapping.BoundSql;
import sun.misc.BASE64Encoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static com.rulex.bsb.utils.TypeUtils.objectToByte;

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

                    //获取payload和复合主键
                    Map<String, byte[]> payloadKeyMap = createPayload(insert, source, boundSql, column);
                    byte[] payload = payloadKeyMap.get("payload");
                    //获取PrimaryId
                    String orgPKHash = null;
                    if (source.getKeys().size() == 1) {


                    } else {
                        orgPKHash = Base64.getEncoder().encodeToString(payloadKeyMap.get("keys"));
                    }
                    if (payload != null) {
                        //调用bsb执行上链
                        DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                        BSBService.producer(data, orgPKHash);
                        BSBService.Consumer();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成payload和复合主键
     *
     * @param insert      拦截的insert对象
     * @param source      xml解析拦截对象
     * @param boundSql    获取参数
     * @param columnNames 数据库字段名
     * @return Map<String, byte[]> 返回payload值和处理后的复合主键
     */
    public static Map<String, byte[]> createPayload(Insert insert, Source source, BoundSql boundSql, List<String> columnNames) throws Exception {

        //生成payload
        DataBean.Alteration.Builder alteration = DataBean.Alteration.newBuilder();
        alteration.setOperationValue(1);
        alteration.setRecordid(source.getId());

        //处理拦截的数据
        Map<String, List> map = processParam(insert, boundSql, columnNames, source);

        alteration.addAllFields(map.get("Values"));

        //处理复合主键
        byte[] initial = new byte[0];
        int length;
        for (Object key : map.get("Values")) {
            byte[] byteKey = TypeUtils.objectToByte(key);
            length = initial.length;
            initial = Arrays.copyOf(initial, byteKey.length + length);
            System.arraycopy(byteKey, 0, initial, length, byteKey.length);
        }
        byte[] byteKeys = SHA256.getSHA256Bytes(initial);

        //处理返回结果
        Map<String, byte[]> returnMap = new HashMap();
        returnMap.put("payload", alteration.build().toByteArray());
        returnMap.put("keys", byteKeys);
        return returnMap;

    }

    /**
     * 处理Parameter值,获取复合主键的值和payload中的fields的值
     *
     * @param insert      拦截的insert对象
     * @param boundSql    获取参数
     * @param columnNames 数据库字段名
     * @param source      xml解析拦截对象
     * @return map fields和复合主键集合
     */
    public static Map<String, List> processParam(Insert insert, BoundSql boundSql, List<String> columnNames, Source source) throws Exception {

        //fields的集合
        List<DataBean.FieldValue> fieldValues = new ArrayList<>();

        Object parameter = boundSql.getParameterObject();

        //复合主键集合
        List<Primary> primaries = source.getKeys();
        List<Object> primaryKeys = new ArrayList<>(primaries.size());

        Class clazz = parameter.getClass();
        //如果执行sql的方法参数类型是对象

        for (Field field : source.getFields()) {
            for (String columnName : columnNames) {
                //获取上链的column
                if (field.getColumn().equalsIgnoreCase(columnName)) {

                    ExpressionList expression = (ExpressionList) insert.getItemsList();
                    Object expressionValue = expression.getExpressions().get(columnNames.indexOf(columnName));

                    //用对象给sql占位符赋值
                    if (expressionValue.toString().equals("?")) {

                        if (Class.forName(source.getPojo()) == clazz) {

                            String fieldName = TypeUtils.InitialsLow2Up(field.getName());
                            Method method = clazz.getMethod("get" + fieldName);
                            Object value = method.invoke(parameter);

                            fieldValues.add(typeHandle(field.getType(), value, field.getFieldId()));

                            if (primaries.size() != 1) {
                                for (Primary primary : primaries) {
                                    //获取复合主键的其中一项的值
                                    if (columnName.equalsIgnoreCase(primary.getColumn())) {

                                        primaryKeys.set(primaries.indexOf(primary), value);

                                    }
                                }
                            }
                        }
                        //如果执行sql的方法参数类型是map
                        else if (parameter instanceof java.util.HashMap) {

                            String property = boundSql.getParameterMappings().get(columnNames.indexOf(columnName)).getProperty();

                            Map<Object, Object> map = (Map<Object, Object>) parameter;

                            fieldValues.add(typeHandle(field.getType(), map.get(property), field.getFieldId()));

                            if (primaries.size() != 1) {
                                for (Primary primary : primaries) {
                                    //获取复合主键的其中一项的值
                                    if (columnName.equalsIgnoreCase(primary.getColumn())) {

                                        primaryKeys.set(primaries.indexOf(primary), map.get(property));

                                    }
                                }
                            }
                        }
                    }
                    //直接是在sql中定义值
                    else {
                        fieldValues.add(typeHandle(field.getType(), expressionValue, field.getFieldId()));

                        if (primaries.size() != 1) {
                            for (Primary primary : primaries) {
                                //获取复合主键的其中一项的值
                                if (columnName.equalsIgnoreCase(primary.getColumn())) {

                                    primaryKeys.set(primaries.indexOf(primary), expressionValue);

                                }
                            }
                        }
                    }
                }
            }
        }

        //处理返回结果
        Map<String, List> returnMap = new HashMap();
        returnMap.put("keys", primaryKeys);
        returnMap.put("Values", fieldValues);
        return returnMap;

    }

    /**
     * 设置fieldValue的Builder对象的属性
     *
     * @param fieldType field的type
     * @param value     Builder对象的属性值
     * @return DataBean.FieldValue 处理后的Builder对象
     */
    public static DataBean.FieldValue typeHandle(String fieldType, Object value, int fieldId) {

        DataBean.FieldValue.Builder fieldValue = DataBean.FieldValue.newBuilder();
        if (DataTypes.wrapper_Int.getName().equals(fieldType) || DataTypes.primeval_int.getName().equals(fieldType)) {
            return fieldValue.setIntValue((int) value).setField(fieldId).build();
        } else if (DataTypes.wrapper_Long.getName().equals(fieldType) || DataTypes.primeval_long.getName().equals(fieldType)) {

            return fieldValue.setLongValue((long) value).setField(fieldId).build();
        } else if (DataTypes.wrapper_Double.getName().equals(fieldType) || DataTypes.primeval_double.getName().equals(fieldType)) {

            return fieldValue.setDoubleValue((double) value).setField(fieldId).build();
        } else if (DataTypes.wrapper_Float.getName().equals(fieldType) || DataTypes.primeval_float.getName().equals(fieldType)) {

            return fieldValue.setFloatValue((float) value).setField(fieldId).build();
        } else if (DataTypes.primeval_string.getName().equals(fieldType)) {

            return fieldValue.setStrValue((String) value).setField(fieldId).build();
        } else if (DataTypes.primeval_boolean.getName().equals(fieldType)) {

            return fieldValue.setBoolValue((boolean) value).setField(fieldId).build();
        } else if (DataTypes.primeval_ByteString.getName().equals(fieldType)) {

            return fieldValue.setBytesValue(ByteString.copyFrom((byte[]) value)).setField(fieldId).build();
        }
        return null;
    }
}
