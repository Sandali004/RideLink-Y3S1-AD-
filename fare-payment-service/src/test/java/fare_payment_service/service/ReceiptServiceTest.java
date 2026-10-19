package fare_payment_service.service;

import fare_payment_service.entity.Payment;
import fare_payment_service.entity.Receipt;
import fare_payment_service.exception.PaymentNotFoundException;
import fare_payment_service.exception.ReceiptNotFoundException;
import fare_payment_service.repository.PaymentRepository;
import fare_payment_service.repository.ReceiptRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReceiptServiceTest {

    @Test
    void generateReceiptShouldCreateReceiptFromPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);

        ReceiptService receiptService =
                new ReceiptService(receiptRepository, paymentRepository);

        Payment payment = new Payment(
                "RIDE-001",
                500.0,
                "CARD",
                "SUCCESS",
                "TXN-TEST-123"
        );

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        Receipt savedReceipt = new Receipt(
                1L,
                "RIDE-001",
                500.0,
                "CARD",
                "TXN-TEST-123"
        );

        when(receiptRepository.save(any(Receipt.class)))
                .thenReturn(savedReceipt);

        Receipt result = receiptService.generateReceipt(1L);

        assertEquals(1L, result.getPaymentId());
        assertEquals("RIDE-001", result.getRideId());
        assertEquals(500.0, result.getAmount());
        assertEquals("CARD", result.getPaymentMethod());
        assertEquals(
                "TXN-TEST-123",
                result.getTransactionReference()
        );

        verify(paymentRepository).findById(1L);
        verify(receiptRepository).save(any(Receipt.class));
    }

    @Test
    void generateReceiptShouldThrowWhenPaymentIsMissing() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);

        ReceiptService receiptService =
                new ReceiptService(receiptRepository, paymentRepository);

        when(paymentRepository.findById(99L))
                .thenReturn(Optional.empty());

        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> receiptService.generateReceipt(99L)
        );

        assertEquals("Payment not found", exception.getMessage());
        verify(paymentRepository).findById(99L);
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    void getReceiptByIdShouldReturnExistingReceipt() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);

        ReceiptService receiptService =
                new ReceiptService(receiptRepository, paymentRepository);

        Receipt receipt = new Receipt(
                2L,
                "RIDE-003",
                750.0,
                "CASH",
                "TXN-TEST-456"
        );

        when(receiptRepository.findById(10L))
                .thenReturn(Optional.of(receipt));

        Receipt result = receiptService.getReceiptById(10L);

        assertSame(receipt, result);
        verify(receiptRepository).findById(10L);
    }

    @Test
    void getReceiptByIdShouldThrowWhenReceiptIsMissing() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        ReceiptRepository receiptRepository = mock(ReceiptRepository.class);

        ReceiptService receiptService =
                new ReceiptService(receiptRepository, paymentRepository);

        when(receiptRepository.findById(99L))
                .thenReturn(Optional.empty());

        ReceiptNotFoundException exception = assertThrows(
                ReceiptNotFoundException.class,
                () -> receiptService.getReceiptById(99L)
        );

        assertEquals("Receipt not found", exception.getMessage());
        verify(receiptRepository).findById(99L);
    }
}