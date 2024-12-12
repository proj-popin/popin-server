package com.poppin.poppinserver.visit.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.poppin.poppinserver.alarm.domain.FCMToken;
import com.poppin.poppinserver.alarm.usecase.TokenQueryUseCase;
import com.poppin.poppinserver.alarm.usecase.TopicCommandUseCase;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import com.poppin.poppinserver.core.type.EPopupTopic;
import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.popup.dto.popup.request.VisitorsInfoDto;
import com.poppin.poppinserver.popup.dto.popup.response.PopupStoreDto;
import com.poppin.poppinserver.popup.usecase.BlockedPopupQueryUseCase;
import com.poppin.poppinserver.popup.usecase.PopupQueryUseCase;
import com.poppin.poppinserver.user.domain.User;
import com.poppin.poppinserver.user.usecase.UserQueryUseCase;
import com.poppin.poppinserver.visit.domain.Visit;
import com.poppin.poppinserver.visit.dto.visitorData.response.VisitorDataInfoDto;
import com.poppin.poppinserver.visit.repository.VisitRepository;
import com.poppin.poppinserver.visit.usecase.VisitorDataQueryUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;


    private final UserQueryUseCase userQueryUseCase;
    private final BlockedPopupQueryUseCase blockedPopupQueryUseCase;
    private final PopupQueryUseCase popupQueryUseCase;
    private final VisitorDataQueryUseCase visitorDataQueryUseCase;
    private final TokenQueryUseCase tokenQueryUseCase;
    private final TopicCommandUseCase topicCommandUseCase;

    /* 실시간 방문자 조회 */
    public Optional<Integer> showRealTimeVisitors(Long popupId) {

        Popup popup = popupQueryUseCase.findPopupById(popupId);

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime thirtyMinutesAgo = now.minus(30, ChronoUnit.MINUTES);

        Optional<Integer> realTimeVisitorsCount = visitRepository.showRealTimeVisitors(popup, thirtyMinutesAgo);

        return realTimeVisitorsCount;
    }

    /*방문하기 버튼 누를 시*/
    public PopupStoreDto visit(Long userId, VisitorsInfoDto visitorsInfoDto) throws FirebaseMessagingException {
        Long popupId = Long.valueOf(visitorsInfoDto.popupId());

        User user = userQueryUseCase.findUserById(userId);
        Popup popup = popupQueryUseCase.findPopupById(popupId);

        /*30분 전 시간*/
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minus(30, ChronoUnit.MINUTES);

        Integer duplicateVisitors = visitRepository.findDuplicateVisitors(userId, popup.getId(), thirtyMinutesAgo);
        if (duplicateVisitors > 0) {
            throw new CommonException(ErrorCode.DUPLICATED_REALTIME_VISIT); // 30분 이내 재 방문 방지
        }

        Optional<Visit> visit = visitRepository.findByUserId(userId, popupId);

        //재오픈인경우
        if (visit.isPresent()) {
            visit.get().changeStatus("VISIT_NOW");
            visitRepository.save(visit.get());
        } else {
            //방문하기
            Visit visitor = Visit.builder()
                    .user(user)
                    .popup(popup)
                    .status("VISIT_COMPLETE")
                    .build();

            visitRepository.save(visitor);
            user.addVisitedPopupCnt();

            Optional<FCMToken> token = tokenQueryUseCase.findTokenByUserId(userId);
            if (token.isEmpty()) {
                throw new CommonException(ErrorCode.NOT_FOUND_TOKEN);
            } else {
                topicCommandUseCase.subscribePopupTopic(token.get(), popup, EPopupTopic.HOOGI);
            }

            Optional<Integer> realTimeVisitorsCount = visitRepository.showRealTimeVisitors(popup,
                    thirtyMinutesAgo); /*실시간 방문자 수*/

            if (realTimeVisitorsCount.isEmpty()) {
                realTimeVisitorsCount = Optional.of(0);
            } // empty 면 0으로.
        }

        VisitorDataInfoDto visitorDataDto = visitorDataQueryUseCase.findVisitorData(popup.getId());
        Optional<Integer> visitorCnt = showRealTimeVisitors(popup.getId()); // 실시간 방문자
        Boolean isBlocked = blockedPopupQueryUseCase.existBlockedPopupByUserIdAndPopupId(userId, popup.getId());

        return PopupStoreDto.fromEntity(popup, visitorDataDto, visitorCnt, isBlocked);
    }

    public void changeVisitStatus(Long popupId) {
        List<Visit> visitList = visitRepository.findByPopupId(popupId);
        for (Visit v : visitList) {
            v.changeStatus("RECEIVE_REOPEN_ALERT");
            visitRepository.save(v);
        }
    }
}
