package com.example.common.config.Mybatis;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.example.common.config.Security.PermissionService;
import com.example.common.domain.VRoleApi;
import com.example.common.model.Permission;
import com.example.common.util.SecurityUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 角色数据权限处理器
 */
@Component
public class RoleDataPermission2Handler extends JsqlParserSupport implements InnerInterceptor {

    @Resource
    PermissionService permissionService;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 查询操作前置处理
     * <p>
     * 这是 MyBatis Plus InnerInterceptor 接口中定义的一个拦截点方法。
     * 在执行查询（SELECT）操作之前调用。
     * 你可以在这里对即将执行的 SQL 语句进行修改或增强，比如加上数据权限过滤条件。
     * 你的代码里就是在这里读取原始 SQL，解析并拼接权限条件，最后替换成新的 SQL
     *
     * @param executor      Executor(可能是代理对象)
     * @param ms            MappedStatement
     * @param parameter     parameter
     * @param rowBounds     rowBounds
     * @param resultHandler resultHandler
     * @param boundSql      boundSql
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        if (!ms.getId().equals(permissionService.getContext().getMappedStatementId())) {
            return;
        }
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        String sql = mpBs.sql();
        try {
            // 反射调用权限条件生成方法
            Class<?> clazz = permissionService.getContext().getClazz();
            Object bean = applicationContext.getBean(clazz);
            Method method = clazz.getMethod(permissionService.getContext().getCallMethod());
            Object invoke = method.invoke(bean);
            String whereStr = invoke.toString();
            if (StrUtil.isBlank(whereStr)) {
                return;
            }
            String dataScope = Permission.getDataScope();
            if (sql.contains(dataScope)) {
                // 替换占位符为权限条件（注意权限条件格式要正确）
                String sqlLower = sql.toLowerCase();
                boolean hasWhere = sqlLower.contains(" where ");

                String replacement = hasWhere ? " AND " + whereStr + " " : " WHERE " + whereStr + " ";
                sql = sql.replace(dataScope, replacement);
            } else {
                // 没有占位符，用JSQLParser解析SQL，追加权限条件
                Select select = (Select) CCJSqlParserUtil.parse(sql);
                Expression permissionExp = CCJSqlParserUtil.parseCondExpression(whereStr);

                SelectBody selectBody = select.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectBody;
                    Expression oldWhere = plainSelect.getWhere();
                    if (oldWhere == null) {
                        plainSelect.setWhere(permissionExp);
                    } else {
                        plainSelect.setWhere(new AndExpression(oldWhere, permissionExp));
                    }
                }
                sql = select.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("权限条件生成失败", e);
        }

        mpBs.sql(this.parserSingle(sql, ms.getId()));

    }


    /**
     * 查询
     * <p>
     * 这是你继承自 JsqlParserSupport 类的一个钩子方法，用于处理解析后的 Select 语句。
     * 当你调用 parserSingle(sql, ms.getId()) 解析 SQL 时，会自动调用这个方法。
     * 你可以在这里对解析后的 SQL 结构（抽象语法树 AST）进行更细粒度的操作，比如修改 WHERE 条件、添加 JOIN，或者重写查询字段。
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {

    }

    /**
     * 修改操作前置处理
     * <p>
     * 这是你自己写的一个方法（并不是 MyBatis Plus InnerInterceptor 的接口方法），看起来是你计划在执行 UPDATE 操作之前调用的钩子。
     * 你可以在这里对 UPDATE 语句做前置处理，比如增加权限控制、审计字段、日志等。
     * 目前你代码里只是简单打印了参数，并调用了 parserSingle 解析 SQL，但没有修改 SQL
     *
     * @param executor  Executor(可能是代理对象)
     * @param ms        MappedStatement
     * @param parameter parameter
     */
    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {

    }

    /**
     * 修改
     * <p>
     * 这是你继承自 JsqlParserSupport 的另一个钩子方法，用于处理 UPDATE 语句的 AST。
     * 当调用 parserSingle 解析 UPDATE 语句时会触发。
     * 你可以在这里对 UPDATE 语句进行自定义修改，比如添加 WHERE 条件限制，防止误更新
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {

    }

    /**
     * 处理数据权限
     */
    public StringBuilder handleDataScope() {
        StringBuilder sqlString = new StringBuilder();

        List<List<String>> conditions = new ArrayList<>();
        Map<String, VRoleApi> currentActionRoleAuth = permissionService.getContext().getCurrentActionRoleAuth();

        String masterAlias = permissionService.getContext().getMasterAlias();
        Long userId = SecurityUtil.getUserId();
        Long roleId = SecurityUtil.getRoleId();

        currentActionRoleAuth.forEach((action, vRoleApi) -> {

//            vRoleApi.setDataScope("user_id");
//            vRoleApi.setDataKey("user_type");
//            vRoleApi.setOperatingStatus("100,200");

            List<String> condition = new ArrayList<>();

            if ("%".equals(vRoleApi.getDataScope()) && "%".equals(vRoleApi.getOperatingStatus())) {
                conditions.clear();
                return;
            }
            switch (vRoleApi.getDataScope()) {
                case "user_id":
                    condition.add(StrUtil.format(" {}user_id = '{}'", masterAlias, userId));
                    break;
                case "role_id":
                    condition.add(StrUtil.format(" {}role_id = '{}'", masterAlias, roleId));
                    break;

            }
            if (!"%".equals(vRoleApi.getOperatingStatus())) {
                if (vRoleApi.getOperatingStatus().contains(",")) {
                    String[] split = vRoleApi.getOperatingStatus().split(",");
                    String inString = "'" + ArrayUtil.join(split, "','") + "'";
                    condition.add(StrUtil.format(" {}{} in ({})", masterAlias, vRoleApi.getDataKey(), inString));
                } else {
                    condition.add(StrUtil.format(" {}{} = '{}'", masterAlias, vRoleApi.getDataKey(), vRoleApi.getOperatingStatus()));
                }
            }

            if (!condition.isEmpty()) {
                conditions.add(condition);
            }
            if(permissionService.getContext().getAfterFunction() != null){
                permissionService.getContext().getAfterFunction().apply(vRoleApi,conditions);
            }

        });

        if (!conditions.isEmpty()) {
            conditions.forEach(condition -> {
                sqlString.append("(").append(String.join(" AND ", condition)).append(")");
            });
        }
        return sqlString;

    }


}
