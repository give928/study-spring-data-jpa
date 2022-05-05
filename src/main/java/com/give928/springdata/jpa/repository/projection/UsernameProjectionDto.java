package com.give928.springdata.jpa.repository.projection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UsernameProjectionDto {
    private final String username;
}
