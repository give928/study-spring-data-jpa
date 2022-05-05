package com.give928.springdata.jpa.repository;

import com.give928.springdata.jpa.entity.Item;
import com.give928.springdata.jpa.entity.PersistableItem;
import com.give928.springdata.jpa.repository.ItemRepository;
import com.give928.springdata.jpa.repository.PersistableItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    PersistableItemRepository persistableItemRepository;

    @Test
    @DisplayName("id 직접 할당인 경우 식별자 객체가 not null 이어서 merge 를 수행(select -> insert)")
    void emSaveMerge() {
        // given
        Item item = new Item("A");

        // when
        Item savedItem = itemRepository.save(item);

        // then
        assertThat(savedItem.getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("id 직접 할당인 경우 식별자 객체가 not null 이더라도 Persistable 인터페이스를 구현해서 새로운 엔티티로 판단하고 persist 를 수행(only insert)")
    void emSavePersist() {
        // given
        PersistableItem item = new PersistableItem("A");

        // when
        PersistableItem savedItem = persistableItemRepository.save(item);

        // then
        assertThat(savedItem.getId()).isEqualTo(item.getId());
    }
}
