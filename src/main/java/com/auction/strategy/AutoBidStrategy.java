package com.auction.strategy;

import com.auction.model.Auction;
import com.auction.model.Bidder;

import java.math.BigDecimal;

/**
 * AutoBidStrategy implements BidStrategy for automated / proxy bidding.
 *
 * Training Demonstration:
 * Demonstrates a basic proxy-bidding concept:
 * - The bidder provides a maximum budget ceiling (maxLimit).
 * - Instead of immediately jumping to maxLimit, the strategy computes the lowest valid
 *   incremental bid needed to outbid the current price (current bid + min increment).
 * - If the required amount exceeds maxLimit, bidding stops (returns BigDecimal.ZERO).
 * Note: Clearly labeled as a training demonstration for OOP strategy pattern.
 */
public class AutoBidStrategy implements BidStrategy {

    @Override
    public BigDecimal calculateBid(Auction auction, Bidder bidder, BigDecimal maxLimit) {
        if (auction == null || maxLimit == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal minRequired;
        if (auction.getCurrentBid() == null || auction.getCurrentBid().compareTo(BigDecimal.ZERO) == 0) {
            minRequired = auction.getStartingPrice();
        } else {
            minRequired = auction.getCurrentBid().add(auction.getMinimumIncrement());
        }

        // If the minimum step required is within the bidder's maximum budget ceiling
        if (minRequired.compareTo(maxLimit) <= 0) {
            return minRequired;
        }

        // Maximum budget ceiling exceeded
        return BigDecimal.ZERO;
    }

    @Override
    public String getStrategyName() {
        return "Auto / Proxy Bid Strategy (Incremental up to budget ceiling)";
    }
}
