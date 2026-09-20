package com.jdmart.repository;

import com.jdmart.model.Product;
import com.jdmart.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
}
