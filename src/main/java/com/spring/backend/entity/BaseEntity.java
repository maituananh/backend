package com.spring.backend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false, nullable = false)
  protected Long id;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  protected Instant createdAt;

  @Column(name = "created_by", updatable = false)
  protected Long createdBy;

  @UpdateTimestamp
  @Column(name = "updated_at")
  protected Instant updatedAt;

  @Column(name = "updated_by")
  protected Long updatedBy;
}
