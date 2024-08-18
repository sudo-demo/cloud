package com.example.common.config.Security;

import com.example.common.config.Mybatis.DataScopeInterceptor;
import com.example.common.domain.VRoleApi;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.example.common.util.SecurityUtils;

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

        @ApiModelProperty("角色id")
        private Set<Long> roleIds;

        @ApiModelProperty("模块/控制器")
        private String appController;

        @ApiModelProperty("方法")
        private String action;

        @ApiModelProperty( "当前所有角色的控制器权限" )
        private Map< Long, Map< String, Map< String, VRoleApi>> > currentControllerRoleAuth = new HashMap<>();

        @ApiModelProperty( "当前所有角色的方法权限" )
        private Map< Long, Map< String, Map< String, VRoleApi > > > currentActionRoleAuth = new HashMap<>();

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

    /**
     * 初始化之前清空参数
     */
    public void clearDataScope( HttpServletRequest request ) {
        clearContext();
        getContext().setRoleIds( SecurityUtils.getRoleIds() );
        try {
            HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler( request );
            if ( executionChain != null && ( executionChain.getHandler() instanceof HandlerMethod) ) {
                HandlerMethod handlerMethod = ( HandlerMethod ) executionChain.getHandler();
                String controller = handlerMethod.getBeanType().getSimpleName().replace( "Controller", "" );
                getContext().setAppController( controller );
                getContext().setAction( handlerMethod.getMethod().getName() );

            }
        } catch ( Exception e ) {
            log.error(e.getMessage(),e);
            e.printStackTrace();
        }
    }

    /**
     * 验证接口权限
     */
    public boolean verifyAuth( HttpServletRequest request ) {
        clearDataScope( request );
        init();
        if ( CollectionUtils.isEmpty( getContext().getCurrentActionRoleAuth() ) ) {
            return false;
        }
        return true;
    }

    public void init() {

    }
}
