package com.rulex.dsm.interceptor;

import com.google.protobuf.ByteString;
import com.rulex.bsb.dao.LevelDBDaoImpl;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.tools.pojo.RulexBean;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;

@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
@Component
public class SqlStatementInterceptor implements Interceptor {

    @Resource
    private BSBService bsbService;


    CCJSqlParserManager parser = new CCJSqlParserManager();
    private List<Source> sourceList;

    /**
     * 拦截器执行方法
     *
     * @param invocation Invocation封装了目标对象、要执行的方法以及方法参数
     * @return
     */
    @Override
    public Object intercept(Invocation invocation) throws IOException {
        //target目标对象
        RoutingStatementHandler target = (RoutingStatementHandler) invocation.getTarget();
        BoundSql boundSql = target.getBoundSql();
        String sql = boundSql.getSql();
        //解析sql获取table和column
        String tablename = null;
        List<String> column = new ArrayList<>();
        try {
            //解析xml获取list<source>
            if (null == sourceList) {
                parseXML();
            }
            net.sf.jsqlparser.statement.Statement stmt = parser.parse(new StringReader(sql));
            boolean t = false;
            if (stmt instanceof Insert) {
                Insert insert = (Insert) stmt;
                Table table = insert.getTable();
                tablename = table.getName();
                for(Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(table.getName())) {
                        t = true;
                    }
                }
                List<Column> columns = insert.getColumns();
                for(Column c : columns) {
                    column.add(c.getColumnName());
                }
            } else if (stmt instanceof Update) {


            } else if (stmt instanceof Delete) {

            }
            if (t) {
                System.out.println(boundSql + "-------" + tablename + "-----" + column + "-----" + sourceList);
                //获取payload
                byte[] judge = judge(boundSql, tablename, column, sourceList);
                System.out.println(judge);
//                byte[] judge = bytes("1234");
                //调用bsb执行上链
                DataBean.Data data = DataBean.Data.newBuilder().setParam(ByteString.copyFrom(judge)).build();
                bsbService.producer(data);
                bsbService.customer();

            }
            return invocation.proceed();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            LevelDBDaoImpl.mataDB.close();
            LevelDBDaoImpl.dataDB.close();
        }
    }

    //mybatis拦截器获取参数
//        System.out.println(sql);
//        Object parameterObject = boundSql.getParameterObject();
//        if (parameterObject instanceof com.rulex.dsm.pojo.User) {
//            com.rulex.dsm.pojo.User user = (com.rulex.dsm.pojo.User) parameterObject;
//            MetaObject metaObject = SystemMetaObject.forObject(user);
//            String name = (String) metaObject.getValue("name");
//            System.out.println(name);
//            Integer age = (Integer) metaObject.getValue("age");
//            System.out.println(age);
////            Integer age = user.getAge();
////            String name = user.getName();
//        } else if (parameterObject instanceof Map) {
//            Map paramMap = (Map) parameterObject;
//            Iterator iterator = paramMap.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<Object, Object> next = (Map.Entry<Object, Object>) iterator.next();
//                Object key = next.getKey();
//                Object value = next.getValue();
//                System.out.println(key + "------" + value);
//            }
//        } else {
//            System.out.println("other");
//        }

    @Override
    public Object plugin(Object arg0) {//拦截器用于封装目标对象
        if (arg0 instanceof StatementHandler) {
            return Plugin.wrap(arg0, this);
        } else {
            return arg0;
        }
    }

    @Override
    public void setProperties(Properties arg0) {
    }

    public Document readerXML() throws DocumentException {
        // 读取xml文件
        SAXReader sr = new SAXReader();
        File file = new File(SqlStatementInterceptor.class.getClass().getResource("/").getPath() + "rulex-condition.xml");
        Document doc = sr.read(file);
        return doc;
    }

    public void parseXML() throws DocumentException {
        sourceList = new ArrayList<>();
        Document doc = readerXML();
        //解析根节点
        Element root = doc.getRootElement();
        //解析record节点
        List<Element> sources = root.elements("source");
        if (sources == null) {
            return;
        }
        for(Element s : sources) {
            Source source = new Source();
            String name = s.attribute("name").getValue();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            source.setName(name);
            String groupable = s.attributeValue("groupable");
            source.setGroupable(Boolean.valueOf(groupable));
            String table = s.attributeValue("table");
            source.setTable(table);
            String pojo = s.attributeValue("pojo");
            source.setPojo(pojo);
            List<Field> field = new ArrayList<>();
            List<Element> fields = s.elements();
            String paramName;
            String isnull;
            String type;
            String maxvalue;
            String minvalue;
            String maxsize;
            String minsize;
            String transforable;
            String length;
            String column;
            for(Element f : fields) {
                Field fi = new Field();
                paramName = f.attributeValue("name");
                fi.setName(paramName);
                column = f.attributeValue("column");
                fi.setColumn(column);
                isnull = f.attributeValue("isnull");
                if (isnull.equals("false") || StringUtils.isBlank(isnull)) {//不填或false表示不能为空
                    fi.setIsnull(false);
                } else {//表示可以为空
                    fi.setIsnull(Boolean.valueOf(isnull));
                }
                type = f.attributeValue("type");
                fi.setType(type);
                if (type.equals("Integer") || type.equals("Long") || type.equals("Float") || type.equals("Double")) {
                    maxvalue = f.attributeValue("maxvalue");
                    minvalue = f.attributeValue("minvalue");
                    fi.setMaxvalue(maxvalue);
                    fi.setMinvalue(minvalue);
                } else if (type.equals("String")) {
                    maxsize = f.attributeValue("maxsize");
                    minsize = f.attributeValue("minsize");
                    if (!StringUtils.isBlank(maxsize)) fi.setMaxsize(Integer.valueOf(maxsize));
                    if (!StringUtils.isBlank(minsize)) fi.setMinsize(Integer.valueOf(minsize));
                }
                length = f.attributeValue("length");
                if (!StringUtils.isBlank(length)) fi.setLength(Integer.valueOf(length));
                transforable = f.attributeValue("transforable");
                if (!StringUtils.isBlank(transforable)) {
                    fi.setTransforable(Boolean.valueOf(transforable));
                } else {
                    fi.setTransforable(false);
                }
                field.add(fi);
            }
            source.setFields(field);
            sourceList.add(source);
        }
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

        for(Source source : sources) {
            //根据tableName，获取对应的source
            if (source.getTable().equals(tableName)) {
                Class clazz = RulexBean.class;
                //反射获取RulexBean的内部类
                Class innerClazz1[] = clazz.getDeclaredClasses();
                for(Class Class1 : innerClazz1) {
                    //获取表对应的内部类
                    if (Class1.getSimpleName().equals(TypeUtils.InitialsLow2Up(source.getName()))) {

                        //获取当前表对应的Builder对象
                        Method newBuilder = Class1.getMethod("newBuilder");
                        Constructor con = Class1.getDeclaredConstructor();
                        con.setAccessible(true);
                        Object obj1 = con.newInstance();
                        Object builder = newBuilder.invoke(obj1);
                        //得到Builder类
                        Class Class2 = builder.getClass();

                        //判断执行sql的Parameter值，将符合的值加入payload中
                        SqlStatementInterceptor ssi = new SqlStatementInterceptor();
                        builder = ssi.processParam(boundSql, columnName, source, Class2, builder);

                        //执行Builder对象的build方法
                        Method method2 = Class2.getMethod("build");
                        Object message = method2.invoke(builder);

                        //执行RulexBean内部类对象的toByteArray方法
                        Method toByte = Class1.getMethod("toByteArray");
                        byte[] payload = (byte[]) toByte.invoke(message);

                        return payload;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 处理Parameter值
     *
     * @param boundSql     获取参数
     * @param columnNames  数据库表名
     * @param source       xml解析拦截对象
     * @param builderClass Builder类
     * @param builder      Builder对象
     * @return Object 处理后的Builder对象
     */
    public Object processParam(BoundSql boundSql, List<String> columnNames, Source source, Class builderClass, Object builder) throws Exception {

        Object parameter = boundSql.getParameterObject();
        Class clazz = parameter.getClass();
        //如果执行sql的方法参数类型是对象
        if (Class.forName(source.getPojo()) == clazz) {

            for(String columnName : columnNames) {
                for(Field field : source.getFields()) {

                    if (columnName.equals(field.getColumn())) {

                        Object obj = clazz.newInstance();
                        String fieldName = TypeUtils.InitialsLow2Up(field.getName());
                        Method method = clazz.getMethod("get" + fieldName);
                        Object value = method.invoke(obj);

                        ByteString sign = ByteString.copyFrom(bytes((String) value));
                        Method method2 = builderClass.getMethod("set" + fieldName, com.google.protobuf.ByteString.class);
                        method2.invoke(builder, sign);
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


    public static void main(String[] args) {

        List<Source> sources = new ArrayList<>();

        Source source = new Source();
        source.setName("one");
        source.setTable("one");
        sources.add(source);
        try {
            new SqlStatementInterceptor().judge(null, "one", null, sources);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}