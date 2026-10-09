package com.auction.strategy;

import com.auction.model.Auction;
import com.auction.model.Bidder;

import java.math.BigDecimal;

/**
 * ManualBidStrategy implements BidStrategy for direct, single-step manual bids.
 *
 * Training Demonstration:
 * The bidder explicitly specifies the exact bid amount they wish to offer.
 * The strategy validates that the amount meets the minimum requirement
 * (current bid + minimum increment, or starting price if no bids exist).
 */
public class ManualBidStrategy implements BidStrategy {

    @Override
    public BigDecimal calculateBid(Auction auction, Bidder bidder, BigDecimal inputAmount) {
        if (auction == null || inputAmount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal minRequired;
        if (auction.getCurrentBid() == null || auction.getCurrentBid().compareTo(BigDecimal.ZERO) == 0) {
            minRequired = auction.getStartingPrice();
        } else {
            minRequired = auction.getCurrentBid().add(auction.getMinimumIncrement());
        }

        // In manual bidding, if inputAmount is at least the required minimum, it is used directly
        if (inputAmount.compareTo(minRequired) >= 0) {
            return inputAmount;
        }

        return BigDecimal.ZERO; // does not satisfy minimum increment
    }

    @Override
    public String getStrategyName() {
        return "Manual Bid Strategy (Direct placement)";
    }
}
