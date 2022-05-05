package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Member;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final EntityManager em;

    /**
     * JPA 직접 사용(EntityManager)
     * 스프링 JDBC Template 사용
     * MyBatis 사용
     * 데이터베이스 커넥션 직접 사용
     * Querydsl 사용
     * 등등... 사용하고 싶을 때
     */
    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
}
