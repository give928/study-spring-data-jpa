# 김영한님 "실전! 스프링 데이터 JPA" 스터디

참고:스프링 부트를 통해 복잡한 설정이 다 자동화 되었다.

persistence.xml 도 없고, LocalContainerEntityManagerFactoryBean 도 없다. 스프링 부트를 통한 추가 설정은 스프링 부트 메뉴얼을 참고하고, 스프링 부트를 사용하지 않고 순수 스프링과 JPA 설정 방법은 자바 ORM 표준 JPA 프로그래밍 책을 참고하자.

엔티티 설정

- @NoArgsConstructor AccessLevel.PROTECTED: 기본 생성자 막고 싶은데, JPA 스팩상 PROTECTED로 열어두어야 함
- @ToString은 가급적 내부 필드만(연관관계 없는 필드만)

**공통 인터페이스 기능**

JPA에서 수정은 변경감지 기능을 사용하면 된다.
> 트랜잭션 안에서 엔티티를 조회한 다음에 데이터를 변경하면, 트랜잭션 종료 시점에 변경감지 기능이

작동해서 변경된 엔티티를 감지하고 UPDATE SQL을 실행한다.

### 공통 인터페이스 설정

**JavaConfig 설정- 스프링 부트 사용시 생략 가능**

```java
@Configuration
@EnableJpaRepositories(basePackages = "jpabook.jpashop.repository")
public class AppConfig {}
```

스프링 부트 사용시 @SpringBootApplication 위치를 지정(해당 패키지와 하위 패키지 인식)

만약 위치가 달라지면 @EnableJpaRepositories 필요

**스프링 데이터 JPA가 구현 클래스 대신 생성**

![Untitled](./docs/images/repository1.png)

![Untitled](./docs/images/repository2.png)

- org.springframework.data.repository.Repository 를 구현한 클래스는 스캔 대상
  - MemberRepository 인터페이스가 동작한 이유
  - 실제 출력해보기(com.sun.proxy.$Proxy)
  - memberRepository.getClass() class com.sun.proxy.$ProxyXXX
- @Repository 애노테이션 생략 가능
  - 컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리
  - JPA 예외를 스프링 예외로 변환하는 과정도 자동으로 처리
- JpaRepository 인터페이스: 공통 CRUD 제공
- 제네릭은 <엔티티 타입, 식별자 타입> 설정

**제네릭 타입**

- T : 엔티티
- ID : 엔티티의 식별자 타입
- S : 엔티티와 그 자식 타입

**주요 메서드**

- save(S) : 새로운 엔티티는 저장하고 이미 있는 엔티티는 병합한다.
- delete(T) : 엔티티 하나를 삭제한다. 내부에서 EntityManager.remove() 호출
- findById(ID) : 엔티티 하나를 조회한다. 내부에서 EntityManager.find() 호출
- getOne(ID) : 엔티티를 프록시로 조회한다. 내부에서 EntityManager.getReference() 호출
- findAll(...) : 모든 엔티티를 조회한다. 정렬( Sort )이나 페이징( Pageable ) 조건을 파라미터로 제공할 수
  있다.

### 쿼리 메서드 기능

- 메소드 이름으로 쿼리 생성
  - 메소드 이름을 분석해서 JPQL 쿼리 실행

    ```java
    public interface MemberRepository extends JpaRepository<Member, Long> {
        List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    }
    ```

  - **쿼리 메소드 필터 조건**
    스프링 데이터 JPA 공식 문서 참고: ([https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation))
  - **스프링 데이터 JPA가 제공하는 쿼리 메소드 기능**
    - 조회: find...By ,read...By ,query...By get...By,
      - [https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation)
        예:) findHelloBy 처럼 ...에 식별하기 위한 내용(설명)이 들어가도 된다.
    - COUNT: count...By 반환타입 long
    - EXISTS: exists...By 반환타입 boolean
    - 삭제: delete...By, remove...By 반환타입 long
    - DISTINCT: findDistinct, findMemberDistinctBy
    - LIMIT: findFirst3, findFirst, findTop, findTop3
      - [https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result)

    참고: 이 기능은 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 함께 변경해야 한다.
    그렇지 않으면 애플리케이션을 시작하는 시점에 오류가 발생한다.
    이렇게 애플리케이션 로딩 시점에 오류를 인지할 수 있는 것이 스프링 데이터 JPA의 매우 큰 장점이다.

- 메소드 이름으로 JPA NamedQuery 호출

  ```java
  @Query(name = "Member.findByUsername")
  List<Member> findByUsername(@Param("username") String username);
  ```

  - @Query 를 생략하고 메서드 이름만으로 Named 쿼리를 호출할 수 있다.
  - 스프링 데이터 JPA는 선언한 "도메인 클래스 + .(점) + 메서드 이름"으로 Named 쿼리를 찾아서 실행
  - 만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용한다.
  - 필요하면 전략을 변경할 수 있지만 권장하지 않는다.
  - 참고: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-lookup-strategies
  - 참고: 스프링 데이터 JPA를 사용하면 실무에서 Named Query를 직접 등록해서 사용하는 일은 드물다.
    대신 @Query 를 사용해서 리파지토리 메소드에 쿼리를 직접 정의한다.
- @Query 어노테이션을 사용해서 리파지토리 인터페이스에 쿼리 직접 정의
  - @org.springframework.data.jpa.repository.Query 어노테이션을 사용
  - 실행할 메서드에 정적 쿼리를 직접 작성하므로 이름 없는 Named 쿼리라 할 수 있음
  - JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류를 발견할 수 있음(매우 큰 장점!)
  - 참고: 실무에서는 메소드 이름으로 쿼리 생성 기능은 파라미터가 증가하면 메서드 이름이 매우 지저분해진다. 따라서 @Query 기능을 자주 사용하게 된다.
  - 단순 값 하나를 조회 - JPA 값 타입( @Embedded )도 이 방식으로 조회할 수 있다.

    ```java
    @Query("select m.username from Member m")
    List<String> findAllUsernames();
    ```

  - DTO로 직접 조회

    ```java
    @Query("select new com.give928.study.springdata.jpa.repository.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findAllMemberDtos();
    ```


### 파라미터 바인딩

```jpaql
select m from Member m where m.username = ?0 // 위치 기반
select m from Member m where m.username = :name // 이름 기반
```

참고: 코드 가독성과 유지보수를 위해 이름 기반 파라미터 바인딩을 사용하자 (위치기반은 순서 실수가 바꾸면...)

**컬렉션 파라미터 바인딩 -** Collection 타입으로 in절 지원

```java
@Query("select m from Member m where m.username in :names")
List<Member> findByNames(@Param("names") List<String> names);
```

### 반환 타입

스프링 데이터 JPA 공식 문서: [https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types)

- 컬렉션
- 단건
- 단건 Optional

**조회 결과가 많거나 없으면?**

- 컬렉션
  - 결과 없음: 빈 컬렉션 반환
- 단건 조회
  - 결과 없음: null 반환
  - 결과가 2건 이상: javax.persistence.NonUniqueResultException 예외 발생

참고: 단건으로 지정한 메서드를 호출하면 스프링 데이터 JPA는 내부에서 JPQL의 Query.getSingleResult() 메서드를 호출한다. 이 메서드를 호출했을 때 조회 결과가 없으면 javax.persistence.NoResultException 예외가 발생하는데 개발자 입장에서 다루기가 상당히 불편하다. 스프링 데이터 JPA는 단건을 조회할 때 이 예외가 발생하면 예외를 무시하고 대신에 null 을 반환한다.

### 페이징과 정렬

**페이징과 정렬 파라미터**

- org.springframework.data.domain.Sort : 정렬 기능
- org.springframework.data.domain.Pageable : 페이징 기능 (내부에 Sort 포함)

**특별한 반환 타입**

- org.springframework.data.domain.Page : 추가 count 쿼리 결과를 포함하는 페이징
- org.springframework.data.domain.Slice : 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit + 1 개 조회하고 데이터는 limit 만큼만 반환해서 다음 페이지 여부만 확인)
  - (최근 모바일 리스트 생각해보면 됨)
- List (자바 컬렉션): 추가 count 쿼리 없이 결과만 반환

```java
PageRequest pageRequest = 
        PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
```

두 번째 파라미터로 받은 Pagable 은 인터페이스다. 따라서 실제 사용할 때는 해당 인터페이스를 구현한
org.springframework.data.domain.PageRequest 객체를 사용한다.
PageRequest 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를
입력한다. 여기에 추가로 정렬 정보도 파라미터로 사용할 수 있다. 참고로 페이지는 0부터 시작한다.

주의: Page는 1부터 시작이 아니라 0부터 시작이다.

**참고: count 쿼리를 다음과 같이 분리할 수 있음**

- 복잡한 sql에서 사용, 데이터는 left join, 카운트는 left join 안해도 됨
- 전체 count 쿼리는 매우 무겁다.

```java
@Query(value = “select m from Member m”,
       countQuery = “select count(m.username) from Member m”)
Page<Member> findMemberAllCountBy(Pageable pageable);
```

**Top, First 사용 참고**
https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-
query-result

```java
List<Member> findTop3By();
```

**페이지를 유지하면서 엔티티를 DTO로 변환하기**

```java
Page<Member> page = memberRepository.findByAge(10, pageRequest);
Page<MemberDto> dtoPage = page.map(m -> new MemberDto());
```

### 벌크성 수정 쿼리

```java
@Modifying
@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

- 벌크성 수정, 삭제 쿼리는 @Modifying 어노테이션을 사용
  - 사용하지 않으면 다음 예외 발생 org.hibernate.hql.internal.QueryExecutionRequestException: Not supported for DML operations
- 벌크성 쿼리를 실행하고 나서 영속성 컨텍스트 초기화: @Modifying(clearAutomatically = true)
  (이 옵션의 기본값은 false )
  - 이 옵션 없이 회원을 findById로 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될 수
    있다. 만약 다시 조회해야 하면 꼭 영속성 컨텍스트를 초기화 하자.

> 참고: 벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 때문에, 영속성 컨텍스트에 있는 엔티티의 상태와
DB에 엔티티 상태가 달라질 수 있다.

> 권장하는 방안
> 1. 영속성 컨텍스트에 엔티티가 없는 상태에서 벌크 연산을 먼저 실행한다.
> 2. 부득이하게 영속성 컨텍스트에 엔티티가 있으면 벌크 연산 직후 영속성 컨텍스트를 초기화 한다.

### @EntityGraph

연관된 엔티티들을 SQL 한번에 조회하는 방법

member → team은 지연로딩 관계이다. 따라서 다음과 같이 team의 데이터를 조회할 때 마다 쿼리가 실행된다. (N+1 문제 발생)

참고: 다음과 같이 지연 로딩 여부를 확인할 수 있다.

```java
//Hibernate 기능으로 확인
Hibernate.isInitialized(member.getTeam())

//JPA 표준 방법으로 확인
PersistenceUnitUtil util =
em.getEntityManagerFactory().getPersistenceUnitUtil();
util.isLoaded(member.getTeam());
```

연관된 엔티티를 한번에 조회하려면 페치 조인이 필요하다.

```java
//공통 메서드 오버라이드
@Override
@EntityGraph(attributePaths = {"team"}) 
List<Member> findAll();

//JPQL + 엔티티 그래프 
@EntityGraph(attributePaths = {"team"}) 
@Query("select m from Member m") 
List<Member> findMemberEntityGraph();

//메서드 이름으로 쿼리에서 특히 편리하다. 
@EntityGraph(attributePaths = {"team"}) 
List<Member> findByUsername(String username)
```

- 사실상 페치 조인(FETCH JOIN)의 간편 버전
- LEFT OUTER JOIN 사용

**NamedEntityGraph 사용 방법**

```java
@NamedEntityGraph(name = "Member.team", attributeNodes = {@NamedAttributeNode("team")})
@Entity
public class Member {}
```

```java
@EntityGraph("Member.team")
@Query("select m from Member m")
List<Member> findMemberEntityGraph();
```

### **JPA Hint & Lock**

**JPA Hint**

JPA 쿼리 힌트(SQL 힌트가 아니라 JPA 구현체에게 제공하는 힌트)

**쿼리 힌트 사용**

```java
@QueryHints(value = {@QueryHint(name = "org.hibernate.readOnly", value = "true")})
Member findReadOnlyByUsername(String username);
```

- org.springframework.data.jpa.repository.QueryHints 어노테이션을 사용
- forCounting : 반환 타입으로 Page 인터페이스를 적용하면 추가로 호출하는 페이징을 위한 count 쿼리도 쿼리 힌트 적용(기본값 true )

**Lock**

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<Member> findByUsername(String name);
```

org.springframework.data.jpa.repository.Lock 어노테이션을 사용

## 확장 기능

### 사용자 정의 리포지토리 구현

- 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성
- 스프링 데이터 JPA가 제공하는 인터페이스를 직접 구현하면 구현해야 하는 기능이 너무 많음
- 다양한 이유로 인터페이스의 메서드를 직접 구현하고 싶다면?
  - JPA 직접 사용( EntityManager )
  - 스프링 JDBC Template 사용
  - MyBatis 사용
  - 데이터베이스 커넥션 직접 사용 등등...
  - Querydsl 사용

사용자 정의 인터페이스

```java
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
```

**사용자 정의 인터페이스 구현 클래스**

```java
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final EntityManager em;
    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m")
                .getResultList();
    }
}
```

**사용자 정의 인터페이스 상속**

```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
}
```

**사용자 정의 메서드 호출 코드**

```java
List<Member> result = memberRepository.findMemberCustom();
```

**사용자 정의 구현 클래스**

- 규칙: 리포지토리 인터페이스 이름 + Impl
  - 스프링 데이터 2.x 부터는 사용자 정의 인터페이스 명 + Impl 방식도 지원
    - MemberRepositoryImpl 대신에 MemberRepositoryCustomImpl
    - 기존 방식보다 이 방식이 사용자 정의 인터페이스 이름과 구현 클래스 이름이 비슷하므로 더 직관적이다. 추가로 여러 인터페이스를 분리해서 구현하는 것도 가능하기 때문에 새롭게 변경된 이 방식을 사용하는 것을 더 권장한다.
- 스프링 데이터 JPA가 인식해서 스프링 빈으로 등록

**Impl 대신 다른 이름으로 변경하고 싶으면?**

- **XML 설정**

  ```xml
  <repositories base-package="study.datajpa.repository" repository-impl-postfix="Impl" />
  ```

- **JavaConfig 설정**

  ```java
  @EnableJpaRepositories(basePackages = "study.datajpa.repository", repositoryImplementationPostfix = "Impl")
  ```

- 참고: 실무에서는 주로 QueryDSL이나 SpringJdbcTemplate을 함께 사용할 때 사용자 정의 리포지토리 기능 자주 사용
- 참고: 항상 사용자 정의 리포지토리가 필요한 것은 아니다. 그냥 임의의 리포지토리를 만들어도 된다.
  예를들어 MemberQueryRepository를 인터페이스가 아닌 클래스로 만들고 스프링 빈으로 등록해서
  그냥 직접 사용해도 된다. 물론 이 경우 스프링 데이터 JPA와는 아무런 관계 없이 별도로 동작한다.

### Auditing

@EnableJpaAuditing → 스프링 부트 설정 클래스에 적용해야함

@EntityListeners(AuditingEntityListener.class) → 엔티티에 적용

**사용 어노테이션**

- @CreatedDate
  - `@Column(updatable = false)`
- @LastModifiedDate
- @CreatedBy
  - `@Column(updatable = false)`
- @LastModifiedBy

등록자, 수정자를 처리해주는 AuditorAware 스프링 빈 등록

```java
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.of(UUID.randomUUID().toString());
}
```

실무에서는 세션 정보나, 스프링 시큐리티 로그인 정보에서 ID를 받음

**전체 적용**
@EntityListeners(AuditingEntityListener.class) 를 생략하고 스프링 데이터 JPA 가 제공하는
이벤트를 엔티티 전체에 적용하려면 orm.xml에 다음과 같이 등록하면 된다.

```xml
META-INF/orm.xml

<?xml version=“1.0” encoding="UTF-8”?>
<entity-mappings xmlns=“http://xmlns.jcp.org/xml/ns/persistence/orm”
                 xmlns:xsi=“http://www.w3.org/2001/XMLSchema-instance”
                 xsi:schemaLocation=“http://xmlns.jcp.org/xml/ns/persistence/orm http://xmlns.jcp.org/xml/ns/persistence/orm_2_2.xsd”
                 version=“2.2">
  <persistence-unit-metadata>
    <persistence-unit-defaults>
      <entity-listeners>
        <entity-listener class="org.springframework.data.jpa.domain.support.AuditingEntityListener”/>
      </entity-listeners>
    </persistence-unit-defaults>
  </persistence-unit-metadata>
</entity-mappings>
```

### **Web 확장 - 도메인 클래스 컨버터**

HTTP 파라미터로 넘어온 엔티티의 아이디로 엔티티 객체를 찾아서 바인딩

도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다. (트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.)

사용하지 않는 것이 좋겠다.

### **Web 확장 - 페이징과 정렬**

```java
@GetMapping("/members")
public Page<Member> list(Pageable pageable) {
    return memberRepository.findAll(pageable);
}
```

- 파라미터로 Pageable 을 받을 수 있다.
- Pageable 은 인터페이스, 실제는 org.springframework.data.domain.PageRequest 객체 생성

**요청 파라미터**

- 예) /members?page=0&size=3&sort=id,desc&sort=username,desc
- page: 현재 페이지, **0부터 시작한다.**
- size: 한 페이지에 노출할 데이터 건수
- sort: 정렬 조건을 정의한다. 예) 정렬 속성,정렬 속성...(ASC | DESC), 정렬 방향을 변경하고 싶으면 sort 파라미터 추가 ( asc 생략 가능)

**기본값**

- 글로벌 설정: 스프링 부트

  ```java
  spring.data.web.pageable.default-page-size=20 /# 기본 페이지 사이즈/
  spring.data.web.pageable.max-page-size=2000 /# 최대 페이지 사이즈/
  ```

- 개별 설정
  @PageableDefault 어노테이션을 사용

```java
@RequestMapping(value = "/members", method = RequestMethod.GET)
public String list(@PageableDefault(size = 12, sort = “username”, 
                   direction = Sort.Direction.DESC) Pageable pageable) {
    ...
}
```

**접두사**

- 페이징 정보가 둘 이상이면 접두사로 구분
- @Qualifier 에 접두사명 추가 "{접두사명}_xxx”
- 예제: /members?member_page=0&order_page=1

```java
public String list(
    @Qualifier("member") Pageable memberPageable,
    @Qualifier("order") Pageable orderPageable, ...
```

**Page 내용을 DTO로 변환하기**

- 엔티티를 API로 노출하면 다양한 문제가 발생한다. 그래서 엔티티를 꼭 DTO로 변환해서 반환해야 한다.
- Page는 map() 을 지원해서 내부 데이터를 다른 것으로 변경할 수 있다.

```java
@GetMapping("/members")
public Page<MemberDto> list(Pageable pageable) {
    return memberRepository.findAll(pageable).map(MemberDto::new);
}
```

### **스프링 데이터 JPA 분석**

**스프링 데이터 JPA 구현체 분석**

스프링 데이터 JPA가 제공하는 공통 인터페이스의 구현체
org.springframework.data.jpa.repository.support.SimpleJpaRepository

- @Repository 적용: JPA 예외를 스프링이 추상화한 예외로 변환
- @Transactional 트랜잭션 적용
  - JPA의 모든 변경은 트랜잭션 안에서 동작
  - 스프링 데이터 JPA는 변경(등록, 수정, 삭제) 메서드를 트랜잭션 처리
  - 서비스 계층에서 트랜잭션을 시작하지 않으면 리파지토리에서 트랜잭션 시작
  - 서비스 계층에서 트랜잭션을 시작하면 리파지토리는 해당 트랜잭션을 전파 받아서 사용
  - 그래서 스프링 데이터 JPA를 사용할 때 트랜잭션이 없어도 데이터 등록, 변경이 가능했음(사실은 트랜잭션이 리포지토리 계층에 걸려있는 것임)
- @Transactional(readOnly = true)
  - 데이터를 단순히 조회만 하고 변경하지 않는 트랜잭션에서 readOnly = true 옵션을 사용하면 플러시를 생략해서 약간의 성능 향상을 얻을 수 있음
- @Transactional 이 있으면 jdbc connection에 setAutoCommit(false) 가 적용된다.

**매우 중요!!!**
*save() 메서드*

- 새로운 엔티티면 저장( persist )
- 새로운 엔티티가 아니면 병합( merge )
  - select 해서 없으면 insert 있으면 update
  - 단점 select를 한다.
  - 변경 감지를 사용하자.
  - 영속 상태를 벗어났다가 다시 영속 상태로 만들어야 할때만 사용하자.

새로운 엔티티를 판단하는 기본 전략

- 식별자가 객체일 때 null 로 판단
- 식별자가 자바 기본 타입일 때 0 으로 판단
- Persistable 인터페이스를 구현해서 판단 로직 변경 가능
- 참고: JPA 식별자 생성 전략이 @GenerateValue 면 save() 호출 시점에 식별자가 없으므로 새로운
  엔티티로 인식해서 정상 동작한다. 그런데 JPA 식별자 생성 전략이 @Id 만 사용해서 직접 할당이면 이미
  식별자 값이 있는 상태로 save() 를 호출한다. 따라서 이 경우 merge() 가 호출된다. merge() 는 우선
  DB를 호출해서 값을 확인하고, DB에 값이 없으면 새로운 엔티티로 인지하므로 매우 비효율 적이다. 따라서
  Persistable 를 사용해서 새로운 엔티티 확인 여부를 직접 구현하게는 효과적이다.
- 참고로 등록시간( @CreatedDate )을 조합해서 사용하면 이 필드로 새로운 엔티티 여부를 편리하게 확인할
  수 있다. (@CreatedDate에 값이 없으면 새로운 엔티티로 판단)

  ```java
  @Entity
  @EntityListeners(AuditingEntityListener.class)
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  public class Item implements Persistable<String> {
      @Id
      private String id;
      
      @CreatedDate
      private LocalDateTime createdDate;
      
      public Item(String id) {
          this.id = id;
      }
      
      @Override
      public String getId() {
           return id;
      }
      
      @Override
      public boolean isNew() {
          return createdDate == null;
      }
  }
  ```


### Specifications(명세)

책 도메인 주도 설계(Domain Driven Design)는 SPECIFICATION(명세)라는 개념을 소개 스프링 데이터 JPA는 JPA Criteria를 활용해서 이 개념을 사용할 수 있도록 지원

**술어(predicate)**

- 참 또는 거짓으로 평가
- AND OR 같은 연산자로 조합해서 다양한 검색조건을 쉽게 생성(컴포지트 패턴)
- 예) 검색 조건 하나하나
- 스프링 데이터 JPA는 org.springframework.data.jpa.domain.Specification 클래스로 정의

**명세 기능 사용 방법**

- JpaSpecificationExecutor 인터페이스 상속

명세를 정의하려면 Specification 인터페이스를 구현

명세를 정의할 때는 toPredicate(...) 메서드만 구현하면 되는데 JPA Criteria의 Root, CriteriaQuery, CriteriaBuilder 클래스를 파라미터 제공

JPA Criteria 사용해서 동적 쿼리 작성

불편. **실무에서는 JPA Criteria를 거의 안쓴다! 대신에 QueryDSL을 사용하자.**

### Query By Example

[https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example)

ExampleMatcher, Example 사용해서 동적 쿼리 작성

내부 조인 가능, 외부 조인 불가능

중첩 제약 안됨(a = ? or (b = ? and c = ?))

매칭 조건이 매우 단순함. 문자는 starts/contains/ends/regex, 다른 속성은 = 만 지원

불편. **실무에서 사용하기에는 매칭 조건이 너무 단순하고, LEFT 조인이 안됨**

**실무에서는 QueryDSL을 사용하자**

### **Projections**

엔티티 대신에 DTO를 편리하게 조회할 때 사용

조회할 엔티티의 필드를 getter 형식으로 지정하면 해당 필드만 선택해서 조회(Projection)

메서드 이름은 자유, 반환 타입으로 인지

- **인터페이스 기반 Closed Projections**
  프로퍼티 형식(getter)의 인터페이스를 제공하면, 구현체는 스프링 데이터 JPA가 제공
- **인터페이스 기반 Open Proejctions**
  다음과 같이 스프링의 SpEL 문법도 지원

  ```java
  public interface UsernameOnly {
      @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
      String getUsername();
  }
  ```

  **단! 이렇게 SpEL문법을 사용하면, DB에서 엔티티 필드를 다 조회해온 다음에 계산한다! 따라서 JPQL
  SELECT 절 최적화가 안된다.**

- **클래스 기반 Projection**
  다음과 같이 인터페이스가 아닌 구체적인 DTO 형식도 가능
  생성자의 파라미터 이름으로 매칭
- **동적 Projections**
  다음과 같이 Generic type을 주면, 동적으로 프로젝션 데이터 번경 가능

  ```java
  <T> List<T> findProjectionsByUsername(String username, Class<T> type);
  
  // 사용
  List<UsernameOnly> result = memberRepository.findProjectionsByUsername("member1", UsernameOnly.class);
  ```

- **주의**
  - 프로젝션 대상이 root 엔티티면, JPQL SELECT 절 최적화 가능
  - 프로젝션 대상이 ROOT가 아니면
    - LEFT OUTER JOIN 처리
    - 모든 필드를 SELECT해서 엔티티로 조회한 다음에 계산
- **정리**
  - **프로젝션 대상이 root 엔티티면 유용하다.**
  - **프로젝션 대상이 root 엔티티를 넘어가면 JPQL SELECT 최적화가 안된다!**
  - **실무의 복잡한 쿼리를 해결하기에는 한계가 있다.**
  - **실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 QueryDSL을 사용하자**

### 네이티브 쿼리

가급적 네이티브 쿼리는 사용하지 않는게 좋음, 정말 어쩔 수 없을 때 사용

최근에 나온 궁극의 방법 → 스프링 데이터 Projections 활용

**스프링 데이터 JPA 기반 네이티브 쿼리**

- 페이징 지원
- 반환 타입
  - Object[]
  - Tuple
  - DTO(스프링 데이터 인터페이스 Projections 지원)
- 제약
  - Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음(믿지 말고 직접 처리)
  - JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
  - 동적 쿼리 불가
- **Projections 활용**
  예) 스프링 데이터 JPA 네이티브 쿼리 + 인터페이스 기반 Projections 활용

  ```java
  @Query(value = "SELECT m.member_id as id, m.username, t.name as teamName " +
              "FROM member m left join team t",
              countQuery = "SELECT count(*) from member",
              nativeQuery = true)
  Page<MemberProjection> findByNativeProjection(Pageable pageable);
  ```

- **동적 네이티브 쿼리**
  - 하이버네이트를 직접 활용
  - 스프링 JdbcTemplate, myBatis, jooq같은 외부 라이브러리 사용
