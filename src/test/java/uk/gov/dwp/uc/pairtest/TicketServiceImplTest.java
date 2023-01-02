package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;
    private TicketTypeRequest typeRequest;
    private TicketTypeRequest typeRequest2;


    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    void testTicketPurchaseWithInvalidAccountId() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(0L, typeRequest);
                }
        );

        assertEquals("Account id not valid", exception.getMessage());
    }

    @Test
    void testTicketPurchaseForChildOrInfantWithoutAdult() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        typeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(1L, typeRequest, typeRequest2);
                }
        );

        assertEquals("Child and Infant tickets cannot be purchased without purchasing an Adult ticket", exception.getMessage());
    }

    @Test
    void testTicketPurchaseValidityIfAdultsLessThanInfants() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        typeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(1L, typeRequest, typeRequest2);
                }
        );

        assertEquals("Infants will be sitting on an Adult's lap. 1 Infant per Adult", exception.getMessage());
    }

    @Test
    void testPurchaseForZeroTickets() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(1L, typeRequest);
                }
        );

        assertEquals("Must purchase 1 ticket at minimum", exception.getMessage());
    }

    @Test
    void testExceedMaximumTicketPurchaseLimit() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 19);
        typeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(1L, typeRequest, typeRequest2);
                }
        );

        assertEquals("Only a maximum of 20 tickets that can be purchased at a time", exception.getMessage());
    }

    @Test
    void testTicketPurchaseForAnAdultOnly() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);
        ticketService.purchaseTickets(1L, typeRequest);

        verify(ticketPaymentService, times(1)).makePayment(1L, typeRequest.getTotalAmount());
    }

    @Test
    void testSeatReservationForAnAdultOnly() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 8);
        ticketService.purchaseTickets(1L, typeRequest);

        verify(seatReservationService, times(1)).reserveSeat(1L, typeRequest.getTotalSeats());
    }

    @Test
    void testPurchaseTickets() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);
        typeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 4);
        TicketTypeRequest typeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 4);

        ticketService.purchaseTickets(1L, typeRequest, typeRequest2, typeRequest3);

        ArrayList<TicketTypeRequest> ticketTypeRequests = new ArrayList<>();
        ticketTypeRequests.add(typeRequest);
        ticketTypeRequests.add(typeRequest2);
        ticketTypeRequests.add(typeRequest3);

        verify(ticketPaymentService, times(1)).makePayment(1L,
                ticketTypeRequests.stream().mapToInt(TicketTypeRequest::getTotalAmount).sum());
    }

    @Test
    void testSeatReservation() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);
        typeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 4);
        TicketTypeRequest typeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 4);

        ticketService.purchaseTickets(1L, typeRequest, typeRequest2, typeRequest3);

        ArrayList<TicketTypeRequest> ticketTypeRequests = new ArrayList<>();
        ticketTypeRequests.add(typeRequest);
        ticketTypeRequests.add(typeRequest2);
        ticketTypeRequests.add(typeRequest3);

        verify(seatReservationService, times(1)).reserveSeat(1L,
                ticketTypeRequests.stream().mapToInt(TicketTypeRequest::getTotalSeats).sum());
    }
}