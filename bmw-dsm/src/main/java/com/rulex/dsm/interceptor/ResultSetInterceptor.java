package com.rulex.dsm.interceptor;


import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;
import sun.plugin2.main.server.ResultHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
@Component
public class ResultSetInterceptor implements Interceptor {

    public static Map<String, Object> table_primaryKey = new HashMap();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Statement statement = (Statement) args[0];
        ResultSet rs = statement.getResultSet();
        ResultSetMetaData rsmd = rs.getMetaData();
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        while (rs.next()) {
            // 将各列数据存入map集合
            Map<String, String> ma = new HashMap<String, String>();
            // 获取键值和每一行的各列存入map集合
            for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                // 将 列名 和 列值 存入集合
                ma.put(rsmd.getColumnName(i), rs.getString(i));
            }
            // 将map集合存入list集合
            list.add(ma);
        }

        System.out.println(list);


        return invocation.proceed();
    }






    @Override
    public Object plugin(Object target) {
        if (target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
