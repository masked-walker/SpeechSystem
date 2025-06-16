package com.example.speechsystem.websocket;

import com.alibaba.fastjson.JSON;
import com.example.speechsystem.pojo.Notification;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/notification/{userId}")
public class NotificationWebSocket {

    // 存储每个客户端对应的WebSocket session
    private static final ConcurrentHashMap<Integer, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        if (userId != null) {
            sessionMap.put(userId, session);
            log.info("WebSocket连接成功，当前用户ID：{}，当前在线人数：{}", userId, sessionMap.size());
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Integer userId) {
        if (userId != null) {
            sessionMap.remove(userId);
            log.info("WebSocket连接关闭，用户ID：{}，当前在线人数：{}", userId, sessionMap.size());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Integer userId) {
        log.info("收到来自用户ID {} 的消息: {}", userId, message);
        // 通常用于心跳检测等，此处简单打印日志
    }

    /**
     * 服务端主动发送通知对象
     * @param userId       接收通知的用户ID
     * @param notification 通知对象
     */
    public static void sendNotification(Integer userId, Notification notification) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                // 将Notification对象转换为JSON字符串发送
                String jsonMessage = JSON.toJSONString(notification);
                session.getBasicRemote().sendText(jsonMessage);
                log.info("成功向用户ID {} 推送通知: {}", userId, jsonMessage);
            } catch (IOException e) {
                log.error("向用户ID {} 推送通知失败: {}", userId, e.getMessage());
            }
        } else {
            log.warn("推送通知失败：用户ID {} 不在线。", userId);
        }
    }

    /**
     * 服务端主动发送文本消息（如封禁通知）
     * @param userId 接收消息的用户ID
     * @param message 文本消息
     */
    public static void sendBanNotification(Integer userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("成功向用户ID {} 推送封禁通知: {}", userId, message);
            } catch (IOException e) {
                log.error("向用户ID {} 推送封禁通知失败: {}", userId, e.getMessage());
            }
        } else {
            log.warn("推送封禁通知失败：用户ID {} 不在线。", userId);
        }
    }
}