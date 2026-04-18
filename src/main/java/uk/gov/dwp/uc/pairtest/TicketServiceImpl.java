package uk.gov.dwp.uc.pairtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl() {
        this.paymentService = new TicketPaymentServiceImpl();
        this.seatReservationService = new SeatReservationServiceImpl();
    }

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        LOGGER.info("Processing ticket purchase for account: {}", accountId);

        try {
            TicketValidator.validate(accountId, ticketTypeRequests);
            LOGGER.info("Ticket validation successful");
        } catch (InvalidPurchaseException e) {
            LOGGER.error("Ticket validation failed for account: {}, reason: {}", accountId, e.getMessage());
            throw e;
        }

        int totalAmount = calculateTotalAmount(ticketTypeRequests);
        LOGGER.info("Calculated total amount: {}", totalAmount);
        try {
            paymentService.makePayment(accountId, totalAmount);
            LOGGER.info("Payment processed successfully");
        } catch (Exception e) {
            LOGGER.error("Payment failed for account: {}, amount: {}", accountId, totalAmount, e);
            throw e;
        }

        int totalSeats = calculateTotalSeats(ticketTypeRequests);
        LOGGER.info("Calculated total seats: {}", totalSeats);
        try {
            seatReservationService.reserveSeat(accountId, totalSeats);
            LOGGER.info("Seats reserved successfully");
        } catch (Exception e) {
            LOGGER.error("Seat reservation failed for account: {}, seats: {}", accountId, totalSeats, e);
            throw e;
        }

        LOGGER.info("Ticket purchase completed for account: {}", accountId);
    }

    private int calculateTotalAmount(TicketTypeRequest... ticketTypeRequests) {
        int total = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            Type type = request.getTicketType();
            int price = type.getPrice();
            total += price * request.getNoOfTickets();
        }
        return total;
    }

    private int calculateTotalSeats(TicketTypeRequest... ticketTypeRequests) {
        int totalSeats = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            Type type = request.getTicketType();
            if (type != Type.INFANT) {
                totalSeats += request.getNoOfTickets();
            }
        }
        return totalSeats;
    }
}