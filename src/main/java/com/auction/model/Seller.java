package com.auction.model;

/**
 * Seller extends User — INHERITANCE in action.
 *
 * The Seller creates auctions and lists products.
 * Seller inherits userId, name, email, password from User.
 *
 * POLYMORPHISM: displayRole() is OVERRIDDEN to print "Seller".
 */
public class Seller extends User {

    public Seller(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    /**
     * OVERRIDES User.displayRole().
     * Same method name as Admin.displayRole() but different output.
     * This is POLYMORPHISM — same method, different behaviour.
     */
    @Override
    public void displayRole() {
        System.out.println("Role: Seller | Name: " + getName());
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