package com.board.game.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 테스트용 컨트롤러
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket 메시지 전송 테스트
     * GET /api/test/ws?topic=/topic/game.abc123.RED&message=Hello
     */
    @GetMapping("/ws")
    public Map<String, String> testWebSocket(
            @RequestParam String topic,
            @RequestParam(defaultValue = "Test message") String message) {

        System.out.println("🧪 TEST: Sending to topic: " + topic);
        System.out.println("🧪 TEST: Message: " + message);

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(topic, payload);

        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("topic", topic);
        response.put("message", message);

        return response;
    }

    /**
     * 특정 방의 모든 플레이어에게 테스트 메시지 전송
     * GET /api/test/broadcast?roomId=abc123
     */
    @GetMapping("/broadcast")
    public Map<String, String> testBroadcast(@RequestParam String roomId) {
        String topicRed = "/topic/game." + roomId + ".RED";
        String topicBlue = "/topic/game." + roomId + ".BLUE";

        System.out.println("🧪 TEST BROADCAST to room: " + roomId);

        Map<String, Object> testMessage = new HashMap<>();
        testMessage.put("test", true);
        testMessage.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(topicRed, testMessage);
        messagingTemplate.convertAndSend(topicBlue, testMessage);

        Map<String, String> response = new HashMap<>();
        response.put("status", "broadcast");
        response.put("topicRed", topicRed);
        response.put("topicBlue", topicBlue);

        return response;
    }
}
