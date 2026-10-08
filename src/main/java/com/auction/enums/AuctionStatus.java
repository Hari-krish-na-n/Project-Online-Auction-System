package com.auction.enums;

/**
 * AuctionStatus tracks the lifecycle of every auction.
 *
 * WHY enum?
 *   - Restricts status to only these valid values (no typos like "PENDIG").
 *   - Makes the code self-documenting and type-safe.
 *   - The compiler will warn if you forget to handle a case.
 *
 * Lifecycle:
 *   PENDING  -> Created by seller, waiting for admin review
 *   APPROVED -> Admin approved it (will be marked LIVE at open time)
 *   LIVE     -> Currently accepting bids
 *   CLOSED   -> Auction ended — reserve price was met, winner declared
 *   UNSOLD   -> Auction ended — highest bid was below reserve price
 */
public enum AuctionStatus {
    PENDING,   // just created — sitting in the approval queue
    APPROVED,  // admin approved — ready to go live
    LIVE,      // active — bidders can place bids now
    CLOSED,    // ended with a winning bid above reserve price
    UNSOLD     // ended but highest bid did not meet reserve price
}