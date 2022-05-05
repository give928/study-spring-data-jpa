package com.give928.springdata.jpa.controller;

import com.give928.springdata.jpa.entity.Member;
import com.give928.springdata.jpa.repository.dto.MemberDto;
import com.give928.springdata.jpa.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    /**
     * http://127.0.0.1:8080/members?page=0&size=3&sort=id,desc&sort=username,desc
     */
    @GetMapping("/members")
    public Page<MemberDto> list(
            @PageableDefault(size = 5, sort = "username", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page.map(MemberDto::new);
    }

//    @PostConstruct
    public void init() {
        IntStream.rangeClosed(1, 100).forEach(
                value -> memberRepository.save(Member.builder().username("member" + value).age(value).build()));
    }
}
