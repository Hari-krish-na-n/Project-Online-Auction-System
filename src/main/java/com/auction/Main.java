package com.auction;

import com.auction.exception.AuctionNotActiveException;
import com.auction.exception.InvalidBidException;
import com.auction.model.*;
import com.auction.service.AuctionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Main.java — Entry point of the Online Auction System.
 *
 * Responsibilities:
 *   - Display the main menu (Seller / Admin / Bidder / Exit).
 *   - Read user input using Scanner.
 *   - Delegate all business logic to AuctionService.
 *   - Handle exceptions using try-catch (NOT catch Exception everywhere).
 *
 * Pre-loaded demo users so you can test without typing credentials.
 */
public class Main {

    // ── Shared Scanner (one instance — avoid resource leaks) ─────────────────
    private static final Scanner scanner = new Scanner(System.in);

    // ── Single service instance shared across all menus ───────────────────────
    private static final AuctionService auctionService = new AuctionService();

    // ── Pre-loaded demo users ─────────────────────────────────────────────────
    private static final Admin  admin   = new Admin (1, "Admin Raj",   "admin@auction.com",  "admin123");
    private static final Seller seller1 = new Seller(2, "Seller Ravi", "ravi@auction.com",   "sell123");
    private static final Seller seller2 = new Seller(3, "Seller Priya","priya@auction.com",  "sell456");
    private static final Bidder bidder1 = new Bidder(4, "Bidder Arjun","arjun@auction.com",  "bid123");
    private static final Bidder bidder2 = new Bidder(5, "Bidder Sneha","sneha@auction.com",  "bid456");

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── MAIN ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        printBanner();

        // Demonstrate polymorphism at startup
        System.out.println("  Loaded users:");
        admin.displayRole();    // prints "Role: Admin   | Name: ..."
        seller1.displayRole();  // prints "Role: Seller  | Name: ..."
        bidder1.displayRole();  // prints "Role: Bidder  | Name: ..."
        System.out.println();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1 -> sellerMenu();
                case 2 -> adminMenu();
                case 3 -> bidderMenu();
                case 4 -> {
                    System.out.println("\n  Thank you for using Online Auction System. Goodbye!\n");
                    running = false;
                }
                default -> System.out.println("  Invalid choice. Please enter 1-4.");
            }
        }
        scanner.close();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SELLER MENU
    // ═════════════════════════════════════════════════════════════════════════
    private static void sellerMenu() {
        // Seller selection
        System.out.println("\n  Select Seller:");
        System.out.println("  1. " + seller1.getName());
        System.out.println("  2. " + seller2.getName());
        int sc = readInt("  Choose seller: ");
        Seller currentSeller = (sc == 2) ? seller2 : seller1;
        currentSeller.displayRole();

        boolean back = false;
        while (!back) {
            System.out.println("\n  -------- SELLER MENU --------");
            System.out.println("  1. Create Auction");
            System.out.println("  2. View My Auctions");
            System.out.println("  3. Back");
            System.out.println("  -----------------------------");
            int choice = readInt("  Enter choice: ");

            switch (choice) {
                case 1 -> createAuction(currentSeller);
                case 2 -> auctionService.displaySellerAuctions(currentSeller);
                case 3 -> back = true;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    /** Prompts the seller to fill in all auction details and creates the auction. */
    private static void createAuction(Seller seller) {
        System.out.println("\n  --- Create New Auction ---");
        System.out.print("  Product Name      : ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  Product name cannot be empty."); return; }

        System.out.print("  Description       : ");
        String desc = scanner.nextLine().trim();

        BigDecimal startPrice   = readBigDecimal("  Starting Price (Rs.): ");
        BigDecimal reservePrice = readBigDecimal("  Reserve Price  (Rs.): ");
        BigDecimal minIncrement = readBigDecimal("  Min Increment  (Rs.): ");

        if (startPrice.compareTo(BigDecimal.ZERO) <= 0 ||
            reservePrice.compareTo(BigDecimal.ZERO) <= 0 ||
            minIncrement.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  All prices must be greater than zero.");
            return;
        }
        if (reservePrice.compareTo(startPrice) < 0) {
            System.out.println("  Reserve price cannot be less than starting price.");
            return;
        }

        LocalDateTime startTime = readDateTime("  Auction Start (dd-MM-yyyy HH:mm): ");
        LocalDateTime endTime   = readDateTime("  Auction End   (dd-MM-yyyy HH:mm): ");
        if (startTime == null || endTime == null) return;

        if (!endTime.isAfter(startTime)) {
            System.out.println("  End time must be after start time.");
            return;
        }

        auctionService.createAuction(name, desc, seller,
                startPrice, reservePrice, minIncrement, startTime, endTime);
        seller.createAuctionAction(name); // polymorphic call
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ADMIN MENU
    // ═════════════════════════════════════════════════════════════════════════
    private static void adminMenu() {
        admin.displayRole();
        boolean back = false;
        while (!back) {
            System.out.println("\n  -------- ADMIN MENU --------");
            System.out.println("  1. View Pending Auctions");
            System.out.println("  2. Approve Auction by ID");
            System.out.println("  3. Approve Next in Queue");
            System.out.println("  4. View All Auctions");
            System.out.println("  5. Close Auction (Determine Winner)");
            System.out.println("  6. Back");
            System.out.println("  ----------------------------");
            int choice = readInt("  Enter choice: ");

            switch (choice) {
                case 1 -> auctionService.displayPendingAuctions();
                case 2 -> {
                    int id = readInt("  Enter Auction ID to approve: ");
                    admin.approveAction(id);           // polymorphic call
                    auctionService.approveAuctionById(id);
                }
                case 3 -> auctionService.approveNextAuction();
                case 4 -> auctionService.displayAllAuctions();
                case 5 -> {
                    int id = readInt("  Enter Auction ID to close: ");
                    auctionService.closeAuction(id);
                }
                case 6 -> back = true;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BIDDER MENU
    // ═════════════════════════════════════════════════════════════════════════
    private static void bidderMenu() {
        // Bidder selection
        System.out.println("\n  Select Bidder:");
        System.out.println("  1. " + bidder1.getName());
        System.out.println("  2. " + bidder2.getName());
        int bc = readInt("  Choose bidder: ");
        Bidder currentBidder = (bc == 2) ? bidder2 : bidder1;
        currentBidder.displayRole();

        boolean back = false;
        while (!back) {
            System.out.println("\n  -------- BIDDER MENU --------");
            System.out.println("  1. View Live Auctions");
            System.out.println("  2. Place Bid");
            System.out.println("  3. Add Auction to Watchlist");
            System.out.println("  4. View My Watchlist");
            System.out.println("  5. View Bid History of an Auction");
            System.out.println("  6. View My Bids");
            System.out.println("  7. Back");
            System.out.println("  -----------------------------");
            int choice = readInt("  Enter choice: ");

            switch (choice) {
                case 1 -> auctionService.displayLiveAuctions();
                case 2 -> placeBid(currentBidder);
                case 3 -> {
                    int id = readInt("  Enter Auction ID to watch: ");
                    currentBidder.addToWatchlist(id);
                    System.out.println("  Auction " + id + " added to your watchlist.");
                }
                case 4 -> displayWatchlist(currentBidder);
                case 5 -> {
                    int id = readInt("  Enter Auction ID: ");
                    var history = auctionService.getBidHistory(id);
                    if (history.isEmpty()) {
                        System.out.println("  No bids placed on this auction yet.");
                    } else {
                        System.out.println("\n  Bid History for Auction " + id + ":");
                        history.forEach(System.out::println);
                    }
                }
                case 6 -> auctionService.displayBidderBids(currentBidder);
                case 7 -> back = true;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    /** Handles the place-bid flow with proper exception handling. */
    private static void placeBid(Bidder bidder) {
        int auctionId  = readInt("  Enter Auction ID to bid on: ");
        BigDecimal amt = readBigDecimal("  Enter your bid amount (Rs.): ");

        try {
            // AuctionNotActiveException is CHECKED — must use try-catch
            auctionService.placeBid(auctionId, bidder, amt);

        } catch (AuctionNotActiveException e) {
            // Checked exception: auction is not LIVE
            System.out.println("  [ERROR] " + e.getMessage());

        } catch (InvalidBidException e) {
            // Unchecked exception: bid amount is invalid
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    /** Displays the bidder's watchlist with auction details. */
    private static void displayWatchlist(Bidder bidder) {
        if (bidder.getWatchlist().isEmpty()) {
            System.out.println("  Your watchlist is empty.");
            return;
        }
        System.out.println("\n  Your Watchlist:");
        for (int auctionId : bidder.getWatchlist()) {
            var auction = auctionService.findAuction(auctionId);
            if (auction != null) {
                System.out.printf("  [ID: %d] %-20s | Status: %s | Current Bid: Rs.%s%n",
                        auctionId,
                        auction.getProductName(),
                        auction.getStatus(),
                        auction.getCurrentBid().toPlainString());
            } else {
                System.out.println("  [ID: " + auctionId + "] (Auction not found)");
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /** Reads an integer from the console, re-prompts on invalid input. */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid whole number.");
            }
        }
    }

    /** Reads a BigDecimal from the console, re-prompts on invalid input. */
    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return new BigDecimal(line);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number (e.g. 50000 or 1500.50).");
            }
        }
    }

    /** Reads a LocalDateTime in dd-MM-yyyy HH:mm format, re-prompts on bad format. */
    private static LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(line, DT_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use: dd-MM-yyyy HH:mm (e.g. 06-10-2026 10:00)");
                System.out.print("  Try again? (y/n): ");
                String yn = scanner.nextLine().trim();
                if (!yn.equalsIgnoreCase("y")) return null;
            }
        }
    }

    /** Prints the application banner. */
    private static void printBanner() {
        System.out.println();
        System.out.println("  ==========================================");
        System.out.println("       ONLINE AUCTION SYSTEM v1.0          ");
        System.out.println("       Plain Java | In-Memory | Maven       ");
        System.out.println("  ==========================================");
        System.out.println();
    }

    /** Prints the main role-selection menu. */
    private static void printMainMenu() {
        System.out.println("  ==========================================");
        System.out.println("               MAIN MENU                   ");
        System.out.println("  ==========================================");
        System.out.println("  1. Seller");
        System.out.println("  2. Admin");
        System.out.println("  3. Bidder");
        System.out.println("  4. Exit");
        System.out.println("  ==========================================");
    }
}