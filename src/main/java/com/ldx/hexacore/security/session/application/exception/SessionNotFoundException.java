package com.ldx.hexacore.security.session.application.exception;

/**
 * 세션을 찾을 수 없는 경우 발생하는 예외
 * 
 * 지정된 세션 ID로 세션을 조회할 수 없을 때 발생합니다.
 */
public class SessionNotFoundException extends RuntimeException {
    
    private final String sessionId;
    
    /**
     * 세션 ID와 함께 예외 생성
     */
    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
        this.sessionId = sessionId;
    }
    
    /**
     * 기본 메시지로 예외 생성
     */
    public SessionNotFoundException(String message, boolean isMessage) {
        super(message);
        this.sessionId = null;
    }
    
    /**
     * 메시지와 원인과 함께 예외 생성
     */
    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.sessionId = null;
    }
    
    /**
     * 세션 ID, 메시지, 원인과 함께 예외 생성
     */
    public SessionNotFoundException(String sessionId, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
    }
    
    /**
     * 세션 ID 반환
     */
    public String getSessionId() {
        return sessionId;
    }
}