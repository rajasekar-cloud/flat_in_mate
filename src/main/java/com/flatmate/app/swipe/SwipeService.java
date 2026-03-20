package com.flatmate.app.swipe;

import com.flatmate.app.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeRepository swipeRepository;
    private final StringRedisTemplate redisTemplate;
    private final com.flatmate.app.auth.UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public void recordSwipe(String seekerId, String listingId, String type) {
        com.flatmate.app.auth.User user = userRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isPremium()) {
            checkLimits(seekerId);
        }

        Swipe swipe = Swipe.builder()
                .seekerId(seekerId)
                .listingId(listingId)
                .type(type)
                .createdAt(LocalDate.now().toString())
                .build();

        swipeRepository.save(swipe);

        if ("RIGHT".equalsIgnoreCase(type)) {
            eventPublisher.publishEvent("swipe-events", "SwipeCreated: seeker=" + seekerId + ", listing=" + listingId);
        }

        // Increment Redis Daily Counter
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dailyKey = "view:daily:" + seekerId + ":" + date;
        redisTemplate.opsForValue().increment(dailyKey);
    }

    private void checkLimits(String seekerId) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dailyKey = "view:daily:" + seekerId + ":" + date;

        String dailyCount = redisTemplate.opsForValue().get(dailyKey);
        if (dailyCount != null && Integer.parseInt(dailyCount) >= 3) {
            throw new RuntimeException("Daily limit reached. Subscribe to view more!");
        }

        long totalCount = swipeRepository.countBySeekerId(seekerId);
        if (totalCount >= 12) {
            throw new RuntimeException("Overall free limit reached. Subscribe for unlimited access!");
        }
    }
}
