package com.auction.service;

import com.auction.exception.AuctionNotActiveException;
import com.auction.model.Bid;
import com.auction.model.Bidder;

import java.math.BigDecimal;
import java.util.List;

/**
 * BiddingService is an INTERFACE.
 *
 * WHY interface?
 *   - Defines a contract: any class that implements this MUST provide
 *     placeBid() and getBidHistory().
 *   - Promotes loose coupling — the console menu depends on this interface,
 *     not the concrete class.
 *   - Easy to swap implementations (e.g., for testing).
 */
public interface BiddingService {

    /**
     * Places a bid on an auction.
     *
     * @param auctionId  the auction to bid on
     * @param bidder     the bidder placing the bid
     * @param amount     the bid amount (BigDecimal for exact money)
     * @throws AuctionNotActiveException if the auction is not LIVE (checked)
     * @throws com.auction.exception.InvalidBidException if the amount is invalid (unchecked)
     */
    void placeBid(int auctionId, Bidder bidder, BigDecimal amount)
            throws AuctionNotActiveException;

    /**
     * Returns the bid history for a given auction.
     *
     * @param auctionId the auction ID
     * @return List<Bid> ordered list of all bids placed
     */
    List<Bid> getBidHistory(int auctionId);
}