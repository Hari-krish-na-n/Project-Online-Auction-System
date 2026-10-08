# Online Auction System — Agile User Stories & Backlog

## Sprint 1: Core Foundation & Domain Setup

### Overview
This backlog documents the 8 Functional Requirements (FR1–FR8) for the **Online Auction System** project converted into Agile User Stories with story-point estimates and acceptance criteria.

---

### User Stories

#### FR1: User Roles & Authentication
- **User Story**: As a platform participant (Admin, Seller, Bidder), I want to access my role-specific dashboard and actions so that I can securely perform auction activities.
- **Story Points**: 3
- **Acceptance Criteria**:
  - System recognizes `Admin`, `Seller`, and `Bidder` roles.
  - Each role has distinct menus and capabilities.
  - Polymorphic role display at application startup.

#### FR2: Auction Listing Creation
- **User Story**: As a Seller, I want to create a new auction with starting price, reserve price, minimum increment, and duration so that bidders can discover and bid on my products.
- **Story Points**: 5
- **Acceptance Criteria**:
  - Requires title, description, starting price, reserve price, min increment, start/end time.
  - Starting and reserve prices must be > 0; reserve price >= starting price.
  - Newly created auctions enter `PENDING` status.

#### FR3: Admin Approval Workflow
- **User Story**: As an Admin, I want to review submitted listings in a pending queue and approve or reject them so that only compliant items go live.
- **Story Points**: 3
- **Acceptance Criteria**:
  - Displays pending auctions in chronological/FIFO order.
  - Admin can approve by specific ID or approve the next item in the queue.
  - Approved items transition to `APPROVED` / `LIVE` state.

#### FR4: Real-time Bidding Engine
- **User Story**: As a Bidder, I want to place competitive bids on live auctions so that I can win desired items.
- **Story Points**: 5
- **Acceptance Criteria**:
  - Bid must be placed on a `LIVE` auction.
  - New bid must satisfy: `newBid >= currentBid + minimumIncrement`.
  - Chronological bid history updated immediately upon acceptance.

#### FR5: Watchlist & Activity Tracking
- **User Story**: As a Bidder, I want to bookmark auctions to my watchlist and review my past bids so that I can monitor items without searching repeatedly.
- **Story Points**: 3
- **Acceptance Criteria**:
  - Bidder can add/remove auction IDs to personal watchlist.
  - Displays current highest bid and live status for all watched items.
  - Personal bid history displays all submitted amounts.

#### FR6: Auction Closure & Winner Determination
- **User Story**: As a System / Admin, I want to evaluate final bids when an auction ends so that the highest valid bidder is declared the winner or the item is marked unsold.
- **Story Points**: 5
- **Acceptance Criteria**:
  - If highest bid >= reserve price: status becomes `CLOSED`, winner recorded.
  - If highest bid < reserve price or no bids: status becomes `UNSOLD`.

#### FR7: Seller Listing Management & Audits
- **User Story**: As a Seller, I want to audit all my current and past listings so that I can track bidding activity and reserve status.
- **Story Points**: 3
- **Acceptance Criteria**:
  - Seller dashboard filters listings created by the active seller.
  - Shows current highest bid and reserve price status.

#### FR8: Error Handling & Data Integrity
- **User Story**: As a User, I want clear feedback when invalid operations occur so that the application maintains transactional consistency and does not crash.
- **Story Points**: 5
- **Acceptance Criteria**:
  - Checked `AuctionNotActiveException` thrown for non-live bidding.
  - Unchecked `InvalidBidException` thrown for illegal bid amounts.
  - Safe Scanner input parsing prevents crashes on invalid format input.

---

### Definition of Done (DoD)
1. **Compilation**: Clean compilation using standard `javac` and `mvn compile` with Java 21.
2. **Encapsulation**: All entity state is private with validated getters/setters.
3. **No Magic Numbers**: All business constraints referenced from `AuctionConstants`.
4. **Resilience**: Interactive CLI handles non-numeric and negative inputs safely without unhandled exceptions.
5. **Testing**: Core business rules validated via JUnit 5 tests.
