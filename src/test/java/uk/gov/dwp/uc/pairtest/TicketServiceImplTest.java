package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("Should throw exception for invalid account ID")
    void shouldThrowExceptionForInvalidAccountId() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(-1L, new TicketTypeRequest(Type.ADULT, 1)));
    }

    @Test
    @DisplayName("Should throw exception for zero account ID")
    void shouldThrowExceptionForZeroAccountId() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(0L, new TicketTypeRequest(Type.ADULT, 1)));
    }

    @Test
    @DisplayName("Should throw exception for null account ID")
    void shouldThrowExceptionForNullAccountId() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(null, new TicketTypeRequest(Type.ADULT, 1)));
    }

    @Test
    @DisplayName("Should throw exception when no ticket requests provided")
    void shouldThrowExceptionForNoTicketRequests() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L));
    }

    @Test
    @DisplayName("Should throw exception when child tickets purchased without adult")
    void shouldThrowExceptionForChildWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.CHILD, 1)));
    }

    @Test
    @DisplayName("Should throw exception when infant tickets purchased without adult")
    void shouldThrowExceptionForInfantWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.INFANT, 1)));
    }

    @Test
    @DisplayName("Should throw exception when total tickets exceed 25")
    void shouldThrowExceptionForMaxTicketsExceeded() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 26)));
    }

    @Test
    @DisplayName("Should throw exception when infants exceed adults")
    void shouldThrowExceptionWhenInfantsExceedAdults() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, 
                new TicketTypeRequest(Type.ADULT, 1),
                new TicketTypeRequest(Type.INFANT, 2)));
    }

    @Test
    @DisplayName("Should calculate correct amount for adult tickets")
    void shouldCalculateCorrectAmountForAdultTickets() {
        ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 2));

        verify(paymentService).makePayment(1L, 50);
    }

    @Test
    @DisplayName("Should calculate correct amount for mixed tickets")
    void shouldCalculateCorrectAmountForMixedTickets() {
        ticketService.purchaseTickets(1L, 
            new TicketTypeRequest(Type.ADULT, 2),
            new TicketTypeRequest(Type.CHILD, 1));

        verify(paymentService).makePayment(1L, 65);
    }

    @Test
    @DisplayName("Should reserve correct number of seats for adults and children")
    void shouldReserveCorrectSeatsForAdultsAndChildren() {
        ticketService.purchaseTickets(1L, 
            new TicketTypeRequest(Type.ADULT, 2),
            new TicketTypeRequest(Type.CHILD, 1));

        verify(seatReservationService).reserveSeat(1L, 3);
    }

    @Test
    @DisplayName("Should not reserve seats for infants")
    void shouldNotReserveSeatsForInfants() {
        ticketService.purchaseTickets(1L, 
            new TicketTypeRequest(Type.ADULT, 1),
            new TicketTypeRequest(Type.INFANT, 1));

        verify(seatReservationService).reserveSeat(1L, 1);
    }

    @Test
    @DisplayName("Should throw exception for null ticket request")
    void shouldThrowExceptionForNullTicketRequest() {
        TicketTypeRequest[] requests = new TicketTypeRequest[]{null};
        
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, requests));
    }

    @Test
    @DisplayName("Should throw exception for zero ticket count")
    void shouldThrowExceptionForZeroTicketCount() {
        assertThrows(InvalidPurchaseException.class, 
            () -> ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 0)));
    }
}
