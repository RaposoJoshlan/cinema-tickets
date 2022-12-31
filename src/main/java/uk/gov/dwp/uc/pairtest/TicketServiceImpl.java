package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.stream.Stream;


public class TicketServiceImpl implements TicketService {

    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAXIMUM_TICKETS_PURCHASE_LIMIT = 20;

    public TicketServiceImpl() {
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateTicketPurchase(accountId, ticketTypeRequests);
    }

    private void validateTicketPurchase(Long accountId,
                                        TicketTypeRequest... ticketTypeRequests) {
        int totalNoTickets =
                Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum();

        if(accountId < 1) {
            throw new InvalidPurchaseException("Account id not valid");
        }

        if (sumOfTicketType(TicketTypeRequest.Type.ADULT, ticketTypeRequests) < 1 &&
                (sumOfTicketType(TicketTypeRequest.Type.CHILD, ticketTypeRequests) > 0 ||
                        sumOfTicketType(TicketTypeRequest.Type.CHILD, ticketTypeRequests) > 0)) {
            throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without purchasing an Adult ticket");
        }

        if (totalNoTickets > MAXIMUM_TICKETS_PURCHASE_LIMIT) {
            throw new InvalidPurchaseException("Only a maximum of 20 tickets that can be purchased at a time");
        }
    }

    private int sumOfTicketType(TicketTypeRequest.Type type, TicketTypeRequest... ticketTypeRequests) {
        return Stream.of(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType().equals(type))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }
}
