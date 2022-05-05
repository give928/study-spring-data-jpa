package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Member;
import com.give928.springdata.jpa.entity.Team;
import com.give928.springdata.jpa.repository.dto.MemberDto;
import com.give928.springdata.jpa.repository.projection.*;
import com.give928.springdata.jpa.repository.spec.MemberSpec;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberQueryRepository memberQueryRepository;

    @Test
    void testMember() {
        // given
        System.out.println("memberRepository = " + memberRepository.getClass());
        Member member = Member.builder().username("memberA").build();

        // when
        Member savedMember = memberRepository.save(member);

        // then
        Member findMember = memberRepository.findById(savedMember.getId())
                .orElseThrow(IllegalStateException::new);

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    @DisplayName("기본 CRUD")
    void basicCRUD() {
        // given
        Member member1 = Member.builder().username("memberA").build();
        Member member2 = Member.builder().username("memberA").build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId())
                .orElseThrow(IllegalStateException::new);
        Member findMember2 = memberRepository.findById(member2.getId())
                .orElseThrow(IllegalStateException::new);
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all).hasSize(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isZero();
    }

    @Test
    @DisplayName("쿼리 메서드 기능 - 메소드 이름으로 쿼리 생성")
    void findByUsernameAndAgeGreaterThan() {
        // given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member1").age(20).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("member1", 15);

        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUsername()).isEqualTo("member1");
        assertThat(members.get(0).getAge()).isEqualTo(20);
    }

    @Test
    @DisplayName("쿼리 메서드 기능 - limit top3")
    void findTop3By() {
        // given
        Member member1 = Member.builder().username("member1").build();
        Member member2 = Member.builder().username("member2").build();
        Member member3 = Member.builder().username("member3").build();
        Member member4 = Member.builder().username("member4").build();
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        // when
        List<Member> members = memberRepository.findTop3By();

        // then
        assertThat(members).hasSize(3);
    }

    @Test
    @DisplayName("쿼리 메서드 기능 - 메서드 이름으로 JPA NamedQuery 호출")
    void findByUsername() {
        // given
        Member member1 = Member.builder().username("member1").build();
        Member member2 = Member.builder().username("member2").build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<Member> members = memberRepository.findByUsername("member1");

        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0)).isEqualTo(member1);
    }

    @Test
    @DisplayName("쿼리 메서드 기능 - @Query 어노테이션")
    void findMembers() {
        // given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member1").age(20).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<Member> members = memberRepository.findMembers("member1", 10);

        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0)).isEqualTo(member1);
    }

    @Test
    @DisplayName("쿼리 메서드 기능 - @Query 어노테이션 - 값 하나만 조회")
    void findAllUsernames() {
        // given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member2").age(20).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<String> usernames = memberRepository.findAllUsernames();

        // then
        assertThat(usernames).hasSize(2)
                .contains(member1.getUsername(), member2.getUsername());
    }

    @Test
    @DisplayName("쿼리 메서드 기능 - DTO 로 직접 조회")
    void findMemberDtos() {
        // given
        Team team = Team.builder().name("teamA").build();
        teamRepository.save(team);

        Member member1 = Member.builder().username("member1").age(10).team(team).build();
        Member member2 = Member.builder().username("member2").age(20).team(team).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<MemberDto> members = memberRepository.findAllMemberDtos();

        // then
        assertThat(members).hasSize(2);
        assertThat(members.get(0).getUsername()).isEqualTo(member1.getUsername());
        assertThat(members.get(0).getTeamName()).isEqualTo(team.getName());
    }

    @Test
    @DisplayName("컬렉션 파라미터 바인딩")
    void findByUsernames() {
        // given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member2").age(20).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<Member> members = memberRepository.findByUsernames(Arrays.asList("member1", "member2"));

        // then
        assertThat(members).hasSize(2)
                .contains(member1, member2);
    }

    @Test
    @DisplayName("반환 타입")
    void returnType() {
        // given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member2").age(20).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 컬렉션
        List<Member> findMembers1 = memberRepository.findMembersByUsername("member1");
        assertThat(findMembers1).hasSize(1);

        List<Member> findMembers3 = memberRepository.findMembersByUsername("member3");
        assertThat(findMembers3).isNotNull()
                .isEmpty();

        // 단건
        Member findMember1 = memberRepository.findMemberByUsername("member1");
        assertThat(findMember1).isEqualTo(member1);

        Member findMember3 = memberRepository.findMemberByUsername("member3");
        assertThat(findMember3).isNull();

        // 단건 Optional
        Optional<Member> optional1 = memberRepository.findOptionalMemberByUsername("member1");
        assertThat(optional1).isPresent();

        Optional<Member> optional3 = memberRepository.findOptionalMemberByUsername("member3");
        assertThat(optional3).isNotPresent();
    }

    @Test
    @DisplayName("페이징과 정렬 Page")
    void page() {
        // given
        memberRepository.save(Member.builder().username("member1").age(10).build());
        memberRepository.save(Member.builder().username("member2").age(10).build());
        memberRepository.save(Member.builder().username("member3").age(10).build());
        memberRepository.save(Member.builder().username("member4").age(10).build());
        memberRepository.save(Member.builder().username("member5").age(10).build());

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> memberPage = memberRepository.findPageByAge(age, pageRequest);

        Page<MemberDto> memberDtoPage = memberPage.map(m -> MemberDto.builder()
                .id(m.getId())
                .username(m.getUsername())
                .build());

        // then
        List<MemberDto> members = memberDtoPage.getContent(); // 조회된 데이터
        assertThat(members).hasSize(3); // 조회된 데이터 수
        assertThat(memberDtoPage.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(memberDtoPage.getNumber()).isZero(); // 페이지 번호
        assertThat(memberDtoPage.getTotalPages()).isEqualTo(2); // 전체 페이지 번호
        assertThat(memberDtoPage.isFirst()).isTrue(); // 첫번째 항목인가?
        assertThat(memberDtoPage.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }

    @Test
    @DisplayName("페이징과 정렬 Slice")
    void slice() {
        // given
        memberRepository.save(Member.builder().username("member1").age(10).build());
        memberRepository.save(Member.builder().username("member2").age(10).build());
        memberRepository.save(Member.builder().username("member3").age(10).build());
        memberRepository.save(Member.builder().username("member4").age(10).build());
        memberRepository.save(Member.builder().username("member5").age(10).build());

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        // then
        List<Member> content = page.getContent(); // 조회된 데이터
        assertThat(content).hasSize(3); // 조회된 데이터 수
        assertThat(page.getNumber()).isZero(); // 페이지 번호
        assertThat(page.isFirst()).isTrue(); // 첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }

    @Test
    @DisplayName("벌크성 수정 쿼리 - @Modifying(clearAutomatically = true)")
    void bulkUpdate() {
        // given
        memberRepository.save(Member.builder().username("member1").age(10).build());
        memberRepository.save(Member.builder().username("member2").age(19).build());
        memberRepository.save(Member.builder().username("member3").age(20).build());
        memberRepository.save(Member.builder().username("member4").age(21).build());
        memberRepository.save(Member.builder().username("member5").age(40).build());

        // when
        int resultCount = memberRepository.bulkPlusAge(20);

        // then
        assertThat(resultCount).isEqualTo(3);

        // @Modifying(clearAutomatically = true) 가 있어야 영속성 컨텍스트를 초기화해서 엔티티를 다시 조회한다.
        Member findMember5 = memberRepository.findOptionalMemberByUsername("member5")
                .orElseThrow(IllegalStateException::new);
        assertThat(findMember5.getAge()).isEqualTo(41);
    }

    @Test
    @DisplayName("지연 로딩")
    void findMemberAfterLazyLoadingTeam() {
        // given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member1").age(10).team(teamB).build());
        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findMembers();

        // then
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass()); // class com.give928.springdata.jpa.entity.Team$HibernateProxy$gHFJeXNT
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName()); // N(team) + 1(member)
        }
        assertThat(members).hasSize(2);
    }

    @Test
    @DisplayName("페치 조인")
    void findAllFetchJoinTeam() {
        // given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member1").age(10).team(teamB).build());
        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findAllFetchJoinTeam();

        // then
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass()); // class com.give928.springdata.jpa.entity.Team
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName()); // N(team) + 1(member)
        }
        assertThat(members).hasSize(2);
    }

    @Test
    @DisplayName("메서드에 @EntityGraph(attributePaths = {\"team\"}) 적용해서 패치 조인")
    void findMembersEntityGraph() {
        // given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member1").age(10).team(teamB).build());
        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findMembersEntityGraph();

        // then
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass()); // class com.give928.springdata.jpa.entity.Team$HibernateProxy$gHFJeXNT
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName()); // N(team) + 1(member)
        }
        assertThat(members).hasSize(2);
    }

    @Test
    @DisplayName("Member 엔티티에 @NamedEntityGraph 적용하고 메서드에서 @EntityGraph 적용해서 패치 조인")
    void findMembersNamedEntityGraph() {
        // given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member1").age(10).team(teamB).build());
        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findMembersNamedEntityGraph();

        // then
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass()); // class com.give928.springdata.jpa.entity.Team$HibernateProxy$gHFJeXNT
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName()); // N(team) + 1(member)
        }
        assertThat(members).hasSize(2);
    }

    @Test
    @DisplayName("JPA 쿼리 힌트")
    void queryHint() {
        // given
        memberRepository.save(Member.builder().username("member1").age(10).build());
        em.flush();
        em.clear();

        // when
        Member member = memberRepository.findReadOnlyMemberByUsername("member1")
                .orElseThrow(IllegalStateException::new);
        member.update("member1", 20);
        em.flush(); //변경 감지 Update 가 실행되지 않는다.

        // then
        assertThat(member.getAge()).isEqualTo(20);

        em.clear();
        Member findMember = memberRepository.findReadOnlyMemberByUsername("member1")
                .orElseThrow(IllegalStateException::new);
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("Lock")
    void lock() {
        // given
        memberRepository.save(Member.builder().username("member1").age(10).build());
        em.flush();
        em.clear();

        // when
        Member member = memberRepository.findLockMemberByUsername("member1")
                .orElseThrow(IllegalStateException::new);

        // then
        // SQL 에 for update (데이터베이스 방언에 따라 다름) 가 붙는다.
        assertThat(member).isNotNull();
    }

    @Test
    @DisplayName("사용자 정의 리포지토리 사용")
    void callCustomRepository() {
        // given

        // when
        // 리포지토리 인터페이스에서 사용자 정의 인터페이스 상속받아서 사용
        // 구현 클래스 규칙: "리포지토리 인터페이스 이름 + Impl" or "사용자 정의 인터페이스 명 + Impl"
        // MemberRepositoryImpl or MemberRepositoryCustomImpl
        List<Member> members = memberRepository.findMemberCustom();

        // then
        assertThat(members).isEmpty();
    }

    @Test
    @DisplayName("임의의 리포지토리 사용")
    void test() {
        // given

        // when
        // 사용자 정의 리포지토리에는 핵심 비지니스만 넣어두고
        // 핵심 비지니스가 아닌 것은 임의의 리포지토리로 분리해서 사용하자.
        List<Member> members = memberQueryRepository.findAllMembers();

        // then
        assertThat(members).isEmpty();
    }

    @Test
    void auditing() throws InterruptedException {
        // given
        Member member = Member.builder().username("member1").age(10).build();
        memberRepository.save(member); //@PrePersist

        Thread.sleep(100);
        member.update("member2", 20);

        em.flush(); // @PreUpdate
        em.clear();

        // when
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(IllegalStateException::new);

        // then
        assertThat(findMember.getCreatedDate()).isNotNull();
        assertThat(findMember.getCreatedBy()).isNotNull();
        assertThat(findMember.getLastModifiedDate()).isNotNull();
        assertThat(findMember.getLastModifiedBy()).isNotNull();
        assertThat(findMember.getCreatedDate()).isNotEqualTo(findMember.getLastModifiedDate());
        System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
        System.out.println("findMember.getLastModifiedDate() = " + findMember.getLastModifiedDate());
        System.out.println("findMember.getCreatedBy() = " + findMember.getCreatedBy());
        System.out.println("findMember.getLastModifiedBy() = " + findMember.getLastModifiedBy());
    }

    @Test
    @DisplayName("명세 기능 사용해서 동적 쿼리로 조회")
    void specification() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        Specification<Member> spec = MemberSpec.username("member1").and(MemberSpec.teamName("teamA"));
        List<Member> result = memberRepository.findAll(spec); // JpaSpecificationExecutor.findAll

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Query By Example 기능 사용해서 동적 쿼리로 조회")
    void queryByExample() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        // Probe 생성
        Team team = Team.builder().name("teamA").build(); // 내부조인으로 teamA 가능
        Member member = Member.builder().username("member1").team(team).build();

        // ExampleMatcher 생성, age 프로퍼티는 무시
        ExampleMatcher matcher = ExampleMatcher.matching()
                  .withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("인터페이스 기반 Closed Projection 으로 특정 필드만 선택해서 조회")
    void closedProjection() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        List<UsernameClosedProjection> result = memberRepository.findUsernameClosedProjectionsByUsername("member1");
        System.out.println("result.get(0).getUsername() = " + result.get(0).getUsername()); // result.get(0).getUsername() = member1

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("인터페이스 기반 Open Projection 으로 특정 필드만 선택해서 조회")
    void openProjection() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        List<UsernameOpenProjection> result = memberRepository.findUsernameOpenProjectionsByUsername("member1");
        System.out.println("result.get(0).getUsername() = " + result.get(0).getUsername()); // result.get(0).getUsername() = member1 - 10 - teamA

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("클래스 기반 Projection 으로 특정 필드만 선택해서 조회")
    void classProjection() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        List<UsernameProjectionDto> result = memberRepository.findUsernameProjectionDtosByUsername("member1");
        System.out.println("result.get(0).getUsername() = " + result.get(0).getUsername()); // result.get(0).getUsername() = member1

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("동적 Projection 으로 특정 필드만 선택해서 조회")
    void dynamicProjection() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        List<UsernameProjectionDto> result = memberRepository.findDynamicProjectionsByUsername("member1", UsernameProjectionDto.class);
        System.out.println("result.get(0).getUsername() = " + result.get(0).getUsername()); // result.get(0).getUsername() = member1

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("중첩 구조 Projection 으로 특정 필드만 선택해서 조회")
    void nestedClosedProjection() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        List<UsernameAndTeamNestedClosedProjection> result = memberRepository.findDynamicProjectionsByUsername("member1", UsernameAndTeamNestedClosedProjection.class);
        System.out.println("result.get(0).getUsername() = " + result.get(0).getUsername()); // result.get(0).getUsername() = member1
        System.out.println("result.get(0).getTeam().getName() = " + result.get(0).getTeam().getName()); // result.get(0).getTeam().getName() = teamA

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("네이티브 쿼리로 조회")
    void nativeQuery() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        em.flush();
        em.clear();

        // when
        List<Member> result = memberRepository.findFromNativeQueryByUsername("member1");
        System.out.println("result.get(0).getUsername() = " + result.get(0).getUsername()); // result.get(0).getUsername() = member1

        // then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("네이티브 쿼리 + 프로젝션 으로 조회")
    void nativeQueryProjection() {
        // given
        Team teamA = Team.builder().name("teamA").build();
        teamRepository.save(teamA);
        memberRepository.save(Member.builder().username("member1").age(10).team(teamA).build());
        memberRepository.save(Member.builder().username("member2").age(20).team(teamA).build());
        memberRepository.save(Member.builder().username("member3").age(30).team(teamA).build());
        memberRepository.save(Member.builder().username("member4").age(40).team(teamA).build());
        memberRepository.save(Member.builder().username("member5").age(50).team(teamA).build());
        em.flush();
        em.clear();

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<MemberProjection> page = memberRepository.findPageFromNativeQueryProjection(pageRequest);
        List<MemberProjection> members = page.getContent();

        // then
        assertThat(members).hasSize(3); // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(page.getNumber()).isZero(); // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); // 첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }
}
