package com.rulex.dsm.interceptor;

import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.tools.pojo.RulexBean;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Statement;
import java.util.*;


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


    /**
     * 拦截判断，生成payload
     *
     * @param boundSql   获取参数
     * @param tableName  数据库表名
     * @param columnName 数据库字段名
     * @param sources    xml解析拦截对象
     * @return byte[] 返回payload值
     */
    public byte[] judge(BoundSql boundSql, String tableName, List<String> columnName, List<Source> sources) throws Exception {

        for (Source source : sources) {
            //根据tableName，获取对应的source
            if (source.getTable().equals(tableName)) {

                Class clazz = RulexBean.class;
                //反射获取RulexBean的内部类
                Class innerClazz1[] = clazz.getDeclaredClasses();
                for (Class Class1 : innerClazz1) {
                    //获取表对应的内部类
                    if (Class1.getSimpleName().equals(source.getName())) {


                        Method method2 = Class1.getMethod("newBuilder");
                        Constructor con = Class1.getDeclaredConstructor();
                        con.setAccessible(true);
                        Object obj = con.newInstance();
                        Object invoke = method2.invoke(obj);
                        //获取当前表对应的Builder对象
                        for (Class Class2 : Class1.getDeclaredClasses()) {

                            Constructor con = Class2.getDeclaredConstructor();
                            con.setAccessible(true);
                            Object obj = con.newInstance();

                            //判断执行sql的Parameter值，将符合的值加入payload中
                            SqlStatementInterceptor ssi = new SqlStatementInterceptor();
                            obj = ssi.processParam(boundSql, columnName, source, Class2, obj);

                            //执行Builder对象的build方法
                            Method method2 = Class2.getMethod("build");
                            Object message = method2.invoke(obj);

                            //执行RulexBean内部类对象的toByteArray方法
                            Method method1 = Class1.getMethod("toByteArray");
                            byte[] payload = (byte[]) method1.invoke(message);

                            return payload;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 处理Parameter值
     *
     * @param boundSql   获取参数
     * @param tableName  数据库表名
     * @param columnName 数据库字段名
     * @param sources    xml解析拦截对象
     * @return byte[] 返回payload值
     */
    public Object processParam(BoundSql boundSql, List<String> columnNames, Source source, Class builderClass, Object builder) throws Exception {

        Object parameter = boundSql.getParameterObject();
        Class clazz = parameter.getClass();
        //如果执行sql的方法参数类型是对象
        if (Class.forName(source.getPojo()) == clazz) {

            for (String columnName : columnNames) {
                for (Field field : source.getFields()) {

                    if (columnName.equals(field.getColumn())) {

                        Object obj = clazz.newInstance();
                        String fieldName = transform(field.getName());
                        Method method = clazz.getMethod("get" + fieldName);
                        Object value = method.invoke(obj);


                        Method method2 = builderClass.getMethod("set" + fieldName, com.google.protobuf.ByteString.class);
                        method2.invoke(builder, value);
                    }
                }
            }
            return builder;
            //如果执行sql的方法参数类型是map
        } else if (parameter instanceof java.util.HashMap) {
            return null;
        } else {
            return null;
        }
    }




    public static void main(String[] args) throws Exception {

        List<Source> sources = new ArrayList<>();

        Source source = new Source();
        source.setName("Three");
        source.setTable("three");
        sources.add(source);
        new SqlStatementInterceptor().judge(null, "three", null, sources);

    }
}