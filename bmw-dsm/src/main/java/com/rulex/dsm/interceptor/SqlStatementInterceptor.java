package com.rulex.dsm.interceptor;

import com.google.protobuf.ByteString;
import com.rulex.bsb.pojo.DataBean;
import com.rulex.bsb.service.BSBService;
import com.rulex.bsb.utils.DataException;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.service.InsertService;
import com.rulex.dsm.utils.XmlUtil;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.io.StringReader;
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
        RoutingStatementHandler target = (RoutingStatementHandler) invocation.getTarget();
        BoundSql boundSql = target.getBoundSql();
        try {
            //获取拦截规则
            if (null == sourceList) {
                sourceList = XmlUtil.parseXML();
            }
            net.sf.jsqlparser.statement.Statement stmt = parser.parse(new StringReader(boundSql.getSql()));
            boolean t = false;
            String tablename = null;
            List<String> column = new ArrayList<>();
            if (stmt instanceof Insert) {
                Insert insert = (Insert) stmt;
                tablename = insert.getTable().getName();
                for (Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {
                        t = true;
                    }
                }
                List<Column> columns = insert.getColumns();
                for (Column c : columns) {
                    column.add(c.getColumnName());
                }
            } else if (stmt instanceof Update) {
                Update update = (Update) stmt;
                List<Table> tables = update.getTables();
                boolean b = false;
                for (Table table : tables) {
                    for (Source source : sourceList) {
                        if (source.getTable().equalsIgnoreCase(table.getName())) {
                            b = true;
                        }
                    }
                }
                if (b) {

                }


            } else if (stmt instanceof Delete) {

            } else if (stmt instanceof Drop) {
                //上区块链的表不能删除
                Drop drop = (Drop) stmt;
                tablename = drop.getName();
                for (Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {
                        throw new DataException("The data for this database table cannot be deleted because it is on the block chain.");
                    }
                }
            } else if (stmt instanceof Alter) {
                //上区块链的字段信息不能修改
                Alter alter = (Alter) stmt;
                tablename = alter.getTable().getName();
                for (Source source : sourceList) {
                    if (source.getTable().equalsIgnoreCase(tablename)) {

                        for (Field field : source.getFields()) {

                            if (alter.getColumnName().equalsIgnoreCase(field.getColumn())) {

                                throw new DataException("The data for this database table cannot be alter because it is on the block chain.");

                            }
                        }
                    }
                }
            }
            if (t) {
                //获取payload
                byte[] payload = InsertService.judge(boundSql, tablename, column, sourceList);
                if (payload != null) {
                    //调用bsb执行上链
                    DataBean.Data data = DataBean.Data.newBuilder().setPayload(ByteString.copyFrom(payload)).build();
                    BSBService.producer(data);
                    BSBService.Consumer();
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


}