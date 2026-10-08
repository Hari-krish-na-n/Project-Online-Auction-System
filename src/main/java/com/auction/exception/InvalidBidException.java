package com.auction.exception;

/**
 * InvalidBidException — UNCHECKED EXCEPTION (extends RuntimeException).
 *
 * WHY unchecked?
 *   - RuntimeException does NOT force the caller to use try-catch.
 *   - An invalid bid is a programming/input error — the bidder should not
 *     have been allowed to submit it in the first place.
 *   - This signals a programming mistake or deliberate bad input.
 *
 * Thrown when:
 *   1. Bid amount is below the minimum required (currentBid + increment).
 *   2. Bidder tries to bid on their own auction (if they are also a seller).
 *   3. Bid amount is zero or negative.
 */
public class InvalidBidException extends RuntimeException {

    public InvalidBidException(String message) {
        super(message);
    }
}