package com.poppin.poppinserver.popup.service;

import com.poppin.poppinserver.alarm.domain.InformAlarm;
import com.poppin.poppinserver.alarm.dto.informAlarm.response.NoticeDto;
import com.poppin.poppinserver.alarm.service.AlarmService;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import com.poppin.poppinserver.core.util.HeaderUtil;
import com.poppin.poppinserver.core.util.SelectRandomUtil;
import com.poppin.poppinserver.interest.domain.Interest;
import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.popup.domain.PreferedPopup;
import com.poppin.poppinserver.popup.domain.TastePopup;
import com.poppin.poppinserver.popup.dto.popup.response.BootstrapDto;
import com.poppin.poppinserver.popup.dto.popup.response.PopupStoreDto;
import com.poppin.poppinserver.popup.repository.PopupRepository;
import com.poppin.poppinserver.popup.repository.specification.PopupSpecification;
import com.poppin.poppinserver.user.domain.User;
import com.poppin.poppinserver.user.repository.UserQueryRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BootstrapService {
    private final PopupRepository popupRepository;
    private final UserQueryRepository userQueryRepository;

    private final AlarmService alarmService;
    private final PopupService popupService;



    private final HeaderUtil headerUtil;
    private final SelectRandomUtil selectRandomUtil;

    @Transactional(readOnly = true)
    public BootstrapDto bootstrap(HttpServletRequest request) {
        Long userId = headerUtil.parseUserId(request);
        if (userId != null && !userQueryRepository.existsById(userId)) {
            throw new CommonException(ErrorCode.ACCESS_DENIED_ERROR);
        }

        // 인기 팝업 조회
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();

        List<Popup> popularTop5Popup = popupRepository.findTopOperatingPopupsByInterestAndViewCount(startOfDay,
                endOfDay,
                PageRequest.of(0, 5));

        // 새로 오픈 팝업 조회
        List<Popup> newlyOpenedPopup = popupRepository.findNewOpenPopupByAll(PageRequest.of(0, 5));

        // 종료 임박 팝업 조회
        List<Popup> popups = popupRepository.findClosingPopupByAll(PageRequest.of(0, 5));

        if (userId != null) { // 로그인 요청일 경우
            List<PopupStoreDto> popularTop5PopupStores = popupService.getPopupStoreDtos(popularTop5Popup, userId);
            List<PopupStoreDto> newlyOpenedPopupStores = popupService.getPopupStoreDtos(newlyOpenedPopup, userId);
            List<PopupStoreDto> closingSoonPopupStores = popupService.getPopupStoreDtos(popups, userId);

            // 취향 저격 팝업 조회
            List<Popup> recommendPopup = getRecommendPopup(userId);
            List<PopupStoreDto> recommendedPopupStores = popupService.getPopupStoreDtos(recommendPopup, userId);

            // 관심 저장 팝업 조회
            User user = userQueryRepository.findById(userId)
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

            Set<Interest> interest = user.getInterest();
            List<Popup> interestedPopup = interest.stream()
                    .map(Interest::getPopup)
                    .toList();

            List<PopupStoreDto> interestedPopupStores = popupService.getPopupStoreDtos(interestedPopup, userId);

            // 공지 조회
            List<InformAlarm> informAlarms = alarmService.getInformAlarms(Long.valueOf(userId));

            List<NoticeDto> notice = NoticeDto.fromEntities(informAlarms);

            return BootstrapDto.builder()
                    .popularTop5PopupStores(popularTop5PopupStores)
                    .newlyOpenedPopupStores(newlyOpenedPopupStores)
                    .closingSoonPopupStores(closingSoonPopupStores)
                    .interestedPopupStores(interestedPopupStores)
                    .recommendedPopupStores(recommendedPopupStores)
                    .notices(notice)
                    .build();
        } else { // 비로그인 요청일 경우 유저 관련 로직 생략
            List<PopupStoreDto> popularTop5PopupStores = popupService.guestGetPopupStoreDtos(popularTop5Popup);
            List<PopupStoreDto> newlyOpenedPopupStores = popupService.guestGetPopupStoreDtos(newlyOpenedPopup);
            List<PopupStoreDto> closingSoonPopupStores = popupService.guestGetPopupStoreDtos(popups);

            return BootstrapDto.builder()
                    .popularTop5PopupStores(popularTop5PopupStores)
                    .newlyOpenedPopupStores(newlyOpenedPopupStores)
                    .closingSoonPopupStores(closingSoonPopupStores)
                    .interestedPopupStores(null)
                    .recommendedPopupStores(null)
                    .notices(null)
                    .build();
        }

    } // 부트스트랩 로딩 api

    @Transactional
    public List<Popup> getRecommendPopup(Long userId) {
        // 사용자가 설정한 태그의 팝업들 5개씩 다 가져오기
        // 태그의 개수만큼 랜덤 변수 생성해서 하나 뽑기
        // 5개 선정
        // 관심 테이블에서

        User user = userQueryRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        //취향설정이 되지 않은 유저의 경우
        if (user.getTastePopup() == null || user.getPreferedPopup() == null || user.getWhoWithPopup() == null) {
            return null;
        }

        List<List<Popup>> popups = new ArrayList<>();
        List<String> selectedList = new ArrayList<>();

        // 사용자가 설정한 카테고리에 해당하는 팝업들을 카테고리 별로 5개씩 리스트에 저장
        TastePopup tastePopup = user.getTastePopup();
        List<String> selectedTaste = selectRandomUtil.selectTaste(tastePopup);
        for (String taste : selectedTaste) {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "viewCnt"));
            Specification<Popup> combinedSpec = Specification.where(PopupSpecification.hasTaste(taste, true))
                    .and(PopupSpecification.isOperating());

            List<Popup> popupList = popupRepository.findAll(combinedSpec, pageable).getContent();

            if (!popupList.isEmpty()) {
                selectedList.add(taste);
                popups.add(popupList);
            }

        }

        PreferedPopup preferedPopup = user.getPreferedPopup();
        List<String> selectedPrefered = selectRandomUtil.selectPreference(preferedPopup);
        for (String prefered : selectedPrefered) {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "viewCnt"));
            Specification<Popup> combinedSpec = Specification.where(PopupSpecification.hasPrefered(prefered, true))
                    .and(PopupSpecification.isOperating());

            List<Popup> popupList = popupRepository.findAll(combinedSpec, pageable).getContent();

            if (!popupList.isEmpty()) {
                selectedList.add(prefered);
                popups.add(popupList);
            }
        }

        if (selectedList.isEmpty()) {
            return null;
        }
        Random random = new Random();
        Integer randomIndex = random.nextInt(selectedList.size());

        log.info("취향 저격 " + selectedList.get(randomIndex));

        return popups.get(randomIndex);
    } // 취향저격 팝업 조회
}
