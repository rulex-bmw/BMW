package com.rulex.dsm.service;

import com.rulex.bsb.utils.SHA256;
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
import org.apache.ibatis.plugin.Invocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

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
                        // 获取update 所有需要拦截的参数
                        List<Map<String, Object>> params = getParamters(update, source);// 所有拦截的参数，result(0):params result(1):keys
                        if (params.size() > 0) {

                            // 编写查询语句
                            String select = "selce * from " + tablename + " where " + update.getWhere();
                            // 获取查询结果中，所有的primarykey
                            PreparedStatement pps = ((PreparedStatement) invocation.getArgs()[0]).getConnection().prepareStatement(select, Statement.RETURN_GENERATED_KEYS);
                            ResultSet rs = pps.executeQuery();
                            ResultSetMetaData metaData = rs.getMetaData();
                            List<Map<String, Object>> pks = new ArrayList<>();
                            Map<String, Object> primaryKeys = new HashMap<>();// 所有主键
                            while (rs.next()) {
                                int count = metaData.getColumnCount();
                                for(int i = 0; i < count; i++) {
                                    String cn = metaData.getColumnName(i);
                                    // 获取sourceList中primaryKey的列名
                                    for(Primary primary : source.getKeys()) {
                                        if (cn.equalsIgnoreCase(primary.getColumn())) {
                                            primaryKeys.put(cn, rs.getObject(i));
                                        }
                                    }
                                }
                                pks.add(primaryKeys);
                            }

                            // 判断是否修改主键
                            if (params.get(1).size() > 0) {
                                // 修改主键


                            } else {
                                // 不修改主键
                                // 1.查询orgPKHash 找到typeHash 2.生成payload
                                List<Primary> keys = source.getKeys();
                                int size = keys.size();
                                byte[][] bytes = new byte[size][];
                                for(int i = 0; i < size; i++) {
                                    bytes[i] = TypeUtils.objectToByte(primaryKeys.get(keys.get(i).getColumn()));
                                }

                                // 生成原始hash
                                String orgpkHash = Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(TypeUtils.concatByteArrays(bytes)));
                                String sql = "select typeHash from key_indexes where orgPKHash = ?;";
                                List<Map<String, Object>> query = SqliteUtils.query(sql, new Object[]{orgpkHash});


                            }


                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }


    /**
     * 获取所有要拦截的参数和值
     *
     * @param update  修改sql 的statment对象
     * @param sources xml
     */
    public static List<Map<String, Object>> getParamters(Update update, Source sources) {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> pks = new HashMap<>();
        List<Field> fields = sources.getFields();
        List<Primary> keys = sources.getKeys();
        //获取参数条件和值
        try {
            List<Column> columns = update.getColumns();
            List<Expression> expressions = update.getExpressions();
            int size = columns.size();
            for(int i = 0; i < size; i++) {
                String columnName = columns.get(i).getColumnName();
                // 生成新的主键
                for(Primary key : keys) {
                    if (key.getColumn().equalsIgnoreCase(columnName)) {
                        pks.put(columnName, getExpressionValue(key.getType(), expressions.get(i)));
                    }
                }
                // update所有参数
                for(Field field : fields) {
                    if (field.getColumn().equalsIgnoreCase(columnName)) {
                        params.put(columnName, getExpressionValue(field.getType(), expressions.get(i)));
                    }
                }
            }
            results.add(params);
            results.add(pks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }


    /**
     * 从sql参数中获取指定类型的值
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

}
