package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


import static org.junit.jupiter.api.Assertions.*;

class TicketServiceImplTest {

    private TicketServiceImpl ticketService;
    private TicketTypeRequest typeRequest;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void purchaseTickets() {
    }

    @Test
    void ticketPurchaseWithInvalidAccountId(){
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
    void ticketPurchaseForChildOrInfantWithoutAdult() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(1L, typeRequest);
                }
        );

        assertEquals("Child and Infant tickets cannot be purchased without purchasing an Adult ticket", exception.getMessage());
    }

    @Test
    void exceedMaximumTicketPurchaseLimit() {
        typeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);

        InvalidPurchaseException exception = assertThrows(
                InvalidPurchaseException.class,
                () -> {
                    ticketService.purchaseTickets(1L, typeRequest);
                }
        );

        assertEquals("Only a maximum of 20 tickets that can be purchased at a time", exception.getMessage());
    }
}