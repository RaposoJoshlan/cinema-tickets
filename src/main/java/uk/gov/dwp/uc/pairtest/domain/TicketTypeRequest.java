package uk.gov.dwp.uc.pairtest.domain;

/**
 * Immutable Object
 */
public class TicketTypeRequest {

    private final int noOfTickets;
    private final Type type;

    private final int totalSeats;
    private final int totalAmount;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = type;
        this.noOfTickets = noOfTickets;
        this.totalSeats = getNoOfTickets() * type.seats;
        this.totalAmount = getNoOfTickets() * type.ticketPrice;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public enum Type {
        ADULT(20, 1), CHILD(10, 1), INFANT(0, 0);

        private final int ticketPrice;
        private final int seats;

        Type(int ticketPrice, int seats) {
            this.ticketPrice = ticketPrice;
            this.seats = seats;
        }
    }

}
