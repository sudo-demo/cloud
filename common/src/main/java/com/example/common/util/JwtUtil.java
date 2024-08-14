package com.example.common.util;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.example.common.domain.LoginUser;
import com.example.common.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;


@Slf4j
@Component
public class JwtUtil {
    private final String CLAIM_KEY_USERNAME = "sub";//用户
    private final String TOKEN_ID = "ID";//tokenid
    private final String ACCESS_TOKEN = "login_tokens:";// token在redis中的key

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    /**
     * 秘钥
     */
    @Value("${jwt.secret}")
    private String secret;
    /**
     * 超时时间
     */
    @Value("${jwt.expiration}")
    private Integer expiration;


    private String getTokenKey(String tokenId) {
        return ACCESS_TOKEN + tokenId;
    }

    public byte[] getSecret() {
        return secret.getBytes();
    }


    public String createToken(User user) {
        String tokenId = IdUtil.nanoId();
        String token = JWT.create()
                .setSubject(CLAIM_KEY_USERNAME)
                .setSigner(JWTSignerUtil.hs256(getSecret()))
//                .setIssuedAt(new Date())
//                .setExpiresAt(DateUtil.offsetMillisecond(DateUtil.date(), expiration))//设置过期时间
                .setPayload(TOKEN_ID, tokenId)
                .sign();

        // 存储
        refreshToken(user, tokenId);

        return token;
    }

    public void refreshToken(User user, String tokenId) {
        RedisUtil.set(getTokenKey(tokenId), JSON.toJSONString(user, JSONWriter.Feature.WriteMapNullValue), expiration);
    }

    public String getTokenId(String token) {
        try {
            JWT jwt = JWT.of(token);
            return (String) jwt.getPayload(TOKEN_ID);
        } catch (Exception e) {
            // Token 验证失败
//            e.printStackTrace();
            return null;
        }
    }

    public User getLoginUser(HttpServletRequest request) {
        String tokenId = getTokenId(getToken(request));
        if (ObjectUtil.isNull(tokenId)) {
            return null;
        }
        Object user = RedisUtil.get(getTokenKey(tokenId));
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        return JSON.parseObject(user.toString(), User.class);
    }

    public User getUser() {
        // 先从上下文获取用户
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return loginUser.getUser();
        } catch (Exception exception) {
            // 不做处理
        }
        String token = getToken();
        return ObjectUtil.isNull(getLoginUser(token)) ? new User() : getLoginUser(token);
    }


    public Set<Long> getRoleIds() {
        User user = getUser();
        return user.getRoleIds();
    }

    public User getLoginUser(String token) {
        String tokenId = getTokenId(token);
        if (ObjectUtil.isNull(tokenId)) {
            return null;
        }
        Object user = RedisUtil.get(getTokenKey(tokenId));
        return JSON.parseObject(user.toString(), User.class);
    }

    public String getToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(tokenHeader);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(tokenHead)) {
            token = token.replaceFirst(tokenHead, "");
        }
        return token;
    }

    public String getToken(HttpServletRequest request) {
        // 从header获取token标识
        String token = request.getHeader(tokenHeader);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(tokenHead)) {
            token = token.replaceFirst(tokenHead, "");
        }
        return token;
    }


}
