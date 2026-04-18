package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class TicketServiceImplIntegrationTest {

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(
            new TicketPaymentServiceImpl(),
            new SeatReservationServiceImpl()
        );
    }

    @Test
    @DisplayName("Should complete purchase flow for valid adult tickets")
    void shouldCompletePurchaseForAdultTickets() {
        assertDoesNotThrow(() -> 
            ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 2))
        );
    }

    @Test
    @DisplayName("Should complete purchase flow for mixed tickets")
    void shouldCompletePurchaseForMixedTickets() {
        assertDoesNotThrow(() -> 
            ticketService.purchaseTickets(1L, 
                new TicketTypeRequest(Type.ADULT, 2),
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.INFANT, 1))
        );
    }

    @Test
    @DisplayName("Should complete purchase for maximum allowed tickets")
    void shouldCompletePurchaseForMaxTickets() {
        assertDoesNotThrow(() -> 
            ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 25))
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid account ID")
    void shouldThrowForInvalidAccountId() {
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(-5L, new TicketTypeRequest(Type.ADULT, 1))
        );
    }

    @Test
    @DisplayName("Should throw exception for child without adult")
    void shouldThrowForChildWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.CHILD, 2))
        );
    }

    @Test
    @DisplayName("Should throw exception when tickets exceed maximum")
    void shouldThrowWhenExceedingMaxTickets() {
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 30))
        );
    }

    @Test
    @DisplayName("Should throw exception when infants exceed adults")
    void shouldThrowWhenInfantsExceedAdults() {
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(1L, 
                new TicketTypeRequest(Type.ADULT, 1),
                new TicketTypeRequest(Type.INFANT, 2))
        );
    }
    
    @Test
    @DisplayName("Should handle multiple ticket type requests")
    void shouldHandleMultipleTicketTypes() {
        assertDoesNotThrow(() -> 
            ticketService.purchaseTickets(100L, 
                new TicketTypeRequest(Type.ADULT, 12),
                new TicketTypeRequest(Type.CHILD, 1),
                new TicketTypeRequest(Type.INFANT, 12))
        );
    }

    @Test
    @DisplayName("Should throw exception when number of mixed tickets exceed max allowed tickets")
    void shouldThrowWhenMixedTicketsExceedMaxTickets() {
        assertThrows(InvalidPurchaseException.class, () -> 
            ticketService.purchaseTickets(100L, 
                new TicketTypeRequest(Type.ADULT, 15),
                new TicketTypeRequest(Type.CHILD, 3),
                new TicketTypeRequest(Type.INFANT, 15))
        );
    }

    @Test
    @DisplayName("Should log error when payment fails")
    void shouldLogErrorWhenPaymentFails() {
        TicketPaymentService failingPaymentService = mock(TicketPaymentService.class);
        doThrow(new RuntimeException("Payment gateway unavailable")).when(failingPaymentService)
            .makePayment(anyLong(), anyInt());

        TicketService service = new TicketServiceImpl(
            failingPaymentService,
            new SeatReservationServiceImpl()
        );

        assertThrows(RuntimeException.class, () -> 
            service.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 1))
        );
    }

    @Test
    @DisplayName("Should log error when seat reservation fails")
    void shouldLogErrorWhenSeatReservationFails() {
        SeatReservationService failingSeatService = mock(SeatReservationService.class);
        doThrow(new RuntimeException("Seat service unavailable")).when(failingSeatService)
            .reserveSeat(anyLong(), anyInt());

        TicketService service = new TicketServiceImpl(
            new TicketPaymentServiceImpl(),
            failingSeatService
        );

        assertThrows(RuntimeException.class, () -> 
            service.purchaseTickets(1L, new TicketTypeRequest(Type.ADULT, 1))
        );
    }
}