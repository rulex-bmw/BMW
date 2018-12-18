package com.rulex.dsm.service;

import com.google.protobuf.ByteString;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.pojo.DataTypes;
import org.apache.ibatis.mapping.BoundSql;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class InsertService {

    /**
     * 拦截判断，生成payload
     *
     * @param boundSql   获取参数
     * @param tableName  数据库表名
     * @param columnName 数据库字段名
     * @param sources    xml解析拦截对象
     * @return byte[] 返回payload值
     */
    public static byte[] judge(BoundSql boundSql, String tableName, List<String> columnName, List<Source> sources) throws Exception {

        for (Source source : sources) {
            //根据tableName，获取对应的source
            if (source.getTable().equals(tableName)) {
                Class clazz = Class.forName("com.rulex.tools.pojo.RulexBean");
                //反射获取RulexBean的内部类
                Class innerClazz[] = clazz.getDeclaredClasses();
                for (Class Class : innerClazz) {
                    //获取表对应的内部类
                    if (Class.getSimpleName().equals(TypeUtils.InitialsLow2Up(source.getName()))) {

                        //获取当前表对应的Builder对象
                        Method newBuilder = Class.getMethod("newBuilder");
                        Constructor con = Class.getDeclaredConstructor();
                        con.setAccessible(true);
                        Object builder = newBuilder.invoke(con.newInstance());
                        //得到Builder类
                        Class builderClass = builder.getClass();

                        //判断执行sql的Parameter值，将符合的值加入payload中
                        builder = processParam(boundSql, columnName, source, builderClass, builder);

                        //执行Builder对象的build方法
                        Object message = builderClass.getMethod("build").invoke(builder);

                        //执行RulexBean内部类对象的toByteArray方法
                        Method toByte = Class.getMethod("toByteArray");
                        byte[] payload = (byte[]) toByte.invoke(message);

                        return payload;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 处理 Parameter值
     *
     * @param boundSql     获取参数
     * @param columnNames  数据库表名
     * @param source       xml解析拦截对象
     * @param builderClass Builder类
     * @param builder      Builder对象
     * @return Object 处理后的Builder对象
     */
    public static Object processParam(BoundSql boundSql, List<String> columnNames, Source source, Class builderClass, Object builder) throws Exception {

        Object parameter = boundSql.getParameterObject();
        Class clazz = parameter.getClass();
        //如果执行sql的方法参数类型是对象
        if (Class.forName(source.getPojo()) == clazz) {

            for (String columnName : columnNames) {
                for (Field field : source.getFields()) {

                    if (columnName.equals(field.getColumn())) {

                        String fieldName = TypeUtils.InitialsLow2Up(field.getName());
                        Method method = clazz.getMethod("get" + fieldName);
                        Object value = method.invoke(parameter);

                        builder = processBuilder(field, fieldName, value, builderClass, builder);
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

    /**
     * 反射设置Builder对象的属性
     *
     * @param field        Field对象
     * @param fieldName    field的name值
     * @param value        Builder对象的属性值
     * @param builderClass Builder类
     * @param builder      Builder对象
     * @return Object 处理后的Builder对象
     */
    public static Object processBuilder(Field field, String fieldName, Object value, Class builderClass, Object builder) throws Exception {

        if (DataTypes.wrapper_Int.getName().equals(field.getType()) || DataTypes.primeval_int.getName().equals(field.getType())) {
            builderClass.getMethod("set" + fieldName, int.class).invoke(builder, (int) value);
        } else if (DataTypes.wrapper_Long.getName().equals(field.getType()) || DataTypes.primeval_long.getName().equals(field.getType())) {

            builderClass.getMethod("set" + fieldName, long.class).invoke(builder, (long) value);
        } else if (DataTypes.wrapper_Double.getName().equals(field.getType()) || DataTypes.primeval_double.getName().equals(field.getType())) {

            builderClass.getMethod("set" + fieldName, double.class).invoke(builder, (double) value);
        } else if (DataTypes.wrapper_Float.getName().equals(field.getType()) || DataTypes.primeval_float.getName().equals(field.getType())) {

            builderClass.getMethod("set" + fieldName, float.class).invoke(builder, (float) value);
        } else if (DataTypes.primeval_string.getName().equals(field.getType())) {

            builderClass.getMethod("set" + fieldName, String.class).invoke(builder, (String) value);
        } else if (DataTypes.primeval_boolean.getName().equals(field.getType())) {

            builderClass.getMethod("set" + fieldName, boolean.class).invoke(builder, (boolean) value);
        } else if (DataTypes.primeval_ByteString.getName().equals(field.getType())) {

            builderClass.getMethod("set" + fieldName, ByteString.class).invoke(builder, ByteString.copyFrom((byte[]) value));
        }
        return builder;
    }


    /**
     * 添加rollId和上链数据key的索引
     *
     */
}
