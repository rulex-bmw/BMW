package com.rulex.dsm.interceptor;


import com.rulex.dsm.bean.Source;
import com.rulex.dsm.service.DelService;
import com.rulex.dsm.service.UpdateService;
import com.rulex.dsm.utils.XmlUtil;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.*;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Component
public class BMWExecutorInterceptor implements Interceptor {

    CCJSqlParserManager parser = new CCJSqlParserManager();

    public static List<Source> sourceList = new ArrayList<>();

    public static Map<Long, String> sqls = new HashMap<>();

    /**
     * 拦截器执行方法
     *
     * @param invocation Invocation封装了目标对象、要执行的方法以及方法参数
     * @return
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取xml中的一个select/update/insert/delete节点，主要描述的是一条SQL语句
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];

        Object parameter = null;
        // 获取参数，if语句成立，表示sql语句有参数
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);

        try {
            if (0 == sourceList.size()) {
                // 获取拦截规则
                sourceList = XmlUtil.parseXML();
            }
            net.sf.jsqlparser.statement.Statement stmt = parser.parse(new StringReader(boundSql.getSql()));

            if (stmt instanceof Update) {

                // 判断是否需要拦截
                if (UpdateService.decideTointerceptor((Update) stmt, sourceList)) {
                    // 解析sql
                    sqls.put(Thread.currentThread().getId(), UpdateService.getsql(boundSql, mappedStatement.getConfiguration()));
                }

            } else if (stmt instanceof Delete) {

                // 判断是否需要拦截
                if (DelService.decideTointerceptor((Delete) stmt, sourceList)) {
                    // 解析sql
                    sqls.put(Thread.currentThread().getId(), UpdateService.getsql(boundSql, mappedStatement.getConfiguration()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return invocation.proceed();
    }


    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
