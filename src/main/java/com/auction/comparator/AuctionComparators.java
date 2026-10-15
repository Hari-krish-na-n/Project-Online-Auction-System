package com.auction.comparator;

import com.auction.model.Auction;
import com.auction.model.Bid;

import java.util.Comparator;

/**
 * Comparators for custom sorting of entities in the Online Auction System.
 *
 * Concepts Explained:
 * - Comparable defines the natural ordering of an entity (e.g. Auction by auctionId ascending, Bid by bidId ascending).
 * - Comparator provides alternative ordering strategies independent of the entity's natural ordering,
 *   such as ordering bids by monetary amount descending or generating leaderboard reports.
 * - Chained Comparators (Comparator.comparing().reversed().thenComparing()) allow multi-level sorting,
 *   such as ranking auctions by current highest bid descending, and breaking ties using auctionId ascending.
 */
public final class AuctionComparators {

    private AuctionComparators() {
        // Utility class: prevent instantiation
    }

    /**
     * Comparator ordering Bids by monetary amount in descending order (highest bid first).
     * Tie-breaking: earlier placed bids (lower bidId) take precedence if amounts are equal.
     */
    public static final Comparator<Bid> BID_AMOUNT_DESCENDING = (b1, b2) -> {
        if (b1 == b2) return 0;
        if (b1 == null) return 1;
        if (b2 == null) return -1;
        int cmp = b2.getAmount().compareTo(b1.getAmount());
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(b1.getBidId(), b2.getBidId());
    };

    /**
     * Chained Comparator for ranking Auctions:
     * 1. Current highest bid descending (highest priced auctions first)
     * 2. Auction ID ascending to break ties deterministically
     */
    public static final Comparator<Auction> HIGHEST_BID_DESCENDING_THEN_ID_ASCENDING =
            Comparator.comparing(Auction::getCurrentBid, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed()
                    .thenComparing(Auction::getAuctionId);

    /**
     * Comparator ordering Auctions by product name alphabetically (case-insensitive).
     */
    public static final Comparator<Auction> PRODUCT_NAME_INSENSITIVE =
            Comparator.comparing(Auction::getProductName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Auction::getAuctionId);
}
