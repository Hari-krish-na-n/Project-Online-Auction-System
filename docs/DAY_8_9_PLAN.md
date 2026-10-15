# Implementation Plan: Day 8 & Day 9 — Online Auction System

## Overview
This plan outlines the implementation for Day 8 (Collections Framework Part 2 & Functional Requirements FR4–FR6) and Day 9 (Generics, Reusable CrudRepository, try-with-resources CSV loader) into the existing Java 21 / Maven application.

---

## 1. Domain & Architecture Integration
- **Entities**:
  - `Auction`: Implement `Comparable<Auction>` (natural order by ID ascending), implement robust and consistent `equals(Object)` and `hashCode()` based on stable ID. Add fields/methods for anti-sniping and proxy bidding.
  - `Bid`: Implement `Comparable<Bid>` (natural order by ID ascending), implement `equals(Object)` and `hashCode()` based on stable ID. Add tie-breaking support.
  - `User`, `Bidder`, `Seller`, `Admin`: Implement `equals()` and `hashCode()` on `User` based on `userId`.
- **Comparators**:
  - Bid amount descending comparator: `Comparator<Bid>`.
  - Chained comparator for auctions: `Comparator.comparing(Auction::getCurrentBid).reversed().thenComparing(Auction::getAuctionId)`.
- **Map Implementations**:
  - `HashMap<Integer, Auction>` & `HashMap<Integer, User>`: O(1) instant key lookup.
  - `HashMap<Integer, BigDecimal>` or `HashMap<String, BigDecimal>`: Proxy-bid maximum budget mapping by bidder ID / auction ID.
  - `LinkedHashMap<Integer, Bid>` or `LinkedHashMap<String, Object>`: Ordered in-memory activity audit trail preserving exact insertion sequence.
  - `TreeMap<String, List<Auction>>` / `TreeMap<Integer, Auction>`: Category / auction reporting sorted naturally by key.
  - `merge()` demonstration: aggregation of bids per bidder and auctions per category / status.
- **FR4 — Valid Bidding**:
  - Enforce status == LIVE.
  - Validate `bidAmount >= currentBid + minimumIncrement`.
  - Atomically update highest bid only upon valid acceptance.
  - Meaningful exceptions (`AuctionNotActiveException`, `InvalidBidException`).
- **FR5 — Proxy / Automatic Bidding**:
  - Store proxy limits per (auctionId, bidderId).
  - Automatically calculate next increment without exceeding bidder's max limit.
  - Support competing proxy bidders with deterministic tie-breaking (earlier proxy submission wins at equal limit).
- **FR6 — Anti-Sniping**:
  - If a valid bid is placed within the final 2 minutes (`AuctionConstants.ANTI_SNIPE_WINDOW_MINUTES`), extend the deadline by 5 minutes (`AuctionConstants.ANTI_SNIPE_EXTENSION_MINUTES`).
  - Do not extend deadline for rejected bids.
  - Support repeated anti-snipe extensions.

---

## 2. Day 9: Generics & Try-With-Resources
- **A. Generic Repository**:
  - `CrudRepository<T, ID>` interface (`save`, `findById`, `findAll`, `deleteById`).
  - `InMemoryRepository<T, ID>` implementing `CrudRepository` with an `idExtractor: Function<T, ID>`, thread-safe map, handling duplicate IDs and missing records.
  - Demonstrated with both `User` (or `Bidder`) and `Auction`.
- **B. Generic Utilities**:
  - `Pair<K, V>`.
  - `Box<T extends Number>`.
  - `GenericUtils.max(T a, T b)` with `<T extends Comparable<T>>`.
  - PECS demonstrations (`? extends Number` producer, `? super Integer` consumer).
  - Documentation and test explanations of type erasure and raw type safety risks.
- **C. Resource Management (try-with-resources & AutoCloseable)**:
  - `AuctionCsvLoader` using `BufferedReader` in try-with-resources to parse seed auctions or items from CSV data.
  - Validates records and reports malformed rows cleanly without crashing.
  - `AuditSessionResource` implementing `AutoCloseable` demonstrating explicit closing order and suppression behavior.

---

## 3. Test Suites & Verification
- `Day8CollectionsPart2Test`:
  - Map lookups (`get`, `put`, `containsKey`, `getOrDefault`, `remove`, `merge`).
  - `Comparable` natural ordering and `Comparator` / chained comparator sorting.
  - `equals` and `hashCode` contract, hash-based collection behavior, and missing-hashCode bug demonstration.
  - FR4 valid / low / invalid / inactive bid scenarios.
  - FR5 proxy bidding limits and competing proxy bidders.
  - FR6 anti-sniping threshold boundaries and repeated extensions.
- `Day9GenericsTest`:
  - Generic repository CRUD operations on `User` and `Auction`.
  - Generic `Pair`, `Box<T extends Number>`, `max()` with bounds.
  - PECS wildcard reading and writing.
  - CSV parsing with valid, empty, missing, and malformed inputs with try-with-resources.
  - `AutoCloseable` closing order verification.

---

## 4. Documentation & Git Commits
- Update root `README.md` and create dedicated Day 8 & Day 9 documentation:
  - `Day-08/README.md`
  - `Day-09/README.md`
- Verify tests via `mvn clean test` and package via `mvn package`.
- Git status, diff check, clean commits:
  - `feat: apply collections framework part 2 to auction system`
  - `feat: apply generics and reusable repository to auction system`
- Push to origin on `main`.
