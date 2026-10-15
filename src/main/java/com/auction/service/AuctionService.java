package com.auction.service;

import com.auction.enums.AuctionStatus;
import com.auction.exception.AuctionNotActiveException;
import com.auction.exception.InvalidBidException;
import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.model.Bidder;
import com.auction.model.Seller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * AuctionService is the central service class.
 * It implements BiddingService (the interface) and handles all auction logic.
 *
 * COLLECTIONS USED:
 *
 *   Map<Integer, Auction> auctions
 *     WHY HashMap?
 *       - Stores auctions with auctionId as the KEY.
 *       - O(1) lookup by ID: auctions.get(101) finds instantly.
 *       - No duplicates for the same auction ID.
 *
 *   Queue<Auction> pendingQueue
 *     WHY Queue (LinkedList)?
 *       - Auctions wait for admin approval in FIFO order.
 *       - First auction submitted = first auction reviewed.
 *       - Queue.poll() removes from the front automatically.
 */
public class AuctionService implements BiddingService {

    /**
     * Map<Integer, Auction> auctions:
     * - HashMap used for fast O(1) lookup of auctions by auctionId.
     */
    private final Map<Integer, Auction> auctions = new HashMap<>();

    /**
     * Queue<Auction> pendingQueue:
     * - LinkedList implements Queue for FIFO moderation.
     */
    private final Queue<Auction> pendingQueue = new LinkedList<>();

    /**
     * LinkedHashMap<Integer, Bid> activityLog:
     * - LinkedHashMap preserves insertion order for chronological audit trail of all accepted bids.
     */
    private final Map<Integer, Bid> activityLog = new LinkedHashMap<>();

    /**
     * Map of Proxy Bids:
     * - Key: auctionId, Value: LinkedHashMap mapping bidderId -> maximumProxyAmount
     * - Preserves proxy registration order (earlier proxy wins on tie).
     */
    private final Map<Integer, Map<Integer, BigDecimal>> proxyBids = new HashMap<>();

    // ── CREATE AUCTION ────────────────────────────────────────────────────────
    /**
     * Creates a new auction and places it in the pending approval queue.
     *
     * @return the newly created Auction
     */
    public Auction createAuction(String productName,
                                 String description,
                                 Seller seller,
                                 BigDecimal startingPrice,
                                 BigDecimal reservePrice,
                                 BigDecimal minimumIncrement,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime) {

        Auction auction = new Auction(productName, description, seller,
                startingPrice, reservePrice, minimumIncrement, startTime, endTime);

        // Place in the pending queue — Admin must approve it
        pendingQueue.add(auction);

        // Map put() operation: store auction by ID
        auctions.put(auction.getAuctionId(), auction);

        System.out.println("\n[SUCCESS] Auction created! ID: " + auction.getAuctionId()
                + " | Status: PENDING (awaiting admin approval)");
        return auction;
    }

    // ── VIEW PENDING AUCTIONS ─────────────────────────────────────────────────
    /**
     * Displays all auctions currently waiting for admin approval.
     * Does NOT remove them from the queue.
     */
    public void displayPendingAuctions() {
        if (pendingQueue.isEmpty()) {
            System.out.println("  No auctions pending approval.");
            return;
        }
        System.out.println("\n  Auctions pending approval (" + pendingQueue.size() + "):");
        for (Auction a : pendingQueue) {
            System.out.printf("  [ID: %d] %-20s | Seller: %s%n",
                    a.getAuctionId(), a.getProductName(), a.getSeller().getName());
        }
    }

    // ── APPROVE AUCTION ───────────────────────────────────────────────────────
    /**
     * Admin approves the NEXT auction in the pending queue (FIFO).
     * Status changes to APPROVED, then immediately to LIVE.
     *
     * @return the approved auction, or null if queue is empty
     */
    public Auction approveNextAuction() {
        Auction auction = pendingQueue.poll(); // removes from front of queue
        if (auction == null) {
            System.out.println("  No auctions in the pending queue.");
            return null;
        }
        auction.setStatus(AuctionStatus.APPROVED);
        System.out.println("  [APPROVED] " + auction.getProductName()
                + " (ID: " + auction.getAuctionId() + ")");

        // Immediately mark it LIVE so bidders can start bidding
        auction.setStatus(AuctionStatus.LIVE);
        System.out.println("  [LIVE] Auction is now LIVE — bidders can place bids!");
        return auction;
    }

    /**
     * Admin approves a specific auction by its ID.
     *
     * @param auctionId the ID of the auction to approve
     */
    public void approveAuctionById(int auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            System.out.println("  Auction ID " + auctionId + " not found.");
            return;
        }
        if (auction.getStatus() != AuctionStatus.PENDING) {
            System.out.println("  Auction is not in PENDING state. Current status: " + auction.getStatus());
            return;
        }
        // Remove from pending queue
        pendingQueue.remove(auction);
        auction.setStatus(AuctionStatus.LIVE);
        System.out.println("  [APPROVED & LIVE] Auction ID " + auctionId
                + " (" + auction.getProductName() + ") is now LIVE!");
    }

    // ── DISPLAY AUCTIONS ──────────────────────────────────────────────────────
    /** Displays ALL auctions (all statuses). */
    public void displayAllAuctions() {
        if (auctions.isEmpty()) {
            System.out.println("  No auctions in the system yet.");
            return;
        }
        System.out.println("\n  All Auctions (" + auctions.size() + "):");
        for (Auction a : auctions.values()) {
            a.displaySummary();
        }
    }

    /** Displays only LIVE auctions — for bidders to browse. */
    public void displayLiveAuctions() {
        boolean found = false;
        System.out.println();
        for (Auction a : auctions.values()) {
            if (a.getStatus() == AuctionStatus.LIVE) {
                a.displaySummary();
                found = true;
            }
        }
        if (!found) {
            System.out.println("  No live auctions available right now.");
        }
    }

    /** Displays auctions created by a specific seller. */
    public void displaySellerAuctions(Seller seller) {
        boolean found = false;
        System.out.println("\n  Your Auctions:");
        for (Auction a : auctions.values()) {
            if (a.getSeller().getUserId() == seller.getUserId()) {
                a.displaySummary();
                found = true;
            }
        }
        if (!found) {
            System.out.println("  You have not created any auctions yet.");
        }
    }

    // ── FIND AUCTION ──────────────────────────────────────────────────────────
    /**
     * Finds an auction by its ID.
     *
     * @param auctionId the ID to look up
     * @return the Auction, or null if not found
     */
    public Auction findAuction(int auctionId) {
        return auctions.get(auctionId); // Map.get() — O(1) lookup
    }

    public Auction findAuctionById(int auctionId) {
        return findAuction(auctionId);
    }

    // ── PLACE BID (BiddingService interface) ─────────────────────────────────
    /**
     * Places a bid on an auction.
     *
     * Validates:
     *   1. Auction exists.
     *   2. Auction is LIVE           — throws AuctionNotActiveException (CHECKED).
     *   3. Bidder is not the seller  — throws InvalidBidException (UNCHECKED).
     *   4. Bid meets the minimum     — throws InvalidBidException (UNCHECKED).
     *
     * @param auctionId the auction to bid on
     * @param bidder    the bidder placing the bid
     * @param amount    the bid amount
     * @throws AuctionNotActiveException if auction is not LIVE
     * @throws InvalidBidException       if bid is invalid
     */
    @Override
    public void placeBid(int auctionId, Bidder bidder, BigDecimal amount)
            throws AuctionNotActiveException {

        // Map containsKey check
        if (!auctions.containsKey(auctionId)) {
            throw new AuctionNotActiveException("Auction ID " + auctionId + " does not exist.");
        }

        // Map get() operation
        Auction auction = auctions.get(auctionId);

        // ── Check 2: Is the auction LIVE? (CHECKED EXCEPTION) ────────────────
        if (auction.getStatus() != AuctionStatus.LIVE) {
            throw new AuctionNotActiveException(
                    "Auction '" + auction.getProductName() + "' is not active. "
                    + "Current status: " + auction.getStatus());
        }

        // ── Check 3: Bidder cannot bid on their own auction (UNCHECKED) ───────
        if (auction.getSeller().getUserId() == bidder.getUserId()) {
            throw new InvalidBidException(
                    "You cannot bid on your own auction: " + auction.getProductName());
        }

        // ── Check 4: Bid amount must be positive (UNCHECKED) ─────────────────
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Bid amount must be greater than zero.");
        }

        // ── Check 5: Bid must meet minimum required amount (UNCHECKED) ────────
        BigDecimal minimumRequired = auction.getCurrentBid()
                .add(auction.getMinimumIncrement());

        if (amount.compareTo(minimumRequired) < 0) {
            throw new InvalidBidException(
                    "Bid too low! Minimum required: Rs." + minimumRequired.toPlainString()
                    + " | You bid: Rs." + amount.toPlainString());
        }

        // ── All checks passed — record the bid atomically ────────────────────
        Bid bid = new Bid(auctionId, bidder, amount);
        auction.addBid(bid);  // adds to List<Bid> and updates currentBid

        // LinkedHashMap insertion: preserves chronological activity history order
        activityLog.put(bid.getBidId(), bid);

        // FR6 — Anti-Sniping Rule:
        // If a valid bid is placed within final 2 minutes of auction end, extend by 5 minutes
        applyAntiSniping(auction, LocalDateTime.now());

        bidder.placeBidAction(auctionId); // polymorphic call on Bidder
        System.out.println("  [BID PLACED] Rs." + amount.toPlainString()
                + " by " + bidder.getName()
                + " on '" + auction.getProductName() + "'");
    }

    /**
     * FR6: Anti-sniping calculation.
     * If a bid occurs within ANTI_SNIPE_WINDOW_MINUTES before auction endTime,
     * the deadline is extended by ANTI_SNIPE_EXTENSION_MINUTES.
     *
     * @param auction the auction to check and potentially extend
     * @param bidTime the timestamp when the bid occurred
     * @return true if the auction was extended, false otherwise
     */
    public boolean applyAntiSniping(Auction auction, LocalDateTime bidTime) {
        if (auction == null || auction.getEndTime() == null || bidTime == null) {
            return false;
        }

        LocalDateTime snipeThreshold = auction.getEndTime().minusMinutes(com.auction.AuctionConstants.ANTI_SNIPE_WINDOW_MINUTES);
        // Condition: bidTime is on or after (endTime - 2 min) and before/at endTime
        if (!bidTime.isBefore(snipeThreshold) && !bidTime.isAfter(auction.getEndTime())) {
            LocalDateTime newEndTime = auction.getEndTime().plusMinutes(com.auction.AuctionConstants.ANTI_SNIPE_EXTENSION_MINUTES);
            auction.setEndTime(newEndTime);
            System.out.println("  [ANTI-SNIPING TRIGGERED] Bid arrived in final "
                    + com.auction.AuctionConstants.ANTI_SNIPE_WINDOW_MINUTES
                    + " minutes! Deadline extended by "
                    + com.auction.AuctionConstants.ANTI_SNIPE_EXTENSION_MINUTES
                    + " minutes to: " + newEndTime);
            return true;
        }
        return false;
    }

    // ── GET BID HISTORY (BiddingService interface) ────────────────────────────
    /**
     * Returns the complete bid history for an auction.
     *
     * @param auctionId the auction ID
     * @return List<Bid> — ordered list of all bids
     */
    @Override
    public List<Bid> getBidHistory(int auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return new ArrayList<>(); // return empty list — never return null
        }
        return auction.getBidHistory();
    }

    // ── CLOSE AUCTION & DETERMINE WINNER ─────────────────────────────────────
    /**
     * Closes an auction and determines the winner.
     *
     * If the highest bid >= reserve price → CLOSED (winner announced).
     * If the highest bid <  reserve price → UNSOLD (reserve not met).
     *
     * @param auctionId the auction to close
     */
    public void closeAuction(int auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            System.out.println("  Auction ID " + auctionId + " not found.");
            return;
        }
        if (auction.getStatus() != AuctionStatus.LIVE) {
            System.out.println("  Only LIVE auctions can be closed. Status: " + auction.getStatus());
            return;
        }

        determineWinner(auction);
    }

    /**
     * Determines the winner of an auction based on bid history.
     * Called internally by closeAuction().
     *
     * @param auction the auction to evaluate
     */
    private void determineWinner(Auction auction) {
        List<Bid> history = auction.getBidHistory();

        System.out.println("\n========================================");
        System.out.println("  AUCTION RESULT: " + auction.getProductName());
        System.out.println("========================================");

        if (history.isEmpty()) {
            // No bids were placed at all
            auction.setStatus(AuctionStatus.UNSOLD);
            System.out.println("  Result : UNSOLD");
            System.out.println("  Reason : No bids were placed.");
            System.out.println("========================================");
            return;
        }

        // The last bid in the list is the highest (each bid must be higher than the last)
        Bid winningBid = history.get(history.size() - 1);

        if (winningBid.getAmount().compareTo(auction.getReservePrice()) >= 0) {
            // Reserve price MET — auction CLOSED with a winner
            auction.setStatus(AuctionStatus.CLOSED);
            System.out.println("  Result : SOLD");
            System.out.println("  Winner : " + winningBid.getBidder().getName());
            System.out.println("  Amount : Rs." + winningBid.getAmount().toPlainString());
        } else {
            // Reserve price NOT MET — auction UNSOLD
            auction.setStatus(AuctionStatus.UNSOLD);
            System.out.println("  Result       : UNSOLD");
            System.out.println("  Reason       : Reserve price not met");
            System.out.println("  Highest Bid  : Rs." + winningBid.getAmount().toPlainString()
                    + " by " + winningBid.getBidder().getName());
            System.out.println("  Reserve Price: Rs." + auction.getReservePrice().toPlainString());
        }
        System.out.println("========================================");
    }

    // ── BIDDER: VIEW MY BIDS ──────────────────────────────────────────────────
    /**
     * Displays all bids placed by a specific bidder across all auctions.
     *
     * @param bidder the bidder whose bids to display
     */
    public void displayBidderBids(Bidder bidder) {
        boolean found = false;
        System.out.println("\n  Your Bid History:");
        for (Auction a : auctions.values()) {
            for (Bid b : a.getBidHistory()) {
                if (b.getBidder().getUserId() == bidder.getUserId()) {
                    System.out.println("  Auction: '" + a.getProductName()
                            + "' | " + b);
                    found = true;
                }
            }
        }
        if (!found) {
            System.out.println("  You have not placed any bids yet.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FR5 — PROXY / AUTOMATIC BIDDING & MAP OPERATIONS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * FR5: Configure maximum proxy-bid amount for a bidder on a specific auction.
     * Stored in Map<Integer, Map<Integer, BigDecimal>> proxyBids.
     * Demonstrates put(), containsKey(), and getOrDefault().
     */
    public void configureProxyBid(int auctionId, Bidder bidder, BigDecimal maxLimit) {
        if (!auctions.containsKey(auctionId)) {
            throw new IllegalArgumentException("Auction ID " + auctionId + " not found.");
        }
        if (bidder == null) {
            throw new IllegalArgumentException("Bidder cannot be null.");
        }
        if (maxLimit == null || maxLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Proxy bid limit must be greater than zero.");
        }

        Auction auction = auctions.get(auctionId);
        if (auction.getSeller().getUserId() == bidder.getUserId()) {
            throw new InvalidBidException("Seller cannot set proxy bid on own auction.");
        }

        BigDecimal minRequired = auction.getCurrentBid().add(auction.getMinimumIncrement());
        if (maxLimit.compareTo(minRequired) < 0) {
            throw new InvalidBidException("Proxy limit Rs." + maxLimit + " is below minimum required bid Rs." + minRequired);
        }

        // LinkedHashMap inside proxyBids preserves insertion order for tie-breaking
        Map<Integer, BigDecimal> auctionProxies = proxyBids.computeIfAbsent(auctionId, k -> new LinkedHashMap<>());
        auctionProxies.put(bidder.getUserId(), maxLimit);
        System.out.println("  [PROXY CONFIGURED] Bidder '" + bidder.getName() + "' proxy limit set to Rs." + maxLimit + " for Auction #" + auctionId);
    }

    /**
     * Retrieves the proxy-bid limit for a bidder on an auction.
     * Demonstrates getOrDefault().
     */
    public BigDecimal getProxyBidLimit(int auctionId, int bidderId) {
        Map<Integer, BigDecimal> auctionProxies = proxyBids.get(auctionId);
        if (auctionProxies == null) {
            return BigDecimal.ZERO;
        }
        return auctionProxies.getOrDefault(bidderId, BigDecimal.ZERO);
    }

    /**
     * Removes a proxy bid limit if the bidder cancels.
     * Demonstrates remove().
     */
    public boolean removeProxyBid(int auctionId, int bidderId) {
        Map<Integer, BigDecimal> auctionProxies = proxyBids.get(auctionId);
        if (auctionProxies != null && auctionProxies.containsKey(bidderId)) {
            auctionProxies.remove(bidderId);
            return true;
        }
        return false;
    }

    /**
     * Executes automatic proxy bidding between configured proxy bidders on an auction.
     * Tie-breaking rule: The bidder who configured their proxy limit earlier wins ties.
     */
    public void executeProxyBids(int auctionId, List<Bidder> knownBidders) throws AuctionNotActiveException {
        Auction auction = auctions.get(auctionId);
        if (auction == null || auction.getStatus() != AuctionStatus.LIVE) {
            return;
        }

        Map<Integer, BigDecimal> limits = proxyBids.get(auctionId);
        if (limits == null || limits.isEmpty()) {
            return;
        }

        // Bidder lookup map: HashMap O(1)
        Map<Integer, Bidder> bidderMap = new HashMap<>();
        for (Bidder b : knownBidders) {
            bidderMap.put(b.getUserId(), b);
        }

        boolean bidPlacedInRound;
        do {
            bidPlacedInRound = false;
            BigDecimal minRequired = auction.getCurrentBid().add(auction.getMinimumIncrement());

            for (Map.Entry<Integer, BigDecimal> entry : limits.entrySet()) {
                int bidderId = entry.getKey();
                BigDecimal maxLimit = entry.getValue();
                Bidder bidder = bidderMap.get(bidderId);

                if (bidder == null) continue;

                // Check if bidder is already the current highest bidder
                List<Bid> history = auction.getBidHistory();
                if (!history.isEmpty() && history.get(history.size() - 1).getBidder().getUserId() == bidderId) {
                    continue; // Already winning
                }

                if (maxLimit.compareTo(minRequired) >= 0) {
                    placeBid(auctionId, bidder, minRequired);
                    bidPlacedInRound = true;
                    break; // Re-evaluate competing bids at next price increment
                }
            }
        } while (bidPlacedInRound);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  AGGREGATION WITH merge() & ORDERED MAPS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Counts the total number of bids placed by each bidder across all auctions.
     * Demonstrates Map.merge() for frequency aggregation:
     *   map.merge(key, 1, Integer::sum);
     */
    public Map<String, Integer> countBidsPerBidder() {
        Map<String, Integer> bidCounts = new HashMap<>();
        for (Auction auction : auctions.values()) {
            for (Bid bid : auction.getBidHistory()) {
                String bidderName = bid.getBidder() != null ? bid.getBidder().getName() : "Unknown";
                bidCounts.merge(bidderName, 1, Integer::sum);
            }
        }
        return bidCounts;
    }

    /**
     * Counts total auctions per status.
     * Demonstrates Map.merge() aggregation.
     */
    public Map<AuctionStatus, Integer> countAuctionsByStatus() {
        Map<AuctionStatus, Integer> counts = new HashMap<>();
        for (Auction auction : auctions.values()) {
            counts.merge(auction.getStatus(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Returns a report of auctions sorted by auctionId using TreeMap (natural ordering).
     * Demonstrates TreeMap for key-sorted reports.
     */
    public SortedMap<Integer, Auction> getAuctionsSortedById() {
        return new TreeMap<>(auctions);
    }

    /**
     * Returns an ordered in-memory activity log of all accepted bids.
     * LinkedHashMap preserves insertion order.
     */
    public Map<Integer, Bid> getActivityLog() {
        return Collections.unmodifiableMap(activityLog);
    }
}