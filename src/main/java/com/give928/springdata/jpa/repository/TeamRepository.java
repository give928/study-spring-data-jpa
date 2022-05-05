package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
