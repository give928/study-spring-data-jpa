package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
