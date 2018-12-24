package com.rulex.dsm.utils;

import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Primary;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.mapping.ParameterMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlUtils {

    /**
     * 获取所有要拦截的参数和主键
     *
     * @param stmt   拦截的Statement对象
     * @param source xml中对应的record
     * @return List<Map<String, Object>>    所有拦截的参数，result(0):params result(1):keys result(2):where condition
     */
   /* public static List<Map<String, Object>> getParamters(net.sf.jsqlparser.statement.Statement stmt, Source source) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> pks = new HashMap<>();
        Map<String, Object> whereParam = new HashMap<>();
        List<Field> fields = source.getFields();
        List<Primary> keys = source.getKeys();
        if (stmt instanceof Insert) {
            Insert insert = (Insert) stmt;


        } else if (stmt instanceof Update) {
            Update update = (Update) stmt;
            List<Column> columns = update.getColumns();// 所有参数列
            int size = columns.size();

            String where = update.getWhere().toString();   // 获取where之后sql

            // 获取参数，类型是pojo或hashmap
            List<Expression> expressions = update.getExpressions(); // sql上的所有表达式的值

            Object po = boundSql.getParameterObject();  // paramter传入的参数

            List<ParameterMapping> paramMappings = boundSql.getParameterMappings(); // 参数映射关系
            int index = 0;  // 占位符所对应paramMappings的下标

            Class<?> param = po.getClass(); // 如果是pojo类型转化为类
            Class<?> pojo = Class.forName(source.getPojo());    // 获取source中对应的pojo

            for(int i = 0; i < size; i++) {// 所有列循环

                Expression value = expressions.get(i);  // 获取表达式的值
                String column = columns.get(i).getColumnName(); // 获取列名

                if (!value.toString().equals("?")) {// 不使用占位符,直接获取value

                    for(Field field : fields) {
                        if (field.getColumn().equalsIgnoreCase(column)) {
                            params.put(column, getExpressionValue(field.getType(), value));
                            break;
                        }
                    }

                    for(Primary key : keys) {
                        if (key.getColumn().equalsIgnoreCase(column)) {
                            pks.put(column, getExpressionValue(key.getType(), value));
                            break;
                        }
                    }

                } else {// 使用占位符

                    if (po instanceof Map) {    // hashmap类型

                        // 从映射关系中找到value,放入params
                        Map<String, Object> parameterObject = (HashMap) po;
                        ParameterMapping pm = paramMappings.get(index);
                        Object p = parameterObject.get(pm.getProperty());
                        params.put(column, p);

                        for(Primary key : keys) {
                            if (key.getColumn().equalsIgnoreCase(column)) {
                                pks.put(column, p);
                                break;
                            }
                        }

                        index++;

                    } else if (pojo == param) {

                        for(Field field : fields) {

                            if (field.getColumn().equalsIgnoreCase(column)) {

                                // 反射获取参数值
                                String fieldName = TypeUtils.InitialsLow2Up(field.getName());
                                Method method = param.getMethod("get" + fieldName);
                                Object p = method.invoke(param);
                                params.put(column, p);

                                if (field.isIsprimaykey()) {
                                    pks.put(column, p); // 放入主键map
                                }

                            }
                        }
                    }
                }
            }

            // where条件中是否有占位符
            if (StringUtils.lastIndexOf(where, '?') != -1) {

                // 统计where中占位符个数
                int count = TypeUtils.strCount(where, "?");

                //将参数放入obj[]
                Object[] obj = new Object[count];

                if (po instanceof Map) {

                    // 从映射关系中找到value,放入params
                    Map<String, Object> parameterObject = (HashMap) po;

                    for(int i = 0; i < count; i++) {
                        int in = paramMappings.size() - count + i;
                        ParameterMapping pm = paramMappings.get(in);
                        obj[i] = parameterObject.get(pm.getProperty());
                    }

                } else if (pojo == param) {

                }

                whereParam.put("where", where);
                whereParam.put("condition", obj);
            }

            results.add(params);
            results.add(pks);
            results.add(whereParam);

        } else if (stmt instanceof Delete) {
            Delete update = (Delete) stmt;


        }


        return results;
    }
*/
    /**
     * 从sql参数中获取指定类型的值
     *
     * @param type       设定的数据类型
     * @param expression 为转化类型的expression对象
     * @return Object     返回param的值
     */
   /* public static Object getExpressionValue(String type, Expression expression) throws Exception {

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
    }*/

}
