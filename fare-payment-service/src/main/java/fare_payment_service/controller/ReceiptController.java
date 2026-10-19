package fare_payment_service.controller;

import fare_payment_service.entity.Receipt;
import fare_payment_service.service.ReceiptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping("/{paymentId}")
    public Receipt generateReceipt(@PathVariable Long paymentId) {

        return receiptService.generateReceipt(paymentId);
    }

    @GetMapping("/{id}")
    public Receipt getReceipt(@PathVariable Long id) {

        return receiptService.getReceiptById(id);
    }
}