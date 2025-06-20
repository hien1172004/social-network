package backend.example.mxh.controller;

import backend.example.mxh.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketUserController {
    private final UserService userService;

    @MessageMapping("/status/online")
    public void online(@Payload long userId) {
        userService.setUserOnline(userId);
        log.info("User {} is online", userId);
    }

    @MessageMapping("/status/offline")
    public void offline(@Payload long userId) {
        userService.setUserOffline(userId);
        log.info("User {} is offline", userId);
    }

    @MessageMapping("/user/ping")
    public void ping(@Payload long userId) {
        userService.updateLastActiveTime(userId);
        log.info("User {} is ping", userId);
    }
}
