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
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 角色数据权限处理器：拦截SQL并动态拼接数据权限条件
 */
@Component
@Slf4j
public class RoleDataPermissionHandler extends JsqlParserSupport implements InnerInterceptor {

    @Resource
    private PermissionService permissionService;
    @Resource
    private ApplicationContext applicationContext;

    // 数据范围占位符（从Permission类统一获取）
    private static final String DATA_SCOPE_PLACEHOLDER = Permission.getDataScope();

    /**
     * 查询操作前置拦截：修改SQL添加权限条件
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 跳过无需拦截的场景
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        PermissionService.PermissionContext context = permissionService.getContext();
        if (context == null || !ms.getId().equals(context.getMappedStatementId())) {
            return;
        }

        // 生成权限条件并处理SQL
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        String originalSql = mpBs.sql();
        String permissionCondition = buildPermissionCondition();
        if (StrUtil.isNotBlank(permissionCondition)) {
            String processedSql = processSqlWithPermission(originalSql, permissionCondition);
            mpBs.sql(this.parserSingle(processedSql, ms.getId()));
        }
    }

    /**
     * 更新操作前置拦截：防止无WHERE条件的全表更新
     * <p>
     * 此方法会在执行UPDATE语句前被调用，用于检查更新操作是否包含WHERE条件，
     * 防止误操作导致全表更新。当前实现为空，可根据需要添加具体校验逻辑。
     * </p>
     *
     * @param executor MyBatis执行器，用于执行SQL语句
     * @param ms MappedStatement对象，包含SQL语句的映射信息
     * @param parameter SQL语句的参数对象
     * @see #processUpdate 实际执行WHERE条件检查的方法
     */
    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        // 可添加更新权限校验逻辑
    }

    /**
     * 处理SELECT语句的拦截逻辑（可扩展实现细粒度控制）
     * <p>
     * 此方法会在解析SELECT语句时被调用，子类可通过重写该方法实现对SELECT语句的
     * 细粒度控制，例如添加自定义条件、修改查询字段等。
     * </p>
     *
     * @param select 被解析的SELECT语句对象，包含完整的查询结构
     * @param index SQL语句在批量操作中的索引位置（从0开始）
     * @param sql 原始SQL语句字符串
     * @param obj 附加参数，通常为MappedStatement的ID
     * @see InnerInterceptor MyBatis-Plus内部拦截器接口
     * @see #beforeQuery 前置查询拦截入口
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        // 扩展用：细粒度控制SELECT语句
    }

    /**
     * 处理UPDATE语句的拦截逻辑
     * <p>
     * 该方法会在解析UPDATE语句时被调用，主要用于检查UPDATE语句是否包含WHERE条件，
     * 防止无条件的全表更新操作，确保数据安全。
     * </p>
     *
     * @param update 被解析的UPDATE语句对象
     * @param index SQL语句在批量操作中的索引位置（从0开始）
     * @param sql 原始SQL语句字符串
     * @param obj 附加参数，通常为MappedStatement的ID
     * @throws SecurityException 当UPDATE语句缺少WHERE条件时抛出
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        if (update.getWhere() == null) {
            throw new SecurityException("更新操作必须包含WHERE条件（数据权限拦截）");
        }
    }

    // ------------------------- 核心逻辑：构建权限条件 -------------------------

    /**
     * 生成权限过滤条件（反射调用入口）
     */
    private String buildPermissionCondition() {
        PermissionService.PermissionContext context = permissionService.getContext();
        if (context == null) {
            return "";
        }

        try {
            // 反射调用上下文指定的方法生成条件（如handleDataScope）
            Class<?> clazz = context.getClazz();
            String methodName = context.getCallMethod();
            if (clazz == null || StrUtil.isBlank(methodName)) {
                return "";
            }
            Object bean = applicationContext.getBean(clazz);
            Method method = clazz.getMethod(methodName);
            Object result = method.invoke(bean);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            throw new RuntimeException("权限条件生成失败", e);
        }
    }

    /**
     * 处理SQL：替换占位符或拼接WHERE条件
     */
    private String processSqlWithPermission(String originalSql, String condition) {
        if (originalSql.contains(DATA_SCOPE_PLACEHOLDER)) {
            // 替换占位符
            boolean hasWhere = originalSql.toLowerCase().contains(" where ");
            String replacement = hasWhere ? " AND " + condition : " WHERE " + condition;
            return originalSql.replace(DATA_SCOPE_PLACEHOLDER, replacement);
        } else {
            // 无占位符时拼接条件（用JsqlParser安全解析）
            try {
                Select select = (Select) CCJSqlParserUtil.parse(originalSql);
                Expression permissionExp = CCJSqlParserUtil.parseCondExpression(condition);
                SelectBody selectBody = select.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectBody;
                    Expression oldWhere = plainSelect.getWhere();
                    plainSelect.setWhere(oldWhere == null ? permissionExp : new AndExpression(oldWhere, permissionExp));
                }
                return select.toString();
            } catch (Exception e) {
                throw new RuntimeException("SQL解析失败", e);
            }
        }
    }

    /**
     * 核心：生成数据权限SQL条件（直接在一个方法内体现完整逻辑）
     */
    public String handleDataScope() {
        // 1. 获取权限上下文，为空直接返回空条件
        PermissionService.PermissionContext context = permissionService.getContext();
        if (context == null) {
            return "";
        }

        // 2. 原始逻辑：用嵌套列表存储条件（外层：条件组，内层：组内AND条件）
        List<List<String>> conditions = new ArrayList<>();
        // 获取当前角色的方法权限映射（原始逻辑中的权限数据源）
        Map<String, VRoleApi> currentActionRoleAuth = context.getCurrentActionRoleAuth();

        // 3. 权限映射为空则返回空
        if (CollectionUtils.isEmpty(currentActionRoleAuth)) {
            return "";
        }

        // 4. 准备原始逻辑中的基础参数
        String masterAlias = context.getMasterAlias(); // 主表别名
        Long userId = SecurityUtil.getUserId();       // 当前用户ID
        Long roleId = SecurityUtil.getRoleId();       // 当前角色ID

        // 5. 遍历权限映射，生成条件（完全还原原始循环逻辑）
        for (Map.Entry<String, VRoleApi> entry : currentActionRoleAuth.entrySet()) {
            VRoleApi vRoleApi = entry.getValue(); // 单个权限配置

            // 5.1 原始逻辑：特殊情况处理（数据范围和操作状态均为"%"则清空条件）
            if ("%".equals(vRoleApi.getDataScope()) && "%".equals(vRoleApi.getOperatingStatus())) {
                conditions.clear(); // 清空所有条件，后续拼接结果为空
                return ""; // 直接返回空，避免继续执行
            }

            // 5.2 原始逻辑：存储单组条件（组内条件用AND连接）
            List<String> singleCondition = new ArrayList<>();

            // 5.3 处理数据范围条件
            String dataScope = vRoleApi.getDataScope();
            switch (dataScope){
                case "user_id":
                    // 原始格式：主表别名 + "user_id = '用户ID'"
                    singleCondition.add(StrUtil.format(" {}user_id = '{}'", masterAlias, userId));
                    break;
                case "role_id":
                    // 原始格式：主表别名 + "role_id = '角色ID'"
                    singleCondition.add(StrUtil.format(" {}role_id = '{}'", masterAlias, roleId));
                    break;
            }

            // 5.4 处理操作状态条件（还原原始IN/=逻辑）
            String operatingStatus = vRoleApi.getOperatingStatus();
            if (!"%".equals(operatingStatus)) { // 不是"%"才处理
                String dataKey = vRoleApi.getDataKey(); // 关联字段名
                if (operatingStatus.contains(",")) {
                    // 原始逻辑：多值用IN，格式：主表别名.字段 IN ('值1','值2')
                    String[] split = operatingStatus.split(",");
                    String inString = "'" + ArrayUtil.join(split, "','") + "'";
                    singleCondition.add(StrUtil.format(" {}{} in ({})", masterAlias, dataKey, inString));
                } else {
                    // 原始逻辑：单值用=，格式：主表别名.字段 = '值'
                    singleCondition.add(StrUtil.format(" {}{} = '{}'", masterAlias, dataKey, operatingStatus));
                }
            }

            // 5.5 单组条件非空则添加到外层列表（原始逻辑的条件组存储）
            if (!singleCondition.isEmpty()) {
                conditions.add(singleCondition);
            }

            // 5.6 原始逻辑：执行后置处理函数（保留异常捕获）
            if (context.getAfterFunction() != null) {
                try {
                    context.getAfterFunction().apply(vRoleApi, conditions);
                } catch (Exception e) {
                    log.warn("后置处理函数执行异常", e);
                }
            }
        }

        // 6. 还原原始拼接逻辑：外层条件组用AND连接，内层用AND连接
        List<String> flatConditions = new ArrayList<>();
        for (List<String> conditionGroup : conditions) {
            flatConditions.add("(" + String.join(" AND ", conditionGroup) + ")");
        }

        // 7. 最终拼接结果（与原始逻辑完全一致）
        return flatConditions.isEmpty() ? "" : String.join(" AND ", flatConditions);
    }
}