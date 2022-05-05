package com.give928.springdata.jpa.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PersistableItem implements Persistable<String> {
    @Id
    @Column(name = "item_code")
    private String id;

    @CreatedDate
    private LocalDateTime createdDate; // persist 전에 호출된다.

    public PersistableItem(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdDate == null; // id 에 값이 있어도 createdDate 가 없어서 새로운 엔티티로 판단
    }
}
