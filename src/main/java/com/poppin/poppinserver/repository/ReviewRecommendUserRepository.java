package com.poppin.poppinserver.repository;

import com.poppin.poppinserver.domain.Popup;
import com.poppin.poppinserver.domain.Review;
import com.poppin.poppinserver.domain.ReviewRecommendUser;
import com.poppin.poppinserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRecommendUserRepository extends JpaRepository<ReviewRecommendUser, Long> {

   Optional<ReviewRecommendUser> findByUserAndReview(User user , Review review);

   void deleteAllByReviewPopup(Popup popup);
}
