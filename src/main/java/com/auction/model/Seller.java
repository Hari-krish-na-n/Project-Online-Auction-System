package com.auction.model;

/**
 * Seller extends User — INHERITANCE in action.
 *
 * The Seller creates auctions and lists products.
 * Seller inherits BaseEntity fields and User fields.
 *
 * POLYMORPHISM: displayRole() and getRoleDescription() are OVERRIDDEN.
 */
public class Seller extends User {

    public Seller(int userId, String name, String email, String password) {
        // super() chains to User and BaseEntity constructors
        super(userId, name, email, password);
    }

    /**
     * OVERRIDES User.displayRole().
     */
    @Override
    public void displayRole() {
        System.out.println("Role: Seller | Name: " + getName());
    }

    /**
     * OVERRIDES User.getRoleDescription().
     */
    @Override
    public String getRoleDescription() {
        return "Seller: Creates new auction listings, configures reserve prices and increments, and tracks item progress.";
    }

    /**
     * Prints a confirmation when the seller creates an auction.
     * Seller-specific method — Admin and Bidder do NOT have it.
     *
     * @param productName the product being listed
     */
    public void createAuctionAction(String productName) {
        System.out.println("[Seller] " + getName() + " created auction for: " + productName);
    }
}