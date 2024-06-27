## Java Persistence API  

---  

목표 - 객체와 테이블 설계 매핑
- 객체와 테이블을 제대로 설계하고 매핑하는 방법  
- 기본 키와 외래 키 매핑  
- 1 : N, N : 1, N : M 매핑  

목표 - JPA 내부 동작 방식 이해  
- JPA가 어떤 SQL을 만들어 내는지 이해  
- JPA가 언제 SQL을 실행하는지 이해  

SQL에 의존적인 개발을 피하기 어렵다.  
어쨌든 SQL은 써야 하고 알아야 한다.  

객체지향과 관계형 DB의 패러다임 불일치  
1. 상속  
객체 상속 관계  
테이블 슈퍼타입, 서브타입 관계  
비슷해 보이긴 하지만 다르다.  

2. 연관관계  
객체는 참조를 사용  
테이블을 외래 키를 사용한 JOIN  
연관관계를 다루는 방식이 다르다.  
객체를 테이블에 맞추어 모델링하게 된다면?  
→ 객체답게 참조가 불가능함  

3. 엔티티 신뢰 문제  
객체는 자유롭게 객체 그래프를 탐색할 수 있어야 하는데, 참조하는 객체를 모두 DB에서 가져왔는지 신뢰할 수 없음  
→ 모든 객체를 미리 로딩할 수는 없다.

## JPA

--- 

Java Persistence API
자바 진영의 ORM 기술 표준  
- Object Relational Mapping(객체 관계 매핑)  
- 객체는 객체대로 설계하고, 관계형 DB는 관계형 DB대로 설계  
- ORM 프레임워크가 중간에서 이를 매핑  

JPA는 애플리케이션과 JDBC 사이에서 동작한다.  
![https://medium.com/@dafikabukcu/what-is-hibernate-and-jpa-ef77ba1dac15](https://miro.medium.com/v2/resize:fit:720/format:webp/1*bUVgpAByzqRlxT_IwuKREg.png)  
출처 : https://medium.com/@dafikabukcu/what-is-hibernate-and-jpa-ef77ba1dac15  

JPA는 표준 명세일 뿐이다.
- 인터페이스의 모음  
- 구현체에는 Hibernate, EclipseLink, DataNucleus가 있지만, Hibernate를 많이 씀  

JPA를 왜 사용해야 하는가?
- SQL 중심적인 개발에서 객체 중심으로 개발  
- 생산성  
- 유지보수  
    - JPA를 사용하지 않는다면, 필드 변경시 모든 SQL을 수정해야 함  
    - JPA를 사용하면 필드만 추가하면 됨  
- 패러다임의 불일치 해결  
- 성능  
    - 1차 캐시와 동일성(identity) 보장  
        - 같은 트랜잭션 안에서는 같은 엔티티를 반환 - 상황에 따라 다르지만, 약간의 조회 성능 향상  
        - DB Isolation Level이 Read Commit이어도, 애플리케이션에서 Repeatable Read 보장  
    - 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)  
        - 트랜잭션을 커밋할 때까지 INSERT SQL을 모아뒀다가 JDBC BATCH SQL 기능을 사용해서 한번에 SQL 전송  
    - 지연 로딩(Lazy Loading)과 즉시 로딩  
        - 지연 로딩 : 객체가 실제 사용될 때 로딩  
        - 즉시 로딩 : JOIN SQL로 한번에 연관된 객체까지 미리 조회  
- 데이터 접근 추상화와 벤더 독립성  
- 표준  

객체지향과 관계형 DB를 모두 잘 알아서 JPA를 잘 쓸 수 있다.  

## JPA 구동 방식

---  

![HowJPAWorks](/images/HowJPAWorks.png)  

EntityManagerFactory는 애플리케이션 로딩 시점에 딱 하나만 만들어 애플리케이션 전체에서 공유  

JPA에서는 Transaction 단위가 중요
데이터를 변경하는 모든 작업은 반드시 Transaction 단위 안에서 작업 해야함  

EntityManager
- 정말 간단하게 말하면 DB connection을 하나 받는 것이라 생각하면 됨  
- 생성해둔 DBCP의 DB connection을 사용, 사용 후에는 반드시 close 해줘야 함   
- 쓰레드 간에 공유를 하면 안됨  

엔티티 클래스에 어노테이션을 이용해 별도로 테이블을 지정하지 않으면, 클래스명과 테이블을 비교하여 매핑한다.
엔티티 클래스와 테이블 명이 다를 경우, @Table(name = “”)으로 지정하면 된다.  

### JPQL  
SQL을 추상화한 객체 지향 쿼리 언어
- JPA를 사용하면 엔티티 객체를 중심으로 개발  
- 문제는 검색 쿼리  
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색  
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능  
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요  

SQL과 문법이 유사하다.  
SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원  

JPQL은 엔티티 객체를 대상으로 쿼리  
SQL은 DB 테이블을 대상으로 쿼리
SQL을 추상화했기 때문에, 특정 DB SQL에 의존하지 않는다.  

```java
List<Member> result = entityManager.createQuery("select m from Member as m", Member.class).getResultList();
```

## 영속성 관리    

---  

JPA에서 가장 중요한 2가지
- 객체와 관계형 데이터베이스 매핑하기  
- 영속성 컨텍스트  

### 영속성 컨텍스트  
JPA를 이해하는데 가장 중요한 용어  
"엔티티를 영구 저장하는 환경"이라는 뜻  
영속성 컨텍스트는 논리적인 개념으로, 엔티티 매니저를 통해 접근  

엔티티의 생명주기  
비영속(new/transient)
- 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
- 그냥 엔티티 객체를 new로 생성한 것과 같은 상태  

영속(managed)  
- 영속성 컨텍스트에 관리되는 상태   
- 영속 상태가 된다고 해서, 바로 쿼리가 날아가 DB에 저장되는 것은 아님  
- 실제 쿼리는 트랜잭션을 commit하는 시점에 날아감  

준영속(detached)  
- 영속성 컨텍스트에 저장되었다가 분리된 상태  
- 영속성 컨텍스트가 제공하는 기능을 사용 못함  
- 준영속 상태로 만드는 방법  
  - entityManager.detach(entity) : 특정 엔티티만 전환  
  - entityManager.clear() : 영속성 컨텍스트를 완전히 초기화  
  - entityManager.close() : 영속성 컨텍스트를 종료  

삭제(removed)  
- 삭제된 상태  

<br/>

### 영속성 컨텍스트의 이점  
1차 캐시  
- 한 트랜잭션 내에서만 효과가 있음  
- 새로 생성한 엔티티를 entityManager.persist()를 통해 영속 상태로 만들면, 1차 캐시로 이동하고 커밋된 후에 DB로 넘어감  
- 찾고자 하는 엔티티가 1차 캐시에 있다면,  
  - 1차 캐시에서 찾은 엔티티 반환  
- 찾고자 하는 엔티티가 1차 캐시에 없다면,
  - DB 조회 후, 이를 1차 캐시에 저장하고 엔티티 반환  

영속 엔티티의 동일성(identity) 보장  
- 1차 캐시로 REPEATABLE READ 레벨의 트랜잭션 격리 수준을 DB가 아닌 애플리케이션 레벨에서 제공  

트랜잭션을 지원하는 쓰기 지연(transactional write-behind)  
- 트랜잭션이 시작하고, entityManager.persist()를 거치더라도 INSERT 쿼리를 날리지 않고, 트랜잭션이 커밋되는 시점에 INSERT 쿼리가 날아감  
- 영속성 컨텍스트에는 1차 캐시외에도 '쓰기 지연 SQL 저장소'가 있음
- entityManager.persist()가 호출되는 순간, 1차 캐시에 엔티티를 저장하고 쓰기 지연 SQL 저장소에 INSERT SQL이 생성됨  
- entityTransaction.commit()이 호출되면 쓰지 지연 SQL 저장소에 있던 쿼리들이 flush되며 DB에 보내지고, 실제 DB 트랜잭션이 커밋됨  

변경 감지(Dirty Checking)  
- 커밋 시점에 여러일이 일어남
  1. flush() 호출  
  2. 엔티티와 1차 캐시의 스냅샷(영속성 컨텍스트에 진입한 시점의 상태)을 비교  
  3. 변경 사항이 있다면 UPDATE SQL을 생성해 쓰기 지연 SQL 저장소에 저장
  4. 쓰기 지연 SQL 저장소의 쿼리들을 flush하고
  5. 실제 DB 트랜잭션 커밋  
- 플러시(flush)  
  - 영속성 컨텍스트의 변경내용을 DB에 동기화  
  - 영속성 컨텍스트를 비우지 않음  
  - 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화하면 됨  
  - 플러시 발생시 일어나는 일들
    - 변경 감지
    - 수정된 엔티티 쓰지 지연 SQL 저장소에 등록  
    - 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송  
  - 영속성 컨텍스트를 플러시하는 방법  
    - entityManager.flush() - 직접 호출  
    - 트랜잭션 커밋 - 플러시 자동 호출  
    - JPQL 쿼리 실행 - 플러시 자동 호출
  - 플러시 모드 옵션  
    - FlushModeType.AUTO : 커밋이나 쿼리를 실행할 때 플러시(기본값)  
    - FlushModeType.COMMIT : 커밋할 때만 플러시  

지연 로딩(Lazy Loading)    


## 엔티티 매핑  

---  
### DB 스키마 자동 생성  
DDL을 애플리케이션 실행 시점에 자동 생성  
DB Dialect를 사용해 DB에 맞는 적절한 DDL 생성  

hibernate.hbm2ddl.auto 속성

|      옵션      | 설명                               |  
|:------------:|:---------------------------------|  
|    create    | 기존 테이블 삭제 후 다시 생성(DROP > CREATE) |  
| create-drop  | create와 같으나 종료시점에 테이블 DROP       |  
|    update    | 변경분만 반영(운영DB에는 사용하면 안됨)          |  
|   validate   | 엔티티와 테이블이 정상 매핑되었는지 확인           | 
|     none     | 사용하지 않음                          |  

주의점  
- 운영 장비에는 절대 create, create-drop, update 사용 X  
  - 운영 DB를 날리거나 말아먹는 방법이라 그런듯  
- 개발 초기 단계는 create 또는 update  
- 테스트 서버는 update 또는 validate  
- 스테이징과 운영 서버는 validate 또는 none  

듣고 나서 생각해보니 혼자 개발할 때, 개발 완전 초기 단계가 아닌 이상 자동 생성 옵션 자체를 안 쓰는게 나을 것 같다.   


### DDL 생성 기능  
@Column, @Table 어노테이션을 이용해서 제약조건을 추가하는 경우  
DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고, JPA의 실행 로직에는 영향을 주지 않는다.  


### 객체와 테이블 매핑  
@Entity  
- @Entity가 붙은 클래스를 JPA가 관리
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수  
- 주의
  - 기본 생성자 필수(파라미터 없는 public 또는 protected 생성자)
  - final 클래스, enum, interface, inner 클래스 사용 X  
  - 저장할 필드에 final 사용 X  
- 속성 : name  
  - JPA에서 사용할 엔티티 이름을 지정  
  - 기본값: 클래스 이름을 그대로 사용  
  - 같은 클래스 이름이 없으면 가급적 기본값 사용  

@Table  
- 엔티티와 매핑할 테이블 지정  

### 필드와 컬럼 매핑  
@Column  
- name 속성을 이용해 실제 DB에 사용되는 칼럼명을 지정할 수 있다.  
- insertable, updatable을 통해 등록, 변경 가능 여부를 지정할 수 있다.  
- nullable(DDL)를 false로 하면 DDL 생성 시 not null 제약조건이 붙는다.  
- unique(DDL), @Table의 uniqueConstraints와 같지만 한 칼럼에 간단히 제약조건 걸 때 사용한다.  
- columnDefinition(DDL), length(DDL), precision(DDL), scale(DDL)  

@Enumerated  
- Enum 타입 매핑  
- 속성값으로 ORDINAL(default)를 사용할 경우 enum 순서를 DB에 저장하니 주의해야 한다.  
- 속성값으로 STRING를 사용할 경우 enum 이름을 DB에 저장한다.  

@Temporal  
- 날짜 타입 매핑  
- LocalDate, LocalDateTime을 사용할 때는 생략 가능  

@Transient  
- 특정 필드를 컬럼에 매핑하지 않음  
- DB에 저장 X, 조회 X  
- 주로 메모리상에서만 임시로 값을 보관하고 싶을 때 사용  

### 기본 키 매핑  
직접 할당할 경우, @Id만 사용  

@GeneratedValue(자동 생성)  
- IDENTITY  
  - DB에 위임  
  - MySQL(AUTO_INCREMENT)  
  - JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행  
  - AUTO_INCREMENT는 DB에 INSERT SQL을 실행한 후에 ID 값을 알 수 있음  
  - 영속성 컨텍스트에서 관리하기 위해선 PK(ID)가 필요한데, IDENTITY 전략의 경우 INSERT 쿼리가 실행되어야 알 수 있음  
  - 그래서 예외적으로 entityManager.persist()가 호출되는 순간 INSERT 쿼리를 날리고, ID를 받아옴  
- SEQUENCE : DB 시퀀스 오브젝트 사용, ORACLE
  - @SequenceGenerator  
    - name : 식별자 생성기 이름
    - sequenceName : DB에 등록되어 있는 시퀀스 이름  
    - initialValue : DDL 생성 시에만 사용  
    - allocationSize : 시퀀스 한번 호출에 증가하는 수(default : 50)  
      - 기본값 50 사용할 경우, 시퀀스 한번 호출시 DB에 50만큼 증가시켜두고, 애플리케이션 메모리상에서 증분만큼 사용  
- TABLE : 키 생성용 테이블 사용, 모든 DB에서 사용  
- AUTO : Dialect에 따라 자동 지정, 기본값  

권장하는 식별자 전략  
- 기본키 제약 조건 : NOT NULL, 유일, 변하면 안된다.  
- 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자.  
- 권장 : Long + 대체키 + 키 생성전략 사용  


### 연관관계 매핑  
@ManyToOne, @JoinColumn  

