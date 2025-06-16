package com.example.speechsystem.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.speechsystem.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class TokenUtils {

    // Token过期时间（毫秒），这里设置为7天
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;
    // Token私钥，实际项目中应配置在application.properties中
    private static final String TOKEN_SECRET = "your_complex_secret_key_here";

    // Redis中Token和用户信息的Key前缀
    private static final String REDIS_TOKEN_KEY_PREFIX = "token:";
    private static final String REDIS_USER_INFO_KEY_PREFIX = "user:";


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成Token (此方法已不直接使用，因为UserController中改为使用JwtUtils)
     * @param user 用户对象
     * @return Token字符串
     */
    public String genToken(User user) {
        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        String token = JWT.create()
                .withAudience(String.valueOf(user.getId())) // 将 user id 保存到 payload
                .withClaim("username", user.getUsername()) // 将 username 保存到 payload
                .withExpiresAt(expirationDate) // 设置过期时间
                .sign(Algorithm.HMAC256(TOKEN_SECRET)); // 使用私钥进行签名
        return token;
    }

    // =================================================================
    // ==================== 在此处添加缺失的方法 ========================
    /**
     * 将Token字符串存入Redis
     * @param token JWT字符串
     * @param userId 用户ID
     */
    public void saveToken(String token, Integer userId) {
        String key = REDIS_TOKEN_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }
    // =================================================================
    // =================================================================


    /**
     * 将用户信息存入Redis
     * @param user 用户对象
     */
    public void saveUserInfo(User user) {
        String key = REDIS_USER_INFO_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, user, EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 从Redis中获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserInfo(Integer userId) {
        String key = REDIS_USER_INFO_KEY_PREFIX + userId;
        return (User) redisTemplate.opsForValue().get(key);
    }

    /**
     * 更新Redis中的用户信息
     * @param user 用户对象
     */
    public void updateUserInfo(User user) {
        String key = REDIS_USER_INFO_KEY_PREFIX + user.getId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, user, EXPIRE_TIME, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 从Redis中移除Token和用户信息
     * @param userId 用户ID
     */
    public void removeToken(Integer userId) {
        String tokenKey = REDIS_TOKEN_KEY_PREFIX + userId;
        redisTemplate.delete(tokenKey);
        // 退出登录时也一并清除用户信息缓存
        removeUserInfo(userId);
    }

    public void removeUserInfo(Integer userId) {
        String userKey = REDIS_USER_INFO_KEY_PREFIX + userId;
        redisTemplate.delete(userKey);
    }
}