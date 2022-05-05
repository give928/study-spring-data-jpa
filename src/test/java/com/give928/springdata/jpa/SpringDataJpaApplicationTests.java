package com.give928.springdata.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringDataJpaApplicationTests {

	@Test
	void contextLoads() {
		boolean loaded = true;

		assertThat(loaded).isEqualTo(Boolean.TRUE);
	}

}
