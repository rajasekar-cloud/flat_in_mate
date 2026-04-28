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
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlanId(planId);
        subscription.setRazorpayOrderId(mockRazorpayId);
        subscription.setStatus("ACTIVE");
        subscription.setExpiresAt(LocalDateTime.now().plusMonths(1).toString());
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
