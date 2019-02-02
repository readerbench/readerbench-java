package com.readerbench.datasourceprovider.elasticsearch;

import java.util.Objects;

public class Review {

    String review;

    Integer score;

    public Review(String review, Integer score) {
        this.review = review;
        this.score = score;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review1 = (Review) o;
        return Objects.equals(review, review1.review) &&
                Objects.equals(score, review1.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(review, score);
    }
}
