## 🏗️ 계층별 규칙

- **허용**: Application/Domain 계층, 모든 외부 기술
- **금지**: 다른 Adapter 직접 참조, 비즈니스 로직 포함
- **필수**: 포트 인터페이스 구현

## 📦 어댑터 템플릿 구조

## 🎯 핵심 원칙
```
❌ 도메인 모델에 없으면 만들지 마세요!
❌ 도메인 로직 포함 금지
❌ 다른 어댑터에 직접 의존 금지
❌ 포트 인터페이스 없이 애플리케이션 직접 호출 금지
```

```
adapter/
├── inbound/                # 인바운드 어댑터
│   ├── filter/            # 보안 필터
│   ├── event/             # 이벤트 구독
│   └── config/            # 인바운드 설정
├── outbound/               # 아웃바운드 어댑터
│   ├── persistence/       # 영속성
│   │   ├── command/       # Write Model
│   │   └── query/         # Read Model
│   ├── messaging/         # 메시징
│   └── external/          # 외부 시스템
└── config/                # 어댑터 설정
```

## 🔒 Security Filter 템플릿

### JWT 인증 필터
```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.inbound.filter;

import com.dx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.dx.hexacore.security.auth.application.command.port.in.ValidateTokenCommand;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 토큰 인증 필터.
 * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출하여 검증합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final TokenManagementUseCase tokenManagementUseCase;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String token = extractToken(request);
            
            if (token != null) {
                var validationResult = tokenManagementUseCase.validateToken(
                    ValidateTokenCommand.of(token)
                );
                
                if (validationResult.isValid()) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                        validationResult.getSubject(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    
                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT authentication successful for user: {}", 
                        validationResult.getSubject());
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestPath().pathWithinApplication().value();
        // 인증이 필요없는 경로는 필터를 적용하지 않음
        return path.startsWith("/health") || 
               path.startsWith("/metrics") ||
               path.equals("/");
    }
}
```

### Rate Limit 필터
```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.inbound.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Rate Limiting 필터.
 * IP 주소 또는 사용자별로 API 호출 빈도를 제한합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long capacity = 100;
    private final long refillTokens = 100;
    private final Duration refillPeriod = Duration.ofMinutes(1);
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key);
        
        if (bucket.tryConsume(1)) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
            response.setHeader("X-RateLimit-Remaining", 
                String.valueOf(bucket.getAvailableTokens()));
            
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.setHeader("X-RateLimit-Retry-After", "60");
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            
            log.warn("Rate limit exceeded for key: {}", key);
        }
    }
    
    private String resolveKey(HttpServletRequest request) {
        // 인증된 사용자가 있으면 사용자 ID 사용, 없으면 IP 주소 사용
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return getClientIp(request);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(capacity, 
                Refill.intervally(refillTokens, refillPeriod));
            return Bucket.builder()
                .addLimit(limit)
                .build();
        });
    }
}
```

## 💾 Persistence Adapter - Command Side 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence;

import com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.out.*;
import com.dx.hexacore.security.{애그리거트명소문자}.domain.*;
import com.dx.hexacore.security.{애그리거트명소문자}.domain.vo.*;
import com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence.entity.*;
import com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/**
 * {Aggregate} 영속성 어댑터.
 * 도메인 모델과 JPA 엔티티 간의 변환을 담당합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class {Aggregate}JpaAdapter implements {Aggregate}Repository {
    
    private final {Aggregate}JpaRepository jpaRepository;
    private final {Aggregate}JpaMapper mapper;
    
    @Override
    @Transactional
    public {Aggregate} save({Aggregate} aggregate) {
        log.debug("Saving {Aggregate} with id: {}", aggregate.getId());
        
        {Aggregate}JpaEntity entity = mapper.toEntity(aggregate);
        {Aggregate}JpaEntity saved = jpaRepository.save(entity);
        
        return mapper.toDomain(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<{Aggregate}> findById({AggregateId} id) {
        log.debug("Finding {Aggregate} by id: {}", id);
        
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional
    public void deleteById({AggregateId} id) {
        log.debug("Deleting {Aggregate} with id: {}", id);
        
        jpaRepository.deleteById(id.getValue());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById({AggregateId} id) {
        return jpaRepository.existsById(id.getValue());
    }
}
```

## 🗄️ JPA Entity 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

/**
 * {Aggregate} JPA 엔티티.
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "{table_name}")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class {Aggregate}JpaEntity {
    
    @Id
    @Column(name = "id", length = 50)
    private String id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private String status;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
}
```

## 🗺️ Mapper 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence;

import com.dx.hexacore.security.{애그리거트명소문자}.domain.*;
import com.dx.hexacore.security.{애그리거트명소문자}.domain.vo.*;
import com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence.entity.*;
import org.mapstruct.*;

/**
 * JPA 영속성 계층 매퍼.
 * 
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface {Aggregate}JpaMapper {
    
    @Mapping(source = "id.value", target = "id")
    {Aggregate}JpaEntity toEntity({Aggregate} domain);
    
    @Mapping(source = "id", target = "id.value")
    {Aggregate} toDomain({Aggregate}JpaEntity entity);
}
```

## 🎪 Event Adapter 템플릿

### 도메인 이벤트 발행 어댑터
```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.event;

import com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.out.EventPublisher;
import com.dx.hexacore.security.{애그리거트명소문자}.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring ApplicationEvent를 사용하는 이벤트 발행 어댑터.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {} with id: {}", 
            event.getClass().getSimpleName(), event.getEventId());
        
        applicationEventPublisher.publishEvent(event);
    }
}
```

### 이벤트 리스너 어댑터
```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.inbound.event;

import com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.in.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트 수신 어댑터.
 * 
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class {Aggregate}EventListener {
    
    private final Handle{Event}UseCase handle{Event}UseCase;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle({Event}Occurred event) {
        log.info("Handling {Event}Occurred event: {}", event.getAggregateId());
        
        try {
            handle{Event}UseCase.handle(
                Handle{Event}Command.of(event.getAggregateId(), event.getOccurredAt())
            );
        } catch (Exception e) {
            log.error("Failed to handle {Event}Occurred event", e);
            // 이벤트 처리 실패 시 보상 트랜잭션이나 재시도 로직
        }
    }
}
```

## 🔧 Configuration 템플릿

### Security Configuration
```java
package com.dx.hexacore.security.config;

import com.dx.hexacore.security.auth.adapter.inbound.filter.JwtAuthenticationFilter;
import com.dx.hexacore.security.session.adapter.inbound.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정.
 * 
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health", "/metrics").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 📚 어댑터 테스트 템플릿

### Filter 테스트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.inbound.filter;

import com.dx.hexacore.security.auth.application.command.port.in.TokenManagementUseCase;
import com.dx.hexacore.security.auth.application.command.port.in.TokenValidationResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private TokenManagementUseCase tokenManagementUseCase;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter filter;
    
    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void shouldAuthenticateValidToken() throws Exception {
        // Given
        String validToken = "valid.jwt.token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
        given(tokenManagementUseCase.validateToken(any()))
            .willReturn(TokenValidationResult.valid("user123", validToken));
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
            .isEqualTo("user123");
    }
    
    @Test
    void shouldSkipAuthenticationWhenNoToken() throws Exception {
        // Given
        given(request.getHeader("Authorization")).willReturn(null);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(filterChain).doFilter(request, response);
        verify(tokenManagementUseCase, never()).validateToken(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
```

### Persistence Adapter 테스트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence;

import com.dx.hexacore.security.{애그리거트명소문자}.domain.*;
import com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence.entity.*;
import com.dx.hexacore.security.{애그리거트명소문자}.adapter.outbound.persistence.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
    {Aggregate}JpaAdapter.class,
    {Aggregate}JpaMapperImpl.class
})
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class {Aggregate}JpaAdapterTest {
    
    @Autowired
    private {Aggregate}JpaAdapter adapter;
    
    @Autowired
    private {Aggregate}JpaRepository repository;
    
    @Test
    void shouldSaveAndLoad() {
        // Given
        var aggregate = {Aggregate}.create(/* parameters */);
        
        // When
        var saved = adapter.save(aggregate);
        var loaded = adapter.findById(saved.getId());
        
        // Then
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(saved.getId());
        assertThat(repository.count()).isEqualTo(1);
    }
}
```