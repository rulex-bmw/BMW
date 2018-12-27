package com.rulex.dsm.interceptor;

import com.rulex.bsb.utils.DataException;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.service.DelService;
import com.rulex.dsm.service.InsertService;
import com.rulex.dsm.service.UpdateService;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
@Component
public class BMWStmtInterceptor implements Interceptor {

    CCJSqlParserManager parser = new CCJSqlParserManager();

    @Resource
    private BMWExecutorInterceptor bmwExecutorInterceptor;

    /**
     * 拦截器执行方法
     *
     * @param invocation Invocation封装了目标对象、要执行的方法以及方法参数
     * @return
     */
    @Override
    public Object intercept(Invocation invocation) throws Exception {

        RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        List<Source> sourceList = bmwExecutorInterceptor.sourceList;

        try {
            net.sf.jsqlparser.statement.Statement stmt = parser.parse(new StringReader(boundSql.getSql()));

            if (stmt instanceof Insert) {

                InsertService.credibleInsert((Insert) stmt, boundSql, sourceList);

            } else if (stmt instanceof Update) {
                // 获取当前线程的sql
                long id = Thread.currentThread().getId();
                String sql = bmwExecutorInterceptor.sqls.get(id);
                if (null != sql && !StringUtils.isBlank(sql)) {
                    bmwExecutorInterceptor.sqls.remove(id);

                    // 将修改变为可信数据
                    UpdateService.credibleUpdate((Update) parser.parse(new StringReader(sql)), invocation, sourceList);
                }

            } else if (stmt instanceof Delete) {
                long id = Thread.currentThread().getId();
                String sql = bmwExecutorInterceptor.sqls.get(id);
                if (null != sql && !StringUtils.isBlank(sql)) {
                    bmwExecutorInterceptor.sqls.remove(id);

                    // 标记数据已删除
                    DelService.credibleDel((Delete) parser.parse(new StringReader(sql)), invocation, sourceList);

                }

            } else if (stmt instanceof Drop) {

                // 上区块链的表不能删除
                Drop drop = (Drop) stmt;
                for (Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(drop.getName())) {
                        throw new DataException("The data for this database table cannot be deleted because it is on the block chain.");
                    }
                }
            } else if (stmt instanceof Alter) {

                // 上区块链的字段信息不能修改
                Alter alter = (Alter) stmt;
                for (Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(alter.getTable().getName())) {

                        //修改表名报错
                        if ((alter.getColumnName() == null)) {
                            throw new DataException("The data for this database table cannot be alter because it is on the block chain.");
                        }

                        //修改上链的表列名报错
                        for (Field field : source.getFields()) {
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
     *//*
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
    }*/

}