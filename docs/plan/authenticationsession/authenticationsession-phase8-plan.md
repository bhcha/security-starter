# AuthenticationSession Phase 8: Outbound Adapter 구현 계획

## 구현 목표
AuthenticationSession 애그리거트의 Outbound Adapter를 구현하여 영속성 및 캐싱 기능을 제공한다.

## 구현 범위

### 1. JPA Persistence Adapter
- SessionJpaAdapter: 세션 데이터의 영속성 관리
- SessionJpaEntity: JPA 엔티티 매핑
- SessionMapper: 도메인 ↔ JPA 엔티티 변환
- AuthenticationAttemptJpaEntity: 인증 시도 기록 엔티티
- SessionJpaRepository: Spring Data JPA 리포지토리

### 2. Cache Adapter
- SessionCacheAdapter: 세션 데이터 캐싱
- CaffeineSessionCache: Caffeine 기반 로컬 캐시 구현
- RedisSessionCache: Redis 기반 분산 캐시 구현 (선택적)
- CacheConfiguration: 캐시 설정 및 조건부 활성화

### 3. Configuration
- SessionPersistenceConfiguration: JPA 어댑터 설정
- SessionCacheConfiguration: 캐시 어댑터 설정
- 조건부 Bean 등록 (@ConditionalOnProperty)

## 컴포넌트별 상세 계획

### SessionJpaAdapter
- RecordAuthenticationAttemptPort 구현
- LoadSessionStatusQueryPort 구현
- LoadFailedAttemptsQueryPort 구현
- 트랜잭션 관리 (@Transactional)
- 예외 처리 및 변환

### SessionCacheAdapter
- 세션 상태 캐싱
- 실패 시도 집계 데이터 캐싱
- TTL 설정 (세션별 15분)
- 캐시 무효화 전략

### Entity 설계
```
SessionJpaEntity
- sessionId (PK)
- userId
- lockoutUntil
- createdAt
- updatedAt
- attempts (OneToMany)

AuthenticationAttemptJpaEntity
- attemptId (PK)
- sessionId (FK)
- attemptedAt
- successful
- reason
- clientIp
- riskLevel
```

## 테스트 전략
1. 단위 테스트: 각 어댑터의 독립적 동작 검증
2. 통합 테스트: @DataJpaTest, @SpringBootTest 활용
3. 캐시 테스트: 캐시 히트/미스, TTL 검증
4. 동시성 테스트: 동시 세션 접근 시나리오

## 완료 기준
- [ ] 모든 Outbound Port 구현 완료
- [ ] JPA 어댑터 및 엔티티 구현 완료
- [ ] 캐시 어댑터 구현 완료 (Caffeine 필수, Redis 선택)
- [ ] 단위 테스트 작성 및 통과 (커버리지 80% 이상)
- [ ] 통합 테스트 작성 및 통과
- [ ] 조건부 설정 검증 완료
- [ ] Adapter 레이어 명세서 작성
- [ ] 애그리거트 인터페이스 문서 작성