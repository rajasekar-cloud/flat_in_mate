package com.flatmate.app.swipe;

import com.flatmate.app.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        // Prevent duplicate swipes — the seeker already acted on this listing
        if (swipeRepository.findBySeekIdAndListingId(seekerId, listingId).isPresent()) {
            throw new RuntimeException("You have already swiped on this listing.");
        }

        if (!user.isPremium()) {
            checkLimits(seekerId);
        }

        Swipe swipe = new Swipe();
        swipe.setSeekerId(seekerId);
        swipe.setListingId(listingId);
        swipe.setType(type);
        swipe.setCreatedAt(LocalDate.now().toString());

        swipeRepository.save(swipe);

        if ("RIGHT".equalsIgnoreCase(type)) {
            eventPublisher.publishEvent("swipe-events", "SwipeCreated: seeker=" + seekerId + ", listing=" + listingId);
        }

        // Increment Redis Daily Counter
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dailyKey = "view:daily:" + seekerId + ":" + date;
        redisTemplate.opsForValue().increment(dailyKey);
    }

    /** Returns the set of listing IDs this seeker has already swiped on (LEFT or RIGHT). */
    public Set<String> getSwipedListingIds(String seekerId) {
        return swipeRepository.findSwipedListingIds(seekerId);
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

    public List<InterestedSeekerDTO> getInterestedSeekers(String listingId) {
        return swipeRepository.findByListingId(listingId).stream()
                .filter(s -> "RIGHT".equalsIgnoreCase(s.getType()))
                .map(s -> {
                    com.flatmate.app.auth.User user = userRepository.findById(s.getSeekerId())
                            .orElse(null);
                    
                    if (user == null) return null;

                    InterestedSeekerDTO dto = new InterestedSeekerDTO();
                    dto.setSeekerId(user.getUserId());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setProfilePic(user.getProfilePic());
                    dto.setGender(user.getGender());
                    dto.setDescription(user.getBio());
                    dto.setSwipedAt(s.getCreatedAt());
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
}
