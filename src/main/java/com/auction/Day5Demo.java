package com.auction;

import com.auction.model.*;
import com.auction.service.AuctionService;
import com.auction.service.BidEngineService;
import com.auction.service.PaymentService;
import com.auction.strategy.AutoBidStrategy;
import com.auction.strategy.BidStrategy;
import com.auction.strategy.ManualBidStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Day5Demo — Interactive & automated demonstration of Day 5 concepts:
 * 1. BaseEntity and inheritance with super() constructor chaining.
 * 2. Polymorphic user role dispatch via User references.
 * 3. BidStrategy interface with ManualBidStrategy and AutoBidStrategy.
 * 4. BidEngineService processing bids polymorphically via BidStrategy interface.
 * 5. Method overloading in PaymentService (pay() variations).
 * 6. Abstract class vs Interface practical contrast.
 */
public class Day5Demo {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   ONLINE AUCTION SYSTEM — DAY 5 DEMO");
        System.out.println("   Inheritance, Polymorphism, Interfaces, and Method Overloading");
        System.out.println("======================================================================\n");

        // ── 1. BaseEntity & Inheritance with super() Chaining ────────────────
        System.out.println("--- 1. BaseEntity & Role Hierarchy (Inheritance) ---");
        User adminUser = new Admin(101, "Admin Vikram", "vikram@auction.com", "pass123");
        User sellerUser = new Seller(102, "Seller Anita", "anita@auction.com", "pass123");
        User bidderUser = new Bidder(103, "Bidder Rohan", "rohan@auction.com", "pass123");

        System.out.println("Created users inheriting BaseEntity (id, createdAt):");
        System.out.println("Admin entity id: " + adminUser.getId() + " | Created at: " + adminUser.getCreatedAt());
        System.out.println("Seller entity id: " + sellerUser.getId() + " | Created at: " + sellerUser.getCreatedAt());
        System.out.println("Bidder entity id: " + bidderUser.getId() + " | Created at: " + bidderUser.getCreatedAt());
        System.out.println();

        // ── 2. Runtime Polymorphism via User references ──────────────────────
        System.out.println("--- 2. Runtime Polymorphism (User reference -> Overridden methods) ---");
        User[] users = { adminUser, sellerUser, bidderUser };
        for (User u : users) {
            System.out.print("[Polymorphic Call] ");
            u.displayRole();
            System.out.println("  Description: " + u.getRoleDescription());
        }
        System.out.println();

        // ── 3. Strategy Pattern (BidStrategy interface & implementations) ────
        System.out.println("--- 3. BidStrategy Interface & Strategy Pattern ---");
        AuctionService auctionService = new AuctionService();
        BidEngineService bidEngine = new BidEngineService(auctionService);

        Seller seller = (Seller) sellerUser;
        Bidder bidder = (Bidder) bidderUser;

        // Create an auction and approve it to make it LIVE
        Auction item = auctionService.createAuction(
                "Vintage Mechanical Watch",
                "Rare 1968 mechanical chronograph in mint condition",
                seller,
                new BigDecimal("5000.00"), // starting price
                new BigDecimal("7000.00"), // reserve price
                new BigDecimal("500.00"),  // minimum increment
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction(); // Transitions PENDING -> APPROVED -> LIVE

        System.out.println("\nInitial Live Auction Status:");
        System.out.println("  Title: " + item.getProductName());
        System.out.println("  Starting Price: Rs." + item.getStartingPrice());
        System.out.println("  Current Bid: Rs." + item.getCurrentBid());
        System.out.println("  Minimum Increment: Rs." + item.getMinimumIncrement());

        try {
            // Demonstration of ManualBidStrategy
            System.out.println("\n[Strategy 1: ManualBidStrategy]");
            BidStrategy manualStrategy = new ManualBidStrategy();
            bidEngine.processBid(item.getAuctionId(), bidder, manualStrategy, new BigDecimal("5500.00"));
            System.out.println("  Updated Current Bid: Rs." + item.getCurrentBid());

            // Demonstration of AutoBidStrategy
            System.out.println("\n[Strategy 2: AutoBidStrategy]");
            BidStrategy autoStrategy = new AutoBidStrategy();
            // Bidder sets a maximum budget limit of Rs. 8000. Auto strategy will increment by minimum increment.
            bidEngine.processBid(item.getAuctionId(), bidder, autoStrategy, new BigDecimal("8000.00"));
            System.out.println("  Updated Current Bid: Rs." + item.getCurrentBid());

        } catch (Exception e) {
            System.out.println("Error processing bid: " + e.getMessage());
        }
        System.out.println();

        // ── 4. Method Overloading in PaymentService ───────────────────────────
        System.out.println("--- 4. Method Overloading (Compile-Time Polymorphism) ---");
        PaymentService paymentService = new PaymentService();
        BigDecimal settlementAmount = item.getCurrentBid();

        System.out.println("Demonstrating 3 overloaded pay() methods:");
        // Overload 1: pay(int, Bidder, BigDecimal)
        paymentService.pay(item.getAuctionId(), bidder, settlementAmount);

        // Overload 2: pay(int, Bidder, BigDecimal, String)
        paymentService.pay(item.getAuctionId(), bidder, settlementAmount, "UPI");

        // Overload 3: pay(int, Bidder, BigDecimal, String, String)
        paymentService.pay(item.getAuctionId(), bidder, settlementAmount, "CREDIT_CARD", "DIWALI_FEST2026");
        System.out.println();

        // ── 5. Contrast: Abstract Class vs Interface ─────────────────────────
        System.out.println("--- 5. Abstract Class vs Interface Practical Contrast ---");
        System.out.println("1. Abstract Class (BaseEntity / User):");
        System.out.println("   - Encapsulates shared state (fields: id, createdAt, name, email).");
        System.out.println("   - Provides concrete helper methods (getId(), getCreatedAt()) alongside abstract contracts (displayRole()).");
        System.out.println("   - Subclass can only extend one abstract class (single inheritance).");
        System.out.println("2. Interface (BidStrategy / BiddingService):");
        System.out.println("   - Defines behavioral contracts (calculateBid(), getStrategyName()) without instance state.");
        System.out.println("   - Allows loose coupling and hot-swappable strategies (ManualBidStrategy, AutoBidStrategy).");
        System.out.println("   - A class can implement multiple interfaces simultaneously.");
        System.out.println("\n======================================================================");
        System.out.println("   DAY 5 DEMONSTRATION COMPLETE SUCCESS");
        System.out.println("======================================================================");
    }
}
