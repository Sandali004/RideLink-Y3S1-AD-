package fare_payment_service.service;

import fare_payment_service.entity.Payment;
import fare_payment_service.exception.PaymentNotFoundException;
import fare_payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment processPayment(
            String rideId,
            double amount,
            String paymentMethod) {

        String paymentStatus = "SUCCESS";
        String transactionReference = "TXN-" + UUID.randomUUID();

        Payment payment = new Payment(
                rideId,
                amount,
                paymentMethod,
                paymentStatus,
                transactionReference
        );

        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(
                        () -> new PaymentNotFoundException(
                                "Payment not found"
                        )
                );
    }
}