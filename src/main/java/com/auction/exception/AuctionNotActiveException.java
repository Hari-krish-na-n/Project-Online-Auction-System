package com.auction.exception;

/**
 * AuctionNotActiveException — CHECKED EXCEPTION (extends Exception).
 *
 * WHY checked?
 *   - The compiler FORCES the caller to handle it with try-catch or declare throws.
 *   - Bidding on an inactive auction is a foreseeable, recoverable situation.
 *   - Using a checked exception signals: "you MUST handle this case".
 *
 * Thrown when a bidder tries to bid on an auction that is NOT in LIVE status.
 */
public class AuctionNotActiveException extends Exception {

    public AuctionNotActiveException(String message) {
        super(message);
    }
}