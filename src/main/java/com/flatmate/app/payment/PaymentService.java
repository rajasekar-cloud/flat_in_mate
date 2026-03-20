package com.flatmate.app.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final com.flatmate.app.auth.UserRepository userRepository;

    public String createSubscriptionOrder(String userId, String planId) {
        String mockRazorpayId = "sub_" + System.currentTimeMillis();
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .planId(planId)
                .razorpayOrderId(mockRazorpayId)
                .status("ACTIVE")
                .expiresAt(LocalDateTime.now().plusMonths(1).toString())
                .build();
        subscriptionRepository.save(subscription);

        // Update User Premium Status
        userRepository.findById(userId).ifPresent(user -> {
            user.setPremium(true);
            userRepository.save(user);
        });

        return mockRazorpayId;
    }

    public void handleWebhook(String payload) {
        // Simplified Razorpay Webhook logic
        // In reality, verify signature first
        if (payload.contains("subscription.charged") || payload.contains("payment.captured")) {
            // Extract userId from payload (simulated)
            // For now, we assume the user is identified in the order
            System.out.println("Payment Success Webhook Received. Updating user status...");
        }
    }
}
