package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, String> {
}
