package com.auction.service;

import com.auction.model.Bidder;

import java.math.BigDecimal;

/**
 * PaymentService demonstrates Method Overloading (Compile-Time Polymorphism).
 *
 * Method Overloading rules demonstrated:
 * 1. Same method name ('pay') within the same class.
 * 2. Different parameter list (different number of arguments or different types).
 * 3. Resolved at compile-time based on static argument types.
 */
public class PaymentService {

    /**
     * Overload 1: Basic payment by auctionId, bidder, and amount.
     * Default mode: Direct account wallet settlement.
     */
    public boolean pay(int auctionId, Bidder bidder, BigDecimal amount) {
        System.out.println("[Payment - Wallet] Debited Rs." + amount +
                " from bidder '" + bidder.getName() + "' for Auction #" + auctionId);
        return true;
    }

    /**
     * Overload 2: Payment with custom payment method (e.g. "UPI", "CREDIT_CARD", "NET_BANKING").
     */
    public boolean pay(int auctionId, Bidder bidder, BigDecimal amount, String paymentMethod) {
        System.out.println("[Payment - " + paymentMethod + "] Debited Rs." + amount +
                " from bidder '" + bidder.getName() + "' for Auction #" + auctionId);
        return true;
    }

    /**
     * Overload 3: Payment with payment method and a promo / discount voucher code.
     */
    public boolean pay(int auctionId, Bidder bidder, BigDecimal amount, String paymentMethod, String promoCode) {
        System.out.println("[Payment - " + paymentMethod + " + Promo: " + promoCode +
                "] Debited Rs." + amount + " from bidder '" + bidder.getName() + "' for Auction #" + auctionId);
        return true;
    }
}
