package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.stream.Stream;


public class TicketServiceImpl implements TicketService {

    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAXIMUM_TICKETS_PURCHASE_LIMIT = 20;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateTicketPurchase(accountId, ticketTypeRequests);

        ticketPaymentService.makePayment(accountId,
                Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getTotalAmount).sum());

        seatReservationService.reserveSeat(accountId,
                Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getTotalSeats).sum());

    }

    private void validateTicketPurchase(Long accountId,
                                        TicketTypeRequest... ticketTypeRequests) {
        int totalNoTickets =
                Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum();

        if (accountId < 1) {
            throw new InvalidPurchaseException("Account id not valid");
        }

        if (sumOfTicketType(TicketTypeRequest.Type.ADULT, ticketTypeRequests) < 1 &&
                (sumOfTicketType(TicketTypeRequest.Type.CHILD, ticketTypeRequests) > 0 ||
                        sumOfTicketType(TicketTypeRequest.Type.CHILD, ticketTypeRequests) > 0)) {
            throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without purchasing an Adult ticket");
        }

        if (sumOfTicketType(TicketTypeRequest.Type.ADULT, ticketTypeRequests) < sumOfTicketType(TicketTypeRequest.Type.INFANT, ticketTypeRequests)) {
            throw new InvalidPurchaseException("Infants will be sitting on an Adult's lap. 1 Infant per Adult");
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
