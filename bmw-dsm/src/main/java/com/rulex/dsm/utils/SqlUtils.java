package com.rulex.dsm.utils;

import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlUtils {

    /**
     * 获取所有参数和主键
     *
     * @param stmt
     * @param
     * @param
     * @return
     */
    public static List<Map<String, Object>> getParamters(net.sf.jsqlparser.statement.Statement stmt, Source source, String classname, BoundSql boundSql) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> pks = new HashMap<>();
        List<Field> fields = source.getFields();
        if (stmt instanceof Insert) {
            Insert insert = (Insert) stmt;


        } else if (stmt instanceof Update) {
            Update update = (Update) stmt;
            List<Column> columns = update.getColumns();// 所有参数列
            int size = columns.size();
            // 获取参数，类型是pojo或hashmap
            List<Expression> expressions = update.getExpressions();
            for(int i = 0; i < size; i++) {// 所有列循环
                Expression value = expressions.get(i);
                if (!value.toString().equals("?")) {// 不使用占位符,直接获取value
                    String column = columns.get(i).getColumnName();
                    for(Field field : fields) {
                        if (field.getColumn().equalsIgnoreCase(column)) {
                            params.put(column, getExpressionValue(field.getType(), value));
                        }
                    }
                } else {// 使用占位符
                    Object po = boundSql.getParameterObject();
                    Class<?> param = po.getClass();
                    if (po instanceof Map) {
                        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
                        Map<String, Object> parameterObject = (HashMap) po;


                    } else if (param == Class.forName(source.getPojo())) {
//                for (Column cn : columns) {
//                    for (Field field : source.getFields()) {
//                        if (cn.equals(field.getColumn())) {
//
//                            String fieldName = TypeUtils.InitialsLow2Up(field.getName());
//                            Method method = param.getMethod("get" + fieldName);
//                            Object value = method.invoke(parameter);
//
//                            builder = processBuilder(field, fieldName, value, builderClass, builder);
//                        }
//                    }
//                }
                    }
                }

            }


//            List<Column> columns = update.getColumns();
//            List<Expression> expressions = update.getExpressions();
//            int size = columns.size();


        } else if (stmt instanceof Delete) {
            Delete update = (Delete) stmt;


        }


        return null;
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
