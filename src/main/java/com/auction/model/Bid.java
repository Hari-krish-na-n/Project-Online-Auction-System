package com.auction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bid represents a single bid placed by a Bidder on an Auction.
 *
 * WHY BigDecimal for amount?
 *   - double/float have precision errors: 0.1 + 0.2 = 0.30000000000000004
 *   - For money, BigDecimal gives exact decimal precision.
 *   - Always use BigDecimal when dealing with currency values.
 *
 * Bid objects are stored inside List<Bid> bidHistory inside the Auction class.
 */
public class Bid implements Comparable<Bid> {

    // auto-increments across all Bid objects created
    private static int idCounter = 1;

    private int           bidId;
    private int           auctionId;
    private Bidder        bidder;     // who placed this bid
    private BigDecimal    amount;     // how much (exact money value)
    private LocalDateTime timestamp;  // when the bid was placed

    // ── Constructor ──────────────────────────────────────────────────────────
    public Bid(int auctionId, Bidder bidder, BigDecimal amount) {
        this.bidId     = idCounter++;
        this.auctionId = auctionId;
        this.bidder    = bidder;
        this.amount    = amount;
        this.timestamp = LocalDateTime.now();
    }

    public Bid(int bidId, int auctionId, Bidder bidder, BigDecimal amount, LocalDateTime timestamp) {
        this.bidId     = bidId;
        this.auctionId = auctionId;
        this.bidder    = bidder;
        this.amount    = amount;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int           getBidId()     { return bidId;     }
    public int           getAuctionId() { return auctionId; }
    public Bidder        getBidder()    { return bidder;    }
    public BigDecimal    getAmount()    { return amount;    }
    public LocalDateTime getTimestamp() { return timestamp; }

    /**
     * Natural ordering for Bid: by bidId ascending (chronological placement sequence).
     */
    @Override
    public int compareTo(Bid other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(this.bidId, other.bidId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid bid = (Bid) o;
        return bidId == bid.bidId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(bidId);
    }

    // ── toString — used when printing bid history ─────────────────────────────
    @Override
    public String toString() {
        String time = timestamp.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        return String.format("  Bid #%d | Bidder: %-15s | Amount: Rs.%-10s | Time: %s",
                bidId, bidder != null ? bidder.getName() : "Anonymous", amount.toPlainString(), time);
    }
}