package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void testMember() {
        // given
        Member member = Member.builder().username("memberA").build();

        // when
        Member savedMember = memberJpaRepository.save(member);

        // then
        Member findMember = memberJpaRepository.findById(savedMember.getId())
                .orElseThrow(IllegalStateException::new);

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void basicCRUD() {
        // given
        Member member1 = Member.builder().username("member1").build();
        Member member2 = Member.builder().username("member2").build();
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId())
                .orElseThrow(IllegalStateException::new);
        Member findMember2 = memberJpaRepository.findById(member2.getId())
                .orElseThrow(IllegalStateException::new);
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all).hasSize(2);

        //카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);
        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isZero();
    }

    @Test
    void findByUsernameAndAgeGreaterThan() {
        // given
        Member member1 = Member.builder().username("member1").age(10).build();
        Member member2 = Member.builder().username("member1").age(20).build();
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // when
        List<Member> members = memberJpaRepository.findByUsernameAndAgeGreaterThan("member1", 15);

        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUsername()).isEqualTo("member1");
        assertThat(members.get(0).getAge()).isEqualTo(20);
    }

    @Test
    void namedQueryFindByUsername() {
        // given
        Member member1 = Member.builder().username("member1").build();
        Member member2 = Member.builder().username("member2").build();
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // when
        List<Member> members = memberJpaRepository.findByUsername("member1");

        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0)).isEqualTo(member1);
    }

    @Test
    void page() {
        // given
        memberJpaRepository.save(Member.builder().username("member1").age(10).build());
        memberJpaRepository.save(Member.builder().username("member2").age(10).build());
        memberJpaRepository.save(Member.builder().username("member3").age(10).build());
        memberJpaRepository.save(Member.builder().username("member4").age(10).build());
        memberJpaRepository.save(Member.builder().username("member5").age(10).build());

        int age = 10;
        int offset = 0;
        int limit = 3;

        // when
        List<Member> members = memberJpaRepository.findPageByAge(age, offset, limit);
        long memberCount = memberJpaRepository.countByAge(age);

        // 페이지 계산 공식 적용...
        // totalPage = totalCount / size ...
        // 마지막 페이지 ...
        // 최초 페이지 ..

        // then
        assertThat(members).hasSize(3);
        assertThat(memberCount).isEqualTo(5);
    }

    @Test
    void bulkUpdate() {
        // given
        memberJpaRepository.save(Member.builder().username("member1").age(10).build());
        memberJpaRepository.save(Member.builder().username("member2").age(19).build());
        memberJpaRepository.save(Member.builder().username("member3").age(20).build());
        memberJpaRepository.save(Member.builder().username("member4").age(21).build());
        memberJpaRepository.save(Member.builder().username("member5").age(40).build());

        // when
        int resultCount = memberJpaRepository.bulkPlusAge(20);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    void auditing() throws InterruptedException {
        // given
        Member member = Member.builder().username("member1").age(10).build();
        memberJpaRepository.save(member); //@PrePersist

        Thread.sleep(100);
        member.update("member2", 20);

        em.flush(); //@PreUpdate
        em.clear();

        // when
        Member findMember = memberJpaRepository.findById(member.getId())
                .orElseThrow(IllegalStateException::new);

        // then
        assertThat(findMember.getCreatedDate()).isNotNull();
//        System.out.println("findMember.getCreatedDate() = " + findMember.getCreatedDate());
//        System.out.println("findMember.getUpdatedDate() = " + findMember.getUpdatedDate());
    }
}
