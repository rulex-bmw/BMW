package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Primary;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import org.apache.ibatis.mapping.BoundSql;

import java.lang.reflect.Method;
import java.util.*;

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

                    // 获取payload和复合主键
                    Map<String, Object> payloadKeyMap = createPayload(insert, source, boundSql, column);
                    byte[] payload = (byte[]) payloadKeyMap.get("payload");
                    System.out.println(Base64.getEncoder().encodeToString(payload));
                    if (payload != null) {
                        // 获取PrimaryKey
                        if (source.getKeys().size() == 1 && source.getKeys().get(0).getIsAuto()) {

                            Thread insertThread = new InsertThread(source, payload, tableName, (List<String>) payloadKeyMap.get("where"));

                            insertThread.start();

                        } else {
                            String orgPKHash = Base64.getEncoder().encodeToString((byte[]) payloadKeyMap.get("keys"));

                            // 调用bsb执行上链
                            DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                            BSBService.producer(data, orgPKHash);

                        }
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
    public static Map<String, Object> createPayload(Insert insert, Source source, BoundSql boundSql, List<String> columnNames) throws Exception {

        // 生成payload
        DataBean.Alteration.Builder alteration = DataBean.Alteration.newBuilder();
//        alteration.setOperationValue(1);
        alteration.setRecordid(source.getId());

        // 处理返回结果
        Map<String, Object> returnMap = new HashMap();

        // 处理拦截的数据
        Map<String, List> map = processParam(insert, boundSql, columnNames, source);
        if (map.get("values").size() != 0) {
            alteration.addAllFields(map.get("values"));
            returnMap.put("payload", alteration.build().toByteArray());

            if ((source.getKeys().size() == 1 && source.getKeys().get(0).getIsAuto())) {
                returnMap.put("where", map.get("where"));
            } else {

                List<Object> keys = primaryKeyHandle(insert, boundSql, columnNames, source);

                // 处理复合主键

                byte[] initial = new byte[0];
                int length;
                for (Object key : keys) {
                    byte[] byteKey = TypeUtils.objectToByte(key);
                    length = initial.length;
                    initial = Arrays.copyOf(initial, byteKey.length + length);
                    System.arraycopy(byteKey, 0, initial, length, byteKey.length);
                }
                byte[] byteKeys = SHA256.getSHA256Bytes(initial);
                returnMap.put("keys", byteKeys);
            }

        } else {
            returnMap.put("payload", null);
        }
        return returnMap;

    }

    /**
     * 处理Parameter值,获取payload中的fields的值和查询自增主键的where条件值
     *
     * @param insert      拦截的insert对象
     * @param boundSql    获取参数
     * @param columnNames 数据库字段名
     * @param source      xml解析拦截对象
     * @return map fields和自增主键的where条件值集合
     */
    public static Map<String, List> processParam(Insert insert, BoundSql boundSql, List<String> columnNames, Source source) throws Exception {

        // fields的集合
        List<DataBean.FieldValue> fieldValues = new ArrayList<>();

        // 数据库where查询条件的集合
        List<String> whereValues = new ArrayList<>();
        List<Primary> primaries = source.getKeys();

        Object parameter = boundSql.getParameterObject();
        Class clazz = parameter.getClass();

        int index = -1;
        for (String columnName : columnNames) {

            //处理ParameterMappings下标
            ExpressionList expression = (ExpressionList) insert.getItemsList();
            Object expressionValue = expression.getExpressions().get(columnNames.indexOf(columnName));
            if (expressionValue.toString().equals("?")) {
                index = index + 1;
            }

            for (Field field : source.getFields()) {
                // 获取上链的column
                if (field.getColumn().equalsIgnoreCase(columnName)) {

                    // 用对象给sql占位符赋值
                    if (expressionValue.toString().equals("?")) {

                        if (Class.forName(source.getPojo()) == clazz) {

                            String fieldName = TypeUtils.InitialsLow2Up(field.getName());
                            Method method = clazz.getMethod("get" + fieldName);
                            Object value = method.invoke(parameter);

                            Map<String, Object> map = typeHandle(field.getType(), value, field.getFieldId());
                            fieldValues.add((DataBean.FieldValue) map.get("FieldValue"));

                            if ((primaries.size() == 1 && primaries.get(0).getIsAuto())) {
                                //自增主键查数据库的条件
                                if (value instanceof String) {
                                    whereValues.add(columnName + "='" + value + "'");
                                } else {
                                    whereValues.add(columnName + "=" + value.toString());
                                }
                            }
                        }
                        // 如果执行sql的方法参数类型是map
                        else if (parameter instanceof java.util.HashMap) {

                            String property = boundSql.getParameterMappings().get(index).getProperty();

                            Map<Object, Object> map = (Map<Object, Object>) parameter;

                            Map<String, Object> retMap = typeHandle(field.getType(), map.get(property), field.getFieldId());
                            fieldValues.add((DataBean.FieldValue) retMap.get("FieldValue"));

                            if ((primaries.size() == 1 && primaries.get(0).getIsAuto())) {
                                //自增主键查数据库的条件
                                if (retMap.get("value") instanceof String) {
                                    whereValues.add(columnName + "='" + retMap.get("value") + "'");
                                } else {
                                    whereValues.add(columnName + "=" + retMap.get("value").toString());
                                }
                            }
                        }
                    }
                    // 直接是在sql中定义值
                    else {
                        Map<String, Object> map = typeHandle(field.getType(), expressionValue, field.getFieldId());

                        fieldValues.add((DataBean.FieldValue) map.get("FieldValue"));

                        if ((primaries.size() == 1 && primaries.get(0).getIsAuto())) {
                            //自增主键查数据库的条件
                            whereValues.add(columnName + "=" + expressionValue);
                        }
                    }
                }
            }
        }

        // 处理返回结果
        Map<String, List> returnMap = new HashMap();
        returnMap.put("values", fieldValues);
        returnMap.put("where", whereValues);
        return returnMap;

    }


    /**
     * 处理Parameter值,获取复合主键的值
     *
     * @param insert      拦截的insert对象
     * @param boundSql    获取参数
     * @param columnNames 数据库字段名
     * @param source      xml解析拦截对象
     * @return List<Object> 复合主键集合
     */
    public static List<Object> primaryKeyHandle(Insert insert, BoundSql boundSql, List<String> columnNames, Source source) throws Exception {

        Object parameter = boundSql.getParameterObject();

        // 复合主键集合
        List<Primary> primaries = source.getKeys();
        List<Object> primaryKeys = new ArrayList<>();

        for (Primary primary : primaries) {
            primaryKeys.add(primary);
        }

        Class clazz = parameter.getClass();

        int index = -1;
        for (String columnName : columnNames) {

            //处理ParameterMappings下标
            ExpressionList expression = (ExpressionList) insert.getItemsList();
            Object expressionValue = expression.getExpressions().get(columnNames.indexOf(columnName));
            if (expressionValue.toString().equals("?")) {
                index = index + 1;
            }

            for (Primary primary : primaries) {
                // 获取复合主键的值
                if (columnName.equalsIgnoreCase(primary.getColumn())) {

                    // 用对象给sql占位符赋值
                    if (expressionValue.toString().equals("?")) {

                        if (Class.forName(source.getPojo()) == clazz) {

                            String fieldName = TypeUtils.InitialsLow2Up(primary.getName());
                            Method method = clazz.getMethod("get" + fieldName);
                            Object value = method.invoke(parameter);

                            primaryKeys.set(primaries.indexOf(primary), value);
                        }
                    }

                    // 如果执行sql的方法参数类型是map
                    else if (parameter instanceof java.util.HashMap) {

                        String property = boundSql.getParameterMappings().get(index).getProperty();

                        Map<Object, Object> map = (Map<Object, Object>) parameter;

                        Object value = typeHandle(primary.getType(), map.get(property));


                        primaryKeys.set(primaries.indexOf(primary), value);

                    }

                    // 直接是在sql中定义值
                    else {
                        Object value = typeHandle(primary.getType(), expressionValue);

                        primaryKeys.set(primaries.indexOf(primary), value);
                    }

                }
            }
        }

        return primaryKeys;

    }


    /**
     * 设置fieldValue的Builder对象的属性
     *
     * @param fieldType field的type
     * @param value     Builder对象的属性值
     * @return Map<String,Object> 处理后的Builder对象和value值
     */
    public static Map<String, Object> typeHandle(String fieldType, Object value, int fieldId) {

        DataBean.FieldValue.Builder fieldValue = DataBean.FieldValue.newBuilder().setField(fieldId);

        Map<String, Object> map = new HashMap();

        if (DataTypes.wrapper_Int.getName().equals(fieldType) || DataTypes.primeval_int.getName().equals(fieldType)) {
            if (value instanceof LongValue) {
                map.put("FieldValue", fieldValue.setIntValue((int) (((LongValue) value).getValue())).build());
                map.put("value", (int) ((LongValue) value).getValue());
                return map;
            }

            map.put("FieldValue", fieldValue.setIntValue((int) value).build());
            map.put("value", value);
            return map;
        } else if (DataTypes.wrapper_Long.getName().equals(fieldType) || DataTypes.primeval_long.getName().equals(fieldType)) {
            if (value instanceof LongValue) {
                map.put("FieldValue", fieldValue.setLongValue(((LongValue) value).getValue()).build());
                map.put("value", ((LongValue) value).getValue());
                return map;
            }
            map.put("FieldValue", fieldValue.setLongValue((long) value).build());
            map.put("value", value);
            return map;
        } else if (DataTypes.wrapper_Double.getName().equals(fieldType) || DataTypes.primeval_double.getName().equals(fieldType)) {
            if (value instanceof DoubleValue) {
                map.put("FieldValue", fieldValue.setDoubleValue(((DoubleValue) value).getValue()).build());
                map.put("value", ((DoubleValue) value).getValue());
                return map;
            }
            map.put("FieldValue", fieldValue.setDoubleValue((double) value).build());
            map.put("value", value);
            return map;
        } else if (DataTypes.wrapper_Float.getName().equals(fieldType) || DataTypes.primeval_float.getName().equals(fieldType)) {
            map.put("FieldValue", fieldValue.setFloatValue((float) value).build());
            map.put("value", value);
            return map;
        } else if (DataTypes.primeval_string.getName().equals(fieldType)) {
            if (value instanceof StringValue) {
                map.put("FieldValue", fieldValue.setStringValue(((StringValue) value).getValue()).build());
                map.put("value", ((StringValue) value).getValue());
                return map;
            }
            map.put("FieldValue", fieldValue.setStringValue((String) value).build());
            map.put("value", value);
            return map;

        } else if (DataTypes.primeval_boolean.getName().equals(fieldType)) {
            map.put("FieldValue", fieldValue.setBooleanValue((boolean) value).build());
            map.put("value", value);
            return map;
        } else if (DataTypes.primeval_ByteString.getName().equals(fieldType)) {
            map.put("FieldValue", fieldValue.setBytesValue(ByteString.copyFrom((byte[]) value)).build());
            map.put("value", value);
            return map;
        }
        return null;
    }


    /**
     * 设置复合主键的值
     *
     * @param type  primaryKey的type
     * @param value primaryKey的值
     * @return Object 处理后的primaryKey的值
     */
    public static Object typeHandle(String type, Object value) {

        if (DataTypes.wrapper_Int.getName().equals(type) || DataTypes.primeval_int.getName().equals(type)) {
            if (value instanceof LongValue) {
                return (int) ((LongValue) value).getValue();
            }
            return value;
        } else if (DataTypes.wrapper_Long.getName().equals(type) || DataTypes.primeval_long.getName().equals(type)) {
            if (value instanceof LongValue) {
                return ((LongValue) value).getValue();
            }
            return value;
        } else if (DataTypes.wrapper_Double.getName().equals(type) || DataTypes.primeval_double.getName().equals(type)) {
            if (value instanceof DoubleValue) {
                return ((DoubleValue) value).getValue();
            }
            return value;
        } else if (DataTypes.wrapper_Float.getName().equals(type) || DataTypes.primeval_float.getName().equals(type)) {
            if (value instanceof DoubleValue) {
                return (float) ((DoubleValue) value).getValue();
            }
            return value;
        } else if (DataTypes.primeval_string.getName().equals(type)) {
            if (value instanceof StringValue) {
                return ((StringValue) value).getValue();
            }
            return value;
        } else if (DataTypes.primeval_boolean.getName().equals(type)) {
            return value;
        } else if (DataTypes.primeval_ByteString.getName().equals(type)) {
            return value;
        }
        return null;
    }


}
