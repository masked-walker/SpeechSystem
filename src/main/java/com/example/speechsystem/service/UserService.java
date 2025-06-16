package com.example.speechsystem.service;

import com.example.speechsystem.mapper.UserMapper;
import com.example.speechsystem.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 新增用户
     *
     * @param username 用户名
     * @param email 用户邮箱
     * @param password 密码（建议后续加密）
     * @return 插入结果
     */
    public String insert(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return "succeed";
    }

    /**
     * 查询用户信息
     *
     * @param id 用户ID
     * @return 用户对象
     */
    public User findById(int id) {
        return userMapper.findById(id);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     */
    public void update(User user) {
        userMapper.update(user);
    }

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     */
    public void delete(int id) {
        userMapper.delete(id);
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    public List<User> findAll() {
        return userMapper.findAll();
    }
}
