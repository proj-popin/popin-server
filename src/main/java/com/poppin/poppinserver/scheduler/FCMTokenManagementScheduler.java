package com.poppin.poppinserver.scheduler;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.poppin.poppinserver.domain.FCMToken;
import com.poppin.poppinserver.repository.FCMTokenRepository;
import com.poppin.poppinserver.service.FCMTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class FCMTokenManagementScheduler {

    private final FCMTokenRepository fcmTokenRepository;

    private final FCMTokenService fcmTokenService;

    // 3. alarm 테이블 수정
    // 만기 토큰 삭제 스케줄러
    @Scheduled(cron = "0 0 0 * * *")
    private void deleteFCMToken() throws FirebaseMessagingException {

        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        LocalDateTime now = zonedDateTime.toLocalDateTime();

        log.info("token manage scheduler start");
        List<FCMToken> expiredTokenList = fcmTokenRepository.findExpiredTokenList(now);

        for (FCMToken token : expiredTokenList){
            fcmTokenService.fcmRemoveToken(token);
        }
    }
}
