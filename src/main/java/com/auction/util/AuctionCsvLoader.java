package com.auction.util;

import com.auction.model.Auction;
import com.auction.model.Seller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Seed Loader demonstrating try-with-resources and AutoCloseable.
 *
 * Concepts Demonstrated:
 * - try-with-resources: automatically closes Reader/BufferedReader at block termination,
 *   even if an exception or malformed line is encountered.
 * - Validation of individual rows with clear reporting of malformed lines without crashing.
 */
public class AuctionCsvLoader {

    public static class ParseResult {
        private final List<Auction> auctions = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        public List<Auction> getAuctions() {
            return auctions;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        public boolean hasErrors() {
            return !errorMessages.isEmpty();
        }
    }

    /**
     * Parses auction records from a CSV reader using try-with-resources.
     * Expected CSV format:
     * id,productName,startingPrice,reservePrice,minIncrement
     *
     * @param in the Reader source
     * @param defaultSeller default seller to attach to parsed listings
     * @return ParseResult containing successfully parsed auctions and any line error messages
     * @throws IOException if I/O reading fails
     */
    public ParseResult loadAuctions(Reader in, Seller defaultSeller) throws IOException {
        ParseResult result = new ParseResult();
        if (in == null) {
            result.getErrorMessages().add("Reader input cannot be null.");
            return result;
        }

        // try-with-resources guarantees BufferedReader is properly closed
        try (BufferedReader reader = new BufferedReader(in)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip blank lines and header row
                if (line.isEmpty() || line.startsWith("#") || (lineNumber == 1 && line.toLowerCase().contains("productname"))) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    result.getErrorMessages().add("Line " + lineNumber + ": Malformed CSV row, expected at least 5 columns, found " + parts.length + " -> '" + line + "'");
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String productName = parts[1].trim();
                    BigDecimal startPrice = new BigDecimal(parts[2].trim());
                    BigDecimal reservePrice = new BigDecimal(parts[3].trim());
                    BigDecimal minInc = new BigDecimal(parts[4].trim());

                    if (productName.isEmpty()) {
                        result.getErrorMessages().add("Line " + lineNumber + ": Product name cannot be empty.");
                        continue;
                    }
                    if (startPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        result.getErrorMessages().add("Line " + lineNumber + ": Starting price must be positive.");
                        continue;
                    }
                    if (reservePrice.compareTo(startPrice) < 0) {
                        result.getErrorMessages().add("Line " + lineNumber + ": Reserve price cannot be lower than starting price.");
                        continue;
                    }

                    Auction auction = new Auction(
                            id,
                            productName,
                            "CSV Seed: " + productName,
                            defaultSeller,
                            startPrice,
                            reservePrice,
                            minInc,
                            LocalDateTime.now(),
                            LocalDateTime.now().plusDays(7)
                    );
                    result.getAuctions().add(auction);

                } catch (NumberFormatException nfe) {
                    result.getErrorMessages().add("Line " + lineNumber + ": Number format error: " + nfe.getMessage());
                }
            }
        }

        return result;
    }
}
