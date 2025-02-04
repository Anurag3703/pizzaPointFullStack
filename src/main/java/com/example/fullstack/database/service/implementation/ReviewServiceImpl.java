package com.example.fullstack.database.service.implementation;

import com.example.fullstack.database.model.Review;
import com.example.fullstack.database.repository.ReviewRepository;
import com.example.fullstack.database.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void addReview(Review review) {
        reviewRepository.save(review);
    }
}
