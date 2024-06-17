package com.poppin.poppinserver.service;

import com.poppin.poppinserver.domain.*;
import com.poppin.poppinserver.dto.interest.response.InterestDto;
import com.poppin.poppinserver.exception.CommonException;
import com.poppin.poppinserver.exception.ErrorCode;
import com.poppin.poppinserver.repository.*;
import com.poppin.poppinserver.type.EPopupTopic;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class InterestService {
    private final UserRepository userRepository;
    private final PopupRepository popupRepository;
    private final InterestRepository interestRepository;

    private final FCMService fcmService;
    @Transactional // 쿼리 5번 날라감. 최적화 필요
    public InterestDto userAddInterest(Long popupId, Long userId, String token){
        //중복검사
        interestRepository.findByUserIdAndPopupId(userId, popupId)
                .ifPresent(interest -> {
                    throw new CommonException(ErrorCode.DUPLICATED_INTEREST);
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POPUP));

        Interest interest = Interest.builder()
                .user(user)
                .popup(popup)
                .build();

        interestRepository.save(interest);

        popup.addInterestCnt();

        /*알림 구독*/
        String fcmToken = token;

        fcmService.fcmAddTopic(fcmToken, popup, EPopupTopic.MAGAM);
        fcmService.fcmAddTopic(fcmToken, popup, EPopupTopic.OPEN);
        fcmService.fcmAddTopic(fcmToken, popup, EPopupTopic.CHANGE_INFO);

        return InterestDto.fromEntity(interest,user,popup);
    }

    public Boolean removeInterest(Long userId, Long popupId, String fcmToken){
        Interest interest = interestRepository.findByUserIdAndPopupId(userId, popupId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE));

        interestRepository.delete(interest);

//        /*FCM 구독취소*/
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POPUP));

        fcmService.fcmRemoveTopic(fcmToken,popup, EPopupTopic.MAGAM);
        fcmService.fcmRemoveTopic(fcmToken,popup, EPopupTopic.OPEN);
        fcmService.fcmRemoveTopic(fcmToken,popup, EPopupTopic.CHANGE_INFO);
        return true;
    }
}
