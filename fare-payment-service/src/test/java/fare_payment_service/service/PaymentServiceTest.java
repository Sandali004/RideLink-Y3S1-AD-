package fare_payment_service.service;

import fare_payment_service.entity.Payment;
import fare_payment_service.exception.PaymentNotFoundException;
import fare_payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Test
    void processPaymentShouldCreateSuccessfulPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);

        Payment savedPayment = new Payment(
                "RIDE-001",
                500.0,
                "CARD",
                "SUCCESS",
                "TXN-TEST-123"
        );

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        Payment result = paymentService.processPayment(
                "RIDE-001",
                500.0,
                "CARD"
        );

        assertEquals("RIDE-001", result.getRideId());
        assertEquals(500.0, result.getAmount());
        assertEquals("CARD", result.getPaymentMethod());
        assertEquals("SUCCESS", result.getPaymentStatus());
        assertNotNull(result.getTransactionReference());

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentByIdShouldReturnExistingPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);

        Payment payment = new Payment(
                "RIDE-002",
                750.0,
                "CASH",
                "SUCCESS",
                "TXN-TEST-456"
        );

        when(paymentRepository.findById(10L))
                .thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById(10L);

        assertSame(payment, result);
        verify(paymentRepository).findById(10L);
    }

    @Test
    void getPaymentByIdShouldThrowWhenPaymentIsMissing() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        PaymentService paymentService = new PaymentService(paymentRepository);

        when(paymentRepository.findById(99L))
                .thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentById(99L)
        );

        assertEquals("Payment not found", exception.getMessage());
        verify(paymentRepository).findById(99L);
    }
}