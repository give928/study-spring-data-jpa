package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    private final EntityManager em;

    public List<Member> findAllMembers() {
        return em.createQuery("select m from Member as m", Member.class)
                .getResultList();
    }
}
