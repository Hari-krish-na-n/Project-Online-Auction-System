package com.auction;

import com.auction.model.Auction;
import com.auction.model.Bidder;
import com.auction.model.Seller;
import com.auction.model.User;
import com.auction.repository.CrudRepository;
import com.auction.repository.InMemoryRepository;
import com.auction.util.AuctionCsvLoader;
import com.auction.util.AuditSessionResource;
import com.auction.util.GenericUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying Day 9 requirements:
 * 1. Generic CrudRepository & InMemoryRepository operations for User and Auction
 * 2. Generic Pair<K, V> container
 * 3. Bounded Generic Box<T extends Number>
 * 4. Generic max() method with <T extends Comparable<T>>
 * 5. PECS (Producer Extends, Consumer Super) demonstrations
 * 6. CSV parsing with try-with-resources (valid, empty, malformed input)
 * 7. AutoCloseable resource management and closing order
 */
class Day9GenericsTest {

    private Seller testSeller;
    private Bidder testBidder1;
    private Bidder testBidder2;

    @BeforeEach
    void setUp() {
        testSeller = new Seller(1, "Seller Ravi", "ravi@auction.com", "pass");
        testBidder1 = new Bidder(10, "Bidder Arjun", "arjun@auction.com", "pass");
        testBidder2 = new Bidder(20, "Bidder Sneha", "sneha@auction.com", "pass");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION A: GENERIC REPOSITORY (CrudRepository & InMemoryRepository)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Generic Repository: Reused for User entity")
    void testUserRepositoryOperations() {
        CrudRepository<User, Integer> userRepo = new InMemoryRepository<>(User::getUserId);

        // Save
        userRepo.save(testSeller);
        userRepo.save(testBidder1);
        assertEquals(2, userRepo.count());

        // FindById
        Optional<User> found = userRepo.findById(10);
        assertTrue(found.isPresent());
        assertEquals("Bidder Arjun", found.get().getName());

        // FindById non-existent
        Optional<User> missing = userRepo.findById(999);
        assertTrue(missing.isEmpty());

        // FindAll
        List<User> all = userRepo.findAll();
        assertEquals(2, all.size());

        // Upsert / Duplicate ID behavior
        Bidder updatedBidder = new Bidder(10, "Bidder Arjun Updated", "arjun.new@auction.com", "pass");
        userRepo.save(updatedBidder);
        assertEquals(2, userRepo.count(), "Save with existing ID must update without increasing count");
        assertEquals("Bidder Arjun Updated", userRepo.findById(10).get().getName());

        // DeleteById
        assertTrue(userRepo.deleteById(10));
        assertFalse(userRepo.existsById(10));
        assertFalse(userRepo.deleteById(10), "Deleting non-existent ID returns false");
        assertEquals(1, userRepo.count());
    }

    @Test
    @DisplayName("Generic Repository: Reused for Auction entity")
    void testAuctionRepositoryOperations() {
        CrudRepository<Auction, Integer> auctionRepo = new InMemoryRepository<>(Auction::getAuctionId);

        Auction a1 = new Auction(101, "Laptop", "desc", testSeller, new BigDecimal("40000"), new BigDecimal("50000"), new BigDecimal("1000"), LocalDateTime.now(), LocalDateTime.now().plusDays(2));
        Auction a2 = new Auction(102, "Monitor", "desc", testSeller, new BigDecimal("15000"), new BigDecimal("20000"), new BigDecimal("500"), LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        auctionRepo.save(a1);
        auctionRepo.save(a2);

        assertEquals(2, auctionRepo.count());
        assertTrue(auctionRepo.existsById(101));
        assertTrue(auctionRepo.existsById(102));

        assertEquals(a1, auctionRepo.findById(101).orElse(null));
        assertTrue(auctionRepo.deleteById(101));
        assertEquals(1, auctionRepo.count());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION B: GENERIC UTILITY EXAMPLES & PECS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Generic Utility: Pair<K, V> holds two differently typed values")
    void testGenericPair() {
        GenericUtils.Pair<String, Integer> userScore = GenericUtils.Pair.of("Arjun", 95);
        assertEquals("Arjun", userScore.getFirst());
        assertEquals(95, userScore.getSecond());

        GenericUtils.Pair<Integer, BigDecimal> bidPair = GenericUtils.Pair.of(101, new BigDecimal("25000.00"));
        assertEquals(101, bidPair.getFirst());
        assertEquals(new BigDecimal("25000.00"), bidPair.getSecond());
    }

    @Test
    @DisplayName("Generic Utility: Box<T extends Number> accepts numeric types only")
    void testBoundedBox() {
        GenericUtils.Box<Integer> intBox = new GenericUtils.Box<>(100);
        GenericUtils.Box<Double> doubleBox = new GenericUtils.Box<>(75.5);
        GenericUtils.Box<BigDecimal> bigDecimalBox = new GenericUtils.Box<>(new BigDecimal("120.00"));

        assertEquals(100, intBox.getValue());
        assertEquals(75.5, doubleBox.getValue());
        assertTrue(intBox.isGreaterThan(doubleBox));
        assertTrue(bigDecimalBox.isGreaterThan(intBox));
    }

    @Test
    @DisplayName("Generic Utility: max() method with <T extends Comparable<T>>")
    void testGenericMax() {
        // With Strings
        assertEquals("Zebra", GenericUtils.max("Apple", "Zebra"));

        // With Integers
        assertEquals(42, GenericUtils.max(15, 42));

        // With BigDecimal
        BigDecimal b1 = new BigDecimal("1500.50");
        BigDecimal b2 = new BigDecimal("1200.00");
        assertEquals(b1, GenericUtils.max(b1, b2));
    }

    @Test
    @DisplayName("Generic Utility: PECS Rule (Producer Extends, Consumer Super)")
    void testPECSDemo() {
        // 1. Producer Extends (? extends Number) — read from list
        List<Integer> intList = List.of(10, 20, 30);
        List<Double> doubleList = List.of(1.5, 2.5, 3.0);
        assertEquals(60.0, GenericUtils.sumOfNumbers(intList), 0.001);
        assertEquals(7.0, GenericUtils.sumOfNumbers(doubleList), 0.001);

        // 2. Consumer Super (? super Integer) — write to collection
        List<Number> numberList = new ArrayList<>();
        GenericUtils.populateIntegers(numberList, 3);
        assertEquals(List.of(1, 2, 3), numberList);

        List<Object> objectList = new ArrayList<>();
        GenericUtils.populateIntegers(objectList, 2);
        assertEquals(List.of(1, 2), objectList);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SECTION C: TRY-WITH-RESOURCES & AUTOCLOSEABLE
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("try-with-resources: CSV Loader parses valid data and reports malformed rows cleanly")
    void testCsvLoaderWithTryWithResources() throws Exception {
        String csvContent =
                "id,productName,startingPrice,reservePrice,minIncrement\n" +
                "101,Sony Camera,45000.00,50000.00,1000.00\n" +
                "102,Bose Headphones,15000.00,18000.00,500.00\n" +
                "103,Bad Price,-200.00,500.00,50.00\n" +         // Negative start price
                "104,Bad Reserve,1000.00,500.00,50.00\n" +       // Reserve < start
                "Malformed row without columns\n";                // Missing columns

        AuctionCsvLoader loader = new AuctionCsvLoader();
        AuctionCsvLoader.ParseResult result = loader.loadAuctions(new StringReader(csvContent), testSeller);

        // Valid auctions parsed: 101 and 102
        assertEquals(2, result.getAuctions().size());
        assertEquals("Sony Camera", result.getAuctions().get(0).getProductName());
        assertEquals("Bose Headphones", result.getAuctions().get(1).getProductName());

        // Errors reported clearly without throwing or halting execution
        assertTrue(result.hasErrors());
        assertEquals(3, result.getErrorMessages().size());
    }

    @Test
    @DisplayName("try-with-resources: Empty and missing CSV inputs handled cleanly")
    void testCsvLoaderEmptyInput() throws Exception {
        AuctionCsvLoader loader = new AuctionCsvLoader();
        AuctionCsvLoader.ParseResult result = loader.loadAuctions(new StringReader(""), testSeller);
        assertEquals(0, result.getAuctions().size());
        assertFalse(result.hasErrors());
    }

    @Test
    @DisplayName("AutoCloseable: Verifies deterministic closing and resource lifecycle")
    void testAutoCloseableOrder() {
        List<String> eventLog = new ArrayList<>();

        try (AuditSessionResource res1 = new AuditSessionResource("SessionA", eventLog);
             AuditSessionResource res2 = new AuditSessionResource("SessionB", eventLog)) {

            res1.logAction("Login");
            res2.logAction("BidPlaced");

        } // Implicit close() occurs here in REVERSE order of declaration: SessionB then SessionA

        assertEquals(List.of(
                "OPEN:SessionA",
                "OPEN:SessionB",
                "ACTION:SessionA:Login",
                "ACTION:SessionB:BidPlaced",
                "CLOSE:SessionB",
                "CLOSE:SessionA"
        ), eventLog, "Resources in try-with-resources must be closed in reverse order of declaration");
    }
}
