package com.give928.springdata.jpa.repository.spec;

import com.give928.springdata.jpa.entity.Member;
import com.give928.springdata.jpa.entity.Team;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class MemberSpec {
    public static Specification<Member> teamName(final String teamName) {
        return (root, query, builder) -> {
            if (StringUtils.hasText(teamName)) {
                Join<Member, Team> t = root.join("team", JoinType.INNER); // 회원과 조인
                return builder.equal(t.get("name"), teamName);
            }
            return null;
        };
    }

    public static Specification<Member> username(final String username) {
        return (root, query, builder) -> builder.equal(root.get("username"), username);
    }
}
