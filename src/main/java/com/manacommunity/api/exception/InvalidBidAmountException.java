package com.manacommunity.api.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a bid amount is below the required minimum next bid. */
public class InvalidBidAmountException extends ManaCommunityException {

    public InvalidBidAmountException(int bidAmount, int minNextBid) {
        super("Bid amount ₹" + bidAmount + " is too low. "
                + "Minimum required bid is ₹" + minNextBid + ".",
                HttpStatus.BAD_REQUEST, "INVALID_BID_AMOUNT");
    }
}
