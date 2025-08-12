package com.example.common.config.Security;

import com.example.common.config.Mybatis.DataScopeInterceptor;
import com.example.common.domain.VRoleApi;
import com.example.common.util.RedisUtil;
import com.example.common.util.SecurityUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 权限服务类：负责权限上下文管理、权限初始化与验证、数据范围控制
 */
@Component
@Accessors(chain = true)
@Slf4j
public class PermissionService {

    @Resource
    RedisUtil redisUtil;

    // 线程局部变量存储权限上下文，保证线程安全
    private static final ThreadLocal<PermissionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 清除当前线程的权限上下文，避免内存泄漏
     */
    public void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取当前线程的权限上下文（懒加载创建）
     * @return 权限上下文对象
     */
    public PermissionContext getContext() {
        PermissionContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = new PermissionContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    /**
     * 手动设置权限上下文
     * @param context 权限上下文对象
     */
    public void setContext(PermissionContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 权限上下文实体：存储当前请求的权限相关信息
     */
    @Data
    @ApiModel("权限上下文实体")
    public static class PermissionContext {

        @ApiModelProperty("所有角色ID集合")
        private Set<Long> roleIds;

        @ApiModelProperty("当前角色ID")
        private Long roleId;

        @ApiModelProperty("模块/控制器标识（格式：模块名/控制器名）")
        private String appController;

        @ApiModelProperty("请求方法名")
        private String action;

        @ApiModelProperty("接口名称")
        private String apiName;

        @ApiModelProperty("当前所有角色的控制器权限映射（key：模块名，value：控制器下的方法权限）")
        private Map<String, Map<String, VRoleApi>> currentControllerRoleAuth = new HashMap<>();

        @ApiModelProperty("当前所有角色的方法权限映射（key：方法名，value：方法权限详情）")
        private Map<String, VRoleApi> currentActionRoleAuth = new HashMap<>();

        @ApiModelProperty("MyBatis映射语句ID")
        private String mappedStatementId;

        @ApiModelProperty("主表别名")
        private String masterAlias;

        @ApiModelProperty("权限过滤SQL条件")
        private String conditions;

        @ApiModelProperty("数据权限处理类（默认数据范围拦截器）")
        private Class<?> clazz = DataScopeInterceptor.class;

        @ApiModelProperty("数据权限处理方法名")
        private String callMethod = "handleDataScope";

        @ApiModelProperty("权限语句拼装后处理函数")
        private BiFunction<VRoleApi, List<List<String>>, List<List<String>>> afterFunction;

    }

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${spring.application.name}")
    private String appModel; // 应用模块名


    /**
     * 清除并初始化数据范围上下文（从请求中提取控制器和方法信息）
     * @param request HTTP请求对象
     */
    public void clearDataScope(HttpServletRequest request) {
        // 清除旧上下文，避免线程复用导致的数据污染
        clearContext();
        PermissionContext context = getContext();

        // 从安全工具类获取角色信息
        context.setRoleIds(SecurityUtil.getRoleIds());
        context.setRoleId(SecurityUtil.getRoleId());

        try {
            // 获取请求对应的处理器链，解析控制器和方法名
            HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(request);
            if (executionChain == null) {
                return; // 无对应处理器，直接返回
            }

            Object handler = executionChain.getHandler();
            // Java 8不支持instanceof模式匹配，使用传统方式
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                // 提取控制器名（去除"Controller"后缀）
                String controllerName = handlerMethod.getBeanType().getSimpleName()
                        .replace("Controller", "");
                // 拼接控制器标识（模块名/控制器名）
                context.setAppController(appModel + "/" + controllerName);
                // 提取方法名
                context.setAction(handlerMethod.getMethod().getName());
            }
        } catch (Exception e) {
            log.error("解析请求处理器信息失败", e);
            // 仅日志记录，避免异常向上传播影响主流程
        }
    }

    /**
     * 验证接口权限（基于初始化的权限上下文）
     * @param request HTTP请求对象
     * @return true-有权限，false-无权限
     */
    public boolean verifyAuth(HttpServletRequest request) {
        // 清除并初始化上下文
        clearDataScope(request);
        // 初始化权限信息
        init();
        // 方法权限非空则表示有权限
        return !CollectionUtils.isEmpty(getContext().getCurrentActionRoleAuth());
    }

    /**
     * 初始化权限信息（从Redis加载角色权限并填充到上下文）
     */
    public void init() {
        PermissionContext context = getContext();
        Long roleId = context.getRoleId();
        String appController = context.getAppController();

        // 从Redis获取角色-控制器对应的权限缓存（key: role_角色ID, field: 控制器标识）
        VRoleApi roleApiCache = redisUtil.hget("role_" + roleId, appController);
        if (roleApiCache == null) {
            return; // 无缓存直接返回
        }

        // 提取子节点（方法权限集合），减少重复调用
        Map<String, VRoleApi> methodAuthMap = roleApiCache.getChildren();
        if (CollectionUtils.isEmpty(methodAuthMap)) {
            return; // 无方法权限直接返回
        }

        String action = context.getAction();
        // 获取当前方法对应的权限详情
        VRoleApi actionAuth = methodAuthMap.get(action);

        // 设置接口名称（为空不处理，避免NPE）
        if (actionAuth != null) {
            context.setApiName(actionAuth.getApiName());
        }

        // 填充控制器权限（直接put避免中间Map创建）
        context.getCurrentControllerRoleAuth()
                .put(roleApiCache.getAppModel(), methodAuthMap);

        // 填充方法权限（存在权限时才添加）
        if (actionAuth != null) {
            context.getCurrentActionRoleAuth()
                    .put(action, actionAuth);
        }
    }

    /**
     * 数据范围处理（切换方法和控制器时重新初始化权限）
     * @param action 方法名
     * @param appController 控制器标识
     */
    public void dataScope(String action, String appController) {
        PermissionContext context = getContext();
        Set<Long> roleIds = context.getRoleIds();

        // 重置条件并设置角色、方法信息
        context.setConditions(null);
        context.setRoleIds(roleIds);
        context.setAction(action);

        // 控制器标识变化时才重新初始化权限（避免重复操作）
        if (!Objects.equals(context.getAppController(), appController)) {
            context.setAppController(appController);
            init();
        }
    }
}
