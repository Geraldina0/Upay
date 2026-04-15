package com.example.demo.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Object emailAttr = session.getAttributes().get("email");
        if (emailAttr instanceof String email) {
            userSessions.put(email, session);
            System.out.println("WebSocket connected for user: " + email);
        } else {
            System.out.println("WebSocket connected without user email");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        Object emailAttr = session.getAttributes().get("email");
        if (emailAttr instanceof String email) {
            userSessions.remove(email);
            System.out.println("WebSocket disconnected for user: " + email);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("Received message: " + message.getPayload());
    }

    public void sendToUser(String email, String message) {
        WebSocketSession session = userSessions.get(email);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                throw new RuntimeException("Failed to send websocket message to user: " + email, e);
            }
        }
    }
}