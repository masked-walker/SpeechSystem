package com.example.speechsystem.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.speechsystem.pojo.User; // 确保 User 类的导入是正确的

import java.util.Date;
import java.util.Map;

public class JwtUtils {

    // Token过期时间（毫秒），这里设置为7天
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;
    // Token私钥，实际项目中应配置在application.properties中，并且更加复杂
    private static final String TOKEN_SECRET = "your_complex_secret_key_for_speech_system_project";

    /**
     * 生成JWT签名
     * @param claims 载荷中携带的信息
     * @return 加密后的token
     */
    public static String generateJwt(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // 创建JWT builder
        // 将整个 claims Map 存储在名为 "claims" 的一个主载荷下
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(expirationDate) // 设置过期时间
                .sign(Algorithm.HMAC256(TOKEN_SECRET)); // 使用私钥进行签名
    }

    /**
     * 校验JWT签名并解析出我们存入的Map
     * @param token 加密后的token
     * @return 解析后的claims Map
     */
    public static Map<String, Object> parseJWT(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(TOKEN_SECRET)).build();
            DecodedJWT jwt = verifier.verify(token);

            // 直接获取名为 "claims" 的载荷，并将其作为 Map 返回
            // 这修正了之前错误的 asObject() 调用
            return jwt.getClaim("claims").asMap();
        } catch (Exception e) {
            // token 校验失败，例如过期、签名错误等
            // 打印日志或处理异常
            System.err.println("JWT a解析失败: " + e.getMessage());
            return null;
        }
    }
}