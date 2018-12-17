package com.rulex.dsm.interceptor;


import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;
import sun.plugin2.main.server.ResultHandler;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
@Component
public class ResultSetInterceptor implements Interceptor {

    public static Map<String, Object> table_primaryKey = new HashMap();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        Statement statement = (Statement) args[0];
        ResultSet rs = statement.getResultSet();




        return invocation.proceed();
    }






    @Override
    public Object plugin(Object target) {
        if (target instanceof ResultHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
