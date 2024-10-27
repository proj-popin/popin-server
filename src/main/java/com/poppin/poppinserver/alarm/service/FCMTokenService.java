package com.poppin.poppinserver.alarm.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.poppin.poppinserver.alarm.domain.FCMToken;
import com.poppin.poppinserver.alarm.domain.PopupTopic;
import com.poppin.poppinserver.alarm.repository.FCMTokenRepository;
import com.poppin.poppinserver.alarm.repository.PopupTopicRepository;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import com.poppin.poppinserver.core.type.EPopupTopic;
import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.user.domain.User;
import com.poppin.poppinserver.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMTokenService {

    private final FCMTokenRepository fcmTokenRepository;

    private final PopupTopicRepository popupTopicRepository;
    private final UserRepository userRepository;
    private final FCMSubscribeService fcmSubscribeService;

    /* FCM TOKEN 등록, 회원가입 시 사용하기 */
    @Transactional
    public void applyFCMToken(String fcmToken, Long userId) {

        log.info("Applying FCM token: {}", fcmToken);

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        // FCM 토큰이 이미 존재하는 경우 -> 에러: 회원가입 한 유저는 새로운 FCM 토큰이어야 함.
        Optional<FCMToken> fcmTokenOptional = fcmTokenRepository.findByTokenOpt(fcmToken);

        if (fcmTokenOptional.isPresent()) {
            throw new CommonException(ErrorCode.ALREADY_EXIST_FCM_TOKEN);
        }

        // FCM 토큰 저장
        FCMToken fcmTokenEntity = new FCMToken(user, fcmToken, LocalDateTime.now());
        fcmTokenRepository.save(fcmTokenEntity);
    }


    public void fcmRemoveToken(FCMToken token) throws FirebaseMessagingException {

        // 관심 팝업 관련 구독 내역 존재 시 전부 삭제
        Optional<List<PopupTopic>> topicsNeedToDelete = popupTopicRepository.findByToken(token);
        if (topicsNeedToDelete.isPresent()) {
            for (PopupTopic topic : topicsNeedToDelete.get()) {
                popupTopicRepository.delete(topic);
                fcmSubscribeService.unsubscribePopupTopic(token, topic.getPopup(), EPopupTopic.MAGAM);
                fcmSubscribeService.unsubscribePopupTopic(token, topic.getPopup(), EPopupTopic.OPEN);
                fcmSubscribeService.unsubscribePopupTopic(token, topic.getPopup(), EPopupTopic.CHANGE_INFO);
            }
        }
        // 토큰 삭제
        fcmTokenRepository.delete(token);
    }

    // 토큰 update 필요 여부 검증 메서드, 앱진입, 로그인 시 사용
    public void verifyFCMToken(Long userId, String fcmToken) {
        log.info("verify token : {}", fcmToken);

        Optional<FCMToken> fcmTokenOptional = fcmTokenRepository.findByUserId(userId);
        if (fcmTokenOptional.isPresent()) {
            String currentToken = fcmTokenOptional.get().getToken();
            if (!currentToken.equals(fcmToken)) {
                fcmTokenOptional.get().setToken(fcmToken);
                fcmTokenRepository.save(fcmTokenOptional.get());
            }
        }
    }

    public void fcmAddPopupTopic(String token, Popup popup, EPopupTopic topic) {
        if (token != null) {
            // 팝업 관련
            try {
                log.info("subscribe popup topic");

                FCMToken pushToken = fcmTokenRepository.findByToken(token);
                if (pushToken == null) {
                    throw new CommonException(ErrorCode.NOT_FOUND_TOKEN);
                }
                fcmSubscribeService.subscribePopupTopic(pushToken, popup, topic); // 관심팝업
            } catch (CommonException | FirebaseMessagingException e) {
                log.error("failed to subscribe popup topic");
                e.printStackTrace();
            }
        } else {
            log.error("토큰이 존재하지 않아 앱푸시 팝업 주제 추가하지 않습니다");
        }
    }

    /*
          Method : 관심 팝업 삭제 시 주제 테이블에 구독 해제
          Author : sakang
          Date   : 2024-04-27
    */
    public void fcmRemovePopupTopic(String token, Popup popup, EPopupTopic topic) {

        try {
            log.info("unsubscribe popup topic");
            FCMToken pushToken = fcmTokenRepository.findByToken(token);
            if (pushToken == null) {
                throw new CommonException(ErrorCode.NOT_FOUND_TOKEN);
            }
            fcmSubscribeService.unsubscribePopupTopic(pushToken, popup, topic); // 구독 및 저장
        } catch (CommonException | FirebaseMessagingException e) {
            log.error("failed to unsubscribe popup topic");
            e.printStackTrace();
        }
    }

    public String resetPopupTopic() {
        List<FCMToken> fcmTokenList = fcmTokenRepository.findAll();
        for (FCMToken token : fcmTokenList) {
            Optional<List<PopupTopic>> topics = popupTopicRepository.findByToken(token);
            if (topics.isPresent()) {
                for (PopupTopic topic : topics.get()) {
                    fcmRemovePopupTopic(token.getToken(), topic.getPopup(), EPopupTopic.MAGAM);
                    fcmRemovePopupTopic(token.getToken(), topic.getPopup(), EPopupTopic.OPEN);
                    fcmRemovePopupTopic(token.getToken(), topic.getPopup(), EPopupTopic.CHANGE_INFO);
                }
            }
        }
        return "finish";
    }

}

