package com.example.common.config.Security;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.domain.VRoleApi;
import com.example.common.util.JwtUtil;
import com.example.common.util.RedisUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Component
@Accessors(chain = true)
public class Permission2Service {

    /**
     * 角色id
     */
    private Set<Long> roleIds;

    /**
     * 模块/控制器
     */
    private String appController;

    /**
     * 方法
     */
    private String action;

    /**
     * 当前所有角色的控制器权限
     */
    private Map<Long, VRoleApi> currentControllerRoleAuth = new HashMap<>();


    /**
     * 当前所有角色的方法权限、不随 $appController 改变
     */
    private  Map<Long,VRoleApi> currentActionRoleAuth = new HashMap<>();

    /**
     * 用户当前所有角色
     */
    private Set<Long> roleId = new HashSet<>();

    @Value("${spring.application.name}")
    private String app;

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Resource
    JwtUtil jwtUtil;

    /**
     * 权限sql
     */
    private String conditions;


    /**
     * 验证接口权限
     */
    public boolean verifyAuth()
    {
        System.out.println("控制器权限："+this.currentControllerRoleAuth);
        System.out.println("方法权限："+this.currentActionRoleAuth);
        if(ObjectUtil.isEmpty(this.currentActionRoleAuth)){
            return false;
        }
        return true;
    }


//    public void init(HttpServletRequest request) throws Exception {
//        this.currentControllerRoleAuth = new HashMap<>();
//        this.currentActionRoleAuth = new HashMap<>();
//        this.roleIds = jwtUtil.getRoleIds();
//        this.conditions = null;
//        HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(request);
//        if (executionChain != null && (executionChain.getHandler() instanceof HandlerMethod)) {
//            HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
//            String controller = handlerMethod.getBeanType().getSimpleName().replace("Controller", "");
//
//            this.appController = app + "/" + controller;
//            this.action = handlerMethod.getMethod().getName();
//            System.out.println("角色：" + this.roleIds);
//            System.out.println("请求的模块/控制器：" + this.appController);
//            System.out.println("请求的方法：" + this.action);
//        }
//        for (Long roleId : this.roleIds) {
//            VRoleApi cacheMapValue = RedisUtil.hget("role_" + roleId, this.appController);
//            if(cacheMapValue == null)  continue;
//            currentControllerRoleAuth.put(roleId,cacheMapValue);
//            if(cacheMapValue.getChildren().get(this.action) != null){
//                currentActionRoleAuth.put(roleId,cacheMapValue.getChildren().get(this.action));
//            }
//        }
//
//
//
//    }
}
