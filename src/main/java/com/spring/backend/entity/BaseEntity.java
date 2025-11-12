package com.spring.backend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false, nullable = false)
  protected Long id;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  protected Instant createdAt;

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  protected Long createdBy;

  @LastModifiedDate
  @Column(name = "updated_at")
  protected Instant updatedAt;

  @LastModifiedBy
  @Column(name = "updated_by")
  protected Long updatedBy;
}
