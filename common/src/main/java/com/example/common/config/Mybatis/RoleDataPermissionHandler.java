package com.example.common.config.Mybatis;


import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.example.common.config.Security.PermissionService;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huanghongjia
 */
@Component
public class RoleDataPermissionHandler extends JsqlParserSupport implements InnerInterceptor {

    @Resource
    PermissionService permissionService;


    /**
     * 查询操作前置处理
     *
     * @param executor      Executor(可能是代理对象)
     * @param ms            MappedStatement
     * @param parameter     parameter
     * @param rowBounds     rowBounds
     * @param resultHandler resultHandler
     * @param boundSql      boundSql
     * @throws SQLException
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        mpBs.sql(this.parserSingle(mpBs.sql(), ms.getId()));

    }


    /**
     * 查询
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {

        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect) {
            System.out.println("PlainSelect:" + ((PlainSelect) selectBody).getWhere());
            Expression sqlSegmentExpression = null;
            try {
                Class<?> clazz = permissionService.getContext().getClazz();
                // 获取并调用方法
                Method method = clazz.getMethod(permissionService.getContext().getCallMethod());
                Object invoke = method.invoke(clazz.newInstance());
                sqlSegmentExpression = CCJSqlParserUtil.parseCondExpression(invoke.toString());
                System.out.println("返回值：" + invoke.toString());
            } catch (JSQLParserException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                e.printStackTrace();
            }

            AndExpression andExpression = new AndExpression(((PlainSelect) selectBody).getWhere(), sqlSegmentExpression);
            OrExpression orExpression = new OrExpression(((PlainSelect) selectBody).getWhere(), sqlSegmentExpression);

            ((PlainSelect) selectBody).setWhere(orExpression);
        } else if (selectBody instanceof SetOperationList) {

        }
    }

    /**
     * 修改操作前置处理
     *
     * @param executor  Executor(可能是代理对象)
     * @param ms        MappedStatement
     * @param parameter parameter
     * @throws SQLException
     */
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // do nothing
        System.out.println(executor);
        System.out.println(ms);
        System.out.println(parameter);

        // 获取原始的 BoundSql 对象
        BoundSql boundSql = ms.getBoundSql(parameter);
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        mpBs.sql(this.parserSingle(mpBs.sql(), ms.getId()));
    }

    /**
     * 修改
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        // 在执行 UPDATE 语句前的自定义逻辑
        System.out.println("在执行 UPDATE 语句前的自定义逻辑");
        System.out.println("update:" + update);
        System.out.println("index:" + index);
        System.out.println("sql:" + sql);
        System.out.println("sql:" + obj);


        // 在执行 UPDATE 语句后的自定义逻辑
        System.out.println("在执行 UPDATE 语句后的自定义逻辑");
    }

    /**
     * 处理数据权限
     *
     * @return
     */
    public String handleDataScope() {
        return "1 = 1";
    }


}
