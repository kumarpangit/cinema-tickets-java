package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public final class TicketValidator {

    private static final int MAX_TICKETS = 25;

    private TicketValidator() {
    }

    public static void validate(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid Account ID. It must be positive");
        }

        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket type request is required");
        }

        int totalTickets = 0;
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null) {
                throw new InvalidPurchaseException("Ticket type request cannot be null");
            }
            
            int count = request.getNoOfTickets();
            if (count <= 0) {
                throw new InvalidPurchaseException("Number of tickets must be positive");
            }

            totalTickets += count;

            Type type = request.getTicketType();
            if (type == Type.ADULT) {
                adultCount += count;
            } else if (type == Type.CHILD) {
                childCount += count;
            } else if (type == Type.INFANT) {
                infantCount += count;
            }
        }

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException("Maximum of " + MAX_TICKETS + " tickets allowed per transaction");
        }

        if (adultCount == 0 && (childCount > 0 || infantCount > 0)) {
            throw new InvalidPurchaseException("Child and infant tickets require at least one adult ticket");
        }

        if (infantCount > adultCount) {
            throw new InvalidPurchaseException("Each infant must be accompanied by an adult");
        }
    }
}
