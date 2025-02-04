package com.example.fullstack.database.controller;

import com.example.fullstack.database.model.Review;
import com.example.fullstack.database.service.implementation.ReviewServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    ReviewServiceImpl reviewServiceImpl;
    public ReviewController(ReviewServiceImpl reviewServiceImpl) {
        this.reviewServiceImpl = reviewServiceImpl;
    }

    @PostMapping
    public String addReview(@RequestBody Review review) {
        reviewServiceImpl.addReview(review);
        return "Review added";
    }
}
