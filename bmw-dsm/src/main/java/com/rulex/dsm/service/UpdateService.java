package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.SqliteUtils;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Primary;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;

public class UpdateService {

    private UpdateService() {
    }

    public static void credibleUpdate(Update update, Invocation invocation, List<Source> sourceList) {
        try {
            List<Table> tables = update.getTables();
            // 找到拦截目标
            for(Table table : tables) {
                String tablename = table.getName();
                for(Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {

                        List<Map<String, Object>> params = getParamters(update, source);// 获取update所有需要拦截的参数， key is column

                        List<Map<String, Object>> keys = executerSql(invocation, source, tablename, " " + update.getWhere());// 所有主键key, key is column

                        // 是否修改主键
                        boolean b = false;
                        if (params.get(1).size() > 0) b = true;

                        for(Map<String, Object> key : keys) {

                            Map<String, String> orgHash = getOrgHash(getPrimayKey(key, source));// 获取orgPKHash和typeHash

                            if (null == orgHash) continue;// 未找到原始hash，不执行上链

                            if (b) {
                                // 修改主键
                                String sql = "insert into key_indexes (orgPKHash,typeHash,type,ts) values(?,?,?,?);";
                                Object[] obj = {orgHash.get("orgPKHash"), getNewKey(params.get(1), key, source), 2, System.currentTimeMillis()};
                                SqliteUtils.edit(obj, sql);
                            }

                            // 生成payload
                            byte[] payload = generatePayload(params.get(0), orgHash.get("typeHash"), source);

                            // 执行上链
                            BSBService.producer(DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build(), null);

                        }
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
     * @param stmt
     * @param sources
     * @return
     */
    public static boolean decideTointerceptor(Update stmt, List<Source> sources) {
        List<Column> c = stmt.getColumns();
        int count = 0;
        for(Source source : sources) {
            for(Field field : source.getFields()) {
                for(Column column : c) {
                    if (column.getColumnName().equalsIgnoreCase(field.getColumn())) {
                        count++;
                    }
                }
            }
        }
        return count != 0 ? true : false;
    }


    /**
     * 将sql参数赋值
     *
     * @param boundSql
     * @param configuration
     * @return String 赋值后的sql
     */
    public static String getsql(BoundSql boundSql, Configuration configuration) {
        Object parameterObject = boundSql.getParameterObject();  // 获取参数
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");  // sql语句中多个空格都用一个空格代替

        if (null != parameterMappings && parameterMappings.size() > 0) {

            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换　　　　　　　
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));

            } else {

                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for(ParameterMapping parameterMapping : parameterMappings) {

                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {

                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));

                    } else if (boundSql.hasAdditionalParameter(propertyName)) {

                        Object obj = boundSql.getAdditionalParameter(propertyName);  // 该分支是动态sql
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    }
                }
            }
        }
        return sql;
    }


    /**
     * 如果参数是String，则添加单引号， 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
     *
     * @param obj
     * @return 替换sql中？的参数
     */
    private static String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }


    /**
     * 获取所有要拦截的参数和主键
     *
     * @param update jsqlparser解析后的update sql
     * @param source xml中对应的record
     * @return List<Map<String, Object>>    所有拦截的参数，result(0):params result(1):keys
     */
    public static List<Map<String, Object>> getParamters(Update update, Source source) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> primaryKeys = new HashMap<>();

        List<Field> fields = source.getFields();    // 所有上链字段
        List<Primary> keys = source.getKeys();  // primary key(autoincrement or composite keys)
        List<Column> columns = update.getColumns();// 所有列
        List<Expression> expressions = update.getExpressions();// 所有参数

        int size = columns.size();
        for(int i = 0; i < size; i++) {
            String columnName = columns.get(i).getColumnName();

            // 修改后字段
            for(Field field : fields) {
                if (field.getColumn().equalsIgnoreCase(columnName)) {

                    params.put(columnName, getExpressionValue(field.getType(), expressions.get(i)));

                }
            }

            // 修改后的主键
            for(Primary key : keys) {
                if (key.getColumn().equalsIgnoreCase(columnName)) {

                    primaryKeys.put(columnName, getExpressionValue(key.getType(), expressions.get(i)));

                }
            }
        }
        results.add(primaryKeys);
        results.add(params);
        return results;
    }


    /**
     * 获取expression中的value
     *
     * @param type       设定的数据类型
     * @param expression 为转化类型的expression对象
     * @return Object     返回param的值
     */
    public static Object getExpressionValue(String type, Expression expression) throws Exception {

        if (DataTypes.wrapper_Int.getName().equals(type) || DataTypes.primeval_int.getName().equals(type)) {
            return ((LongValue) expression).getValue();
        } else if (DataTypes.wrapper_Long.getName().equals(type) || DataTypes.primeval_long.getName().equals(type)) {
            return ((LongValue) expression).getValue();
        } else if (DataTypes.wrapper_Double.getName().equals(type) || DataTypes.primeval_double.getName().equals(type)) {
            return ((DoubleValue) expression).getValue();
        } else if (DataTypes.wrapper_Float.getName().equals(type) || DataTypes.primeval_float.getName().equals(type)) {
            return ((DoubleValue) expression).getValue();
        } else if (DataTypes.primeval_string.getName().equals(type)) {
            return ((StringValue) expression).getValue();
        } else if (DataTypes.primeval_timestamp.getName().equals(type)) {
            return ((TimestampValue) expression).getValue();
        } else if (DataTypes.primeval_datatime.getName().equals(type)) {
            return ((TimeValue) expression).getValue();
        }
        return null;
    }


    /**
     * 执行查询sql，获取原主键集合(一条或多条数据的主键)
     *
     * @param invocation
     * @param source
     * @param tablename
     * @param where
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> executerSql(Invocation invocation, Source source, String tablename, String where) throws Exception {
        List<Map<String, Object>> primarykeys = new ArrayList<>();
        // 编写查询语句
        String select = "select * from " + tablename + " where " + where;
        // 获取查询结果中，所有的primarykey
        PreparedStatement pps = ((PreparedStatement) invocation.getArgs()[0])
                .getConnection().prepareStatement(select, Statement.RETURN_GENERATED_KEYS);

        ResultSet rs = pps.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();

        // A collection of primary keys for all modified records
        List<Primary> keyList = source.getKeys();
        int size = keyList.size();
        while (rs.next()) {
            Map<String, Object> keys = new HashMap<>();
            int count = metaData.getColumnCount();

            // get all primarykey column
            for(int j = 0; j < count; j++) {
                String cn = metaData.getColumnName(j);

                for(int i = 0; i < size; i++) {
                    String column = keyList.get(i).getColumn();
                    if (cn.equalsIgnoreCase(column)) {
                        keys.put(cn, TypeUtils.objectToByte(rs.getObject(j)));
                    }
                }
            }
            primarykeys.add(keys);
        }
        return primarykeys;
    }


    /**
     * 获取主键
     *
     * @param key    数据库查出的主键
     * @param source
     * @return orgPKHash or typeHash
     */
    public static String getPrimayKey(Map key, Source source) {
        List<Primary> keys = source.getKeys();
        int size = keys.size();
        byte[][] k = new byte[size][];
        for(int i = 0; i < size; i++) {
            k[i] = TypeUtils.objectToByte(key.get(keys.get(i)));
        }
        return Base64.getEncoder().encodeToString(TypeUtils.concatByteArrays(k));
    }


    /**
     * 获取originHash
     *
     * @param hash 当前主键
     * @return typeHash insert时的原始hash
     */
    public static Map<String, String> getOrgHash(String hash) {
        Map<String, String> index = new HashMap<>();
        String sqliteSql = "select typeHash from key_indexes where orgPKHash = ?;";
        List<Map<String, Object>> query = SqliteUtils.query(sqliteSql, new Object[]{hash});

        if (query.size() == 0) {

            // 根据最新keyHash查找orgPKHash
            sqliteSql = "select orgPKHash from key_indexes where typeHash = ?;";
            query = SqliteUtils.query(sqliteSql, new Object[]{hash});

            if (query.size() == 0) return null;// 未找到orgPKHash，返回null

            return getOrgHash((String) query.get(0).get("orgPKHash"));
        } else {

            index.put("orgPKHash", hash);
            index.put("typeHash", (String) query.get(0).get("typeHash"));
            return index;
        }
    }


    /**
     * 获取最新主键
     *
     * @param newkey  修改后的主键值
     * @param formkey 修改前的主键值
     * @param source
     * @return 最新主键
     */
    public static String getNewKey(Map<String, Object> newkey, Map<String, Object> formkey, Source source) {
        List<Primary> keys = source.getKeys();
        int size = keys.size();
        byte[][] k = new byte[size][];
        for(int i = 0; i < size; i++) {
            String column = keys.get(i).getColumn();
            Object o = newkey.get(column);
            if (o == null) {
                k[i] = TypeUtils.objectToByte(formkey.get(column));
            } else {
                k[i] = TypeUtils.objectToByte(o);
            }
        }
        return Base64.getEncoder().encodeToString(TypeUtils.concatByteArrays(k));
    }


    /**
     * 生成payload
     *
     * @param params   所有修改的参数
     * @param typeHash 原始hash
     * @param source
     * @return payload
     */
    public static byte[] generatePayload(Map<String, Object> params, String typeHash, Source source)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        DataBean.Alteration.Builder alteration = DataBean.Alteration.newBuilder();
        List<Field> fields = source.getFields();

        for(Field field : fields) {
            DataBean.FieldValue.Builder f = DataBean.FieldValue.newBuilder();
            Object value = params.get(field.getColumn());

            if (value != null) {
                f.setField(field.getFieldId());
                setParamter(f, value, field.getType()); // 设置value

                alteration.addFields(f.build());
            }
        }

        return alteration.setRecordid(source.getId())
                .setOperation(DataBean.Operation.UPDATE)
                .setOrgHashKey(ByteString.copyFrom(Base64.getDecoder().decode(typeHash)))
                .build().toByteArray();
    }


    /**
     * 反射设置指定数据类型的值
     *
     * @param field
     * @param value
     * @param type
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void setParamter(DataBean.FieldValue.Builder field, Object value, String type)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<? extends DataBean.FieldValue.Builder> aClass = field.getClass();
        Method method = aClass.getMethod("set" + TypeUtils.InitialsLow2Up(type) + "Value", Object.class);
        method.invoke(field, value);
    }

}
