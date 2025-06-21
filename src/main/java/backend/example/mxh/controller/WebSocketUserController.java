package backend.example.mxh.controller;

import backend.example.mxh.DTO.request.MessageDTO;
import backend.example.mxh.DTO.request.ReadAllMessagesDTO;
import backend.example.mxh.DTO.request.ReadMessageDTO;
import backend.example.mxh.exception.ResourceNotFoundException;
import backend.example.mxh.service.MessageService;
import backend.example.mxh.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketUserController {
    private final UserService userService;
    private final MessageService messageService;
    
    @MessageMapping("/status/online")
    public void online(@Payload long userId) {
        try {
            userService.setUserOnline(userId);
            log.info("User {} is online", userId);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for online status: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error setting user online: {}", userId, e);
            throw e;
        }
    }

    @MessageMapping("/status/offline")
    public void offline(@Payload long userId) {
        try {
            userService.setUserOffline(userId);
            log.info("User {} is offline", userId);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for offline status: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error setting user offline: {}", userId, e);
            throw e;
        }
    }

    @MessageMapping("/user/ping")
    public void ping(@Payload long userId) {
        try {
            userService.updateLastActiveTime(userId);
            log.info("User {} is ping", userId);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for ping: {}", userId);
            throw e;
        } catch (Exception e) {
            log.error("Error updating user ping: {}", userId, e);
            throw e;
        }
    }

    @MessageMapping("/send-message")
    public void sendMessage(@Payload MessageDTO messageDTO) {
        try {
            messageService.sendMessage(messageDTO);
            log.info("Message {} has been sent", messageDTO);
        } catch (Exception e) {
            log.error("Error sending message: {}", messageDTO, e);
            throw e;
        }
    }

    @MessageMapping("/read-message")
    public void readMessage(@Payload ReadMessageDTO readMessageDTO) {
        try {
            messageService.markMessageAsRead(readMessageDTO.getMessageId(), readMessageDTO.getUserId());
        } catch (Exception e) {
            log.error("Error marking message as read: {}", readMessageDTO, e);
            throw e;
        }
    }
    
    @MessageMapping("/read-allmessage")
    public void readAllMessage(@Payload ReadAllMessagesDTO readAllMessagesDTO) {
        try {
            messageService.markAllMessagesAsRead(readAllMessagesDTO.getConversationId(), readAllMessagesDTO.getUserId());
        } catch (Exception e) {
            log.error("Error marking all messages as read: {}", readAllMessagesDTO, e);
            throw e;
        }
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        log.error("WebSocket error: ", exception);
        return "Error: " + exception.getMessage();
    }
}
