package com.auction.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Bidder extends User — INHERITANCE in action.
 *
 * Bidder places bids on auctions and maintains a personal watchlist.
 *
 * POLYMORPHISM: displayRole() is OVERRIDDEN to print "Bidder".
 *
 * COLLECTIONS — Set<Integer> watchlist:
 *   A HashSet is used because:
 *   - A bidder should NOT watch the same auction twice (Set prevents duplicates).
 *   - We only store auction IDs (Integer), not full Auction objects.
 *   - HashSet gives O(1) average time for add/contains operations.
 */
public class Bidder extends User {

    /**
     * WHY Set<Integer>?
     *   - Set does NOT allow duplicate entries.
     *   - If a bidder adds the same auction ID twice, it is stored only once.
     *   - HashSet is used for fast O(1) lookup.
     */
    private Set<Integer> watchlist;

    public Bidder(int userId, String name, String email, String password) {
        super(userId, name, email, password);
        this.watchlist = new HashSet<>();  // empty HashSet — no auctions watched yet
    }

    /**
     * OVERRIDES User.displayRole().
     * Same method, different behaviour — that is POLYMORPHISM.
     */
    @Override
    public void displayRole() {
        System.out.println("Role: Bidder | Name: " + getName());
    }

    /**
     * Adds an auction ID to the watchlist.
     * If the same ID is added again, HashSet ignores the duplicate silently.
     *
     * @param auctionId the auction ID to watch
     */
    public void addToWatchlist(int auctionId) {
        watchlist.add(auctionId);
    }

    /**
     * Returns the bidder's watchlist.
     *
     * @return Set<Integer> of watched auction IDs
     */
    public Set<Integer> getWatchlist() {
        return watchlist;
    }

    /**
     * Checks if the bidder is watching a given auction.
     *
     * @param auctionId the ID to check
     * @return true if the auction is in the watchlist
     */
    public boolean isWatching(int auctionId) {
        return watchlist.contains(auctionId);
    }

    /**
     * Prints a bid-placed confirmation message.
     * Bidder-specific — Admin and Seller do NOT have this.
     *
     * @param auctionId the auction that was bid on
     */
    public void placeBidAction(int auctionId) {
        System.out.println("[Bidder] " + getName() + " placed a bid on Auction ID: " + auctionId);
    }
}