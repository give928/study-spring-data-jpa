package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Member;
import com.give928.springdata.jpa.repository.dto.MemberDto;
import com.give928.springdata.jpa.repository.projection.MemberProjection;
import com.give928.springdata.jpa.repository.projection.UsernameClosedProjection;
import com.give928.springdata.jpa.repository.projection.UsernameOpenProjection;
import com.give928.springdata.jpa.repository.projection.UsernameProjectionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor<Member> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3By();

    /**
     * 생략 가능 도메인.메서드 로 Named 쿼리 찾아서 실행
     * NamedQuery 가 없으면 메서드 이름으로 쿼리 생성 전략을 사용
     */
//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findMembers(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findAllUsernames();

    @Query("select new com.give928.springdata.jpa.repository.dto.MemberDto(m.id, m.username, t.name) from Member as m inner join m.team as t")
    List<MemberDto> findAllMemberDtos();

    @Query("select m from Member as m where m.username in :usernames")
    List<Member> findByUsernames(@Param("usernames") List<String> usernames);

    List<Member> findMembersByUsername(String username); // 컬렉션
    Member findMemberByUsername(String username); // 단건
    Optional<Member> findOptionalMemberByUsername(String username); // 단건 Optional

    // 페이징과 정렬
//    @Query(value = "select m from Member as m left outer join m.team as t",
//            countQuery = "select count(m) from Member as m") // 불필요한 조인을 제거하기 위해 count 쿼리를 분리할 수 있다.
    Page<Member> findPageByAge(int age, Pageable pageable);
    Slice<Member> findSliceByAge(int age, Pageable pageable);

    // 벌크성 수정 쿼리
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkPlusAge(@Param("age") int age);

    @Query("select m from Member as m left outer join fetch m.team as t")
    List<Member> findAllFetchJoinTeam();

    @Query("select m from Member m")
    List<Member> findMembers();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMembersEntityGraph();

    @EntityGraph(value = "Member.team")
    @Query("select m from Member m")
    List<Member> findMembersNamedEntityGraph();

    @QueryHints(value = {@QueryHint(name = "org.hibernate.readOnly", value = "true")}) // 영속성 컨텍스트에서 스냅샷을 만들지 않는다.
    Optional<Member> findReadOnlyMemberByUsername(String member1);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Member> findLockMemberByUsername(String name);

    // Projections
    List<UsernameClosedProjection> findUsernameClosedProjectionsByUsername(String username);
    List<UsernameOpenProjection> findUsernameOpenProjectionsByUsername(String username);
    List<UsernameProjectionDto> findUsernameProjectionDtosByUsername(String username);
    <T> List<T> findDynamicProjectionsByUsername(String username, Class<T> type);

    // native query
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    List<Member> findFromNativeQueryByUsername(String username);

    @Query(value = "SELECT m.member_id as id, m.username, t.name as teamName FROM member as m left outer join team as t",
            countQuery = "SELECT count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findPageFromNativeQueryProjection(Pageable pageable);
}
