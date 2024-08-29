package com.example.common.config.Security;

import cn.hutool.core.util.ObjectUtil;
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
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Component
@Accessors(chain = true)
@Slf4j
public class PermissionService {
    private static final ThreadLocal<PermissionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    public PermissionContext getContext() {
        PermissionContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = new PermissionContext();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    public void setContext(PermissionContext context) {
        CONTEXT_HOLDER.set(context);
    }

    @Data
    @ApiModel("权限实体")
    public static class PermissionContext {

        @ApiModelProperty("所有角色id")
        private Set<Long> roleIds;

        @ApiModelProperty("当前角色id")
        private Long roleId;

        @ApiModelProperty("模块/控制器")
        private String appController;

        @ApiModelProperty("方法")
        private String action;

        @ApiModelProperty("接口名称")
        private String apiName;

        @ApiModelProperty("当前所有角色的控制器权限")
        private Map<String, Map<String, VRoleApi>> currentControllerRoleAuth = new HashMap<>();

        @ApiModelProperty("当前所有角色的方法权限")
        private Map<String, VRoleApi> currentActionRoleAuth = new HashMap<>();

        @ApiModelProperty("mapper")
        private String mappedStatementId;

        @ApiModelProperty("主表的别名")
        private String masterAlias;

        @ApiModelProperty("权限sql")
        private String conditions;

        @ApiModelProperty("处理类")
        private Class<?> clazz = DataScopeInterceptor.class;

        @ApiModelProperty("处理方法")
        private String callMethod = "handleDataScope";

    }

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${spring.application.name}")
    private String appModel;//模块

    /**
     * 初始化之前清空参数
     */
    public void clearDataScope(HttpServletRequest request) {
        clearContext();
        getContext().setRoleIds(SecurityUtil.getRoleIds());
        getContext().setRoleId(SecurityUtil.getRoleId());
        try {
            HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(request);
            if (executionChain != null && (executionChain.getHandler() instanceof HandlerMethod)) {
                HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
                String controller = handlerMethod.getBeanType().getSimpleName().replace("Controller", "");
                getContext().setAppController(appModel + "/" + controller);//控制器名称
                getContext().setAction(handlerMethod.getMethod().getName());//方法名称
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /**
     * 验证接口权限
     */
    public boolean verifyAuth(HttpServletRequest request) {
        clearDataScope(request);
        init();
        if (CollectionUtils.isEmpty(getContext().getCurrentActionRoleAuth())) {
            return false;
        }
        return true;
    }

    /**
     * 权限初始化
     */
    public void init() {
        VRoleApi cacheMapValue = RedisUtil.hget("role_" + getContext().getRoleId(), getContext().getAppController());
        if (cacheMapValue != null) {
            getContext().setApiName(cacheMapValue.getChildren().get(getContext().getAction()).getApiName());
            Map<String, Map<String, VRoleApi>> currentControllerRoleAuth = new HashMap<>();
            currentControllerRoleAuth.put(cacheMapValue.getAppModel(), cacheMapValue.getChildren());
            getContext().getCurrentControllerRoleAuth().putAll(currentControllerRoleAuth);
            if (ObjectUtil.isNotNull(cacheMapValue.getChildren().get(getContext().getAction()))) {
                Map<String, VRoleApi> currentActionRoleAuth = new HashMap<>();
                currentActionRoleAuth.put(getContext().getAction(), cacheMapValue.getChildren().get(getContext().getAction()));
                getContext().getCurrentActionRoleAuth().putAll(currentActionRoleAuth);
            }
        }
    }
}
