package com.give928.springdata.jpa.repository.dto;

import com.give928.springdata.jpa.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class MemberDto {
    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        if (member.getTeam() != null) {
            this.teamName = member.getTeam().getName();
        }
    }
}
