package com.auction.model;

/**
 * Admin extends User — INHERITANCE in action.
 *
 * Admin is responsible for approving or rejecting auctions.
 * Admin inherits common entity identity from BaseEntity and user details from User.
 *
 * POLYMORPHISM: displayRole() and getRoleDescription() are OVERRIDDEN here.
 */
public class Admin extends User {

    public Admin(int userId, String name, String email, String password) {
        // super() calls the parent (User) constructor, which chains to BaseEntity
        super(userId, name, email, password);
    }

    /**
     * OVERRIDES User.displayRole().
     * When called on an Admin object via a User reference, this version runs at runtime.
     */
    @Override
    public void displayRole() {
        System.out.println("Role: Admin | Name: " + getName());
    }

    /**
     * OVERRIDES User.getRoleDescription().
     */
    @Override
    public String getRoleDescription() {
        return "Admin: Oversees the platform, reviews submitted auctions, approves listings, and closes completed auctions.";
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