package com.auction.model;

import com.auction.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Auction represents a product listed for bidding by a Seller.
 *
 * COLLECTIONS — List<Bid> bidHistory:
 *   ArrayList is used because:
 *   - Bid history is ORDERED — bids must appear in chronological sequence.
 *   - Duplicates are allowed (multiple bids can exist at any valid amount).
 *   - ArrayList.add() is O(1) amortised — fast for appending new bids.
 *   - We iterate over all bids to display history — ArrayList supports fast iteration.
 *
 * WHY BigDecimal for prices?
 *   All monetary fields use BigDecimal to avoid floating-point precision errors.
 */
public class Auction {

    // Auction IDs start at 100 and auto-increment
    private static int idCounter = 100;

    private int           auctionId;
    private String        productName;
    private String        description;
    private Seller        seller;

    // All monetary values use BigDecimal — never double/float for money
    private BigDecimal    startingPrice;
    private BigDecimal    reservePrice;      // minimum the seller will accept
    private BigDecimal    minimumIncrement;  // smallest allowed bid increase
    private BigDecimal    currentBid;        // tracks the highest bid so far

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;            // PENDING -> APPROVED -> LIVE -> CLOSED/UNSOLD

    /**
     * WHY List<Bid>?
     *   - Order matters: bids stored in chronological order.
     *   - ArrayList allows fast append and fast iteration.
     *   - Duplicates allowed (multiple bids per auction).
     */
    private List<Bid> bidHistory;

    // ── Constructor ──────────────────────────────────────────────────────────
    public Auction(String productName,
                   String description,
                   Seller seller,
                   BigDecimal startingPrice,
                   BigDecimal reservePrice,
                   BigDecimal minimumIncrement,
                   LocalDateTime startTime,
                   LocalDateTime endTime) {

        this.auctionId        = idCounter++;
        this.productName      = productName;
        this.description      = description;
        this.seller           = seller;
        this.startingPrice    = startingPrice;
        this.reservePrice     = reservePrice;
        this.minimumIncrement = minimumIncrement;
        this.currentBid       = startingPrice;   // bidding starts at starting price
        this.startTime        = startTime;
        this.endTime          = endTime;
        this.status           = AuctionStatus.PENDING;  // always starts as PENDING
        this.bidHistory       = new ArrayList<>();       // empty — no bids placed yet
    }

    // ── Bid history operations ────────────────────────────────────────────────

    /**
     * Adds a valid bid to the history and updates the current highest bid.
     *
     * @param bid the Bid object to store
     */
    public void addBid(Bid bid) {
        bidHistory.add(bid);           // List.add() — append to end of list
        currentBid = bid.getAmount();  // update running highest bid
    }

    /** Returns all bids placed on this auction. */
    public List<Bid> getBidHistory() {
        return bidHistory;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int            getAuctionId()        { return auctionId;        }
    public String         getProductName()      { return productName;      }
    public String         getDescription()      { return description;      }
    public Seller         getSeller()           { return seller;           }
    public BigDecimal     getStartingPrice()    { return startingPrice;    }
    public BigDecimal     getReservePrice()     { return reservePrice;     }
    public BigDecimal     getMinimumIncrement() { return minimumIncrement; }
    public BigDecimal     getCurrentBid()       { return currentBid;       }
    public LocalDateTime  getStartTime()        { return startTime;        }
    public LocalDateTime  getEndTime()          { return endTime;          }
    public AuctionStatus  getStatus()           { return status;           }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setStatus(AuctionStatus status)      { this.status     = status;     }
    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }

    // ── Display helper ────────────────────────────────────────────────────────
    /** Prints a formatted summary of this auction to the console. */
    public void displaySummary() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        System.out.println("-------------------------------------------------");
        System.out.println("  Auction ID   : " + auctionId);
        System.out.println("  Product      : " + productName);
        System.out.println("  Description  : " + description);
        System.out.println("  Seller       : " + seller.getName());
        System.out.println("  Starting Bid : Rs." + startingPrice.toPlainString());
        System.out.println("  Reserve Price: Rs." + reservePrice.toPlainString());
        System.out.println("  Min Increment: Rs." + minimumIncrement.toPlainString());
        System.out.println("  Current Bid  : Rs." + currentBid.toPlainString());
        System.out.println("  Start Time   : " + startTime.format(fmt));
        System.out.println("  End Time     : " + endTime.format(fmt));
        System.out.println("  Status       : " + status);
        System.out.println("-------------------------------------------------");
    }

    @Override
    public String toString() {
        return "Auction[id=" + auctionId + ", product=" + productName + ", status=" + status + "]";
    }
}