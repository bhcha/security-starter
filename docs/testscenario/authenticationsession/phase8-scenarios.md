# AuthenticationSession Phase 8: Outbound Adapter 테스트 시나리오

## SessionJpaAdapter 테스트 시나리오

### 1. RecordAuthenticationAttemptPort 구현 테스트

#### 시나리오 1-1: 새로운 세션에 첫 인증 시도 기록
- Given: 존재하지 않는 세션 ID
- When: 인증 시도를 기록
- Then: 새로운 세션 엔티티가 생성되고 인증 시도가 저장됨

#### 시나리오 1-2: 기존 세션에 인증 시도 추가
- Given: 이미 존재하는 세션
- When: 새로운 인증 시도를 기록
- Then: 기존 세션에 인증 시도가 추가됨

#### 시나리오 1-3: 실패한 인증 시도 기록
- Given: 세션 ID와 실패 정보
- When: 실패한 인증 시도를 기록
- Then: 실패 사유와 함께 저장됨

#### 시나리오 1-4: 성공한 인증 시도 기록
- Given: 세션 ID와 성공 정보
- When: 성공한 인증 시도를 기록
- Then: 성공 상태로 저장됨

#### 시나리오 1-5: 계정 잠금 상태 업데이트
- Given: 5회 실패한 세션
- When: 계정 잠금 이벤트 발생
- Then: lockoutUntil이 설정됨

### 2. LoadSessionStatusQueryPort 구현 테스트

#### 시나리오 2-1: 존재하는 세션 조회
- Given: 저장된 세션 ID
- When: 세션 상태를 조회
- Then: SessionStatusProjection 반환

#### 시나리오 2-2: 존재하지 않는 세션 조회
- Given: 존재하지 않는 세션 ID
- When: 세션 상태를 조회
- Then: Optional.empty() 반환

#### 시나리오 2-3: 잠금된 세션 조회
- Given: lockoutUntil이 설정된 세션
- When: 세션 상태를 조회
- Then: 잠금 상태와 만료 시간 포함

#### 시나리오 2-4: 최근 인증 시도 정보 포함
- Given: 여러 인증 시도가 있는 세션
- When: 세션 상태를 조회
- Then: 가장 최근 시도 정보 포함

### 3. LoadFailedAttemptsQueryPort 구현 테스트

#### 시나리오 3-1: 실패한 시도 목록 조회
- Given: 실패/성공이 섞인 세션
- When: 실패한 시도만 조회
- Then: 실패한 시도만 반환

#### 시나리오 3-2: 시간 범위 내 실패 시도 조회
- Given: 15분 윈도우와 여러 시도
- When: 시간 범위 내 실패 조회
- Then: 범위 내 실패만 반환

#### 시나리오 3-3: 빈 결과 처리
- Given: 실패한 시도가 없는 세션
- When: 실패한 시도를 조회
- Then: 빈 목록 반환

#### 시나리오 3-4: 정렬 순서 검증
- Given: 여러 실패 시도
- When: 실패 시도를 조회
- Then: 시간 역순으로 정렬됨

### 4. 트랜잭션 및 동시성 테스트

#### 시나리오 4-1: 트랜잭션 롤백
- Given: 인증 시도 저장 중
- When: 예외 발생
- Then: 전체 트랜잭션 롤백

#### 시나리오 4-2: 동시 세션 접근
- Given: 동일 세션에 동시 요청
- When: 여러 인증 시도 동시 기록
- Then: 모든 시도가 정확히 저장됨

## SessionCacheAdapter 테스트 시나리오

### 5. 캐시 기본 동작 테스트

#### 시나리오 5-1: 캐시 미스 후 저장
- Given: 캐시에 없는 세션
- When: 세션 조회 후 캐시 저장
- Then: 다음 조회 시 캐시 히트

#### 시나리오 5-2: 캐시 히트
- Given: 캐시에 저장된 세션
- When: 동일 세션 재조회
- Then: DB 조회 없이 캐시에서 반환

#### 시나리오 5-3: TTL 만료
- Given: 15분 전에 캐시된 세션
- When: TTL 만료 후 조회
- Then: 캐시 미스 발생

#### 시나리오 5-4: 캐시 무효화
- Given: 캐시된 세션
- When: 새로운 인증 시도 기록
- Then: 해당 세션 캐시 무효화

### 6. Caffeine 캐시 구현 테스트

#### 시나리오 6-1: 최대 크기 제한
- Given: 캐시 최대 크기 설정
- When: 제한 초과 시
- Then: LRU 정책으로 제거

#### 시나리오 6-2: 동시 접근 처리
- Given: 여러 스레드
- When: 동시 캐시 접근
- Then: 스레드 안전성 보장

### 7. Redis 캐시 구현 테스트 (선택적)

#### 시나리오 7-1: Redis 연결 실패
- Given: Redis 서버 다운
- When: 캐시 작업 시도
- Then: Fallback으로 DB 직접 조회

#### 시나리오 7-2: 직렬화/역직렬화
- Given: 복잡한 세션 객체
- When: Redis 저장/조회
- Then: 정확한 객체 복원

## Configuration 테스트 시나리오

### 8. 조건부 Bean 등록 테스트

#### 시나리오 8-1: JPA 어댑터 활성화
- Given: hexacore.session.persistence.enabled=true
- When: 애플리케이션 시작
- Then: SessionJpaAdapter Bean 생성

#### 시나리오 8-2: 캐시 어댑터 비활성화
- Given: hexacore.session.cache.enabled=false
- When: 애플리케이션 시작
- Then: 캐시 어댑터 Bean 미생성

#### 시나리오 8-3: Redis vs Caffeine 선택
- Given: hexacore.session.cache.type=redis
- When: 애플리케이션 시작
- Then: RedisSessionCache Bean 생성

## 통합 테스트 시나리오

### 9. End-to-End 테스트

#### 시나리오 9-1: 전체 플로우 테스트
- Given: 새로운 사용자
- When: 여러 인증 시도 → 실패 → 잠금 → 조회
- Then: 모든 상태가 정확히 영속화되고 캐시됨

#### 시나리오 9-2: 장애 복구 테스트
- Given: 캐시 서버 장애
- When: 세션 작업 계속
- Then: DB만으로 정상 동작

#### 시나리오 9-3: 성능 테스트
- Given: 1000개의 동시 세션
- When: 대량 조회/업데이트
- Then: 응답 시간 100ms 이내