package com.lockedin.backend.service;

import com.lockedin.backend.entity.Rating;
import com.lockedin.backend.entity.Session;
import com.lockedin.backend.entity.User;
import com.lockedin.backend.repository.RatingRepository;
import com.lockedin.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Two-way post-session ratings, rolled into each user's public trust score. */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    public Rating submitRating(Long sessionId, Long ratedByUserId, Long ratedUserId, int score, String comment) {
        Rating rating = Rating.builder()
                .session(Session.builder().id(sessionId).build())
                .ratedBy(User.builder().id(ratedByUserId).build())
                .ratedUser(User.builder().id(ratedUserId).build())
                .score(score)
                .comment(comment)
                .build();
        rating = ratingRepository.save(rating);

        recalculateTrustScore(ratedUserId);
        return rating;
    }

    public List<Rating> getRatingsFor(Long userId) {
        return ratingRepository.findByRatedUserId(userId);
    }

    private void recalculateTrustScore(Long userId) {
        List<Rating> ratings = ratingRepository.findByRatedUserId(userId);
        double avg = ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);

        User user = userRepository.findById(userId).orElseThrow();
        user.setTrustScore(avg);
        userRepository.save(user);
    }
}
