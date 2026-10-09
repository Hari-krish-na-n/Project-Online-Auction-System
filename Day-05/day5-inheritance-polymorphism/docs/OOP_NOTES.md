# Day 5: Object-Oriented Programming (OOP) Notes

These notes explain the core OOP concepts implemented on Day 5 for our Online Auction System. Written in simple, natural language as part of my learning journey.

---

## 1. Why Common Fields Belong in a Base Class (`BaseEntity`)

When designing the domain models, every core entity in an auction system (such as Users, Auctions, and Bids) needs an identifier (`id`) and an audit timestamp (`createdAt`).

Instead of writing `private int id;` and `private LocalDateTime createdAt;` inside every single class, we extract them into an abstract `BaseEntity` class.

**Why this helps:**
- **Code Reusability (DRY - Don't Repeat Yourself):** We write the fields, constructor logic, and getters once. Subclasses inherit them automatically.
- **Single Point of Maintenance:** If we ever want to add standard auditing fields (like `lastModifiedAt` or `isDeleted`), we only update `BaseEntity` instead of modifying dozens of models.
- **Consistent Identity:** Every model in the application shares a uniform way of accessing its ID and creation timestamp.

---

## 2. Role Hierarchy & Constructor Chaining (`super()`)

Our user hierarchy represents three clear roles on the auction platform:

```text
       BaseEntity
           ▲
           │ (extends)
         User
     ▲     ▲     ▲
     │     │     │ (extends)
   Admin Seller Bidder
```

### How `super()` Constructor Chaining Works
When a child object is instantiated, the Java Virtual Machine ensures the parent class is initialized first.
- In `Admin(int userId, String name, String email, String password)`:
  - We call `super(userId, name, email, password);`
  - Inside `User`, the constructor calls `super(userId);` which initializes the `BaseEntity` fields (`id` and `createdAt`).
  - Then `User` sets `name`, `email`, and `password`.
  - Finally, control returns to `Admin`.

This ensures all inherited fields are properly initialized and encapsulated without child classes needing direct access to private parent variables.

---

## 3. Abstract Classes vs. Interfaces

Understanding when to use an abstract class versus an interface is one of the most important design skills in Java:

| Aspect | Abstract Class (`BaseEntity`, `User`) | Interface (`BidStrategy`, `BiddingService`) |
| :--- | :--- | :--- |
| **State / Fields** | Can hold instance fields (e.g. `id`, `name`, `createdAt`). | Cannot hold instance state; only `static final` constants. |
| **Methods** | Can have both concrete methods and abstract methods. | Primarily defines abstract contracts (default methods allowed since Java 8). |
| **Inheritance** | Single inheritance (a class can extend only one class). | Multiple inheritance (a class can implement multiple interfaces). |
| **Purpose** | Defines **what an object IS** (identity and shared state). | Defines **what an object CAN DO** (behavior contract). |

In our system:
- `User` is an **abstract class** because an Admin *is* a User with identity, credentials, and state.
- `BidStrategy` is an **interface** because it specifies a swappable algorithm contract (`calculateBid`) without carrying entity state.

---

## 4. Runtime Polymorphism (Dynamic Method Dispatch)

Polymorphism means "many forms". Runtime polymorphism occurs when a call to an overridden method is resolved at runtime rather than compile-time.

### Example in our system:
We declare a reference of type `User`:
```java
User user = new Admin(1, "Admin Raj", "admin@auction.com", "pass");
user.displayRole();
System.out.println(user.getRoleDescription());
```

Even though the variable `user` is typed as the parent class `User`, the JVM looks at the **actual object created in heap memory** (`Admin`) and runs `Admin`'s overridden methods.

If we loop through an array:
```java
User[] users = { new Admin(...), new Seller(...), new Bidder(...) };
for (User u : users) {
    u.displayRole(); // Executes Admin's, Seller's, or Bidder's version dynamically
}
```
This allows us to write general code that works with any current or future user role without modifying our loops or display handlers.

---

## 5. Strategy Pattern with `BidStrategy`

Different users or auction participants prefer different bidding styles:
1. **Manual Bidding (`ManualBidStrategy`):** The bidder types their exact offer. The strategy verifies that the offer meets the minimum increment (`currentBid + minIncrement`) and places it directly.
2. **Auto / Proxy Bidding (`AutoBidStrategy`):** The bidder provides a maximum budget ceiling. Rather than jumping directly to their maximum, the strategy calculates the minimum required step to outbid the current price without exceeding the bidder's ceiling.

### Why this is powerful:
Our `BidEngineService` only interacts with the `BidStrategy` interface:
```java
public BigDecimal processBid(int auctionId, Bidder bidder, BidStrategy strategy, BigDecimal amount)
```
We can swap `ManualBidStrategy` and `AutoBidStrategy` seamlessly at runtime. If we later want to add an `AuctionSniperStrategy` or `PercentageIncreaseStrategy`, we can simply implement `BidStrategy` without altering `BidEngineService`.

---

## 6. Method Overloading vs. Method Overriding

Both are forms of polymorphism, but they behave differently:

| Feature | Method Overloading (Compile-Time) | Method Overriding (Run-Time) |
| :--- | :--- | :--- |
| **Location** | Within the same class. | Between a parent class and a subclass. |
| **Method Name** | Same name. | Same name. |
| **Parameters** | **Must differ** (different types, number, or order). | **Must be identical** (same signature). |
| **Resolution** | Resolved at **compile-time** based on argument types. | Resolved at **run-time** based on actual object instance. |
| **Annotation** | None. | `@Override` (recommended and best practice). |

### Overloading Example (`PaymentService`):
```java
public boolean pay(int auctionId, Bidder bidder, BigDecimal amount) { ... }
public boolean pay(int auctionId, Bidder bidder, BigDecimal amount, String paymentMethod) { ... }
public boolean pay(int auctionId, Bidder bidder, BigDecimal amount, String paymentMethod, String promoCode) { ... }
```
Here, the compiler decides which `pay` method to call based on how many arguments we supply.

### Overriding Example (`User` -> `Admin`, `Seller`, `Bidder`):
```java
// In User:
public abstract String getRoleDescription();

// In Admin:
@Override
public String getRoleDescription() {
    return "Admin: Oversees the platform...";
}
```
Here, the JVM decides which implementation to execute at runtime based on the actual object in memory.
