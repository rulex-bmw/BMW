package com.rulex.dsm.interceptor;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.DataException;
import com.rulex.bsb.utils.SHA256;
import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.service.InsertService;
import com.rulex.dsm.service.UpdateService;
import com.rulex.dsm.utils.XmlUtil;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
@Component
public class SqlStatementInterceptor implements Interceptor {

    CCJSqlParserManager parser = new CCJSqlParserManager();

    private List<Source> sourceList = new ArrayList<>();

    /**
     * 拦截器执行方法
     *
     * @param invocation Invocation封装了目标对象、要执行的方法以及方法参数
     * @return
     */
    @Override
    public Object intercept(Invocation invocation) throws Exception {
        //拦截执行sql
        RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        try {
            //获取拦截规则
            if (0 == sourceList.size()) {
                sourceList = XmlUtil.parseXML();
            }
            net.sf.jsqlparser.statement.Statement stmt = parser.parse(new StringReader(boundSql.getSql()));
            boolean t = false;
            String tablename = null;
            List<String> column = new ArrayList<>();
            if (stmt instanceof Insert) {
                Insert insert = (Insert) stmt;
                tablename = insert.getTable().getName();
                for(Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {
                        t = true;
                    }
                }
                List<Column> columns = insert.getColumns();
                for(Column c : columns) {
                    column.add(c.getColumnName());
                }
                if (t) {
                    //获取payload
                    byte[] payload = InsertService.judge(boundSql, tablename, column, sourceList);
                    //获取PrimaryId
                    Object PrimaryId = null;
                    byte[] hashPrimaryId = SHA256.getSHA256Bytes(TypeUtils.objectToByte(PrimaryId));

                    if (payload != null) {
                        //调用bsb执行上链
                        DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                        BSBService.producer(data, hashPrimaryId);
                        BSBService.Consumer();
                    }
                }
            } else if (stmt instanceof Update) {
                UpdateService.credibleUpdate((Update) stmt, invocation, sourceList);
            } else if (stmt instanceof Delete) {


            } else if (stmt instanceof Drop) {
                //上区块链的表不能删除
                Drop drop = (Drop) stmt;
                tablename = drop.getName();
                for(Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {
                        throw new DataException("The data for this database table cannot be deleted because it is on the block chain.");
                    }
                }
            } else if (stmt instanceof Alter) {
                //上区块链的字段信息不能修改
                Alter alter = (Alter) stmt;
                tablename = alter.getTable().getName();
                for(Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {

                        for(Field field : source.getFields()) {

                            if (alter.getColumnName().equalsIgnoreCase(field.getColumn())) {

                                throw new DataException("The data for this database table cannot be alter because it is on the block chain.");

                            }
                        }
                    }
                }
            }

            return invocation.proceed();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object plugin(Object target) {//拦截器用于封装目标对象
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties arg0) {
    }


    /**
     * 包装sql后，重置到invocation中
     *
     * @param invocation
     * @param sql
     * @throws SQLException
     */
    private void resetSql2Invocation(Invocation invocation, String sql) throws SQLException {
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
        MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", sql);
        args[0] = newStatement;
    }

    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder =
                new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for(String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    private String getOperateType(Invocation invocation) {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        SqlCommandType commondType = ms.getSqlCommandType();
        if (commondType.compareTo(SqlCommandType.SELECT) == 0) {
            return "select";
        }
        if (commondType.compareTo(SqlCommandType.INSERT) == 0) {
            return "insert";
        }
        if (commondType.compareTo(SqlCommandType.UPDATE) == 0) {
            return "update";
        }
        if (commondType.compareTo(SqlCommandType.DELETE) == 0) {
            return "delete";
        }
        return null;
    }

    //    定义一个内部辅助类，作用是包装sql
    class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

}