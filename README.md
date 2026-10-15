# Online Auction System

**Online Auction System** is a console-based core Java application that simulates a full-featured online marketplace where sellers list items, bidders compete in live auctions, and administrators manage approvals and closures. The project exists to demonstrate robust Object-Oriented Programming (OOP) design patterns, in-memory domain modeling, transactional bidding constraints, and professional Git workflows without external frameworks or database dependencies. It is designed for students, developers, and reviewers who want to learn and assess clean Java fundamentals, modular layered design, and practical software craftsmanship.

---

## 1. System Architecture

The Online Auction System follows a clean **Layered Architecture** adhering to separation of concerns, single responsibility, and strong encapsulation.

```text
                     ┌──────────────────────────────────────────┐
                     │          Console Presentation UI         │
                     │          (Interactive CLI Menus)         │
                     └────────────────────┬─────────────────────┘
                                          │ User Input & Display
                                          ▼
                     ┌──────────────────────────────────────────┐
                     │            com.auction.Main              │
                     │       (Menu Dispatcher & Controller)     │
                     └────────────────────┬─────────────────────┘
                                          │ Service Invocations
                                          ▼
                     ┌──────────────────────────────────────────┐
                     │          Service Business Layer          │
                     │  ┌────────────────────────────────────┐  │
                     │  │          AuctionService            │  │
                     │  │   (Lifecycle, Approvals, Audit)    │  │
                     │  ├────────────────────────────────────┤  │
                     │  │          BiddingService            │  │
                     │  │   (Atomic Increments, Rules)       │  │
                     │  └────────────────────────────────────┘  │
                     └────────────────────┬─────────────────────┘
                                          │ Domain Entity Access
                                          ▼
                     ┌──────────────────────────────────────────┐
                     │         Domain Model & In-Memory         │
                     │  ┌──────────────┐      ┌──────────────┐  │
                     │  │  User / Role │      │   Auction    │  │
                     │  │ (Admin/Seller│      │ (Status/Bid) │  │
                     │  │   /Bidder)   │      │              │  │
                     │  └──────────────┘      └──────────────┘  │
                     │  ┌──────────────┐      ┌──────────────┐  │
                     │  │     Bid      │      │  Constants   │  │
                     │  │ (Chronology) │      │  (Rules/Fee) │  │
                     │  └──────────────┘      └──────────────┘  │
                     └──────────────────────────────────────────┘
```

---

## 2. Core Architecture & Modules

| Package | Component / Class | Responsibilities |
| :--- | :--- | :--- |
| `com.auction` | `Main.java` | Interactive CLI menu driver, Scanner input validation, polymorphic user demonstrations. |
| `com.auction` | `AuctionConstants.java` | Centralized business constants (`MINIMUM_INCREMENT`, `ANTI_SNIPE`, fees, limits) preventing magic numbers. |
| `com.auction.model` | `User.java` | Abstract base user entity encapsulating `userId`, `name`, `email`, and role credentials. |
| `com.auction.model` | `Admin.java` | Extends `User`: administrative privileges, auction review, catalog audits. |
| `com.auction.model` | `Seller.java` | Extends `User`: listing creation, reserve price assignment, personal catalog management. |
| `com.auction.model` | `Bidder.java` | Extends `User`: watchlist management, active bidding, bid history tracking. |
| `com.auction.model` | `Auction.java` | Encapsulates listing data, pricing (`BigDecimal`), lifecycle states, and chronological bid history. |
| `com.auction.model` | `Bid.java` | Immutable bid instance containing `bidId`, `bidder`, `auction`, timestamp, and monetary amount. |
| `com.auction.service` | `AuctionService.java` | Orchestrates auction lifecycle transitions (`PENDING` → `APPROVED` → `LIVE` → `CLOSED`/`UNSOLD`). |
| `com.auction.service` | `BiddingService.java` | Validates bidding constraints (increments, anti-sniping, caller checks) and logs bids. |
| `com.auction.enums` | `AuctionStatus.java` | Type-safe enum managing states: `PENDING`, `APPROVED`, `LIVE`, `CLOSED`, `UNSOLD`. |
| `com.auction.exception` | `AuctionNotActiveException.java` | Checked exception thrown when an operation targets an inactive/unapproved auction. |
| `com.auction.exception` | `InvalidBidException.java` | Unchecked exception thrown on illegal bid values or under-increment violations. |

---

## 3. Seed Users & Test Credentials

The system initializes pre-seeded demo accounts at startup for zero-friction console testing:

| Role | Name | Email | Password | Access / Privileges |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | Admin Raj | `admin@auction.com` | `admin123` | Platform oversight, queue approvals, manual auction closure & winner determination. |
| **Seller** | Seller Ravi | `ravi@auction.com` | `sell123` | Electronics listing creation, reserve pricing, seller dashboard. |
| **Seller** | Seller Priya | `priya@auction.com` | `sell456` | Antiques listing creation, item catalogue audits. |
| **Bidder** | Bidder Arjun | `arjun@auction.com` | `bid123` | Live auction browsing, competitive bidding, personal watchlist. |
| **Bidder** | Bidder Sneha | `sneha@auction.com` | `bid456` | Live bidding, bid history inspection, audit participation. |

---

## 4. Console Operations & Functional Flow

All operations map cleanly to the 8 core Functional Requirements (FR1–FR8):

### Seller Flow (FR2, FR7)
1. **Create Auction**: Prompts for title, description, starting price, reserve price, minimum increment, start time, and end time.
2. **View My Auctions**: Displays all listings created by the authenticated seller with current bid and approval state.

### Admin Flow (FR3, FR6)
1. **View Pending Auctions**: Lists all newly submitted auctions waiting in the queue.
2. **Approve Auction by ID**: Transitions auction from `PENDING` to `APPROVED`.
3. **Approve Next in Queue**: FIFO queue processing for pending listings.
4. **View All Auctions**: Comprehensive platform catalogue overview.
5. **Close Auction (Determine Winner)**: Evaluates final bids against reserve price, marks as `CLOSED` (winner declared) or `UNSOLD`.

### Bidder Flow (FR4, FR5)
1. **View Live Auctions**: Displays all currently active listings open for bidding.
2. **Place Bid**: Atomic validation requiring `newBid >= currentBid + minimumIncrement`.
3. **Watchlist Operations**: Bookmark listings for quick monitoring.
4. **Bid History Audit**: Inspect the chronological sequence of all bids placed on any auction.

---

## 5. Quick Start

### Prerequisites
- **Java Development Kit (JDK)**: Version 21 or higher
- **Apache Maven**: Version 3.9 or higher

### Build & Package
```bash
# Compile and build executable JAR
mvn clean package
```

### Run Application
```bash
# Option 1: Run executable JAR
java -jar target/online-auction-1.0-SNAPSHOT.jar

# Option 2: Run via batch script (Windows)
./run.bat

# Option 3: Run via Maven Exec plugin
mvn compile exec:java -Dexec.mainClass="com.auction.Main"
```

---

## 6. Implementation Status & Daily Progress

### Day 1: Project Architecture & Backlog Setup
- [x] Analyzed domain requirements from Project Catalog.
- [x] Converted 8 Functional Requirements (FR1–FR8) into formal User Stories (`As a <role> I want <feature> so that <benefit>`).
- [x] Assigned story-point estimates (Total: 34 SP) and established Definition of Done (DoD).
- [x] Established project architecture and modular boundaries.

### Day 2: Foundational Domain & Language Fundamentals
- [x] Established `AuctionConstants.java` preventing magic numbers across bidding rules and platform commissions.
- [x] Utilized `BigDecimal` for financial calculations avoiding floating-point precision hazards.
- [x] Configured `.gitignore` and build artifact exclusions.

### Day 3: Control Flow & Maven Menu Integration
- [x] Implemented multi-level interactive console menus for Seller, Admin, and Bidder.
- [x] Safe Scanner wrappers with strict input validation protecting against negative numbers, non-numeric input, and crashes.
- [x] Maven project configuration with Java 21 compiler plugins and surefire execution.

### Day 4: OOP Concepts, Model Entities & IDE Debugging
- [x] **Inheritance**: Base `User` entity extended by `Admin`, `Seller`, and `Bidder` with `super()` constructor chaining.
- [x] **Encapsulation**: Private state protected by validated getters/setters.
- [x] **Polymorphic Dispatch**: Role display and action methods overridden across subclasses.
- [x] **Custom Exceptions**: `AuctionNotActiveException` (checked) and `InvalidBidException` (unchecked).
- [x] **IDE Debugging**: Bid increment verification validated using breakpoints, conditional breakpoints, watch expressions, and call stacks.

### Day 5: Inheritance, Polymorphism, Interfaces & Git Branching
- [x] **BaseEntity**: Abstract base model encapsulating common `id` and `createdAt` audit fields with constructor chaining.
- [x] **User Hierarchy**: Extended `User` with `Admin`, `Seller`, and `Bidder` implementing polymorphic role descriptions.
- [x] **Strategy Pattern**: `BidStrategy` interface implemented by `ManualBidStrategy` and `AutoBidStrategy`, consumed polymorphically by `BidEngineService`.
- [x] **Method Overloading**: Overloaded `pay()` methods in `PaymentService` handling default, mode-specific, and promotional payments.
- [x] **Git Branching & Workflow**: Worked on `feature/day5-inheritance-polymorphism`, executed safe merge conflict generation and resolution, and performed a documentation rebase.
- [x] **Verification & Documentation**: UML diagram (`docs/uml-day5.md`), OOP guide (`OOP_NOTES.md`), Git logs (`GIT_NOTES.md`), and comprehensive JUnit 5 test suite.

### Day 6: Exception Handling + AI-Assisted Debugging
- [x] **Custom Checked Exception (`InsufficientStockException`)**: Enforces inventory limit business rules when requested order quantity exceeds available stock.
- [x] **Custom Unchecked Exceptions (`InvalidQuantityException`, `InvalidBidException`)**: Rejects non-positive quantities and bids without corrupting item/order state.
- [x] **Exception Chaining & Cause Preservation**: Wrapped low-level domain exceptions into `OrderProcessingException` while preserving original causes accessible via `getCause()`.
- [x] **Multi-Catch & `finally` Auditing**: Built centralized `try-catch-finally` in `AuditService` and `OrderProcessor` ensuring audit trail execution regardless of success or failure.
- [x] **AI-Assisted Debugging Notes**: Documented 2 real JVM stack traces with AI Copilot analysis, verification, root cause diagnosis, and fixes (`Day-06/day6-exception-handling/docs/AI_DEBUGGING_NOTES.md`).
- [x] **Console Graceful Recovery**: Protected console menu loops against unexpected input crashes and exceptions.

### Day 7: Collections Framework (Part 1) — Lists, Sets & Queues
- [x] **FR1 (Authentication)**: `ArrayList<User>` master user catalog + `HashSet<String>` for instant $\mathcal{O}(1)$ username duplicate checks and case-insensitive uniqueness.
- [x] **FR2 (Auction Listings)**: `ArrayList<Auction>` catalog storing validated seller listings in `PENDING` status.
- [x] **FR3 (Admin Moderation Queue)**: `ArrayDeque<Auction>` FIFO moderation queue ensuring fairness and preventing repeated approvals.
- [x] **Library Waitlist Simulation**: Standalone exercise demonstrating `ArrayList` catalogue, `LinkedHashSet` request deduplication, `ArrayDeque` FIFO queue, `PriorityQueue` faculty-first comparator ordering, and `Iterator.remove()` for overdue items.
- [x] **Collections Showcase & Fail-Safe Patterns**: Detailed exploration of List/Set/Queue implementations, safe removal with `Iterator.remove()`, bi-directional traversal with `ListIterator`, and handling `ConcurrentModificationException`.
- [x] **Benchmark Analysis**: Nanosecond timing benchmark measuring `ArrayList` vs `LinkedList` on head insertions and indexed lookups (`Day-07/day7-collections-framework/docs/COLLECTION_TIMING.md`).

### Day 8: Collections Framework (Part 2) & FR4–FR6
- [x] **Map Implementations**:
  - `HashMap`: Fast $\mathcal{O}(1)$ lookup of auctions by `auctionId`, users by `userId`, and proxy bid allocations.
  - `LinkedHashMap`: Preserves exact insertion order for in-memory chronological activity history (`activityLog`) and proxy registrations.
  - `TreeMap`: Generates auction reports sorted naturally by keys (`auctionId` ascending).
  - `Map.merge()`: Aggregates total bids per bidder (`map.merge(bidderName, 1, Integer::sum)`) and auctions per status.
  - Map API demonstrations: `get()`, `put()`, `containsKey()`, `getOrDefault()`, `remove()`, and `merge()`.
- [x] **Comparable & Comparator**:
  - `Comparable`: Implemented natural ordering for `Auction` (by `auctionId` ascending) and `Bid` (by `bidId` ascending).
  - `Comparator`: Implemented alternative ordering with `AuctionComparators.BID_AMOUNT_DESCENDING`.
  - Chained Comparator: Built `AuctionComparators.HIGHEST_BID_DESCENDING_THEN_ID_ASCENDING` using `Comparator.comparing().reversed().thenComparing()`.
- [x] **equals() and hashCode() Contract**:
  - Implemented consistent, value-based `equals()` and `hashCode()` on `Auction`, `Bid`, and `User` using stable entity IDs.
  - Added deterministic tests verifying contract rules, `HashSet`/`HashMap` bucket behavior, and the consequences of the missing-`hashCode` bug.
- [x] **FR4 (Valid Bidding)**:
  - Enforces `LIVE` status check (`AuctionNotActiveException`), non-seller bidder checks, positive amounts, and minimum increment calculations.
  - Atomically updates highest bid upon valid placement and logs activity.
- [x] **FR5 (Proxy / Automatic Bidding)**:
  - Configures maximum proxy limits per bidder and auction.
  - Increments lowest eligible bid step up to the bidder's ceiling.
  - Manages competing proxy bidders with documented deterministic tie-breaking (earlier registration wins).
- [x] **FR6 (Anti-Sniping)**:
  - Detects valid bids submitted within the final 2 minutes of auction deadline.
  - Automatically extends deadline by 5 minutes (`AuctionConstants.ANTI_SNIPE_EXTENSION_MINUTES`).
  - Supports repeated late-bid extensions while ignoring rejected bids.

### Day 9: Generics & Safe Resource Management
- [x] **Generic Repository Abstraction**:
  - Defined `CrudRepository<T, ID>` interface (`save`, `findById`, `findAll`, `deleteById`, `count`, `existsById`).
  - Implemented `InMemoryRepository<T, ID>` with `Function<T, ID>` identifier extractor, concurrency support, and defensive copies.
  - Reused generic repository for both `User` and `Auction` entities without raw types or unchecked casting.
- [x] **Generic Utility Examples & PECS**:
  - `Pair<K, V>`: Generic pair container holding two differently typed values.
  - `Box<T extends Number>`: Upper bounded generic container accepting numeric types only.
  - Generic `max()` method using `<T extends Comparable<T>>`.
  - Producer Extends (`? extends Number`): Reading and summing numeric collections.
  - Consumer Super (`? super Integer`): Populating integer consumers.
  - Documentation of type erasure and compile-time type safety.
- [x] **try-with-resources & AutoCloseable**:
  - `AuctionCsvLoader`: Safe CSV reader utilizing try-with-resources with automatic stream closure and row validation reporting.
  - `AuditSessionResource`: Demonstrates explicit `AutoCloseable` implementation and LIFO closing order verification.