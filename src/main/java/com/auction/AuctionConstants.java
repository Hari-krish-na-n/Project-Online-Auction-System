package com.auction;

/**
 * AuctionConstants represents the business rules and constraints
 * for the Online Auction System.
 *
 * Demonstrates:
 * - Constants using public static final
 * - Eliminating "magic numbers" across the codebase
 * - Centralized business configuration
 */
public final class AuctionConstants {

    // ----------------------------------------------------------------
    // Bidding Rules
    // ----------------------------------------------------------------

    /** Default minimum increment required between consecutive bids */
    public static final double DEFAULT_MINIMUM_INCREMENT = 500.0;

    /** Window in minutes before auction close where anti-sniping activates */
    public static final int ANTI_SNIPE_WINDOW_MINUTES = 2;

    /** Extension duration in minutes when a bid is received in anti-snipe window */
    public static final int ANTI_SNIPE_EXTENSION_MINUTES = 5;

    // ----------------------------------------------------------------
    // Financial Rules
    // ----------------------------------------------------------------

    /** Platform service commission percentage */
    public static final double PLATFORM_COMMISSION_PERCENTAGE = 5.0;

    /** Minimum allowable starting price for an auction */
    public static final double MINIMUM_STARTING_PRICE = 100.0;

    /** Grade A / Premium auction threshold */
    public static final double PREMIUM_AUCTION_THRESHOLD = 100_000.0;

    // ----------------------------------------------------------------
    // Limits
    // ----------------------------------------------------------------

    /** Maximum active listings allowed concurrently per seller */
    public static final int MAX_ACTIVE_AUCTIONS_PER_SELLER = 10;

    /** Maximum watchlist capacity per bidder */
    public static final int MAX_WATCHLIST_CAPACITY = 50;

    private AuctionConstants() {
        // Utility class: prevent instantiation
    }
}
