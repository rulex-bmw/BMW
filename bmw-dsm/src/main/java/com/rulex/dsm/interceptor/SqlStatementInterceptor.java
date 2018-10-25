package com.rulex.dsm.interceptor;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
@Component
public class SqlStatementInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) {//Invocation封装了目标对象、要执行的方法以及方法参数

        RoutingStatementHandler target = (RoutingStatementHandler) invocation.getTarget();//target目标对象
        BoundSql boundSql = target.getBoundSql();
        String sql = boundSql.getSql();
        System.out.println(sql);
        Object parameterObject = boundSql.getParameterObject();

        if (parameterObject instanceof com.rulex.dsm.pojo.User) {
            com.rulex.dsm.pojo.User user = (com.rulex.dsm.pojo.User) parameterObject;
            MetaObject metaObject = SystemMetaObject.forObject(user);
            String name = (String) metaObject.getValue("name");
            System.out.println(name);
            Integer age = (Integer) metaObject.getValue("age");
            System.out.println(age);
//            Integer age = user.getAge();
//            String name = user.getName();
        } else if (parameterObject instanceof Map) {
            Map paramMap = (Map) parameterObject;
            Iterator iterator = paramMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Object, Object> next = (Map.Entry<Object, Object>) iterator.next();
                Object key = next.getKey();
                Object value = next.getValue();
                System.out.println(key + "------" + value);
            }
        } else {
            System.out.println("other");
        }

        // 开始时间
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } catch (Exception e) {
            System.out.println("执行失败！");
            return null;
        } finally {
            long end = System.currentTimeMillis();
            long time = end - start;
            System.out.println(time + "ms");
        }
    }

    @Override
    public Object plugin(Object arg0) {//拦截器用于封装目标对象
//        if (arg0 instanceof StatementHandler) {
        return Plugin.wrap(arg0, this);
//        } else {
//            return arg0;
//        }
    }

    @Override
    public void setProperties(Properties arg0) {
    }



}
