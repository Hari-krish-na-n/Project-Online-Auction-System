# Day 9: Generics & Safe Resource Management

This document details the concepts and architectural implementation for **Day 9** of the Online Auction System: Generic repositories, bounded types, wildcards (PECS), type erasure, and try-with-resources.

---

## 1. Generic Repository Abstraction

### Interface: `CrudRepository<T, ID>`
```java
public interface CrudRepository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean deleteById(ID id);
    long count();
    boolean existsById(ID id);
}
```

### In-Memory Implementation: `InMemoryRepository<T, ID>`
- **Decoupled ID Extraction**: Employs `Function<T, ID> idExtractor` passed during construction (e.g. `User::getUserId` or `Auction::getAuctionId`). This avoids forcing domain models to implement custom marker interfaces.
- **Thread Safety**: Backed by `ConcurrentHashMap<ID, T>`.
- **Defensive Copies**: `findAll()` returns an unmodifiable list copy (`Collections.unmodifiableList(new ArrayList<>(storage.values()))`).
- **Upsert Semantics**: Saving an entity with an existing ID updates the entry without growing size.
- **Reused Entities**: Tested and verified with both `User` and `Auction`.

---

## 2. Generic Utility Demonstrations

### `Pair<K, V>`
- Reusable two-element tuple holding heterogeneous types.
- Used for auction metadata and bidder scores.

### Bounded Box: `Box<T extends Number>`
- Restricts type parameter `T` to subclasses of `java.lang.Number`.
- Prevents invalid non-numeric types from being passed at compile-time.

### Generic `max()` Method
- Signature: `<T extends Comparable<T>> T max(T a, T b)`
- Leverages upper bound to guarantee elements can compare to one another.

### PECS (Producer Extends, Consumer Super)
- **Producer Extends (`? extends Number`)**:
  - Used when reading from a collection.
  - Example: `sumOfNumbers(Collection<? extends Number> numbers)` reads numbers and sums their values.
- **Consumer Super (`? super Integer`)**:
  - Used when writing / populating into a collection.
  - Example: `populateIntegers(Collection<? super Integer> consumer, int count)` inserts `Integer` values into `Collection<Number>` or `Collection<Object>`.

### Type Erasure
- Generics in Java are a compile-time feature.
- During compilation, type arguments are erased to their bounds (or `Object` if unbounded) and synthetic casts are generated.
- Raw types (e.g. `List list = new ArrayList()`) circumvent type checks and can cause runtime `ClassCastException`s. Generic declarations eliminate raw types completely.

---

## 3. try-with-resources & AutoCloseable

### Practical CSV Parsing: `AuctionCsvLoader`
- Uses `BufferedReader` inside try-with-resources:
  ```java
  try (BufferedReader reader = new BufferedReader(in)) {
      ...
  }
  ```
- Ensures underlying file/stream resources are closed reliably even when invalid CSV formats or exceptions occur.
- Validates rows (prices, product names, column count) and reports line-by-line errors gracefully without crashing.

### Explicit Resource Ordering: `AuditSessionResource`
- Implements `AutoCloseable`.
- Demonstrates that multiple resources declared in a try-with-resources block are closed in **reverse order of declaration** (LIFO).
- Tested in `Day9GenericsTest.testAutoCloseableOrder`.

---

## 4. Verification Commands
```bash
mvn clean test -Dtest=Day9GenericsTest
mvn clean package
```
