package com.auction.strategy;

import com.auction.model.Auction;
import com.auction.model.Bidder;

import java.math.BigDecimal;

/**
 * BidStrategy is an interface representing a bidding algorithm or strategy.
 *
 * INTERFACE DESIGN & POLYMORPHISM:
 * - Defines a common contract for different bidding mechanisms (Manual vs Auto/Proxy).
 * - Client code (such as BidEngineService) depends on this interface rather than concrete implementations.
 * - Training demonstration: Keeps business logic transparent and interchangeable.
 */
public interface BidStrategy {

    /**
     * Determines and returns the next bid amount according to this strategy.
     *
     * @param auction the target auction
     * @param bidder the bidder applying the strategy
     * @param inputAmount the amount specified or the maximum ceiling budget
     * @return the calculated bid amount, or BigDecimal.ZERO if no valid bid can be placed
     */
    BigDecimal calculateBid(Auction auction, Bidder bidder, BigDecimal inputAmount);

    /**
     * Returns the name/description of this bidding strategy.
     *
     * @return strategy name
     */
    String getStrategyName();
}
