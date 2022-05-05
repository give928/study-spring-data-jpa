package com.give928.springdata.jpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

@EntityListeners(AuditingEntityListener.class)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item {
    @Id
    @Column(name = "item_code")
    private String id;

    public Item(String id) {
        this.id = id;
    }
}
