package com.give928.springdata.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
//@EnableJpaRepositories(basePackages = {"com.give928.springdata.jpa.repository"}) // 스프링 부트에서 생략 가능 - @SpringBootApplication 하위 패키지는 자동으로 검색
@EnableJpaAuditing
public class SpringDataJpaApplication {
	private static final String CREATED_BY = UUID.randomUUID().toString();

	public static void main(String[] args) {
		SpringApplication.run(SpringDataJpaApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of(CREATED_BY);
	}
}
