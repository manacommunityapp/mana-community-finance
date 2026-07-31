package com.manacommunity.api.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a user is already registered for an event. */
public class AlreadyRegisteredException extends ManaCommunityException {

    public AlreadyRegisteredException(String eventName) {
        super("You are already registered for the event: '" + eventName + "'.",
                HttpStatus.CONFLICT, "ALREADY_REGISTERED");
    }
}
