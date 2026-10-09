package com.auction.service;

import com.auction.exception.AuctionNotActiveException;
import com.auction.exception.InvalidBidException;
import com.auction.model.Auction;
import com.auction.model.Bidder;
import com.auction.strategy.BidStrategy;

import java.math.BigDecimal;

/**
 * BidEngineService demonstrates dependency inversion and polymorphism using BidStrategy.
 *
 * It accepts any BidStrategy implementation without depending directly on concrete classes.
 */
public class BidEngineService {

    private final AuctionService auctionService;

    public BidEngineService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    /**
     * Executes a bid evaluation and placement using a supplied BidStrategy.
     *
     * @param auctionId target auction ID
     * @param bidder the bidding user
     * @param strategy the selected strategy (ManualBidStrategy or AutoBidStrategy)
     * @param amountOrLimit the target bid amount or maximum budget limit
     * @return placed bid amount
     * @throws AuctionNotActiveException if auction is not LIVE
     */
    public BigDecimal processBid(int auctionId, Bidder bidder, BidStrategy strategy, BigDecimal amountOrLimit)
            throws AuctionNotActiveException {
        Auction auction = auctionService.findAuctionById(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction with ID " + auctionId + " not found.");
        }

        System.out.println("-> Applying Strategy: " + strategy.getStrategyName());
        BigDecimal calculatedBid = strategy.calculateBid(auction, bidder, amountOrLimit);

        if (calculatedBid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Strategy evaluated bid as invalid or exceeding limits (Provided: Rs." + amountOrLimit + ")");
        }

        auctionService.placeBid(auctionId, bidder, calculatedBid);
        System.out.println("-> Strategy placed bid successfully: Rs." + calculatedBid);
        return calculatedBid;
    }
}
