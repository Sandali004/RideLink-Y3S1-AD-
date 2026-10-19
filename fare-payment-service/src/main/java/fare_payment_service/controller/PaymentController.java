package fare_payment_service.controller;

import fare_payment_service.entity.Payment;
import fare_payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment makePayment(@Valid @RequestBody Payment payment)  {
    
        return paymentService.processPayment(
                payment.getRideId(),
                payment.getAmount(),
                payment.getPaymentMethod()
        );
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {

        return paymentService.getPaymentById(id);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(
            MethodArgumentNotValidException e) {
    
        return ResponseEntity
                .badRequest()
                .body("Validation error: " +
                        e.getBindingResult()
                                .getFieldError()
                                .getDefaultMessage());
    }
}