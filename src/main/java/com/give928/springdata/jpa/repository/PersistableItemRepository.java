package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.PersistableItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersistableItemRepository extends JpaRepository<PersistableItem, String> {
}
