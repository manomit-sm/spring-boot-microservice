package com.bsolz.microservices.core.review.persistence;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reviews", indexes = { @Index(name = "review_unique_idx", unique = true, columnList = "productId, reviewId")})
@Data
public class ReviewEntity {
    @Id
    @GeneratedValue
    private int id;
    @Version
    private Integer version;
    private int productId;
    private int reviewId;
    private String author;
    private String subject;
    private String content;
}
