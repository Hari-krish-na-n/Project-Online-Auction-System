package com.auction;

import com.auction.model.*;
import com.auction.service.AuctionService;
import com.auction.service.BidEngineService;
import com.auction.service.PaymentService;
import com.auction.strategy.AutoBidStrategy;
import com.auction.strategy.ManualBidStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class Day5ConceptsTest {

    private Admin admin;
    private Seller seller;
    private Bidder bidder;
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        admin = new Admin(1, "Admin test", "admin@test.com", "pass");
        seller = new Seller(2, "Seller test", "seller@test.com", "pass");
        bidder = new Bidder(3, "Bidder test", "bidder@test.com", "pass");
        auctionService = new AuctionService();
    }

    @Test
    void testBaseEntityAndInheritance() {
        // Test BaseEntity fields inherited by User and concrete roles
        assertEquals(1, admin.getId());
        assertEquals(1, admin.getUserId());
        assertNotNull(admin.getCreatedAt());

        assertEquals(2, seller.getId());
        assertNotNull(seller.getCreatedAt());

        assertEquals(3, bidder.getId());
        assertNotNull(bidder.getCreatedAt());
    }

    @Test
    void testRuntimePolymorphism() {
        User u1 = admin;
        User u2 = seller;
        User u3 = bidder;

        assertTrue(u1.getRoleDescription().contains("Admin"));
        assertTrue(u2.getRoleDescription().contains("Seller"));
        assertTrue(u3.getRoleDescription().contains("Bidder"));
    }

    @Test
    void testManualAndAutoBidStrategies() throws Exception {
        Auction auction = auctionService.createAuction(
                "Phone",
                "Flagship phone",
                seller,
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("100.00"),
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusDays(1)
        );
        auctionService.approveNextAuction();

        BidEngineService bidEngine = new BidEngineService(auctionService);

        // Manual strategy: place 1100
        BigDecimal manualBid = bidEngine.processBid(auction.getAuctionId(), bidder, new ManualBidStrategy(), new BigDecimal("1100.00"));
        assertEquals(new BigDecimal("1100.00"), manualBid);
        assertEquals(new BigDecimal("1100.00"), auction.getCurrentBid());

        // Auto strategy: max budget 2000, should place current (1100) + minIncrement (100) = 1200
        BigDecimal autoBid = bidEngine.processBid(auction.getAuctionId(), bidder, new AutoBidStrategy(), new BigDecimal("2000.00"));
        assertEquals(new BigDecimal("1200.00"), autoBid);
        assertEquals(new BigDecimal("1200.00"), auction.getCurrentBid());
    }

    @Test
    void testPaymentOverloading() {
        PaymentService paymentService = new PaymentService();
        assertTrue(paymentService.pay(10, bidder, new BigDecimal("500.00")));
        assertTrue(paymentService.pay(10, bidder, new BigDecimal("500.00"), "UPI"));
        assertTrue(paymentService.pay(10, bidder, new BigDecimal("500.00"), "CARD", "PROMO10"));
    }
}
