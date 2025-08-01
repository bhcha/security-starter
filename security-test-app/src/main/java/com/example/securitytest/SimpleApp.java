package com.example.securitytest;

import com.dx.hexacore.security.auth.application.command.port.in.AuthenticationUseCase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;

/**
 * 간단한 Java 17 호환성 테스트 애플리케이션
 */
@SpringBootApplication
@EnableCaching
public class SimpleApp {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        
        // Hexacore Security 빈이 로드되는지 테스트
        try {
            AuthenticationUseCase authUseCase = context.getBean(AuthenticationUseCase.class);
            System.out.println("✅ Java 17 호환성 테스트 성공!");
            System.out.println("✅ AuthenticationUseCase 빈 로드됨: " + authUseCase.getClass().getName());
        } catch (Exception e) {
            System.out.println("❌ 테스트 실패: " + e.getMessage());
        }
    }
}