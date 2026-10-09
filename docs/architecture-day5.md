# Day 5 Architecture Notes

This note documents the object-oriented architecture established on Day 5:
- Abstract BaseEntity with common identifiers and audit timestamps.
- User hierarchy with Admin, Seller, and Bidder.
- BidStrategy interface with ManualBidStrategy and AutoBidStrategy.
- BidEngineService and PaymentService.
