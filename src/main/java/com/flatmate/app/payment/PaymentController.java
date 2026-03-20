package com.flatmate.app.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody Map<String, Object> request) {
        String userId = request.get("userId").toString();
        String planId = (String) request.get("planId");
        String subscriptionId = paymentService.createSubscriptionOrder(userId, planId);
        return ResponseEntity.ok(Map.of("subscriptionId", subscriptionId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok("Webhook processed");
    }
}
