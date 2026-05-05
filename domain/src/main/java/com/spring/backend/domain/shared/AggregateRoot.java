package com.spring.backend.domain.shared;

import java.time.Instant;

public abstract class AggregateRoot {

    protected Long id;
    protected Instant createdAt;
    protected Long createdBy;
    protected Instant updatedAt;
    protected Long updatedBy;

    protected AggregateRoot() {}

    protected AggregateRoot(Long id, Instant createdAt, Long createdBy,
                             Instant updatedAt, Long updatedBy) {
        this.id = id;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public Long getId()           { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getCreatedBy()    { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getUpdatedBy()    { return updatedBy; }
}
