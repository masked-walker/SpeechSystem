package com.example.speechsystem.controller;

import com.example.speechsystem.pojo.User;
import com.example.speechsystem.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "添加用户")
    @PostMapping
    public String insert(@RequestBody User user) {
        return userService.insert(user.getUsername(), user.getEmail(), user.getPassword());
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public User findById(@PathVariable int id) {
        return userService.findById(id);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping
    public void update(@RequestBody User user) {
        userService.update(user);
    }

    @Operation(summary = "删除指定ID的用户")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        userService.delete(id);
    }

    @Operation(summary = "查询所有用户")
    @GetMapping
    public List<User> findAll() {
        return userService.findAll();
    }
}
