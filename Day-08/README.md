# Day 8: Collections Framework (Part 2) & FR4–FR6

This document details the concepts and architectural implementation for **Day 8** of the Online Auction System: Map implementations, Comparable/Comparator contracts, value-based equality, and functional requirements FR4–FR6.

---

## 1. Map Implementations & Use Cases

| Map Implementation | Use Case in Project | Key Operation Complexity | Justification |
| :--- | :--- | :--- | :--- |
| **`HashMap<Integer, Auction>`** | Master auction lookup by `auctionId` | $\mathcal{O}(1)$ average `get()`, `put()`, `containsKey()` | High-speed, non-blocking lookup without requiring ordered iteration. |
| **`LinkedHashMap<Integer, Bid>`** | In-memory chronological activity audit trail (`activityLog`) | $\mathcal{O}(1)$ average + insertion order guarantee | Preserves the exact sequence bids were validated and accepted. |
| **`LinkedHashMap<Integer, BigDecimal>`** | Proxy-bid registrations per auction | $\mathcal{O}(1)$ average + FIFO tie-breaker | When two bidders specify the same maximum limit, the bidder who registered earlier wins the tie. |
| **`TreeMap<Integer, Auction>`** | Key-sorted auction catalogue reports (`getAuctionsSortedById()`) | $\mathcal{O}(\log N)$ retrieval and range operations | Sorts auctions strictly by key (natural ascending `auctionId`). |

### Demonstrations of Map Methods
- `get(key)`: Fast retrieval of auction instances in `AuctionService.findAuction(auctionId)`.
- `put(key, val)`: Storing new auctions, proxy configurations, and audit bids.
- `containsKey(key)`: Instant existence verification preventing `NullPointerException`s in `placeBid` and `configureProxyBid`.
- `getOrDefault(key, default)`: Graceful retrieval of proxy ceilings without risk of `null` (`getProxyBidLimit`).
- `remove(key)`: Deleting proxy bids if a bidder cancels participation (`removeProxyBid`).
- `merge(key, 1, Integer::sum)`: Aggregating bid frequencies per bidder (`countBidsPerBidder`) and auction status statistics (`countAuctionsByStatus`).

---

## 2. Natural Ordering (`Comparable`) vs Alternative Ordering (`Comparator`)

### Why Both?
- **`Comparable` (Natural Ordering)**:
  - Belongs directly to the entity (`compareTo`).
  - Implemented on `Auction` and `Bid` ordering by primary identifier (`auctionId` and `bidId` ascending).
  - Used by default in `TreeSet`, `TreeMap`, and default sorting collections.
- **`Comparator` (Alternative Ordering)**:
  - Separate class or lambda defining custom business ranking rules without mutating the entity class.
  - Implemented in `AuctionComparators.BID_AMOUNT_DESCENDING` for leaderboard ranking.
- **Chained Comparators**:
  - `AuctionComparators.HIGHEST_BID_DESCENDING_THEN_ID_ASCENDING`:
    ```java
    Comparator.comparing(Auction::getCurrentBid, Comparator.nullsLast(Comparator.naturalOrder()))
              .reversed()
              .thenComparing(Auction::getAuctionId);
    ```
  - Sorts auctions by highest bid descending, and breaks ties by `auctionId` ascending.

---

## 3. The `equals()` and `hashCode()` Contract

### General Contract Rules
1. **Reflexive**: `x.equals(x)` is always `true`.
2. **Symmetric**: `x.equals(y) == y.equals(x)`.
3. **Transitive**: `x.equals(y)` and `y.equals(z)` implies `x.equals(z)`.
4. **Consistent**: Multiple invocations return consistent results if fields have not mutated.
5. **Contract with `hashCode()`**: If `x.equals(y)` is `true`, then `x.hashCode() == y.hashCode()` **MUST** be true.

### Missing `hashCode()` Bug
If a class overrides `equals()` but forgets `hashCode()`, equal objects will have arbitrary identity hash codes:
- When storing an object in a `HashMap` or `HashSet`, it is placed into a bucket determined by its `hashCode()`.
- When looking up the same or an equal object, the JVM computes its `hashCode()`, visits a completely different bucket, and returns `null` or fails `set.contains()`.
- Verified deterministically in `Day8CollectionsPart2Test.testMissingHashCodeContractDemonstration`.

---

## 4. Functional Requirements FR4–FR6

### FR4 — Valid Bidding
- Bids are only permitted on auctions with `status == AuctionStatus.LIVE`.
- Bids must exceed `currentBid + minimumIncrement`.
- Sellers cannot bid on their own auctions.
- Highest bid is updated atomically only after all checks pass.
- Bids are recorded in `List<Bid> bidHistory` and the `LinkedHashMap` activity trail.

### FR5 — Proxy / Automatic Bidding
- Bidders configure a ceiling `maxLimit` associated with `(auctionId, bidderId)`.
- Proxy engine computes the lowest valid increment (`currentBid + minIncrement`) without exceeding `maxLimit`.
- Competing proxy bidders escalate prices automatically until the lower ceiling is matched.
- Tied limits are awarded to the bidder who configured their proxy first (FIFO via `LinkedHashMap`).

### FR6 — Anti-Sniping
- When a valid bid arrives within the final 2 minutes (`AuctionConstants.ANTI_SNIPE_WINDOW_MINUTES`), the auction deadline is extended by 5 minutes (`AuctionConstants.ANTI_SNIPE_EXTENSION_MINUTES`).
- Rejected bids never trigger extensions.
- Supports repeated extensions if additional bids are placed in subsequent late windows.

---

## 5. Verification Commands
```bash
mvn clean test -Dtest=Day8CollectionsPart2Test
```
