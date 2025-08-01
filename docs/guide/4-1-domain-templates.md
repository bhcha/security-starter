## 🏗️ 계층별 규칙

- **허용**: Java 표준 라이브러리, 도메인 내부 클래스
- **금지**: Spring, JPA(엔티티 어노테이션 제외), 외부 라이브러리
- **예외**: `@Entity`, `@Id` 등 JPA 필수 어노테이션만 허용
## 🎯 핵심 원칙
```
❌ 도메인 모델에 없으면 만들지 마세요!
❌ 도메인 서비스에서 Repository 직접 참조 금지
❌ 값 객체에 setter 메서드 추가 금지
❌ 도메인 객체에서 Spring 어노테이션 사용 (JPA 제외) 금지
```

## 📦 도메인 템플릿 구조

```
domain/
├── aggregate/          # 애그리게이트 템플릿
├── value-object/       # 값 객체 템플릿
├── entity/            # 엔티티 템플릿
├── event/             # 도메인 이벤트 템플릿
├── service/           # 도메인 서비스 템플릿
├── specification/     # 명세 패턴 템플릿
└── exception/         # 도메인 예외 템플릿
```

## 🏛️ Aggregate Root 템플릿

### 기본 애그리게이트 루트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.aggregate;

import com.company.security.domain.shared.AggregateRoot;
import com.company.security.domain.shared.DomainEvent;
import javax.persistence.*;
import java.time.Instant;
import java.util.*;

/**
 * {AggregateName} 애그리게이트 루트.
 * 
 * <p>설명: {애그리게이트의 목적과 책임}
 * 
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>{비즈니스 규칙 1}</li>
 *   <li>{비즈니스 규칙 2}</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "{table_name}")
@AggregateRoot
public class {AggregateName} {
    
    @Id
    @AttributeOverride(name = "value", column = @Column(name = "{id_column}"))
    private final {AggregateName}Id id;
    
    @Embedded
    private {ValueObject} {propertyName};
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private {StatusEnum} status;
    
    @Column(name = "created_at", nullable = false)
    private final Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Transient
    private final List<DomainEvent> events = new ArrayList<>();
    
    // JPA를 위한 protected 기본 생성자
    protected {AggregateName}() {
        this.id = null;
        this.createdAt = null;
    }
    
    // private 생성자 - 정적 팩토리 메서드를 통해서만 생성
    private {AggregateName}({AggregateName}Id id, {ValueObject} {propertyName}) {
        this.id = Objects.requireNonNull(id, "{AggregateName} ID cannot be null");
        this.{propertyName} = Objects.requireNonNull({propertyName}, "{PropertyName} cannot be null");
        this.status = {StatusEnum}.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 새로운 {AggregateName}을 생성합니다.
     * 
     * @param {param1} {param1 설명}
     * @param {param2} {param2 설명}
     * @return 생성된 {AggregateName}
     * @throws {Exception} {예외 조건}
     */
    public static {AggregateName} create({ParamType1} {param1}, {ParamType2} {param2}) {
        // 비즈니스 검증
        validate{Param1}({param1});
        validate{Param2}({param2});
        
        {AggregateName} {aggregateName} = new {AggregateName}(
            {AggregateName}Id.generate(),
            {ValueObject}.of({param1})
        );
        
        {aggregateName}.addEvent(new {AggregateName}Created(
            {aggregateName}.id,
            {aggregateName}.createdAt
        ));
        
        return {aggregateName};
    }
    
    /**
     * 영속성 저장소에서 {AggregateName}을 복원합니다.
     */
    public static {AggregateName} reconstitute(
        {AggregateName}Id id,
        {ValueObject} {propertyName},
        {StatusEnum} status,
        Instant createdAt,
        Instant updatedAt,
        Long version
    ) {
        {AggregateName} {aggregateName} = new {AggregateName}(id, {propertyName});
        {aggregateName}.status = status;
        {aggregateName}.createdAt = createdAt;
        {aggregateName}.updatedAt = updatedAt;
        {aggregateName}.version = version;
        return {aggregateName};
    }
    
    // ===== 비즈니스 메서드 =====
    
    /**
     * {비즈니스 행위를 수행합니다}.
     * 
     * @param {param} {param 설명}
     * @return {반환값 설명}
     * @throws {Exception} {예외 조건}
     */
    public {ReturnType} {businessMethod}({ParamType} {param}) {
        // 사전 조건 검증
        if (!canPerform{Action}()) {
            throw new {BusinessException}(
                String.format("Cannot perform {action} in status %s", status)
            );
        }
        
        // 비즈니스 로직 수행
        {businessLogic}
        
        // 상태 변경
        this.{property} = {newValue};
        this.updatedAt = Instant.now();
        
        // 이벤트 발생
        addEvent(new {EventName}(
            this.id,
            {eventData},
            this.updatedAt
        ));
        
        return {result};
    }
    
    /**
     * {상태를 변경합니다}.
     */
    public void transition{Status}({Reason} reason) {
        if (this.status == {TargetStatus}) {
            return; // 이미 목표 상태
        }
        
        validateTransition(this.status, {TargetStatus});
        
        this.status = {TargetStatus};
        this.updatedAt = Instant.now();
        
        addEvent(new {StatusChanged}(
            this.id,
            this.status,
            reason,
            this.updatedAt
        ));
    }
    
    // ===== 도메인 이벤트 관리 =====
    
    private void addEvent(DomainEvent event) {
        events.add(Objects.requireNonNull(event));
    }
    
    public List<DomainEvent> collectEvents() {
        List<DomainEvent> collectedEvents = new ArrayList<>(events);
        events.clear();
        return Collections.unmodifiableList(collectedEvents);
    }
    
    // ===== Private 헬퍼 메서드 =====
    
    private boolean canPerform{Action}() {
        return status == {StatusEnum}.ACTIVE && {additionalConditions};
    }
    
    private static void validate{Param}({ParamType} {param}) {
        if ({validationCondition}) {
            throw new IllegalArgumentException("{Validation error message}");
        }
    }
    
    private void validateTransition({StatusEnum} from, {StatusEnum} to) {
        if (!isValidTransition(from, to)) {
            throw new InvalidStateTransitionException(from, to);
        }
    }
    
    private boolean isValidTransition({StatusEnum} from, {StatusEnum} to) {
        return switch (from) {
            case ACTIVE -> Set.of({ValidTargetStates}).contains(to);
            case SUSPENDED -> to == {StatusEnum}.ACTIVE;
            default -> false;
        };
    }
    
    // ===== Getter (필요한 경우만) =====
    
    public {AggregateName}Id getId() {
        return id;
    }
    
    public {StatusEnum} getStatus() {
        return status;
    }
    
    public boolean is{State}() {
        return status == {StatusEnum}.{STATE};
    }
    
    // equals와 hashCode는 ID 기반으로만
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {AggregateName} that = ({AggregateName}) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

## 💎 Value Object 템플릿

### 기본 값 객체
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.vo;

import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * {ValueObjectName} 값 객체.
 * 
 * <p>{값 객체의 목적과 의미}
 * 
 * <p>제약사항:
 * <ul>
 *   <li>{제약사항 1}</li>
 *   <li>{제약사항 2}</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Embeddable
public final class {ValueObjectName} {
    
    private static final Pattern VALID_PATTERN = Pattern.compile("{regex}");
    private static final int MIN_LENGTH = {minLength};
    private static final int MAX_LENGTH = {maxLength};
    
    private final String value;
    
    // JPA를 위한 protected 기본 생성자
    protected {ValueObjectName}() {
        this.value = null;
    }
    
    // private 생성자
    private {ValueObjectName}(String value) {
        validate(value);
        this.value = value;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 문자열로부터 {ValueObjectName}을 생성합니다.
     * 
     * @param value 원본 값
     * @return 생성된 {ValueObjectName}
     * @throws IllegalArgumentException 유효하지 않은 값인 경우
     */
    public static {ValueObjectName} of(String value) {
        return new {ValueObjectName}(value);
    }
    
    /**
     * 빈 값 여부에 따라 Optional로 반환합니다.
     */
    public static Optional<{ValueObjectName}> ofNullable(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new {ValueObjectName}(value));
    }
    
    /**
     * 포맷팅된 문자열로부터 생성합니다.
     */
    public static {ValueObjectName} parse(String formattedValue) {
        String normalized = normalize(formattedValue);
        return new {ValueObjectName}(normalized);
    }
    
    // ===== 비즈니스 메서드 =====
    
    /**
     * {다른 값 객체와 결합하여 새로운 값을 생성}.
     */
    public {ValueObjectName} combine({OtherValueObject} other) {
        String combined = this.value + other.getValue();
        return new {ValueObjectName}(combined);
    }
    
    /**
     * {특정 조건을 만족하는지 확인}.
     */
    public boolean satisfies({Condition} condition) {
        return {conditionCheck};
    }
    
    /**
     * 마스킹된 값을 반환합니다.
     */
    public String getMasked() {
        if (value.length() <= 4) {
            return "****";
        }
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
    
    // ===== 유틸리티 메서드 =====
    
    public String getValue() {
        return value;
    }
    
    public int length() {
        return value.length();
    }
    
    public boolean isEmpty() {
        return value.isEmpty();
    }
    
    // ===== Private 메서드 =====
    
    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("{ValueObjectName} cannot be empty");
        }
        
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("{ValueObjectName} must be between %d and %d characters", 
                    MIN_LENGTH, MAX_LENGTH)
            );
        }
        
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "{ValueObjectName} contains invalid characters"
            );
        }
    }
    
    private static String normalize(String value) {
        return value.trim().toUpperCase();
    }
    
    // ===== Object 메서드 =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {ValueObjectName} that = ({ValueObjectName}) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

### ID 값 객체 템플릿
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.vo;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * {EntityName}의 식별자.
 * 
 * @since 1.0.0
 */
@Embeddable
public final class {EntityName}Id implements Serializable {
    
    private static final String PREFIX = "{PREFIX}_";
    
    private final String value;
    
    // JPA를 위한 protected 기본 생성자
    protected {EntityName}Id() {
        this.value = null;
    }
    
    private {EntityName}Id(String value) {
        validateId(value);
        this.value = value;
    }
    
    // ===== 정적 팩토리 메서드 =====
    
    /**
     * 새로운 ID를 생성합니다.
     */
    public static {EntityName}Id generate() {
        return new {EntityName}Id(PREFIX + UUID.randomUUID().toString());
    }
    
    /**
     * 기존 ID 값으로부터 생성합니다.
     */
    public static {EntityName}Id of(String value) {
        return new {EntityName}Id(value);
    }
    
    /**
     * 외부 시스템 ID로부터 생성합니다.
     */
    public static {EntityName}Id fromExternalId(String externalId) {
        return new {EntityName}Id(PREFIX + "EXT_" + externalId);
    }
    
    // ===== 유틸리티 메서드 =====
    
    public String getValue() {
        return value;
    }
    
    public boolean isExternal() {
        return value.contains("_EXT_");
    }
    
    // ===== Private 메서드 =====
    
    private static void validateId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("{EntityName}Id cannot be empty");
        }
        
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException(
                "{EntityName}Id must start with " + PREFIX
            );
        }
    }
    
    // ===== Object 메서드 =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {EntityName}Id that = ({EntityName}Id) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

## 🎭 Entity 템플릿 (Aggregate 내부)

```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * {EntityName} 엔티티.
 * 
 * <p>{엔티티의 역할과 책임}
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "{table_name}")
public class {EntityName} {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "{aggregate_id}", nullable = false)
    private {AggregateName} {aggregateName};
    
    @Embedded
    private {ValueObject} {property};
    
    @Column(name = "created_at", nullable = false)
    private final Instant createdAt;
    
    // JPA를 위한 protected 기본 생성자
    protected {EntityName}() {
        this.createdAt = null;
    }
    
    // Package-private 생성자 (Aggregate에서만 생성)
    {EntityName}({AggregateName} {aggregateName}, {ValueObject} {property}) {
        this.{aggregateName} = Objects.requireNonNull({aggregateName});
        this.{property} = Objects.requireNonNull({property});
        this.createdAt = Instant.now();
    }
    
    // ===== 비즈니스 메서드 =====
    
    /**
     * {엔티티의 상태를 변경합니다}.
     */
    void update{Property}({ValueObject} new{Property}) {
        validate{Property}(new{Property});
        this.{property} = new{Property};
    }
    
    /**
     * {비즈니스 규칙을 검증합니다}.
     */
    boolean canBe{Action}() {
        return {businessCondition};
    }
    
    // ===== Package-private 메서드 (Aggregate 내부 사용) =====
    
    {ValueObject} get{Property}() {
        return {property};
    }
    
    // ===== Private 메서드 =====
    
    private void validate{Property}({ValueObject} {property}) {
        if ({validationCondition}) {
            throw new IllegalArgumentException("{Validation message}");
        }
    }
    
    // ID 기반 equals/hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {EntityName} that = ({EntityName}) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

## 📢 Domain Event 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.event;

import com.company.security.domain.shared.DomainEvent;
import java.time.Instant;
import java.util.Objects;

/**
 * {EventDescription}.
 * 
 * @since 1.0.0
 */
public record {EventName}(
    {AggregateName}Id aggregateId,
    {PropertyType} {property},
    String reason,
    Instant occurredAt
) implements DomainEvent {
    
    public {EventName} {
        Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null");
        Objects.requireNonNull(occurredAt, "Occurred at cannot be null");
        
        // 추가 검증
        if ({validationCondition}) {
            throw new IllegalArgumentException("{Validation message}");
        }
    }
    
    @Override
    public String getAggregateId() {
        return aggregateId.getValue();
    }
    
    @Override
    public String getEventType() {
        return "{EVENT_TYPE}";
    }
    
    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
```

## 🛠️ Domain Service 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.service;

import com.company.security.domain.shared.DomainService;
import java.util.Objects;

/**
 * {ServiceDescription}.
 * 
 * <p>이 서비스는 {책임과 역할}
 * 
 * @since 1.0.0
 */
@DomainService
public class {ServiceName} {
    
    private final {Dependency} {dependency};
    
    public {ServiceName}({Dependency} {dependency}) {
        this.{dependency} = Objects.requireNonNull({dependency});
    }
    
    /**
     * {서비스가 수행하는 작업}.
     * 
     * @param {param1} {param1 설명}
     * @param {param2} {param2 설명}
     * @return {반환값 설명}
     */
    public {ReturnType} {performOperation}(
        {Aggregate1} {aggregate1},
        {Aggregate2} {aggregate2}
    ) {
        // 여러 애그리게이트에 걸친 비즈니스 로직
        validate{Precondition}({aggregate1}, {aggregate2});
        
        // 도메인 로직 수행
        {Result} result = calculate{Something}(
            {aggregate1}.get{Property}(),
            {aggregate2}.get{Property}()
        );
        
        // 결과 반환
        return {ReturnType}.of(result);
    }
    
    /**
     * {복잡한 비즈니스 규칙 검증}.
     */
    public boolean {checkBusinessRule}(
        {Aggregate} {aggregate},
        {Context} context
    ) {
        return {complexBusinessLogic};
    }
    
    // ===== Private 헬퍼 메서드 =====
    
    private void validate{Precondition}({params}) {
        if ({validationCondition}) {
            throw new {DomainException}("{Error message}");
        }
    }
    
    private {Result} calculate{Something}({params}) {
        // 복잡한 계산 로직
        return {calculatedResult};
    }
}
```

## 📋 Specification 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.specification;

import com.company.security.domain.shared.Specification;

/**
 * {SpecificationDescription}.
 * 
 * @since 1.0.0
 */
public class {SpecificationName} implements Specification<{TargetType}> {
    
    private final {Criteria} criteria;
    
    private {SpecificationName}({Criteria} criteria) {
        this.criteria = Objects.requireNonNull(criteria);
    }
    
    public static {SpecificationName} of({Criteria} criteria) {
        return new {SpecificationName}(criteria);
    }
    
    @Override
    public boolean isSatisfiedBy({TargetType} target) {
        Objects.requireNonNull(target, "Target cannot be null");
        
        return {businessRuleCheck};
    }
    
    @Override
    public {SpecificationName} and(Specification<{TargetType}> other) {
        return new {SpecificationName}(target -> 
            this.isSatisfiedBy(target) && other.isSatisfiedBy(target)
        );
    }
    
    @Override
    public {SpecificationName} or(Specification<{TargetType}> other) {
        return new {SpecificationName}(target -> 
            this.isSatisfiedBy(target) || other.isSatisfiedBy(target)
        );
    }
    
    @Override
    public {SpecificationName} not() {
        return new {SpecificationName}(target -> 
            !this.isSatisfiedBy(target)
        );
    }
}
```

## 🚨 Domain Exception 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.{subdomain}.exception;

import com.company.security.domain.shared.DomainException;

/**
 * {ExceptionDescription}.
 * 
 * @since 1.0.0
 */
public class {ExceptionName} extends DomainException {
    
    private final {EntityId} entityId;
    private final {Context} context;
    
    public {ExceptionName}({EntityId} entityId, String message) {
        super(message);
        this.entityId = Objects.requireNonNull(entityId);
        this.context = null;
    }
    
    public {ExceptionName}({EntityId} entityId, {Context} context) {
        super(buildMessage(entityId, context));
        this.entityId = Objects.requireNonNull(entityId);
        this.context = Objects.requireNonNull(context);
    }
    
    public {ExceptionName}({EntityId} entityId, String message, Throwable cause) {
        super(message, cause);
        this.entityId = Objects.requireNonNull(entityId);
        this.context = null;
    }
    
    private static String buildMessage({EntityId} entityId, {Context} context) {
        return String.format(
            "{Detailed error message with ID: %s and context: %s}",
            entityId.getValue(),
            context
        );
    }
    
    public {EntityId} getEntityId() {
        return entityId;
    }
    
    public Optional<{Context}> getContext() {
        return Optional.ofNullable(context);
    }
    
    @Override
    public String getErrorCode() {
        return "{ERROR_CODE}";
    }
}
```

## 🔧 도메인 공통 인터페이스

### DomainEvent 인터페이스
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.shared;

import java.time.Instant;

public interface DomainEvent {
    String getAggregateId();
    String getEventType();
    Instant getOccurredAt();
}
```

### Specification 인터페이스
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.shared;

public interface Specification<T> {
    boolean isSatisfiedBy(T target);
    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
    Specification<T> not();
}
```

### AggregateRoot 마커 어노테이션
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.shared;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AggregateRoot {
}
```

### DomainService 마커 어노테이션
```java
package com.dx.hexacore.security.{애그리거트명소문자}.domain.shared;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DomainService {
}
```

## 📝 사용 예시

### ClientCredentials 애그리게이트 구현
```java
// 1. 값 객체 정의
public final class ClientId { ... }
public final class ClientSecret { ... }

// 2. 이벤트 정의
public record ClientCreated(ClientId clientId, Instant occurredAt) implements DomainEvent { ... }
public record ClientAuthenticated(ClientId clientId, SessionId sessionId, Instant occurredAt) implements DomainEvent { ... }

// 3. 애그리게이트 구현
@Entity
@AggregateRoot
public class ClientCredentials {
    // 템플릿 기반 구현
}

// 4. 도메인 서비스 (필요시)
@DomainService
public class TokenGenerationService {
    // 여러 애그리게이트에 걸친 로직
}
```