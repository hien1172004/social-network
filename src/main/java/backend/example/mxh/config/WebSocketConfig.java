package backend.example.mxh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // xac thuc socket phai dang nhap

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Prefix cho các topic server gui cho client
        config.setApplicationDestinationPrefixes("/app"); // Prefix cho các message client gui len server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // client ket noi voi socket
                .setAllowedOrigins("*") // Trong môi trường production nên giới hạn origin
                .withSockJS(); // Hỗ trợ fallback cho các trình duyệt không hỗ trợ WebSocket
    }
}