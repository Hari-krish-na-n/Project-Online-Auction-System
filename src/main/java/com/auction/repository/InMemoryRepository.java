package com.auction.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Generic In-Memory implementation of CrudRepository<T, ID>.
 *
 * Design Decisions & Highlights:
 * 1. ID Extraction: Uses a Function<T, ID> idExtractor. This avoids forcing entities to inherit
 *    a proprietary interface, supporting both legacy classes and new models cleanly.
 * 2. Concurrency: Backed by ConcurrentHashMap<ID, T> for thread-safe operations.
 * 3. Safe Defensive Copies: findAll() returns an unmodifiable list copy to prevent external mutation.
 * 4. Documented Behavior:
 *    - Duplicate IDs: save() replaces the existing entity with the newly provided one (upsert).
 *    - Missing records: findById() returns Optional.empty(); deleteById() returns false.
 *    - Null inputs: explicitly rejected with IllegalArgumentException.
 */
public class InMemoryRepository<T, ID> implements CrudRepository<T, ID> {

    private final Map<ID, T> storage = new ConcurrentHashMap<>();
    private final Function<T, ID> idExtractor;

    /**
     * Constructs an InMemoryRepository with an explicit ID extractor function.
     *
     * @param idExtractor lambda or method reference to retrieve the ID from entity T (e.g., User::getUserId)
     */
    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "idExtractor must not be null");
    }

    @Override
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity to save cannot be null.");
        }
        ID id = idExtractor.apply(entity);
        if (id == null) {
            throw new IllegalArgumentException("Entity ID extracted cannot be null.");
        }
        storage.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    @Override
    public boolean deleteById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }
        return storage.remove(id) != null;
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public boolean existsById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }
        return storage.containsKey(id);
    }
}
