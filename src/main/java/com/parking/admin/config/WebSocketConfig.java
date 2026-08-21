package com.parking.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP 브로커 설정. 구독 토픽: /topic/dashboard/{kpi|gates|events|alerts}.
 * 핸드셰이크 엔드포인트 경로(/ws/dashboard)는 design.md에 명시가 없어 임의로 정했다(가정 — Issues 참고,
 * frontend-expert는 이 경로로 연결해야 한다).
 * 근거: design.md [Screen 1] 모듈경계 DashboardWebSocketPublisher, 입출력계약
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/dashboard").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
