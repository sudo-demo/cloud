package com.example.common.config.Security;

import com.example.common.config.Mybatis.DataScopeInterceptor;
import com.example.common.config.Mybatis.RoleDataPermissionHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author huanghongjia
 */
@Component
@Accessors(chain = true)
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

//        @ApiModelProperty( "当前所有角色的控制器权限" )
//        private Map< Long, Map< String, Map< String, RoleMenuApiV >> > currentControllerRoleAuth = new HashMap<>();
//
//        @ApiModelProperty( "当前所有角色的方法权限" )
//        private Map< Long, Map< String, Map< String, RoleMenuApiV > > > currentActionRoleAuth = new HashMap<>();

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
}
