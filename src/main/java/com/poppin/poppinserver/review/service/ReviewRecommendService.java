package com.poppin.poppinserver.review.service;

import com.poppin.poppinserver.alarm.usecase.SendAlarmCommandUseCase;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import com.poppin.poppinserver.core.type.EPushInfo;
import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.popup.repository.PopupRepository;
import com.poppin.poppinserver.review.domain.Review;
import com.poppin.poppinserver.review.domain.ReviewRecommend;
import com.poppin.poppinserver.review.repository.ReviewQueryRepository;
import com.poppin.poppinserver.review.repository.ReviewRecommendCommandRepository;
import com.poppin.poppinserver.review.repository.ReviewRecommendQueryRepository;
import com.poppin.poppinserver.user.domain.User;
import com.poppin.poppinserver.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ReviewRecommendService {

    private final UserQueryRepository userQueryRepository;
    private final PopupRepository popupRepository;
    private final ReviewQueryRepository reviewRepository;
    private final ReviewRecommendCommandRepository reviewRecommendCommandRepository;
    private final ReviewRecommendQueryRepository reviewRecommendQueryRepository;

    private final SendAlarmCommandUseCase sendAlarmCommandUseCase;


    /*후기 추천 증가*/
    public String recommendReview(Long userId, String StrReviewId, String StrPopupId) {

        Long reviewId = Long.valueOf(StrReviewId);
        Long popupId = Long.valueOf(StrPopupId);

        User user = userQueryRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POPUP));

        Review review = reviewRepository.findByReviewIdAndPopupId(reviewId, popupId);

        // 예외처리
        if (review == null) {
            throw new CommonException(ErrorCode.NOT_FOUND_REVIEW);
        }
        if (review.getUser().getId().equals(userId)) {
            throw new CommonException(ErrorCode.REVIEW_RECOMMEND_ERROR);
        }

        Optional<ReviewRecommend> recommendCnt = reviewRecommendQueryRepository.findByUserAndReview(user, review);
        if (recommendCnt.isPresent()) {
            throw new CommonException(ErrorCode.DUPLICATED_RECOMMEND_COUNT); // 2회이상 같은 후기에 대해 추천 증가 방지
        } else {
            review.addRecommendCnt();
        }

        ReviewRecommend reviewRecommend = ReviewRecommend.builder()
                .user(user)
                .review(review)
                .build();

        reviewRepository.save(review);
        reviewRecommendCommandRepository.save(reviewRecommend);

        sendAlarmCommandUseCase.sendChoochunAlarm(user, popup, review, EPushInfo.CHOOCHUN); // 알림

        return "정상적으로 반환되었습니다";

    }
}
