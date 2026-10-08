package com.auction.model;

/**
 * Admin extends User — INHERITANCE in action.
 *
 * Admin is responsible for approving or rejecting auctions.
 * Admin gets all fields of User for free (userId, name, email, password).
 *
 * POLYMORPHISM: displayRole() is OVERRIDDEN here to print "Admin".
 */
public class Admin extends User {

    public Admin(int userId, String name, String email, String password) {
        // super() calls the parent (User) constructor
        super(userId, name, email, password);
    }

    /**
     * OVERRIDES User.displayRole().
     * When called on an Admin object, this version runs — not User's.
     */
    @Override
    public void displayRole() {
        System.out.println("Role: Admin | Name: " + getName());
    }

    /**
     * Prints a confirmation message when admin approves an auction.
     * This method is Admin-only — Seller and Bidder do NOT have it.
     *
     * @param auctionId the ID of the auction being approved
     */
    public void approveAction(int auctionId) {
        System.out.println("[Admin] " + getName() + " approved Auction ID: " + auctionId);
    }
}