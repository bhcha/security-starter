## 🏗️ 계층별 규칙

- **허용**: Domain 계층, Java 표준 라이브러리, 검증 어노테이션
- **금지**: Adapter 구현체 직접 참조, Spring 기술(@Transactional 제외)
- **필수**: 포트 인터페이스를 통한 외부 통신

## 🎯 핵심 원칙
```
❌ 도메인 모델에 없으면 만들지 마세요!
❌ 비즈니스 로직 직접 구현 금지
❌ 어댑터 구현체에 직접 의존 금지
❌ 트랜잭션 외 기술적 관심사 처리 금지
```

## 📦 애플리케이션 템플릿 구조

```
application/
├── command/            # 명령 측 (Command Side)
│   ├── port/
│   │   ├── in/        # 인바운드 포트 (Use Cases)
│   │   └── out/       # 아웃바운드 포트
│   └── handler/       # 명령 처리기
├── query/             # 조회 측 (Query Side)
│   ├── port/
│   │   ├── in/        # 조회 요청
│   │   └── out/       # 조회 포트
│   ├── handler/       # 조회 처리기
│   └── projection/    # Read Models
└── event/             # 이벤트 핸들러
```

## 📥 Command 인바운드 포트 템플릿

### Command 객체
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.in;

import javax.validation.constraints.*;
import java.util.Objects;

/**
 * {CommandDescription}.
 * 
 * @since 1.0.0
 */
public class {Entity}{Action}Command {
    
    @NotNull(message = "{Field} is required")
    private final {FieldType} {fieldName};
    
    @NotBlank(message = "{Field} cannot be blank")
    @Size(min = {min}, max = {max}, message = "{Field} must be between {min} and {max} characters")
    private final String {stringField};
    
    @Positive(message = "{Field} must be positive")
    private final Integer {numericField};
    
    @Valid
    @NotNull(message = "{Field} is required")
    private final {NestedObject} {nestedField};
    
    // 불변 객체를 위한 생성자
    public {Entity}{Action}Command(
        {FieldType} {fieldName},
        String {stringField},
        Integer {numericField},
        {NestedObject} {nestedField}
    ) {
        this.{fieldName} = Objects.requireNonNull({fieldName}, "{fieldName} cannot be null");
        this.{stringField} = Objects.requireNonNull({stringField}, "{stringField} cannot be null");
        this.{numericField} = {numericField};
        this.{nestedField} = Objects.requireNonNull({nestedField}, "{nestedField} cannot be null");
    }
    
    // 정적 팩토리 메서드
    public static {Entity}{Action}Command of(
        {FieldType} {fieldName},
        String {stringField}
    ) {
        return new {Entity}{Action}Command(
            {fieldName},
            {stringField},
            null,
            {NestedObject}.defaultValue()
        );
    }
    
    // Getter 메서드들
    public {FieldType} get{FieldName}() {
        return {fieldName};
    }
    
    public String get{StringField}() {
        return {stringField};
    }
    
    public Optional<Integer> get{NumericField}() {
        return Optional.ofNullable({numericField});
    }
    
    public {NestedObject} get{NestedField}() {
        return {nestedField};
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {Entity}{Action}Command that = ({Entity}{Action}Command) o;
        return Objects.equals({fieldName}, that.{fieldName}) &&
               Objects.equals({stringField}, that.{stringField}) &&
               Objects.equals({numericField}, that.{numericField}) &&
               Objects.equals({nestedField}, that.{nestedField});
    }
    
    @Override
    public int hashCode() {
        return Objects.hash({fieldName}, {stringField}, {numericField}, {nestedField});
    }
}
```

### Use Case 인터페이스
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.in;

/**
 * {UseCaseDescription}.
 * 
 * @since 1.0.0
 */
public interface {Entity}{Action}UseCase {
    
    /**
     * {Action을 수행합니다}.
     * 
     * @param command {action} 명령
     * @return {action} 결과
     * @throws {ValidationException} 입력값이 유효하지 않은 경우
     * @throws {BusinessException} 비즈니스 규칙 위반 시
     */
    {Entity}{Action}Response {action}({Entity}{Action}Command command);
}
```

### Response DTO
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.in;

import java.time.Instant;
import java.util.Objects;

/**
 * {ResponseDescription}.
 * 
 * @since 1.0.0
 */
public class {Entity}{Action}Response {
    
    private final String {entityId};
    private final {StatusEnum} status;
    private final String message;
    private final Instant processedAt;
    
    private {Entity}{Action}Response(
        String {entityId},
        {StatusEnum} status,
        String message,
        Instant processedAt
    ) {
        this.{entityId} = {entityId};
        this.status = status;
        this.message = message;
        this.processedAt = processedAt;
    }
    
    // 정적 팩토리 메서드들
    public static {Entity}{Action}Response success(String {entityId}) {
        return new {Entity}{Action}Response(
            {entityId},
            {StatusEnum}.SUCCESS,
            "{Action} completed successfully",
            Instant.now()
        );
    }
    
    public static {Entity}{Action}Response success(String {entityId}, String message) {
        return new {Entity}{Action}Response(
            {entityId},
            {StatusEnum}.SUCCESS,
            message,
            Instant.now()
        );
    }
    
    public static {Entity}{Action}Response failed(String message) {
        return new {Entity}{Action}Response(
            null,
            {StatusEnum}.FAILED,
            message,
            Instant.now()
        );
    }
    
    // Getter 메서드들
    public String get{EntityId}() {
        return {entityId};
    }
    
    public {StatusEnum} getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Instant getProcessedAt() {
        return processedAt;
    }
    
    public boolean isSuccess() {
        return status == {StatusEnum}.SUCCESS;
    }
}
```

## 📤 Command 아웃바운드 포트 템플릿

### 저장 포트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.out;

import com.company.security.domain.{subdomain}.aggregate.{Aggregate};
import com.company.security.domain.{subdomain}.vo.{AggregateId};

/**
 * {Aggregate} 저장 포트.
 * 
 * @since 1.0.0
 */
public interface Save{Aggregate}Port {
    
    /**
     * {Aggregate}를 저장합니다.
     * 
     * @param {aggregate} 저장할 애그리게이트
     * @return 저장된 애그리게이트
     */
    {Aggregate} save({Aggregate} {aggregate});
    
    /**
     * {Aggregate}를 업데이트합니다.
     * 
     * @param {aggregate} 업데이트할 애그리게이트
     */
    void update({Aggregate} {aggregate});
}
```

### 조회 포트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.out;

import com.company.security.domain.{subdomain}.aggregate.{Aggregate};
import com.company.security.domain.{subdomain}.vo.{AggregateId};
import java.util.Optional;

/**
 * {Aggregate} 로드 포트.
 * 
 * @since 1.0.0
 */
public interface Load{Aggregate}Port {
    
    /**
     * ID로 {Aggregate}를 조회합니다.
     * 
     * @param id 조회할 ID
     * @return 조회된 애그리게이트
     */
    Optional<{Aggregate}> loadById({AggregateId} id);
    
    /**
     * ID로 {Aggregate}를 조회합니다. 없으면 예외 발생.
     * 
     * @param id 조회할 ID
     * @return 조회된 애그리게이트
     * @throws {NotFoundException} 애그리게이트를 찾을 수 없는 경우
     */
    default {Aggregate} loadByIdOrThrow({AggregateId} id) {
        return loadById(id)
            .orElseThrow(() -> new {NotFoundException}(id));
    }
    
    /**
     * {특정 조건으로 조회합니다}.
     * 
     * @param {criteria} 조회 조건
     * @return 조회된 애그리게이트
     */
    Optional<{Aggregate}> loadBy{Criteria}({CriteriaType} {criteria});
}
```

### 이벤트 발행 포트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.port.out;

import com.company.security.domain.shared.DomainEvent;
import java.util.List;

/**
 * 도메인 이벤트 발행 포트.
 * 
 * @since 1.0.0
 */
public interface PublishDomainEventPort {
    
    /**
     * 단일 이벤트를 발행합니다.
     * 
     * @param event 발행할 이벤트
     */
    void publish(DomainEvent event);
    
    /**
     * 여러 이벤트를 발행합니다.
     * 
     * @param events 발행할 이벤트 목록
     */
    void publishAll(List<DomainEvent> events);
}
```

## 🎯 Command Handler 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.handler;

import com.company.security.application.command.port.in.*;
import com.company.security.application.command.port.out.*;
import com.company.security.domain.{subdomain}.aggregate.{Aggregate};
import com.company.security.domain.{subdomain}.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.Valid;
import java.util.Objects;

/**
 * {Entity} {Action} 명령 처리기.
 * 
 * @since 1.0.0
 */
@Service
@Transactional
public class {Entity}{Action}CommandHandler implements {Entity}{Action}UseCase {
    
    private final Load{Aggregate}Port load{Aggregate}Port;
    private final Save{Aggregate}Port save{Aggregate}Port;
    private final PublishDomainEventPort publishEventPort;
    private final {OtherDependency} {otherDependency};
    
    public {Entity}{Action}CommandHandler(
        Load{Aggregate}Port load{Aggregate}Port,
        Save{Aggregate}Port save{Aggregate}Port,
        PublishDomainEventPort publishEventPort,
        {OtherDependency} {otherDependency}
    ) {
        this.load{Aggregate}Port = Objects.requireNonNull(load{Aggregate}Port);
        this.save{Aggregate}Port = Objects.requireNonNull(save{Aggregate}Port);
        this.publishEventPort = Objects.requireNonNull(publishEventPort);
        this.{otherDependency} = Objects.requireNonNull({otherDependency});
    }
    
    @Override
    public {Entity}{Action}Response {action}(@Valid {Entity}{Action}Command command) {
        // 1. 전제 조건 검증
        validate{Precondition}(command);
        
        // 2. 애그리게이트 로드
        {Aggregate} {aggregate} = load{Aggregate}Port
            .loadByIdOrThrow({AggregateId}.of(command.get{AggregateId}()));
        
        // 3. 도메인 로직 실행
        {DomainResult} result = {aggregate}.{performAction}(
            command.get{Parameter1}(),
            command.get{Parameter2}()
        );
        
        // 4. 부수 효과 처리 (필요시)
        handle{SideEffect}(result);
        
        // 5. 애그리게이트 저장
        save{Aggregate}Port.update({aggregate});
        
        // 6. 이벤트 발행
        publishEventPort.publishAll({aggregate}.collectEvents());
        
        // 7. 응답 반환
        return {Entity}{Action}Response.success(
            {aggregate}.getId().getValue(),
            buildSuccessMessage(result)
        );
    }
    
    private void validate{Precondition}({Entity}{Action}Command command) {
        // 비즈니스 전제 조건 검증
        if ({preconditionCheck}) {
            throw new {PreconditionException}(
                "Cannot perform {action}: {reason}"
            );
        }
    }
    
    private void handle{SideEffect}({DomainResult} result) {
        // 필요한 경우 다른 애그리게이트나 서비스 호출
        if (result.requires{SideEffect}()) {
            {otherDependency}.perform{SideEffect}(result.get{Data}());
        }
    }
    
    private String buildSuccessMessage({DomainResult} result) {
        return String.format(
            "{Action} completed successfully. %s",
            result.getAdditionalInfo()
        );
    }
}
```

## 🔍 Query 인바운드 포트 템플릿

### Query 객체
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.port.in;

import org.springframework.data.domain.Pageable;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

/**
 * {QueryDescription}.
 * 
 * @since 1.0.0
 */
public class Get{Entity}{Criteria}Query {
    
    @NotNull(message = "Search criteria is required")
    private final {CriteriaType} criteria;
    
    @PastOrPresent(message = "From date cannot be in the future")
    private final LocalDate fromDate;
    
    @PastOrPresent(message = "To date cannot be in the future")
    private final LocalDate toDate;
    
    private final {FilterType} filter;
    private final Pageable pageable;
    
    private Get{Entity}{Criteria}Query(Builder builder) {
        this.criteria = Objects.requireNonNull(builder.criteria);
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
        this.filter = builder.filter;
        this.pageable = builder.pageable;
        
        // 날짜 범위 검증
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date must be before or equal to to date");
        }
    }
    
    // Builder 패턴
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private {CriteriaType} criteria;
        private LocalDate fromDate;
        private LocalDate toDate;
        private {FilterType} filter;
        private Pageable pageable = Pageable.unpaged();
        
        public Builder criteria({CriteriaType} criteria) {
            this.criteria = criteria;
            return this;
        }
        
        public Builder dateRange(LocalDate from, LocalDate to) {
            this.fromDate = from;
            this.toDate = to;
            return this;
        }
        
        public Builder filter({FilterType} filter) {
            this.filter = filter;
            return this;
        }
        
        public Builder pageable(Pageable pageable) {
            this.pageable = pageable;
            return this;
        }
        
        public Get{Entity}{Criteria}Query build() {
            return new Get{Entity}{Criteria}Query(this);
        }
    }
    
    // Getter 메서드들
    public {CriteriaType} getCriteria() {
        return criteria;
    }
    
    public Optional<LocalDate> getFromDate() {
        return Optional.ofNullable(fromDate);
    }
    
    public Optional<LocalDate> getToDate() {
        return Optional.ofNullable(toDate);
    }
    
    public Optional<{FilterType}> getFilter() {
        return Optional.ofNullable(filter);
    }
    
    public Pageable getPageable() {
        return pageable;
    }
}
```

### Query Use Case 인터페이스
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.port.in;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * {Entity} 조회 Use Case.
 * 
 * @since 1.0.0
 */
public interface Get{Entity}{Criteria}UseCase {
    
    /**
     * {기준에 따라 Entity를 조회합니다}.
     * 
     * @param query 조회 조건
     * @return 조회 결과
     */
    Page<{Entity}Response> get{Entities}(Get{Entity}{Criteria}Query query);
    
    /**
     * {단일 Entity를 조회합니다}.
     * 
     * @param {entityId} 조회할 ID
     * @return 조회된 Entity
     * @throws {NotFoundException} Entity를 찾을 수 없는 경우
     */
    {Entity}DetailResponse get{Entity}({EntityId} {entityId});
}
```

## 📤 Query 아웃바운드 포트 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.port.out;

import com.company.security.application.query.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.Optional;

/**
 * {Entity} 조회 포트.
 * 
 * @since 1.0.0
 */
public interface Load{Entity}QueryPort {
    
    /**
     * ID로 {Entity} 상세 정보를 조회합니다.
     * 
     * @param id 조회할 ID
     * @return 조회된 상세 정보
     */
    Optional<{Entity}DetailProjection> loadDetailById(String id);
    
    /**
     * 조건에 따라 {Entity} 목록을 조회합니다.
     * 
     * @param criteria 조회 조건
     * @param fromDate 시작일
     * @param toDate 종료일
     * @param pageable 페이징 정보
     * @return 조회된 목록
     */
    Page<{Entity}ListProjection> loadByCriteria(
        {CriteriaType} criteria,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );
    
    /**
     * {특정 통계를 조회합니다}.
     * 
     * @param {param} 조회 파라미터
     * @return 통계 정보
     */
    {Statistics}Projection load{Statistics}({ParamType} {param});
}
```

## 🎯 Query Handler 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.handler;

import com.company.security.application.query.port.in.*;
import com.company.security.application.query.port.out.*;
import com.company.security.application.query.projection.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.Valid;
import java.util.Objects;

/**
 * {Entity} 조회 처리기.
 * 
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class {Entity}QueryHandler implements Get{Entity}{Criteria}UseCase {
    
    private final Load{Entity}QueryPort queryPort;
    private final {Mapper} mapper;
    
    public {Entity}QueryHandler(
        Load{Entity}QueryPort queryPort,
        {Mapper} mapper
    ) {
        this.queryPort = Objects.requireNonNull(queryPort);
        this.mapper = Objects.requireNonNull(mapper);
    }
    
    @Override
    public Page<{Entity}Response> get{Entities}(@Valid Get{Entity}{Criteria}Query query) {
        // 페이징 검증 및 조정
        Pageable pageable = validateAndAdjustPageable(query.getPageable());
        
        // 조회 실행
        Page<{Entity}ListProjection> projections = queryPort.loadByCriteria(
            query.getCriteria(),
            query.getFromDate().orElse(null),
            query.getToDate().orElse(null),
            pageable
        );
        
        // Response로 변환
        return projections.map(mapper::toResponse);
    }
    
    @Override
    public {Entity}DetailResponse get{Entity}({EntityId} {entityId}) {
        Objects.requireNonNull({entityId}, "{EntityId} cannot be null");
        
        {Entity}DetailProjection projection = queryPort
            .loadDetailById({entityId}.getValue())
            .orElseThrow(() -> new {NotFoundException}({entityId}));
        
        return mapper.toDetailResponse(projection);
    }
    
    private Pageable validateAndAdjustPageable(Pageable pageable) {
        // 최대 페이지 크기 제한
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(
                pageable.getPageNumber(),
                MAX_PAGE_SIZE,
                pageable.getSort()
            );
        }
        return pageable;
    }
    
    private static final int MAX_PAGE_SIZE = 100;
}
```

## 📊 Projection 템플릿

### List Projection
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.projection;

import java.time.Instant;

/**
 * {Entity} 목록 조회용 Projection.
 * 
 * @since 1.0.0
 */
public class {Entity}ListProjection {
    
    private final String id;
    private final String name;
    private final String status;
    private final Instant createdAt;
    private final Long totalCount;
    
    public {Entity}ListProjection(
        String id,
        String name,
        String status,
        Instant createdAt,
        Long totalCount
    ) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.totalCount = totalCount;
    }
    
    // Getter 메서드들
    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getTotalCount() { return totalCount; }
}
```

### Detail Projection
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.projection;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * {Entity} 상세 조회용 Projection.
 * 
 * @since 1.0.0
 */
public class {Entity}DetailProjection {
    
    private final String id;
    private final String name;
    private final String description;
    private final String status;
    private final Map<String, Object> metadata;
    private final List<{RelatedItem}> relatedItems;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String createdBy;
    private final String updatedBy;
    
    // Builder 패턴
    private {Entity}DetailProjection(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.status = builder.status;
        this.metadata = builder.metadata;
        this.relatedItems = builder.relatedItems;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String name;
        private String description;
        private String status;
        private Map<String, Object> metadata;
        private List<{RelatedItem}> relatedItems;
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        
        // Builder 메서드들
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        // ... 다른 setter 메서드들
        
        public {Entity}DetailProjection build() {
            return new {Entity}DetailProjection(this);
        }
    }
    
    // Getter 메서드들
    public String getId() { return id; }
    public String getName() { return name; }
    // ... 다른 getter 메서드들
}
```

## 🎪 Event Handler 템플릿

### Projection 업데이트 핸들러
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.event;

import com.company.security.domain.{subdomain}.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.Objects;

/**
 * Read Model 프로젝션 업데이트 핸들러.
 * 
 * @since 1.0.0
 */
@Component
public class {Entity}ProjectionEventHandler {
    
    private final Update{Entity}ProjectionPort updateProjectionPort;
    
    public {Entity}ProjectionEventHandler(
        Update{Entity}ProjectionPort updateProjectionPort
    ) {
        this.updateProjectionPort = Objects.requireNonNull(updateProjectionPort);
    }
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on({Entity}Created event) {
        // 새 프로젝션 생성
        {Entity}Projection projection = {Entity}Projection.createFrom(event);
        updateProjectionPort.save(projection);
    }
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on({Entity}Updated event) {
        // 기존 프로젝션 업데이트
        updateProjectionPort.updateBy{Criteria}(
            event.getAggregateId(),
            projection -> projection.applyUpdate(event)
        );
    }
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on({Entity}Deleted event) {
        // 프로젝션 삭제 또는 상태 변경
        updateProjectionPort.markAsDeleted(event.getAggregateId());
    }
}
```

### 알림 이벤트 핸들러
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.event;

import com.company.security.domain.{subdomain}.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * 알림 이벤트 핸들러.
 * 
 * @since 1.0.0
 */
@Component
public class {Entity}NotificationEventHandler {
    
    private final NotificationPort notificationPort;
    private final {UserContextPort} userContextPort;
    
    public {Entity}NotificationEventHandler(
        NotificationPort notificationPort,
        {UserContextPort} userContextPort
    ) {
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.userContextPort = Objects.requireNonNull(userContextPort);
    }
    
    @EventListener
    public void on({CriticalEvent} event) {
        // 즉시 알림이 필요한 중요 이벤트
        {UserContext} context = userContextPort.loadBy{EntityId}(
            event.getAggregateId()
        );
        
        NotificationRequest request = NotificationRequest.builder()
            .recipient(context.getNotificationEmail())
            .subject("Critical: {Event description}")
            .template("{CRITICAL_EVENT_TEMPLATE}")
            .parameters(Map.of(
                "entityId", event.getAggregateId(),
                "eventType", event.getEventType(),
                "occurredAt", event.getOccurredAt()
            ))
            .priority(NotificationPriority.HIGH)
            .build();
        
        notificationPort.send(request);
    }
    
    @EventListener
    public void on({RegularEvent} event) {
        // 일반 알림
        if (shouldNotify(event)) {
            queueNotification(event);
        }
    }
    
    private boolean shouldNotify({RegularEvent} event) {
        // 알림 필요 여부 판단 로직
        return {notificationCriteria};
    }
    
    private void queueNotification({RegularEvent} event) {
        // 배치 처리를 위한 알림 큐잉
        notificationPort.queue(
            NotificationRequest.fromEvent(event)
        );
    }
}
```

## 🛠️ 공통 Application 서비스 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

/**
 * {ServiceDescription}.
 * 
 * <p>여러 Use Case에서 공통으로 사용되는 애플리케이션 서비스
 * 
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class {CommonService} {
    
    private final {Dependency1} dependency1;
    private final {Dependency2} dependency2;
    
    public {CommonService}(
        {Dependency1} dependency1,
        {Dependency2} dependency2
    ) {
        this.dependency1 = Objects.requireNonNull(dependency1);
        this.dependency2 = Objects.requireNonNull(dependency2);
    }
    
    /**
     * {공통 기능을 수행합니다}.
     * 
     * @param {param} 파라미터
     * @return 처리 결과
     */
    public {Result} perform{Operation}({Parameter} {param}) {
        // 공통 로직 수행
        validate{Parameter}({param});
        
        {ProcessedData} processed = process({param});
        
        return {Result}.from(processed);
    }
    
    private void validate{Parameter}({Parameter} {param}) {
        // 공통 검증 로직
        if ({validationCondition}) {
            throw new {ValidationException}("Invalid parameter: " + {param});
        }
    }
    
    private {ProcessedData} process({Parameter} {param}) {
        // 공통 처리 로직
        return dependency1.process({param});
    }
}
```

## 🔐 보안 관련 애플리케이션 서비스

```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.service;

import com.company.security.domain.auth.vo.ClientId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * 보안 컨텍스트 서비스.
 * 
 * @since 1.0.0
 */
@Service
public class SecurityContextService {
    
    private final CurrentUserPort currentUserPort;
    private final PermissionEvaluator permissionEvaluator;
    
    public SecurityContextService(
        CurrentUserPort currentUserPort,
        PermissionEvaluator permissionEvaluator
    ) {
        this.currentUserPort = Objects.requireNonNull(currentUserPort);
        this.permissionEvaluator = Objects.requireNonNull(permissionEvaluator);
    }
    
    /**
     * 현재 인증된 클라이언트 ID를 반환합니다.
     */
    public ClientId getCurrentClientId() {
        return currentUserPort.getCurrentUser()
            .map(user -> ClientId.of(user.getClientId()))
            .orElseThrow(() -> new UnauthorizedException("No authenticated client"));
    }
    
    /**
     * 현재 사용자가 특정 리소스에 대한 권한이 있는지 확인합니다.
     */
    @PreAuthorize("isAuthenticated()")
    public boolean hasPermission(String resourceId, String permission) {
        CurrentUser user = currentUserPort.getCurrentUser()
            .orElseThrow(() -> new UnauthorizedException("No authenticated user"));
            
        return permissionEvaluator.hasPermission(
            user,
            resourceId,
            permission
        );
    }
    
    /**
     * 리소스 소유자인지 확인합니다.
     */
    public void verifyOwnership(String resourceId, ClientId ownerId) {
        ClientId currentClientId = getCurrentClientId();
        
        if (!currentClientId.equals(ownerId)) {
            throw new ForbiddenException(
                String.format("Client %s is not the owner of resource %s",
                    currentClientId.getValue(), resourceId)
            );
        }
    }
}
```

## 📝 예외 템플릿

### 애플리케이션 예외
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.exception;

/**
 * {Entity}를 찾을 수 없을 때 발생하는 예외.
 * 
 * @since 1.0.0
 */
public class {Entity}NotFoundException extends ApplicationException {
    
    private final String {entityId};
    
    public {Entity}NotFoundException(String {entityId}) {
        super(String.format("{Entity} not found with ID: %s", {entityId}));
        this.{entityId} = {entityId};
    }
    
    public {Entity}NotFoundException({EntityId} {entityId}) {
        this({entityId}.getValue());
    }
    
    public String get{EntityId}() {
        return {entityId};
    }
    
    @Override
    public String getErrorCode() {
        return "{ENTITY}_NOT_FOUND";
    }
}
```

### 검증 예외
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.exception;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command/Query 검증 실패 예외.
 * 
 * @since 1.0.0
 */
public class ValidationException extends ApplicationException {
    
    private final Set<ValidationError> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = Set.of(new ValidationError("general", message));
    }
    
    public ValidationException(Set<ConstraintViolation<?>> violations) {
        super(buildMessage(violations));
        this.errors = violations.stream()
            .map(v -> new ValidationError(
                v.getPropertyPath().toString(),
                v.getMessage()
            ))
            .collect(Collectors.toSet());
    }
    
    private static String buildMessage(Set<ConstraintViolation<?>> violations) {
        return "Validation failed: " + violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
    }
    
    public Set<ValidationError> getErrors() {
        return errors;
    }
    
    @Override
    public String getErrorCode() {
        return "VALIDATION_FAILED";
    }
    
    public record ValidationError(String field, String message) {}
}
```

## 🎯 Mapper 인터페이스 템플릿

```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.mapper;

import com.company.security.application.query.projection.*;
import com.company.security.application.command.port.in.*;
import com.company.security.domain.{subdomain}.aggregate.*;
import org.mapstruct.*;

/**
 * 애플리케이션 계층 매핑 인터페이스.
 * 
 * @since 1.0.0
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface {Entity}ApplicationMapper {
    
    // Command → Domain
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    {Entity} toDomain(Create{Entity}Command command);
    
    // Domain → Response
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "status", target = "status")
    {Entity}Response toResponse({Entity} entity);
    
    // Projection → Response
    {Entity}Response toResponse({Entity}ListProjection projection);
    
    {Entity}DetailResponse toDetailResponse({Entity}DetailProjection projection);
    
    // Custom mappings
    @AfterMapping
    default void enrichResponse(@MappingTarget {Entity}Response.Builder builder, {Entity} entity) {
        // 추가적인 매핑 로직
        if (entity.hasSpecialCondition()) {
            builder.additionalInfo("Special condition applied");
        }
    }
}
```

## 🔧 애플리케이션 설정 템플릿

### Command 설정
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.config;

import com.company.security.application.command.handler.*;
import com.company.security.application.command.port.in.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Command 측 설정.
 * 
 * @since 1.0.0
 */
@Configuration
public class CommandConfiguration {
    
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
    
    @Bean
    public CommandValidator commandValidator() {
        return new CommandValidator();
    }
    
    @Bean
    public TransactionManager commandTransactionManager() {
        return new CommandTransactionManager();
    }
}
```

### Query 설정
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Query 측 설정.
 * 
 * @since 1.0.0
 */
@Configuration
@EnableSpringDataWebSupport
public class QueryConfiguration {
    
    @Bean
    public PageableResolver pageableResolver() {
        return PageableResolver.builder()
            .defaultPageSize(20)
            .maxPageSize(100)
            .build();
    }
    
    @Bean
    public QueryOptimizer queryOptimizer() {
        return new QueryOptimizer();
    }
}
```

### 이벤트 설정
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * 이벤트 처리 설정.
 * 
 * @since 1.0.0
 */
@Configuration
@EnableAsync
public class EventConfiguration {
    
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        eventMulticaster.setErrorHandler(new EventErrorHandler());
        return eventMulticaster;
    }
    
    @Bean
    public EventStore eventStore() {
        return new JpaEventStore();
    }
}
```

## 📚 테스트 템플릿

### Command Handler 테스트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.command.handler;

import com.company.security.application.command.port.in.*;
import com.company.security.application.command.port.out.*;
import com.company.security.domain.{subdomain}.aggregate.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class {Entity}{Action}CommandHandlerTest {
    
    @Mock
    private Load{Aggregate}Port load{Aggregate}Port;
    
    @Mock
    private Save{Aggregate}Port save{Aggregate}Port;
    
    @Mock
    private PublishDomainEventPort publishEventPort;
    
    @InjectMocks
    private {Entity}{Action}CommandHandler handler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("올바른 명령으로 {action}을 수행하면 성공한다")
    void should{Action}Successfully() {
        // Given
        {Entity}{Action}Command command = {Entity}{Action}Command.builder()
            .{field1}("value1")
            .{field2}("value2")
            .build();
            
        {Aggregate} aggregate = {Aggregate}.create(/* ... */);
        when(load{Aggregate}Port.loadByIdOrThrow(any())).thenReturn(aggregate);
        
        // When
        {Entity}{Action}Response response = handler.{action}(command);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        verify(save{Aggregate}Port).update(aggregate);
        verify(publishEventPort).publishAll(anyList());
    }
    
    @Test
    @DisplayName("전제 조건을 만족하지 않으면 예외가 발생한다")
    void shouldThrowExceptionWhenPreconditionFails() {
        // Given
        {Entity}{Action}Command command = {Entity}{Action}Command.builder()
            .{field1}("invalid")
            .build();
        
        // When & Then
        assertThatThrownBy(() -> handler.{action}(command))
            .isInstanceOf({PreconditionException}.class)
            .hasMessageContaining("Cannot perform {action}");
    }
}
```

### Query Handler 테스트
```java
package com.dx.hexacore.security.{애그리거트명소문자}.application.query.handler;

import com.company.security.application.query.port.in.*;
import com.company.security.application.query.port.out.*;
import com.company.security.application.query.projection.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class {Entity}QueryHandlerTest {
    
    @Mock
    private Load{Entity}QueryPort queryPort;
    
    @Mock
    private {Mapper} mapper;
    
    @InjectMocks
    private {Entity}QueryHandler handler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("조회 조건에 따라 {Entity} 목록을 반환한다")
    void shouldReturn{Entities}ByCriteria() {
        // Given
        Get{Entity}{Criteria}Query query = Get{Entity}{Criteria}Query.builder()
            .criteria("test")
            .pageable(PageRequest.of(0, 10))
            .build();
            
        List<{Entity}ListProjection> projections = List.of(
            new {Entity}ListProjection(/* ... */)
        );
        Page<{Entity}ListProjection> page = new PageImpl<>(projections);
        
        when(queryPort.loadByCriteria(any(), any(), any(), any())).thenReturn(page);
        when(mapper.toResponse(any({Entity}ListProjection.class)))
            .thenReturn(new {Entity}Response(/* ... */));
        
        // When
        Page<{Entity}Response> result = handler.get{Entities}(query);
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
```