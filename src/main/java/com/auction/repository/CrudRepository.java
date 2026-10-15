package com.auction.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD Repository interface.
 *
 * Demonstrates:
 * - Generics with type parameters <T, ID>:
 *   T: the domain entity type (e.g. User, Auction, Bid)
 *   ID: the identifier type (e.g. Integer, Long, String)
 * - Safe abstractions without raw types or unchecked casting.
 *
 * Contract:
 * - save(T): Persists or updates the entity; returns the saved instance.
 * - findById(ID): Returns Optional<T> containing entity if present, or Optional.empty().
 * - findAll(): Returns an unmodifiable snapshot list of all stored entities.
 * - deleteById(ID): Deletes entity with specified ID; returns true if deleted, false if not found.
 */
public interface CrudRepository<T, ID> {

    /**
     * Saves a given entity.
     * If an entity with the same ID already exists, it is overwritten/updated.
     *
     * @param entity must not be null
     * @return the saved entity
     * @throws IllegalArgumentException if entity is null
     */
    T save(T entity);

    /**
     * Retrieves an entity by its identifier.
     *
     * @param id must not be null
     * @return Optional containing the entity, or Optional.empty() if not found
     * @throws IllegalArgumentException if id is null
     */
    Optional<T> findById(ID id);

    /**
     * Returns all entities managed by this repository.
     *
     * @return unmodifiable list of all entities
     */
    List<T> findAll();

    /**
     * Deletes the entity with the given identifier.
     *
     * @param id must not be null
     * @return true if an entity was deleted, false if no entity existed with the given ID
     * @throws IllegalArgumentException if id is null
     */
    boolean deleteById(ID id);

    /**
     * Returns the total count of entities in the repository.
     */
    long count();

    /**
     * Checks whether an entity with the given ID exists.
     */
    boolean existsById(ID id);
}
