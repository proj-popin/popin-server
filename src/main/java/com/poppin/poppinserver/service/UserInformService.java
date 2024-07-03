package com.poppin.poppinserver.service;

import com.poppin.poppinserver.domain.*;
import com.poppin.poppinserver.dto.common.PageInfoDto;
import com.poppin.poppinserver.dto.common.PagingResponseDto;
import com.poppin.poppinserver.dto.popup.request.CreatePreferedDto;
import com.poppin.poppinserver.dto.popup.request.CreateTasteDto;
import com.poppin.poppinserver.dto.popup.response.ManageListDto;
import com.poppin.poppinserver.dto.userInform.request.UpdateUserInfromDto;
import com.poppin.poppinserver.dto.userInform.response.UserInformDto;
import com.poppin.poppinserver.dto.userInform.response.UserInformSummaryDto;
import com.poppin.poppinserver.exception.CommonException;
import com.poppin.poppinserver.exception.ErrorCode;
import com.poppin.poppinserver.repository.*;
import com.poppin.poppinserver.type.EInformProgress;
import com.poppin.poppinserver.type.EOperationStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInformService {
    private final UserInformRepository userInformRepository;
    private final PopupRepository popupRepository;
    private final TastePopupRepository tastePopupRepository;
    private final PosterImageRepository posterImageRepository;
    private final UserRepository userRepository;
    private final AlarmKeywordRepository alarmKeywordRepository;
    private final PreferedPopupRepository preferedPopupRepository;

    private final S3Service s3Service;

    @Transactional
    public UserInformDto createUserInform(String name, String contactLink, Boolean fashionBeauty, Boolean characters,
                                          Boolean foodBeverage, Boolean webtoonAni, Boolean interiorThings,
                                          Boolean movie, Boolean musical, Boolean sports, Boolean game, Boolean itTech,
                                          Boolean kpop, Boolean alcohol, Boolean animalPlant, Boolean etc,
                                          List<MultipartFile> images, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        TastePopup tastePopup = TastePopup.builder()
                .fasionBeauty(fashionBeauty)
                .characters(characters)
                .foodBeverage(foodBeverage)
                .webtoonAni(webtoonAni)
                .interiorThings(interiorThings)
                .movie(movie)
                .musical(musical)
                .sports(sports)
                .game(game)
                .itTech(itTech)
                .kpop(kpop)
                .alchol(alcohol)
                .animalPlant(animalPlant)
                .etc(etc)
                .build();
        tastePopupRepository.save(tastePopup);

        PreferedPopup preferedPopup = PreferedPopup.builder()
                .wantFree(false)
                .market(false)
                .experience(false)
                .display(false)
                .build();
        preferedPopupRepository.save(preferedPopup);

        Popup popup = Popup.builder()
                .name(name)
                .tastePopup(tastePopup)
                .preferedPopup(preferedPopup)
                .operationStatus(EOperationStatus.EXECUTING.getStatus())
                .build();
        popup = popupRepository.save(popup);
        log.info(popup.toString());

        // 팝업 이미지 처리 및 저장
        List<String> fileUrls = s3Service.uploadPopupPoster(images, popup.getId());

        List<PosterImage> posterImages = new ArrayList<>();
        for (String url : fileUrls) {
            PosterImage posterImage = PosterImage.builder()
                    .posterUrl(url)
                    .popup(popup)
                    .build();
            posterImages.add(posterImage);
        }
        posterImageRepository.saveAll(posterImages);
        popup.updatePosterUrl(fileUrls.get(0));

        popup = popupRepository.save(popup);

        UserInform userInform = UserInform.builder()
                .informerId(user)
                .popupId(popup)
                .contactLink(contactLink)
                .progress(EInformProgress.NOTEXECUTED)
                .build();
        userInform = userInformRepository.save(userInform);

        return UserInformDto.fromEntity(userInform);
    } // 제보 생성

    @Transactional
    public UserInformDto createGuestUserInform(String name, String contactLink, Boolean fashionBeauty, Boolean characters,
                                          Boolean foodBeverage, Boolean webtoonAni, Boolean interiorThings,
                                          Boolean movie, Boolean musical, Boolean sports, Boolean game, Boolean itTech,
                                          Boolean kpop, Boolean alcohol, Boolean animalPlant, Boolean etc,
                                          List<MultipartFile> images) {

        TastePopup tastePopup = TastePopup.builder()
                .fasionBeauty(fashionBeauty)
                .characters(characters)
                .foodBeverage(foodBeverage)
                .webtoonAni(webtoonAni)
                .interiorThings(interiorThings)
                .movie(movie)
                .musical(musical)
                .sports(sports)
                .game(game)
                .itTech(itTech)
                .kpop(kpop)
                .alchol(alcohol)
                .animalPlant(animalPlant)
                .etc(etc)
                .build();
        tastePopupRepository.save(tastePopup);

        PreferedPopup preferedPopup = PreferedPopup.builder()
                .wantFree(false)
                .market(false)
                .experience(false)
                .display(false)
                .build();
        preferedPopupRepository.save(preferedPopup);

        Popup popup = Popup.builder()
                .name(name)
                .tastePopup(tastePopup)
                .preferedPopup(preferedPopup)
                .operationStatus(EOperationStatus.EXECUTING.getStatus())
                .build();
        popup = popupRepository.save(popup);
        log.info(popup.toString());

        // 팝업 이미지 처리 및 저장
        List<String> fileUrls = s3Service.uploadPopupPoster(images, popup.getId());

        List<PosterImage> posterImages = new ArrayList<>();
        for (String url : fileUrls) {
            PosterImage posterImage = PosterImage.builder()
                    .posterUrl(url)
                    .popup(popup)
                    .build();
            posterImages.add(posterImage);
        }
        posterImageRepository.saveAll(posterImages);
        popup.updatePosterUrl(fileUrls.get(0));

        popup = popupRepository.save(popup);

        UserInform userInform = UserInform.builder()
                .informerId(null)
                .popupId(popup)
                .contactLink(contactLink)
                .progress(EInformProgress.NOTEXECUTED)
                .build();
        userInform = userInformRepository.save(userInform);

        return UserInformDto.fromEntity(userInform);
    } // 제보 생성

    @Transactional
    public UserInformDto readUserInform(Long userInformId){
        UserInform userInform = userInformRepository.findById(userInformId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER_INFORM));

        return UserInformDto.fromEntity(userInform);
    } // 사용자 제보 조회

    @Transactional
    public UserInformDto updateUserInform(UpdateUserInfromDto updateUserInfromDto,
                                          List<MultipartFile> images,
                                          Long adminId){
        UserInform userInform = userInformRepository.findById(updateUserInfromDto.userInformId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER_INFORM));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        CreateTasteDto createTasteDto = updateUserInfromDto.taste();
        TastePopup tastePopup = userInform.getPopupId().getTastePopup();
        tastePopup.update(createTasteDto.fashionBeauty(),
                createTasteDto.characters(),
                createTasteDto.foodBeverage(),
                createTasteDto.webtoonAni(),
                createTasteDto.interiorThings(),
                createTasteDto.movie(),
                createTasteDto.musical(),
                createTasteDto.sports(),
                createTasteDto.game(),
                createTasteDto.itTech(),
                createTasteDto.kpop(),
                createTasteDto.alcohol(),
                createTasteDto.animalPlant(),
                createTasteDto.etc());
        tastePopupRepository.save(tastePopup);

        CreatePreferedDto createPreferedDto = updateUserInfromDto.prefered();
        PreferedPopup preferedPopup = userInform.getPopupId().getPreferedPopup();
        preferedPopup.update(createPreferedDto.market(),
                createPreferedDto.display(),
                createPreferedDto.experience(),
                createPreferedDto.wantFree());
        preferedPopupRepository.save(preferedPopup);

        Popup popup = userInform.getPopupId();

        // 팝업 이미지 처리 및 저장

        // 기존 이미지 싹 지우기
        List<PosterImage> originImages = posterImageRepository.findByPopupId(popup);
        List<String> originUrls = originImages.stream()
                .map(PosterImage::getPosterUrl)
                .collect(Collectors.toList());
        s3Service.deleteMultipleImages(originUrls);
        posterImageRepository.deleteAllByPopupId(popup);

        //새로운 이미지 추가
        List<String> newUrls = s3Service.uploadPopupPoster(images, popup.getId());

        List<PosterImage> posterImages = new ArrayList<>();
        for(String url : newUrls){
            PosterImage posterImage = PosterImage.builder()
                    .posterUrl(url)
                    .popup(popup)
                    .build();
            posterImages.add(posterImage);
        }
        posterImageRepository.saveAll(posterImages);
        popup.updatePosterUrl(newUrls.get(0));

        // 기존 키워드 삭제 및 다시 저장
        alarmKeywordRepository.deleteAll(popup.getAlarmKeywords());

        List<AlarmKeyword> alarmKeywords = new ArrayList<>();
        for(String keyword : updateUserInfromDto.keywords()){
            alarmKeywords.add(AlarmKeyword.builder()
                    .popupId(popup)
                    .keyword(keyword)
                    .build());
        }
        alarmKeywordRepository.saveAll(alarmKeywords);

        popup.update(
                updateUserInfromDto.homepageLink(),
                updateUserInfromDto.name(),
                updateUserInfromDto.introduce(),
                updateUserInfromDto.address(),
                updateUserInfromDto.addressDetail(),
                updateUserInfromDto.entranceReauired(),
                updateUserInfromDto.entranceFee(),
                updateUserInfromDto.resvRequired(),
                updateUserInfromDto.availableAge(),
                updateUserInfromDto.parkingAvailable(),
                updateUserInfromDto.openDate(),
                updateUserInfromDto.closeDate(),
                updateUserInfromDto.openTime(),
                updateUserInfromDto.closeTime(),
                updateUserInfromDto.latitude(),
                updateUserInfromDto.longitude(),
                updateUserInfromDto.operationExcept(),
                EOperationStatus.EXECUTING.getStatus(),
                admin
        );

        userInform.update(EInformProgress.EXECUTING);
        userInform = userInformRepository.save(userInform);
        log.info(userInform.getProgress().toString());

        return UserInformDto.fromEntity(userInform);
    } // 임시저장

    @Transactional
    public UserInformDto uploadPopup(UpdateUserInfromDto updateUserInfromDto,
                                          List<MultipartFile> images,
                                          Long adminId){
        UserInform userInform = userInformRepository.findById(updateUserInfromDto.userInformId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER_INFORM));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        CreateTasteDto createTasteDto = updateUserInfromDto.taste();
        TastePopup tastePopup = userInform.getPopupId().getTastePopup();
        tastePopup.update(createTasteDto.fashionBeauty(),
                createTasteDto.characters(),
                createTasteDto.foodBeverage(),
                createTasteDto.webtoonAni(),
                createTasteDto.interiorThings(),
                createTasteDto.movie(),
                createTasteDto.musical(),
                createTasteDto.sports(),
                createTasteDto.game(),
                createTasteDto.itTech(),
                createTasteDto.kpop(),
                createTasteDto.alcohol(),
                createTasteDto.animalPlant(),
                createTasteDto.etc());
        tastePopupRepository.save(tastePopup);

        CreatePreferedDto createPreferedDto = updateUserInfromDto.prefered();
        PreferedPopup preferedPopup = userInform.getPopupId().getPreferedPopup();
        preferedPopup.update(createPreferedDto.market(),
                createPreferedDto.display(),
                createPreferedDto.experience(),
                createPreferedDto.wantFree());
        preferedPopupRepository.save(preferedPopup);

        Popup popup = userInform.getPopupId();

        // 팝업 이미지 처리 및 저장

        // 기존 이미지 싹 지우기
        List<PosterImage> originImages = posterImageRepository.findByPopupId(popup);
        List<String> originUrls = originImages.stream()
                .map(PosterImage::getPosterUrl)
                .collect(Collectors.toList());
        s3Service.deleteMultipleImages(originUrls);
        posterImageRepository.deleteAllByPopupId(popup);

        //새로운 이미지 추가
        List<String> fileUrls = s3Service.uploadPopupPoster(images, popup.getId());

        List<PosterImage> posterImages = new ArrayList<>();
        for(String url : fileUrls){
            PosterImage posterImage = PosterImage.builder()
                    .posterUrl(url)
                    .popup(popup)
                    .build();
            posterImages.add(posterImage);
        }
        posterImageRepository.saveAll(posterImages);
        popup.updatePosterUrl(fileUrls.get(0));

        // 기존 키워드 삭제 및 다시 저장
        alarmKeywordRepository.deleteAll(popup.getAlarmKeywords());

        List<AlarmKeyword> alarmKeywords = new ArrayList<>();
        for(String keyword : updateUserInfromDto.keywords()){
            alarmKeywords.add(AlarmKeyword.builder()
                    .popupId(popup)
                    .keyword(keyword)
                    .build());
        }
        alarmKeywordRepository.saveAll(alarmKeywords);

        //날짜 요청 유효성 검증
        if (updateUserInfromDto.openDate().isAfter(updateUserInfromDto.closeDate())) {
            throw new CommonException(ErrorCode.INVALID_DATE_PARAMETER);
        }

        //현재 운영상태 정의
        String operationStatus;
        if (updateUserInfromDto.openDate().isAfter(LocalDate.now())){
            operationStatus = EOperationStatus.NOTYET.getStatus();
        } else if (updateUserInfromDto.closeDate().isBefore(LocalDate.now())) {
            operationStatus = EOperationStatus.TERMINATED.getStatus();
        }
        else{
            operationStatus = EOperationStatus.OPERATING.getStatus();
        }

        popup.update(
                updateUserInfromDto.homepageLink(),
                updateUserInfromDto.name(),
                updateUserInfromDto.introduce(),
                updateUserInfromDto.address(),
                updateUserInfromDto.addressDetail(),
                updateUserInfromDto.entranceReauired(),
                updateUserInfromDto.entranceFee(),
                updateUserInfromDto.resvRequired(),
                updateUserInfromDto.availableAge(),
                updateUserInfromDto.parkingAvailable(),
                updateUserInfromDto.openDate(),
                updateUserInfromDto.closeDate(),
                updateUserInfromDto.openTime(),
                updateUserInfromDto.closeTime(),
                updateUserInfromDto.latitude(),
                updateUserInfromDto.longitude(),
                updateUserInfromDto.operationExcept(),
                operationStatus,
                admin
        );

        userInform.update(EInformProgress.EXECUTED);
        userInform = userInformRepository.save(userInform);

        return UserInformDto.fromEntity(userInform);
    } // 제보 최종 업로그

    @Transactional
    public PagingResponseDto reatUserInformList(int page,
                                                int size,
                                                EInformProgress progress){
        Page<UserInform> userInforms = userInformRepository.findAllByProgress(PageRequest.of(page, size), progress);

        PageInfoDto pageInfoDto = PageInfoDto.fromPageInfo(userInforms);
        List<UserInformSummaryDto> userInformSummaryDtos = UserInformSummaryDto.fromEntityList(userInforms.getContent());

        return PagingResponseDto.fromEntityAndPageInfo(userInformSummaryDtos, pageInfoDto);
    } // 제보 리스트 조회
}
