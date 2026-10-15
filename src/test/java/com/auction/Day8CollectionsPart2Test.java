package com.auction;

import com.auction.comparator.AuctionComparators;
import com.auction.enums.AuctionStatus;
import com.auction.exception.AuctionNotActiveException;
import com.auction.exception.InvalidBidException;
import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.User;
import com.auction.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive automated test suite verifying Day 8 requirements:
 * 1. Map implementations: HashMap, LinkedHashMap, TreeMap, get(), put(), containsKey(), getOrDefault(), remove(), merge()
 * 2. Comparable (natural ordering by ID) and Comparator (amount descending, chained comparators, tie breaking)
 * 3. equals() and hashCode() contract, deterministic missing-hashCode demonstration
 * 4. FR4: Valid bidding (status checks, increment validation, atomic updates, exception handling)
 * 5. FR5: Proxy / automatic bidding (max limit enforcement, competing proxy bidders, deterministic tie breaking)
 * 6. FR6: Anti-sniping boundaries, final-2-minute window, repeated extensions
 */
class Day8CollectionsPart2Test {

    private AuctionService auctionService;
    private Seller seller;
    private Bidder bidder1;
    private Bidder bidder2;
    private Bidder bidder3;

    @BeforeEach
    void setUp() {
        auctionService = new AuctionService();
        seller = new Seller(10, "Seller John", "john@auction.com", "pass");
        bidder1 = new Bidder(21, "Alice", "alice@auction.com", "pass");
        bidder2 = new Bidder(22, "Bob", "bob@auction.com", "pass");
        bidder3 = new Bidder(23, "Charlie", "charlie@auction.com", "pass");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION A: MAP IMPLEMENTATIONS & OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Map: HashMap lookup, containsKey, getOrDefault, remove")
    void testMapBasicOperations() {
        Auction auction = auctionService.createAuction(
                "MacBook Air M2", "13-inch, 256GB SSD", seller,
                new BigDecimal("80000.00"), new BigDecimal("90000.00"), new BigDecimal("1000.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(3)
        );

        // findAuctionById uses HashMap get()
        Auction found = auctionService.findAuctionById(auction.getAuctionId());
        assertNotNull(found);
        assertEquals("MacBook Air M2", found.getProductName());

        // Proxy map put(), containsKey(), getOrDefault(), remove()
        auctionService.configureProxyBid(auction.getAuctionId(), bidder1, new BigDecimal("85000.00"));
        assertEquals(new BigDecimal("85000.00"), auctionService.getProxyBidLimit(auction.getAuctionId(), bidder1.getUserId()));
        assertEquals(BigDecimal.ZERO, auctionService.getProxyBidLimit(auction.getAuctionId(), 9999), "Unconfigured bidder returns default 0");

        boolean removed = auctionService.removeProxyBid(auction.getAuctionId(), bidder1.getUserId());
        assertTrue(removed);
        assertEquals(BigDecimal.ZERO, auctionService.getProxyBidLimit(auction.getAuctionId(), bidder1.getUserId()));
    }

    @Test
    @DisplayName("Map: LinkedHashMap preserves exact insertion order of activity log")
    void testLinkedHashMapPreservesInsertionOrder() throws Exception {
        Auction auction = auctionService.createAuction(
                "Camera", "Mirrorless", seller,
                new BigDecimal("50000.00"), new BigDecimal("60000.00"), new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction();

        auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("51000.00"));
        auctionService.placeBid(auction.getAuctionId(), bidder2, new BigDecimal("52000.00"));
        auctionService.placeBid(auction.getAuctionId(), bidder3, new BigDecimal("53000.00"));

        Map<Integer, Bid> activityLog = auctionService.getActivityLog();
        List<BigDecimal> loggedAmounts = new ArrayList<>();
        for (Bid b : activityLog.values()) {
            loggedAmounts.add(b.getAmount());
        }

        // LinkedHashMap guarantees exact sequence: 51000 -> 52000 -> 53000
        assertEquals(3, loggedAmounts.size());
        assertEquals(new BigDecimal("51000.00"), loggedAmounts.get(0));
        assertEquals(new BigDecimal("52000.00"), loggedAmounts.get(1));
        assertEquals(new BigDecimal("53000.00"), loggedAmounts.get(2));
    }

    @Test
    @DisplayName("Map: TreeMap produces key-sorted auction reports")
    void testTreeMapSorting() {
        Auction a3 = new Auction(300, "Tablet", "desc", seller, new BigDecimal("100"), new BigDecimal("150"), new BigDecimal("10"), LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Auction a1 = new Auction(100, "Phone", "desc", seller, new BigDecimal("100"), new BigDecimal("150"), new BigDecimal("10"), LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Auction a2 = new Auction(200, "Laptop", "desc", seller, new BigDecimal("100"), new BigDecimal("150"), new BigDecimal("10"), LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        Map<Integer, Auction> map = new HashMap<>();
        map.put(a3.getAuctionId(), a3);
        map.put(a1.getAuctionId(), a1);
        map.put(a2.getAuctionId(), a2);

        SortedMap<Integer, Auction> sorted = new TreeMap<>(map);
        List<Integer> sortedKeys = new ArrayList<>(sorted.keySet());

        assertEquals(List.of(100, 200, 300), sortedKeys, "TreeMap must order keys naturally ascending");
    }

    @Test
    @DisplayName("Map: merge() aggregates bid frequencies per bidder")
    void testMapMergeAggregation() throws Exception {
        Auction auction = auctionService.createAuction(
                "Smartwatch", "desc", seller,
                new BigDecimal("5000.00"), new BigDecimal("6000.00"), new BigDecimal("200.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction();

        auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("5200.00"));
        auctionService.placeBid(auction.getAuctionId(), bidder2, new BigDecimal("5400.00"));
        auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("5600.00"));
        auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("5800.00"));

        Map<String, Integer> counts = auctionService.countBidsPerBidder();
        assertEquals(3, counts.get("Alice"));
        assertEquals(1, counts.get("Bob"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION B: COMPARABLE & COMPARATOR
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Comparable: Natural ordering by ID for Auction and Bid")
    void testNaturalOrderingComparable() {
        Auction a1 = new Auction(101, "Item A", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Auction a2 = new Auction(102, "Item B", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
        assertEquals(0, a1.compareTo(a1));

        Bid b1 = new Bid(1, 101, bidder1, new BigDecimal("500.00"), LocalDateTime.now());
        Bid b2 = new Bid(2, 101, bidder2, new BigDecimal("600.00"), LocalDateTime.now());
        assertTrue(b1.compareTo(b2) < 0);
    }

    @Test
    @DisplayName("Comparator: Bid amount descending with tie breaking by bidId")
    void testBidComparatorDescending() {
        Bid b1 = new Bid(1, 101, bidder1, new BigDecimal("500.00"), LocalDateTime.now());
        Bid b2 = new Bid(2, 101, bidder2, new BigDecimal("700.00"), LocalDateTime.now());
        Bid b3 = new Bid(3, 101, bidder3, new BigDecimal("500.00"), LocalDateTime.now());

        List<Bid> bids = new ArrayList<>(List.of(b1, b2, b3));
        bids.sort(AuctionComparators.BID_AMOUNT_DESCENDING);

        // Expect: b2 (700), then b1 (500, bidId 1), then b3 (500, bidId 3)
        assertEquals(b2, bids.get(0));
        assertEquals(b1, bids.get(1));
        assertEquals(b3, bids.get(2));
    }

    @Test
    @DisplayName("Comparator: Chained comparator (currentBid desc, then auctionId asc)")
    void testChainedComparator() {
        Auction a1 = new Auction(101, "Phone", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        a1.setCurrentBid(new BigDecimal("15000.00"));

        Auction a2 = new Auction(102, "Laptop", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        a2.setCurrentBid(new BigDecimal("45000.00"));

        Auction a3 = new Auction(103, "Monitor", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        a3.setCurrentBid(new BigDecimal("15000.00")); // Same bid as a1, but higher ID

        List<Auction> list = new ArrayList<>(List.of(a1, a2, a3));
        list.sort(AuctionComparators.HIGHEST_BID_DESCENDING_THEN_ID_ASCENDING);

        // a2 has highest bid (45000) -> index 0
        assertEquals(a2, list.get(0));
        // a1 and a3 tied at 15000 -> tie-breaker by auctionId ascending -> a1 (101) then a3 (103)
        assertEquals(a1, list.get(1));
        assertEquals(a3, list.get(2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION C: EQUALS & HASHCODE CONTRACT
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("equals() and hashCode() Contract: reflexive, symmetric, consistent with HashSet")
    void testEqualsAndHashCodeContract() {
        Auction a1 = new Auction(105, "Drone", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Auction a2 = new Auction(105, "Drone Updated Title", "desc modified", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        Auction a3 = new Auction(106, "Drone", "desc", seller, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        // Reflexive
        assertEquals(a1, a1);
        // Symmetric
        assertEquals(a1, a2);
        assertEquals(a2, a1);
        // Equal objects MUST have equal hashCode
        assertEquals(a1.hashCode(), a2.hashCode());

        // Non-equal
        assertNotEquals(a1, a3);

        // HashSet behavior with equal objects
        Set<Auction> set = new HashSet<>();
        set.add(a1);
        set.add(a2);
        assertEquals(1, set.size(), "HashSet must eliminate duplicate equal objects");
        assertTrue(set.contains(a2));
    }

    @Test
    @DisplayName("equals() and hashCode(): Demonstrates consequence of missing hashCode() contract")
    void testMissingHashCodeContractDemonstration() {
        // Deterministic demonstration: Class that implements equals() but NOT hashCode()
        class BadEntity {
            final int id;
            BadEntity(int id) { this.id = id; }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                BadEntity that = (BadEntity) o;
                return id == that.id;
            }
            // Missing hashCode() -> inherits System.identityHashCode from Object
        }

        BadEntity e1 = new BadEntity(42);
        BadEntity e2 = new BadEntity(42);

        // Objects are equal according to equals()
        assertEquals(e1, e2);

        // But in a HashMap/HashSet, e2 will frequently NOT be found when looked up with e1!
        Map<BadEntity, String> badMap = new HashMap<>();
        badMap.put(e1, "Stored with e1");

        // Contract check: if hashCodes differ, HashMap search looks in different buckets
        if (e1.hashCode() != e2.hashCode()) {
            assertNull(badMap.get(e2), "Contract violation: equal objects with different hashCodes cannot be retrieved from HashMap bucket");
        }

        // Corrected Entity implementing BOTH equals and hashCode
        class GoodEntity {
            final int id;
            GoodEntity(int id) { this.id = id; }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                GoodEntity that = (GoodEntity) o;
                return id == that.id;
            }
            @Override
            public int hashCode() {
                return Integer.hashCode(id);
            }
        }

        GoodEntity g1 = new GoodEntity(42);
        GoodEntity g2 = new GoodEntity(42);
        Map<GoodEntity, String> goodMap = new HashMap<>();
        goodMap.put(g1, "Stored with g1");

        // Guaranteed to succeed
        assertEquals("Stored with g1", goodMap.get(g2), "Corrected contract guarantees retrieval in HashMap");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION D: FR4 — VALID BIDDING
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FR4: Valid bidding succeeds and updates highest bid atomically")
    void testFR4ValidBidding() throws Exception {
        Auction auction = auctionService.createAuction(
                "Gaming Console", "Next gen", seller,
                new BigDecimal("40000.00"), new BigDecimal("45000.00"), new BigDecimal("1000.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction(); // Make LIVE

        // Bid meets startingPrice + minIncrement = 40000 + 1000 = 41000
        auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("41000.00"));
        assertEquals(new BigDecimal("41000.00"), auction.getCurrentBid());
        assertEquals(1, auction.getBidHistory().size());
    }

    @Test
    @DisplayName("FR4: Rejects bids below minimum increment or negative amounts")
    void testFR4InvalidBids() {
        Auction auction = auctionService.createAuction(
                "Console", "Next gen", seller,
                new BigDecimal("40000.00"), new BigDecimal("45000.00"), new BigDecimal("1000.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction();

        // 1. Bid below current (40000) + increment (1000) = 41000
        InvalidBidException ex1 = assertThrows(InvalidBidException.class, () ->
                auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("40500.00"))
        );
        assertTrue(ex1.getMessage().contains("Bid too low"));
        assertEquals(new BigDecimal("40000.00"), auction.getCurrentBid(), "Highest bid must not change on rejected bid");

        // 2. Negative bid
        assertThrows(InvalidBidException.class, () ->
                auctionService.placeBid(auction.getAuctionId(), bidder1, new BigDecimal("-100.00"))
        );

        // 3. Seller bidding on own auction
        Bidder sellerAsBidder = new Bidder(seller.getUserId(), "Seller John", "john@auction.com", "pass");
        InvalidBidException ex3 = assertThrows(InvalidBidException.class, () ->
                auctionService.placeBid(auction.getAuctionId(), sellerAsBidder, new BigDecimal("50000.00"))
        );
        assertTrue(ex3.getMessage().contains("cannot bid on your own auction"));
    }

    @Test
    @DisplayName("FR4: Rejects bids on non-active or unapproved auctions")
    void testFR4InactiveAuction() {
        Auction pending = auctionService.createAuction(
                "Pending Item", "desc", seller,
                new BigDecimal("1000.00"), new BigDecimal("1500.00"), new BigDecimal("100.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );

        // Still PENDING, not approved
        AuctionNotActiveException ex = assertThrows(AuctionNotActiveException.class, () ->
                auctionService.placeBid(pending.getAuctionId(), bidder1, new BigDecimal("1200.00"))
        );
        assertTrue(ex.getMessage().contains("not active"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION E: FR5 — PROXY / AUTOMATIC BIDDING
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FR5: Proxy bidding increments up to maximum and enforces ceiling")
    void testFR5ProxyBiddingLimits() throws Exception {
        Auction auction = auctionService.createAuction(
                "Rare Book", "First edition", seller,
                new BigDecimal("1000.00"), new BigDecimal("2000.00"), new BigDecimal("100.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction();

        // Bidder1 configures proxy limit of 1500
        auctionService.configureProxyBid(auction.getAuctionId(), bidder1, new BigDecimal("1500.00"));
        auctionService.executeProxyBids(auction.getAuctionId(), List.of(bidder1));

        // Placed first minimum increment: 1000 + 100 = 1100
        assertEquals(new BigDecimal("1100.00"), auction.getCurrentBid());
        assertEquals(bidder1.getUserId(), auction.getBidHistory().get(0).getBidder().getUserId());

        // Bidder2 manual bid of 1300
        auctionService.placeBid(auction.getAuctionId(), bidder2, new BigDecimal("1300.00"));
        assertEquals(new BigDecimal("1300.00"), auction.getCurrentBid());

        // Re-execute proxy bids for bidder1: should raise to 1300 + 100 = 1400
        auctionService.executeProxyBids(auction.getAuctionId(), List.of(bidder1));
        assertEquals(new BigDecimal("1400.00"), auction.getCurrentBid());

        // Bidder2 manual bid of 1600 (exceeds bidder1's max limit 1500)
        auctionService.placeBid(auction.getAuctionId(), bidder2, new BigDecimal("1600.00"));
        assertEquals(new BigDecimal("1600.00"), auction.getCurrentBid());

        // Bidder1 proxy should NOT exceed 1500 limit
        auctionService.executeProxyBids(auction.getAuctionId(), List.of(bidder1));
        assertEquals(new BigDecimal("1600.00"), auction.getCurrentBid(), "Proxy must not exceed max ceiling");
    }

    @Test
    @DisplayName("FR5: Competing proxy bidders increment until lower ceiling reached with tie breaking")
    void testFR5CompetingProxyBidders() throws Exception {
        Auction auction = auctionService.createAuction(
                "Antique Clock", "19th century", seller,
                new BigDecimal("5000.00"), new BigDecimal("8000.00"), new BigDecimal("500.00"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );
        auctionService.approveNextAuction();

        // Bidder1 sets max 7000
        auctionService.configureProxyBid(auction.getAuctionId(), bidder1, new BigDecimal("7000.00"));
        // Bidder2 sets max 8500
        auctionService.configureProxyBid(auction.getAuctionId(), bidder2, new BigDecimal("8500.00"));

        // Execute competition
        auctionService.executeProxyBids(auction.getAuctionId(), List.of(bidder1, bidder2));

        // Competing proxy bids will escalate until Bidder1's limit 7000 is reached.
        // Bidder2 with ceiling 8500 matches Bidder1's 7000, holding the winning bid.
        assertEquals(new BigDecimal("7000.00"), auction.getCurrentBid());
        Bid winningBid = auction.getBidHistory().get(auction.getBidHistory().size() - 1);
        assertEquals(bidder2.getUserId(), winningBid.getBidder().getUserId(), "Bidder2 with higher ceiling must win");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION F: FR6 — ANTI-SNIPING EXTENSIONS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FR6: Valid bid placed within final 2 minutes triggers 5-minute extension")
    void testFR6AntiSnipingWithinTwoMinutes() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 10, 15, 12, 0);
        LocalDateTime auctionEnd = baseTime.plusHours(1); // Ends at 13:00

        Auction auction = new Auction(
                201, "Guitar", "Electric", seller,
                new BigDecimal("10000.00"), new BigDecimal("12000.00"), new BigDecimal("500.00"),
                baseTime, auctionEnd
        );

        // Bid arrived at 12:58:30 (1.5 minutes before end -> within 2 min window)
        LocalDateTime bidTimeInsideWindow = auctionEnd.minusSeconds(90);
        boolean extended = auctionService.applyAntiSniping(auction, bidTimeInsideWindow);

        assertTrue(extended);
        assertEquals(auctionEnd.plusMinutes(5), auction.getEndTime(), "Deadline must be extended by 5 minutes");
    }

    @Test
    @DisplayName("FR6: Bid outside final 2 minutes does NOT trigger extension")
    void testFR6AntiSnipingOutsideWindow() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 10, 15, 12, 0);
        LocalDateTime auctionEnd = baseTime.plusHours(1); // Ends at 13:00

        Auction auction = new Auction(
                202, "Violin", "Classical", seller,
                new BigDecimal("10000.00"), new BigDecimal("12000.00"), new BigDecimal("500.00"),
                baseTime, auctionEnd
        );

        // Bid arrived at 12:55:00 (5 minutes before end -> outside 2 min window)
        LocalDateTime bidTimeOutsideWindow = auctionEnd.minusMinutes(5);
        boolean extended = auctionService.applyAntiSniping(auction, bidTimeOutsideWindow);

        assertFalse(extended);
        assertEquals(auctionEnd, auction.getEndTime(), "Deadline must remain unchanged");
    }

    @Test
    @DisplayName("FR6: Supports repeated anti-sniping extensions on subsequent late bids")
    void testFR6RepeatedAntiSnipingExtensions() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 10, 15, 12, 0);
        LocalDateTime initialEnd = baseTime.plusHours(1); // 13:00

        Auction auction = new Auction(
                203, "Piano", "Grand", seller,
                new BigDecimal("50000.00"), new BigDecimal("60000.00"), new BigDecimal("1000.00"),
                baseTime, initialEnd
        );

        // First late bid: at 12:59 -> extends to 13:05
        assertTrue(auctionService.applyAntiSniping(auction, initialEnd.minusMinutes(1)));
        assertEquals(initialEnd.plusMinutes(5), auction.getEndTime()); // 13:05

        // Second late bid: at 13:04 (within 2 minutes of the new 13:05 deadline) -> extends to 13:10
        LocalDateTime secondLateBidTime = auction.getEndTime().minusMinutes(1);
        assertTrue(auctionService.applyAntiSniping(auction, secondLateBidTime));
        assertEquals(initialEnd.plusMinutes(10), auction.getEndTime(), "Repeated extension must advance deadline again");
    }
}
