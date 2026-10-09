# Day 5: Inheritance, Polymorphism, Interfaces, and Git Branching

Welcome to the Day 5 module of the **Online Auction System**. Today's focus bridges advanced Core Java Object-Oriented Programming (OOP) concepts with professional Git version control workflows.

---

## 1. Objectives

1. Build an abstract `BaseEntity` holding common identity and audit fields.
2. Establish a unified `User` hierarchy (`Admin`, `Seller`, `Bidder`) demonstrating constructor chaining (`super()`) and runtime polymorphism (`@Override`).
3. Implement the Strategy Pattern via a `BidStrategy` interface with `ManualBidStrategy` and `AutoBidStrategy`.
4. Demonstrate compile-time polymorphism (Method Overloading) in `PaymentService`.
5. Execute real-world Git operations: branching (`feature/day5-inheritance-polymorphism`), safe merge conflict generation and manual resolution, and linear rebasing.

---

## 2. Project Structure for Day 5

```text
Day-05/day5-inheritance-polymorphism/
├── README.md
└── docs/
    ├── OOP_NOTES.md            # Explanations of OOP concepts, constructor chaining, and contracts
    ├── GIT_NOTES.md            # Step-by-step record of merge conflicts and rebase actions
    └── uml-day5.md             # Mermaid class diagram and architectural breakdown
```

### Corresponding Java Sources (in `src/main/java/com/auction/`):
- `model/BaseEntity.java`: Abstract base class with `id` and `createdAt`.
- `model/User.java`: Abstract base user extending `BaseEntity`.
- `model/Admin.java`, `model/Seller.java`, `model/Bidder.java`: Concrete user roles overriding `displayRole()` and `getRoleDescription()`.
- `strategy/BidStrategy.java`: Strategy interface defining `calculateBid()`.
- `strategy/ManualBidStrategy.java`: Direct manual bid calculation.
- `strategy/AutoBidStrategy.java`: Automated proxy bid calculation up to a maximum budget ceiling.
- `service/BidEngineService.java`: Service accepting swappable `BidStrategy` implementations.
- `service/PaymentService.java`: Overloaded `pay()` methods with different parameter lists.
- `Day5Demo.java`: Automated demonstration suite for all Day 5 concepts.
- `src/test/java/com/auction/Day5ConceptsTest.java`: JUnit 5 test suite verifying all behaviors.

---

## 3. How to Run Day 5 Code

### Run JUnit 5 Tests
```bash
mvn test
```

### Run Day 5 Demonstration
```bash
mvn compile
java -cp target/classes com.auction.Day5Demo
```

### Run the Interactive Auction Console
```bash
java -cp target/classes com.auction.Main
```

---

## 4. Summary of Git Practice

- **Feature Branch:** `feature/day5-inheritance-polymorphism`
- **Conflict Branches:** `git-conflict-practice-a` and `git-conflict-practice-b`
- **Conflict File:** `docs/git-merge-practice.txt`
- **Rebase Branch:** `git-rebase-practice` rebased cleanly onto the feature branch.
