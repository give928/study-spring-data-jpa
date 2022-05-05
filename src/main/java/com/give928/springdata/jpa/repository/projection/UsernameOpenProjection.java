package com.give928.springdata.jpa.repository.projection;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOpenProjection {
    @Value("#{target.username + ' - ' + target.age + ' - ' + target.team.name}")
    String getUsername();
}
