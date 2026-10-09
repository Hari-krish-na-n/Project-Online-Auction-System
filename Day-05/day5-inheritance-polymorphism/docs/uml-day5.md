# Day 5 UML Class Diagram: Inheritance, Polymorphism & Interfaces

This document outlines the Object-Oriented design relationships implemented in the **Online Auction System** for Day 5.

## 1. Class & Interface Diagram (Mermaid)

```mermaid
classDiagram
    %% Inheritance Hierarchy
    class BaseEntity {
        <<abstract>>
        -int id
        -LocalDateTime createdAt
        +getId() int
        +getCreatedAt() LocalDateTime
    }

    class User {
        <<abstract>>
        -String name
        -String email
        -String password
        +displayRole()* void
        +getRoleDescription()* String
        +getUserId() int
        +getName() String
        +getEmail() String
        +setName(String) void
        +setEmail(String) void
    }

    class Admin {
        +displayRole() void
        +getRoleDescription() String
        +approveAction(int) void
    }

    class Seller {
        +displayRole() void
        +getRoleDescription() String
        +createAuctionAction(String) void
    }

    class Bidder {
        -Set~Integer~ watchlist
        +displayRole() void
        +getRoleDescription() String
        +addToWatchlist(int) void
        +getWatchlist() Set~Integer~
        +isWatching(int) boolean
        +placeBidAction(int) void
    }

    BaseEntity <|-- User : extends
    User <|-- Admin : extends
    User <|-- Seller : extends
    User <|-- Bidder : extends

    %% Strategy Pattern & Interfaces
    class BidStrategy {
        <<interface>>
        +calculateBid(Auction, Bidder, BigDecimal)* BigDecimal
        +getStrategyName()* String
    }

    class ManualBidStrategy {
        +calculateBid(Auction, Bidder, BigDecimal) BigDecimal
        +getStrategyName() String
    }

    class AutoBidStrategy {
        +calculateBid(Auction, Bidder, BigDecimal) BigDecimal
        +getStrategyName() String
    }

    BidStrategy <|.. ManualBidStrategy : implements
    BidStrategy <|.. AutoBidStrategy : implements

    class BidEngineService {
        -AuctionService auctionService
        +processBid(int, Bidder, BidStrategy, BigDecimal) BigDecimal
    }

    BidEngineService ..> BidStrategy : depends on (Dependency Inversion)

    %% Payment Method Overloading
    class PaymentService {
        +pay(int, Bidder, BigDecimal) boolean
        +pay(int, Bidder, BigDecimal, String) boolean
        +pay(int, Bidder, BigDecimal, String, String) boolean
    }
```

## 2. Diagram Breakdown

1. **`BaseEntity` & `User` (Inheritance & Common State)**
   - `BaseEntity` is an abstract class holding common identity (`id`) and audit timestamp (`createdAt`).
   - `User` extends `BaseEntity`, reusing identity state while adding core user credentials (`name`, `email`, `password`).
   - `Admin`, `Seller`, and `Bidder` inherit all common attributes without repeating fields, invoking `super()` constructors.

2. **`BidStrategy` (Interface & Strategy Pattern)**
   - Defines a uniform contract (`calculateBid`) for placing bids.
   - `ManualBidStrategy` processes direct manual bids meeting minimum increment rules.
   - `AutoBidStrategy` processes proxy/automated increments up to a defined budget ceiling.
   - `BidEngineService` consumes `BidStrategy` polymorphically at runtime without depending on concrete classes.

3. **`PaymentService` (Method Overloading)**
   - Illustrates compile-time polymorphism where three overloaded `pay(...)` methods share the same name but provide distinct parameter signatures for wallet, custom payment modes, and promotional vouchers.
