package com.rulex.dsm.service;

import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Invocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateService {

    private UpdateService() {
    }


    public static void credibleUpdate(Update update, Invocation invocation, List<Source> sourceList) {
        try {
            List<Table> tables = update.getTables();
            List<Column> columns = update.getColumns();
            List<Expression> expressions = update.getExpressions();
            int size = columns.size();
            List<Map<String, Object>> params = new ArrayList<>();
            //找到拦截目标
            for(Table table : tables) {
                String tablename = table.getName();
                for(Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {
                        //获取所有update参数
                        params = UpdateService.getParamters(update, source);
                        //编写查询语句，放入查询队列
                        Expression where = update.getWhere();
                        String select = "selce * from " + tablename + " where " + where;
                        //获取查询结果找到primarykey
                        PreparedStatement pps = ((PreparedStatement) invocation.getArgs()[0]).getConnection().prepareStatement(select, Statement.RETURN_GENERATED_KEYS);
                        ResultSet rs = pps.executeQuery();


                    }
                }
            }

            for(int i = 0; i < size; i++) {
                String columnName = columns.get(i).getColumnName();
                Expression expression = expressions.get(i);
            }
        } catch (Exception e) {

        }
    }


    /**
     * 获取所有要拦截的参数和值
     */
    public static List<Map<String, Object>> getParamters(Update update, Source sources) {
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        List<Field> fields = sources.getFields();
        //获取参数条件和值
        try {
            List<Column> columns = update.getColumns();
            List<Expression> expressions = update.getExpressions();
            int size = columns.size();
            for(int i = 0; i < size; i++) {
                String columnName = columns.get(i).getColumnName();
                for(Field field : fields) {
                    if (field.getColumn().equalsIgnoreCase(columnName)) {
                        params.put(columnName, getExpressionValue(field.getType(), expressions.get(i)));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 获取指定类型的值
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
