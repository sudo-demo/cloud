package com.example.common.util;


import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.example.common.domain.User;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


@Component
public class JwtUtil {
    private static final String CLAIM_KEY_USERNAME = "sub";// JWT中的用户标识
    private static final String TOKEN_ID = "ID";// JWT中的token ID
    private static final String ACCESS_TOKEN = "login_tokens:";// token在Redis中的key前缀


    private static String tokenHeader;// 从请求中获取token的header名称

    @Value("${jwt.tokenHeader}")
    public void setTokenHeader(String tokenHeader) {
        JwtUtil.tokenHeader = tokenHeader;
    }

    @Contract(pure = true)
    public static String getTokenHeader() {
        return tokenHeader;
    }

    private static String tokenHead;// token前缀

    @Value("${jwt.tokenHead}")
    public void setTokenHead(String tokenHead) {
        JwtUtil.tokenHead = tokenHead;
    }

    @Contract(pure = true)
    public static String getTokenHead() {
        return tokenHead;
    }

    /**
     * 秘钥，用于签名和验证JWT
     */
    @Value("${jwt.secret}")
    private String secret;


    /**
     * 超时时间，单位为秒
     */
    @Value("${jwt.expiration}")
    private Integer expiration;


    /**
     * 根据tokenId生成Redis中的token key
     */
    public static String getTokenKey(String tokenId) {
        return ACCESS_TOKEN + tokenId;
    }

    /**
     * 获取秘钥的字节数组
     */
    public byte[] getSecret() {
        return secret.getBytes();
    }


    /**
     * 创建JWT token
     */
    public String createToken(User user) {
        String tokenId = IdUtil.nanoId(); // 生成唯一的token ID
        String token = JWT.create()
                .setSubject(CLAIM_KEY_USERNAME) // 设置JWT的主题
                .setSigner(JWTSignerUtil.hs256(getSecret())) // 设置签名算法
//                .setIssuedAt(new Date()) // 设置签发时间
//                .setExpiresAt(DateUtil.offsetMillisecond(DateUtil.date(), expiration)) // 设置过期时间
                .setPayload(TOKEN_ID, tokenId) // 设置token ID到payload
                .sign(); // 签名生成token

        // 存储用户信息到Redis
        refreshToken(user, tokenId);

        return token; // 返回生成的token
    }

    /**
     * 刷新token，存储用户信息到Redis
     */
    public void refreshToken(User user, String tokenId) {
//        RedisUtil.set(getTokenKey(tokenId), JSON.toJSONString(user, JSONWriter.Feature.WriteMapNullValue), expiration);
        RedisUtil.set(getTokenKey(tokenId),user, expiration);
    }

    /**
     * 从token中获取token ID
     */
    public static String getTokenId(String token) {
        try {
            JWT jwt = JWT.of(token); // 解析token
            return (String) jwt.getPayload(TOKEN_ID); // 获取token ID
        } catch (Exception e) {
            // Token 验证失败
            return null; // 返回null表示验证失败
        }
    }


    /**
     * 根据token获取存在Redis的token key
     */
    public static String getTokenKey(HttpServletRequest request) {
        return JwtUtil.getTokenKey(JwtUtil.getTokenId(JwtUtil.getToken(request)));
    }

    /**
     * 获取请求中的token
     */
    public static String getToken(HttpServletRequest request){
        String token = request.getHeader(JwtUtil.getTokenHeader()); // 从header获取token
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(JwtUtil.getTokenHead())) {
            token = token.replaceFirst(JwtUtil.getTokenHead(), ""); // 去掉前缀
        }
        return token; // 返回token
    }



}
