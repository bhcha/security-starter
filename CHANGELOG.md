# Changelog

## [1.4.0] - 2025-09-01

### Changed
- **BREAKING CHANGE**: AuthenticationSession 정책 상수들을 설정값으로 교체
  - `MAX_FAILED_ATTEMPTS`와 `LOCKOUT_DURATION_MINUTES` 하드코딩 제거
  - HexacoreSecurityProperties를 통한 동적 설정 지원
  - `hexacore.security.session.lockout.max-attempts`와 `hexacore.security.session.lockout.lockout-duration-minutes` 설정 사용

### Fixed
- SecurityFilterConfigTest 모든 테스트 실패 수정
- BeanRegistrationVerificationTest ApplicationContext 로딩 문제 수정
- ExtendedValidationTest Bean 정의 중복 문제 수정
- SimpleBeanRegistrationTest 설정 문제 수정
- 외부 의존성 통합 테스트 조건부 실행으로 변경

### Improved
- 테스트 안정성 향상 (28개 실패 → 0개 실패)
- JWT Auto Configuration 컨텍스트 로딩 최적화
- Bean 정의 중복 방지 설정 추가

## [1.3.1] - Previous Version
- Token Provider Bean 중복 정의 문제 수정
- Repository 구조 정리