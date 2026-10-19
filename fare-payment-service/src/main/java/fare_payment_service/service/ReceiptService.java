package fare_payment_service.service;

import fare_payment_service.entity.Payment;
import fare_payment_service.entity.Receipt;
import fare_payment_service.repository.PaymentRepository;
import fare_payment_service.repository.ReceiptRepository;
import org.springframework.stereotype.Service;
import fare_payment_service.exception.ReceiptNotFoundException;
import fare_payment_service.exception.PaymentNotFoundException;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PaymentRepository paymentRepository;

    public ReceiptService(
            ReceiptRepository receiptRepository,
            PaymentRepository paymentRepository) {

        this.receiptRepository = receiptRepository;
        this.paymentRepository = paymentRepository;
    }

    public Receipt generateReceipt(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() ->
        new PaymentNotFoundException("Payment not found"));
        
        Receipt receipt = new Receipt(
                payment.getId(),
                payment.getRideId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionReference()
        );

        return receiptRepository.save(receipt);
    }

    public Receipt getReceiptById(Long id) {

        return receiptRepository.findById(id)
                .orElseThrow(() ->
                new ReceiptNotFoundException("Receipt not found"));
    }
}