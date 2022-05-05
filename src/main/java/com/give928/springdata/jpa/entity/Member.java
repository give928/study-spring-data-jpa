package com.give928.springdata.jpa.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString(of = {"id", "username", "age"})
@NamedQuery(name = "Member.findByUsername", query = "select m from Member as m where m.username = :username")
@NamedEntityGraph(name = "Member.team", attributeNodes = {@NamedAttributeNode("team")})
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Member(Long id, String username, int age, Team team) {
        this.id = id;
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    public void update(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
