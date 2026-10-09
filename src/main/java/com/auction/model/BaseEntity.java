package com.auction.model;

import java.time.LocalDateTime;

/**
 * BaseEntity is an abstract base class for entities in the Online Auction System.
 *
 * Why common fields belong in a base class:
 * 1. Code Reusability: Common fields like 'id' and 'createdAt' exist across multiple
 *    domain models (Users, Auctions, Bids, Payments). Centralizing them here prevents duplication.
 * 2. Maintainability: If auditing or metadata fields change (e.g. adding 'updatedAt'),
 *    it can be updated once in BaseEntity without altering every individual entity.
 * 3. Consistent Identity & Timestamping: Enforces uniform ID naming and creation timestamping.
 */
public abstract class BaseEntity {

    private final int id;
    private final LocalDateTime createdAt;

    /**
     * Constructor initializing the common entity identity and audit timestamp.
     *
     * @param id unique entity identifier
     */
    public BaseEntity(int id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor allowing an explicit creation timestamp (e.g., when restoring or seeding).
     *
     * @param id unique entity identifier
     * @param createdAt entity creation timestamp
     */
    public BaseEntity(int id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
