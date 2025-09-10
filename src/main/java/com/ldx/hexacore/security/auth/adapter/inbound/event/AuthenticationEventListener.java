package com.ldx.hexacore.security.auth.adapter.inbound.event;

import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticateCommand;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationResult;
import com.ldx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import com.ldx.hexacore.security.auth.application.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

/**
 * Authentication Event Listener
 * 외부 시스템에서 발생하는 인증 관련 이벤트를 수신하고 처리합니다.
 */
@Component
@ConditionalOnBean(AuthenticationUseCase.class)
class AuthenticationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);

    private final AuthenticationUseCase authenticationUseCase;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public AuthenticationEventListener(
            AuthenticationUseCase authenticationUseCase,
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.authenticationUseCase = authenticationUseCase;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * 외부 인증 이벤트 처리
     * 외부 시스템에서 발생하는 인증 성공/실패 이벤트를 수신하여 처리합니다.
     */
    @EventListener
    @Async
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleExternalAuthEvent(String eventMessage) {
        logger.info("Received external authentication event");
        
        try {
            ExternalAuthEvent event = parseExternalAuthEvent(eventMessage);
            
            if (event == null) {
                logger.warn("Failed to parse external auth event: {}", eventMessage);
                sendToDeadLetterQueue(eventMessage, "Invalid event format");
                return;
            }

            processExternalAuthEvent(event);
            
        } catch (Exception e) {
            logger.error("Error processing external auth event: {}", e.getMessage(), e);
            handleEventProcessingError(eventMessage, e);
        }
    }

    /**
     * 인증 상태 동기화 이벤트 처리
     * 시스템 간 인증 상태 동기화를 위한 이벤트를 처리합니다.
     */
    @EventListener
    @Async
    public void handleAuthSyncEvent(String syncEventMessage) {
        logger.info("Received authentication sync event");
        
        try {
            AuthSyncEvent event = parseAuthSyncEvent(syncEventMessage);
            
            if (event == null) {
                logger.warn("Failed to parse auth sync event: {}", syncEventMessage);
                return;
            }

            processAuthSyncEvent(event);
            
        } catch (Exception e) {
            logger.error("Error processing auth sync event: {}", e.getMessage(), e);
        }
    }

    private void processExternalAuthEvent(ExternalAuthEvent event) {
        logger.debug("Processing external auth event for user: {}", event.getUsername());
        
        switch (event.getEventType()) {
            case "AUTHENTICATION_SUCCESS":
                handleExternalAuthSuccess(event);
                break;
            case "AUTHENTICATION_FAILURE":
                handleExternalAuthFailure(event);
                break;
            default:
                logger.warn("Unknown external auth event type: {}", event.getEventType());
        }
    }

    private void handleExternalAuthSuccess(ExternalAuthEvent event) {
        try {
            // 외부 시스템 인증 성공 시 내부 시스템에 인증 정보 동기화
            AuthenticateCommand command = new AuthenticateCommand(
                event.getUsername(), 
                null // 외부 인증이므로 비밀번호 불필요
            );
            
            AuthenticationResult result = authenticationUseCase.authenticate(command);
            
            if (result.isSuccess()) {
                logger.info("External authentication synchronized successfully for user: {}", 
                    event.getUsername());
            } else {
                logger.warn("Failed to synchronize external authentication for user: {}", 
                    event.getUsername());
            }
            
        } catch (Exception e) {
            logger.error("Error handling external auth success: {}", e.getMessage(), e);
            throw e; // 재시도를 위해 예외 재발생
        }
    }

    private void handleExternalAuthFailure(ExternalAuthEvent event) {
        logger.info("External authentication failed for user: {} - Reason: {}", 
            event.getUsername(), event.getAdditionalInfo());
        
        // 실패 이벤트는 로깅만 수행하고 별도 처리 없음
        // 필요시 보안 이벤트 생성 또는 알림 발송 가능
    }

    private void processAuthSyncEvent(AuthSyncEvent event) {
        logger.debug("Processing auth sync event for authentication: {}", 
            event.getAuthenticationId());
        
        try {
            // 인증 상태 동기화 로직
            // 실제 구현에서는 인증 상태를 업데이트하거나 관련 작업 수행
            logger.info("Authentication {} synchronized with status: {}", 
                event.getAuthenticationId(), event.getStatus());
                
        } catch (Exception e) {
            logger.error("Error processing auth sync event: {}", e.getMessage(), e);
        }
    }

    private ExternalAuthEvent parseExternalAuthEvent(String eventMessage) {
        try {
            return objectMapper.readValue(eventMessage, ExternalAuthEvent.class);
        } catch (Exception e) {
            logger.error("Failed to parse external auth event: {}", e.getMessage());
            return null;
        }
    }

    private AuthSyncEvent parseAuthSyncEvent(String eventMessage) {
        try {
            return objectMapper.readValue(eventMessage, AuthSyncEvent.class);
        } catch (Exception e) {
            logger.error("Failed to parse auth sync event: {}", e.getMessage());
            return null;
        }
    }

    private void handleEventProcessingError(String eventMessage, Exception error) {
        logger.error("Event processing failed, attempting retry. Error: {}", error.getMessage());
        
        // 재시도 횟수 초과 시 데드레터 큐로 전송
        if (isMaxRetryExceeded(error)) {
            sendToDeadLetterQueue(eventMessage, error.getMessage());
        }
    }

    private boolean isMaxRetryExceeded(Exception error) {
        // 실제 구현에서는 재시도 횟수를 확인하는 로직
        // 여기서는 단순히 ValidationException인 경우 재시도하지 않음
        return error instanceof ValidationException;
    }

    private void sendToDeadLetterQueue(String eventMessage, String reason) {
        logger.warn("Sending event to dead letter queue. Reason: {}", reason);
        
        try {
            // 실제 구현에서는 데드레터 큐에 메시지 전송
            // 여기서는 로깅만 수행
            DeadLetterEvent deadLetterEvent = new DeadLetterEvent(
                eventMessage, reason, System.currentTimeMillis()
            );
            
            eventPublisher.publishEvent(deadLetterEvent);
            
        } catch (Exception e) {
            logger.error("Failed to send event to dead letter queue: {}", e.getMessage(), e);
        }
    }
}

/**
 * 외부 인증 이벤트 DTO
 */
class ExternalAuthEvent {
    private String eventType;
    private String username;
    private String source;
    private String additionalInfo;

    public ExternalAuthEvent() {}

    public ExternalAuthEvent(String eventType, String username, String source, String additionalInfo) {
        this.eventType = eventType;
        this.username = username;
        this.source = source;
        this.additionalInfo = additionalInfo;
    }

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
}

/**
 * 인증 동기화 이벤트 DTO
 */
class AuthSyncEvent {
    private String authenticationId;
    private String status;
    private String source;

    public AuthSyncEvent() {}

    public AuthSyncEvent(String authenticationId, String status, String source) {
        this.authenticationId = authenticationId;
        this.status = status;
        this.source = source;
    }

    // Getters and setters
    public String getAuthenticationId() { return authenticationId; }
    public void setAuthenticationId(String authenticationId) { this.authenticationId = authenticationId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}

/**
 * 데드레터 이벤트
 */
class DeadLetterEvent {
    private String originalMessage;
    private String reason;
    private long timestamp;

    public DeadLetterEvent(String originalMessage, String reason, long timestamp) {
        this.originalMessage = originalMessage;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    // Getters
    public String getOriginalMessage() { return originalMessage; }
    public String getReason() { return reason; }
    public long getTimestamp() { return timestamp; }
}