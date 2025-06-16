package com.example.speechsystem.mapper;

import com.example.speechsystem.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    // 插入用户
    @Insert("INSERT INTO users (username, email, password, created_at) VALUES (#{username}, #{email}, #{password}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // 自动生成主键并注入到 User 对象中
    int insert(User user);

    // 根据ID查找用户
    @Select("SELECT * FROM users WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "createdAt", column = "created_at")
    })
    User findById(@Param("id") int id);

    // 更新用户信息
    @Update("UPDATE users SET username = #{username}, email = #{email}, password = #{password} WHERE id = #{id}")
    int update(User user);

    // 删除用户
    @Delete("DELETE FROM users WHERE id = #{id}")
    int delete(@Param("id") int id);

    // 获取所有用户（可选）
    @Select("SELECT * FROM users")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<User> findAll();
}
