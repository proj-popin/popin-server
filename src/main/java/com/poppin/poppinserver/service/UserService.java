package com.poppin.poppinserver.service;

import com.poppin.poppinserver.domain.*;
import com.poppin.poppinserver.dto.faq.response.FaqResponseDto;
import com.poppin.poppinserver.dto.popup.response.*;
import com.poppin.poppinserver.dto.review.response.ReviewFinishDto;
import com.poppin.poppinserver.dto.review.response.ReviewUncertiDto;
import com.poppin.poppinserver.dto.review.response.ReviewCertiDto;
import com.poppin.poppinserver.dto.user.request.CreateUserTasteDto;
import com.poppin.poppinserver.dto.user.request.UserInfoDto;
import com.poppin.poppinserver.dto.user.response.NicknameDto;
import com.poppin.poppinserver.dto.user.response.UserMypageDto;
import com.poppin.poppinserver.dto.user.response.UserPreferenceSettingDto;
import com.poppin.poppinserver.dto.user.response.UserProfileDto;
import com.poppin.poppinserver.dto.visitorData.response.VisitorDataRvDto;
import com.poppin.poppinserver.exception.CommonException;
import com.poppin.poppinserver.exception.ErrorCode;
import com.poppin.poppinserver.repository.*;
import com.poppin.poppinserver.util.RandomNicknameUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PreferedPopupRepository preferedPopupRepository;
    private final TastePopupRepository tastePopupRepository;
    private final WhoWithPopupRepository whoWithPopupRepository;
    private final ReviewRepository reviewRepository;
    private final PopupRepository popupRepository;
    private final VisitRepository visitRepository;
    private final VisitorDataRepository visitorDataRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final S3Service s3Service;
    private final FreqQuestionRepository freqQuestionRepository;
    private final PosterImageRepository posterImageRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final InterestRepository interestRepository;
    private final ReviewRecommendUserRepository reviewRecommendUserRepository;
    private final UserInformRepository userInformRepository;
    private final ManagerInformRepository managerInformRepository;
    private final ModifyInformRepository modifyInfoRepository;
    private final ModifyImageReposiroty modifyImageReposiroty;
    private final ReportReviewRepository reportReviewRepository;
    private final ReportPopupRepository reportPopupRepository;
    private final NotificationRepository notificationRepository;
    private final BlockedPopupRepository blockedPopupRepository;

    @Transactional
    public UserTasteDto createUserTaste(
            Long userId,
            CreateUserTasteDto createUserTasteDto
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        if (user.getPreferedPopup() != null) {
            throw new CommonException(ErrorCode.ALREADY_EXISTS_PREFERENCE);
        }
        PreferedPopup preferedPopup = PreferedPopup.builder()
                .market(createUserTasteDto.preference().market())
                .display(createUserTasteDto.preference().display())
                .experience(createUserTasteDto.preference().experience())
                .wantFree(createUserTasteDto.preference().wantFree())
                .build();
        preferedPopupRepository.save(preferedPopup);

        if (user.getTastePopup() != null) {
            throw new CommonException(ErrorCode.ALREADY_EXISTS_PREFERENCE);
        }
        TastePopup tastePopup = TastePopup.builder()
                .fasionBeauty(createUserTasteDto.taste().fashionBeauty())
                .characters(createUserTasteDto.taste().characters())
                .foodBeverage(createUserTasteDto.taste().foodBeverage())
                .webtoonAni(createUserTasteDto.taste().webtoonAni())
                .interiorThings(createUserTasteDto.taste().interiorThings())
                .movie(createUserTasteDto.taste().movie())
                .musical(createUserTasteDto.taste().musical())
                .sports(createUserTasteDto.taste().sports())
                .game(createUserTasteDto.taste().game())
                .itTech(createUserTasteDto.taste().itTech())
                .kpop(createUserTasteDto.taste().kpop())
                .alcohol(createUserTasteDto.taste().alcohol())
                .animalPlant(createUserTasteDto.taste().animalPlant())
                .build();
        tastePopupRepository.save(tastePopup);

        if (user.getWhoWithPopup() != null) {
            throw new CommonException(ErrorCode.ALREADY_EXISTS_PREFERENCE);
        }
        WhoWithPopup whoWithPopup = WhoWithPopup.builder()
                .solo(createUserTasteDto.whoWith().solo())
                .withFriend(createUserTasteDto.whoWith().withFriend())
                .withFamily(createUserTasteDto.whoWith().withFamily())
                .withLover(createUserTasteDto.whoWith().withLover())
                .build();
        whoWithPopupRepository.save(whoWithPopup);

        user.updatePopupTaste(preferedPopup, tastePopup, whoWithPopup);
        userRepository.save(user);

        return UserTasteDto.builder()
                .preference(PreferedDto.fromEntity(preferedPopup))
                .taste(TasteDto.fromEntity(tastePopup))
                .whoWith(WhoWithDto.fromEntity(whoWithPopup))
                .build();
    }

    @Transactional
    public UserTasteDto readUserTaste(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        if (user.getPreferedPopup() == null) {
            PreferedPopup preferedPopup = createDefaultPreferedPopup();
            preferedPopupRepository.save(preferedPopup);
            user.updatePopupTaste(preferedPopup);
        }

        if (user.getTastePopup() == null) {
            TastePopup tastePopup = createDefaultTastePopup();
            tastePopupRepository.save(tastePopup);
            user.updatePopupTaste(tastePopup);
        }

        if (user.getWhoWithPopup() == null) {
            WhoWithPopup whoWithPopup = createDefaultWhoWithPopup();
            whoWithPopupRepository.save(whoWithPopup);
            user.updatePopupTaste(whoWithPopup);
        }

        userRepository.save(user);
        return UserTasteDto.builder()
                .preference(PreferedDto.fromEntity(user.getPreferedPopup()))
                .taste(TasteDto.fromEntity(user.getTastePopup()))
                .whoWith(WhoWithDto.fromEntity(user.getWhoWithPopup()))
                .build();
    }

    @Transactional
    public UserTasteDto updateUserTaste(Long userId, CreateUserTasteDto createUserTasteDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        PreferedPopup preferedPopup = user.getPreferedPopup();
        preferedPopup.update(createUserTasteDto.preference().market(),
                createUserTasteDto.preference().display(),
                createUserTasteDto.preference().experience(),
                createUserTasteDto.preference().wantFree());
        preferedPopupRepository.save(preferedPopup);

        TastePopup tastePopup = user.getTastePopup();
        tastePopup.update(createUserTasteDto.taste().fashionBeauty(),
                createUserTasteDto.taste().characters(),
                createUserTasteDto.taste().foodBeverage(),
                createUserTasteDto.taste().webtoonAni(),
                createUserTasteDto.taste().interiorThings(),
                createUserTasteDto.taste().movie(),
                createUserTasteDto.taste().musical(),
                createUserTasteDto.taste().sports(),
                createUserTasteDto.taste().game(),
                createUserTasteDto.taste().itTech(),
                createUserTasteDto.taste().kpop(),
                createUserTasteDto.taste().alcohol(),
                createUserTasteDto.taste().animalPlant(),
                null);
        tastePopupRepository.save(tastePopup);

        WhoWithPopup whoWithPopup = user.getWhoWithPopup();
        whoWithPopup.update(createUserTasteDto.whoWith().solo(),
                createUserTasteDto.whoWith().withFriend(),
                createUserTasteDto.whoWith().withFamily(),
                createUserTasteDto.whoWith().withLover());
        whoWithPopupRepository.save(whoWithPopup);

        user.updatePopupTaste(preferedPopup, tastePopup, whoWithPopup);
        userRepository.save(user);

        return UserTasteDto.builder()
                .preference(PreferedDto.fromEntity(preferedPopup))
                .taste(TasteDto.fromEntity(tastePopup))
                .whoWith(WhoWithDto.fromEntity(whoWithPopup))
                .build();
    }

    public UserMypageDto readUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        return UserMypageDto.builder()
                .userImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .writtenReview(user.getReviewCnt())
                .reviewCnt(user.getCertifiedReview())
                .build();
    }

    public UserProfileDto readUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        return UserProfileDto.builder()
                .email(user.getEmail())
                .userImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .provider(user.getProvider())
                .build();
    }

    public String createProfileImage(Long userId, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        String profileImageUrl = s3Service.uploadUserProfile(profileImage, userId);
        user.updateProfileImage(profileImageUrl);
        userRepository.save(user);

        return user.getProfileImageUrl();
    }

    public String updateProfileImage(Long userId, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        String profileImageUrl = s3Service.replaceImage(user.getProfileImageUrl(), profileImage, userId);
        user.updateProfileImage(profileImageUrl);
        userRepository.save(user);

        return user.getProfileImageUrl();
    }

    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        s3Service.deleteImage(user.getProfileImageUrl());
        user.deleteProfileImage();
        userRepository.save(user);
    }

    public UserProfileDto updateUserNickname(Long userId, UserInfoDto userInfoDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        if (userRepository.findByNickname(userInfoDto.nickname()).isPresent() && (userId != user.getId())) {
            throw new CommonException(ErrorCode.DUPLICATED_NICKNAME);
        }
        user.updateUserNickname(userInfoDto.nickname());
        userRepository.save(user);

        return UserProfileDto.builder()
                .provider(user.getProvider())
                .userImageUrl(user.getProfileImageUrl())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        user.softDelete();
        userRepository.save(user);
    }

    /*마이페이지 - 작성완료 후기 조회*/
    public List<ReviewFinishDto> getFinishReviewList(Long userId){

        List<ReviewFinishDto> reviewFinishDtoList = new ArrayList<>();
        List<Review> reviewList = reviewRepository.findByUserId(userId);

        for (Review review : reviewList){
            // 팝업 정보
            Popup popup = popupRepository.findByReviewId(review.getPopup().getId());

            // 팝업 이미지 정보
            List<PosterImage> posterImages  = posterImageRepository.findAllByPopupId(popup);
            List<String> imageList = new ArrayList<>();
            if (!posterImages.isEmpty())
            {
                for(PosterImage posterImage : posterImages){
                    imageList.add(posterImage.getPosterUrl());
                }
            }else{
                imageList.add(null);
            }

            ReviewFinishDto reviewFinishDto = ReviewFinishDto.fromEntity(review.getId(), popup.getId(), popup.getName(), review.getIsCertificated(),review.getCreatedAt(),imageList);
            reviewFinishDtoList.add(reviewFinishDto);
        }
        return reviewFinishDtoList;
    }

    /*마이페이지 - 작성 완료 후기 조회 - 인증 후기 보기*/
    public ReviewCertiDto getCertifiedReview(Long userId, Long reviewId, Long popupId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POPUP)); /*여기서 인증된 후기의 popupId로 조회한다*/

        Review review = reviewRepository.findByIdAndPopupId(reviewId, popupId); /* 후기 */
        if (review == null) throw new CommonException(ErrorCode.NOT_FOUND_REVIEW);

        Visit visit = visitRepository.findByUserIdAndPopupId(userId, popupId);
        if (visit == null) throw new CommonException(ErrorCode.NOT_FOUND_REALTIMEVISIT); /*인증 테이블에 값이 없으면 익셉션 처리*/

        VisitorData visitorData = visitorDataRepository.findByReviewIdAndPopupId(reviewId,popupId);
        VisitorDataRvDto visitorDataRvDto = VisitorDataRvDto.fromEntity(visitorData.getVisitDate(),visitorData.getSatisfaction(),visitorData.getCongestion());

        List<String> reviewImageListUrl = reviewImageRepository.findUrlAllByReviewId(reviewId); /*url을 모두 받기*/

        return ReviewCertiDto.fromEntity(
                popup.getName(),
                popup.getPosterUrl(),
                review.getIsCertificated(),
                user.getNickname(),
                visit.getCreatedAt(),
                review.getCreatedAt(),
                visitorDataRvDto,
                review.getText(),
                reviewImageListUrl
        );
    }

    /*마이페이지 - 작성완료 후기 조회 - 일반 후기 보기*/
    public ReviewUncertiDto getUncertifiedReview(Long userId, Long reviewId, Long popupId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_POPUP)); /*여기서 인증된 후기의 popupId로 조회한다*/

        Review review = reviewRepository.findByIdAndPopupId(reviewId, popupId); /* 후기 */
        if (review == null )throw new CommonException(ErrorCode.NOT_FOUND_REVIEW);

        VisitorData visitorData = visitorDataRepository.findByReviewIdAndPopupId(reviewId,popupId);
        VisitorDataRvDto visitorDataRvDto = VisitorDataRvDto.fromEntity(visitorData.getVisitDate(),visitorData.getSatisfaction(),visitorData.getCongestion());

        List<String> reviewImageListUrl = reviewImageRepository.findUrlAllByReviewId(reviewId); /*url을 모두 받기*/

        return ReviewUncertiDto.fromEntity(
                popup.getName(),
                popup.getPosterUrl(),
                review.getIsCertificated(),
                user.getNickname(),
                review.getCreatedAt(),
                visitorDataRvDto,
                review.getText(),
                reviewImageListUrl
        );
    }

    public List<PopupCertiDto> getCertifiedPopupList(Long userId){
        /* 1. userId로 visit 리스트 뽑기
         *  2. visit 리스트의 popupid 와 popup의 id 일치하는 popup 뽑기
         */
        List<Visit> visitList = visitRepository.findAllByUserId(userId);
        if (visitList.isEmpty())throw new CommonException(ErrorCode.NOT_FOUND_VISIT);

        List<PopupCertiDto> popupCertiDtoList = new ArrayList<>();

        for (Visit visit : visitList){
            Long vdPopupId = visit.getPopup().getId();
            Popup popup = popupRepository.findTopByPopupId(vdPopupId);
            PopupCertiDto popupCertiDto = PopupCertiDto.fromEntity(popup.getName(),popup.getPosterUrl(),visit.getCreatedAt());
            popupCertiDtoList.add(popupCertiDto);
        }
        return popupCertiDtoList;
    }

    public List<FaqResponseDto> readFAQs() {
        List<FreqQuestion> freqQuestionList = freqQuestionRepository.findAllByOrderByCreatedAtDesc();
        List<FaqResponseDto> faqDtoList = new ArrayList<>();
        for (FreqQuestion freqQuestion : freqQuestionList) {
            faqDtoList.add(FaqResponseDto.builder()
                    .id(freqQuestion.getId())
                    .question(freqQuestion.getQuestion())
                    .answer(freqQuestion.getAnswer())
                    .createdAt(freqQuestion.getCreatedAt().toString())
                    .build());
        }
        return faqDtoList;
    }

    public NicknameDto generateRandomNickname() {
        String randomNickname = RandomNicknameUtil.generateRandomNickname();
        return new NicknameDto(randomNickname);
    }

    public void deleteAllRelatedInfo(User user) {
        Long userId = user.getId();
        visitRepository.deleteAllByUserId(userId);  // 유저 팝업 방문 삭제
        interestRepository.deleteAllByUserId(userId);  // 유저 팝업 관심 등록 전부 삭제
        reviewRecommendUserRepository.deleteAllByUserId(userId);    // 유저가 누른 모든 추천 삭제
        deleteUserReports(userId);   // 유저가 남긴 모든 신고 삭제
        deleteUserReviews(userId);  // 유저가 남긴 모든 후기 삭제
        deleteInformRequests(userId);   // 유저가 남긴 모든 제보 삭제
        deleteUserModifyInfoRequests(userId);    // 유저가 남긴 모든 정보수정요청 삭제
        if (user.getProfileImageUrl() != null) {
            s3Service.deleteImage(user.getProfileImageUrl());   // 유저 프로필 이미지 S3에서도 삭제
        }
        deleteUserNotificationAlarmInfo(userId);    // 유저 공지사항 알람 삭제
        deleteBlockedPopups(userId);    // 팝업 차단 목록 삭제
        deleteBlockedUsers(userId);    // 유저 차단 목록 삭제
    }

    /*
        유저가 작성한 모든 후기 삭제
     */
    private void deleteUserReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);

        // 후기 이미지 삭제
        for (Review review : reviews) {
            Long reviewId = review.getId();
            List<ReviewImage> reviewImages = reviewImageRepository.findAllByReviewId(reviewId);
            // S3에서 삭제
            for (ReviewImage reviewImage : reviewImages) {
                s3Service.deleteImage(reviewImage.getImageUrl());
                log.info("Deleting image from S3: {}", reviewImage.getImageUrl());
            }
            // DB에서 삭제
            reviewImageRepository.deleteAllByReviewId(reviewId);
            log.info("Deleting review images from DB for reviewId: {}", reviewId);
            // 방문자 데이터 삭제
            deleteUserVisitData(reviewId);
            // 후기 추천 삭제
            deleteReviewRecommend(reviewId);
            // 후기 신고 삭제
            reportReviewRepository.deleteAllByReviewId(reviewId);
        }

        // 모든 후기 삭제
        reviewRepository.deleteAllByUserId(userId);
        log.info("Finished deleting reviews for userId: {}", userId);
    }

    /*
        유저 방문자 데이터 삭제
    */
    private void deleteUserVisitData(Long reviewId) {
        visitorDataRepository.deleteAllByReviewId(reviewId);
    }

    /*
        유저 후기 추천 삭제
     */
    private void deleteReviewRecommend(Long reviewId) {
        reviewRecommendUserRepository.deleteAllByReviewId(reviewId);
    }

    /*
        유저가 작성한 모든 제보 삭제
     */
    private void deleteInformRequests(Long userId) {
        userInformRepository.deleteAllByInformerId(userId);
        managerInformRepository.deleteAllByInformerId(userId);
    }

    /*
        유저가 작성한 모든 정보수정요청 삭제
     */
    private void deleteUserModifyInfoRequests(Long userId) {
        List<ModifyInfo> modifyInfos = modifyInfoRepository.findAllByUserId(userId);
        for (ModifyInfo modifyInfo : modifyInfos) {
            Long modifyId = modifyInfo.getId();
            modifyImageReposiroty.deleteAllByModifyId(modifyId);
        }
        modifyInfoRepository.deleteAllByUserId(userId);
    }

    /*
        유저가 작성한 모든 신고 삭제
     */
    private void deleteUserReports(Long userId) {
        reportReviewRepository.deleteAllByUserId(userId);
        reportPopupRepository.deleteAllByUserId(userId);
    }

    /*
        유저 차단 목록 삭제
     */
    private void deleteBlockedUsers(Long userId) {
        blockedUserRepository.deleteAllByUserId(userId);
        blockedUserRepository.deleteAllByBlockedId(userId);
    }

    /*
        유저 공지 사항 알람 정보 삭제
     */
    private void deleteUserNotificationAlarmInfo(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
    }
    /*
        팝업 차단 목록 삭제
     */
    private void deleteBlockedPopups(Long userId) {
        blockedPopupRepository.deleteAllByUserId(userId);
    }

    public void addReviewCnt(User user){
        user.addReviewCnt();
        userRepository.save(user);
    }

    @Transactional
    public UserPreferenceSettingDto readUserPreferenceSettingCreated(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        boolean isPreferenceSettingCreated = updatePreferenceSettings(user);

        return UserPreferenceSettingDto.builder()
                .isPreferenceSettingCreated(isPreferenceSettingCreated)
                .build();
    }

    private boolean updatePreferenceSettings(User user) {
        boolean hasPreferences = true;

        PreferedPopup preferedPopup = user.getPreferedPopup();
        TastePopup tastePopup = user.getTastePopup();
        WhoWithPopup whoWithPopup = user.getWhoWithPopup();

        if (preferedPopup == null) {
            preferedPopup = createDefaultPreferedPopup();
            preferedPopupRepository.save(preferedPopup);
            user.updatePopupTaste(preferedPopup);
        }

        if (tastePopup == null) {
            tastePopup = createDefaultTastePopup();
            tastePopupRepository.save(tastePopup);
            user.updatePopupTaste(tastePopup);
        }

        if (whoWithPopup == null) {
            whoWithPopup = createDefaultWhoWithPopup();
            whoWithPopupRepository.save(whoWithPopup);
            user.updatePopupTaste(whoWithPopup);
        }

        userRepository.save(user);

        if (preferedPopup.getDisplay() == false && preferedPopup.getExperience() == false &&
                preferedPopup.getMarket() == false && preferedPopup.getWantFree() == false &&
            tastePopup.getAlcohol() == false && tastePopup.getAnimalPlant() == false &&
                tastePopup.getCharacters() == false && tastePopup.getFashionBeauty() == false &&
                tastePopup.getFoodBeverage() == false && tastePopup.getGame() == false &&
                tastePopup.getInteriorThings() == false && tastePopup.getItTech() == false &&
                tastePopup.getKpop() == false && tastePopup.getMovie() == false &&
                tastePopup.getMusical() == false && tastePopup.getSports() == false &&
                tastePopup.getWebtoonAni() == false &&
            whoWithPopup.getSolo() == false && whoWithPopup.getWithFamily() == false &&
                whoWithPopup.getWithFriend() == false && whoWithPopup.getWithLover() == false
            ) {
            hasPreferences = false;
        }

        return hasPreferences;
    }

    private PreferedPopup createDefaultPreferedPopup() {
        return PreferedPopup.builder()
                .market(false)
                .display(false)
                .experience(false)
                .wantFree(false)
                .build();
    }

    private TastePopup createDefaultTastePopup() {
        return TastePopup.builder()
                .fasionBeauty(false)
                .characters(false)
                .foodBeverage(false)
                .webtoonAni(false)
                .interiorThings(false)
                .movie(false)
                .musical(false)
                .sports(false)
                .game(false)
                .itTech(false)
                .kpop(false)
                .alcohol(false)
                .animalPlant(false)
                .build();
    }

    private WhoWithPopup createDefaultWhoWithPopup() {
        return WhoWithPopup.builder()
                .solo(false)
                .withFriend(false)
                .withFamily(false)
                .withLover(false)
                .build();
    }

    @Transactional
    public void createblockedUser(Long userId, Long blockUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        User blockedUser = userRepository.findById(blockUserId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        if (userId.equals(blockUserId)) {
            throw new CommonException(ErrorCode.CANNOT_BLOCK_MYSELF);
        }

        Optional<BlockedUser> checkBlockedUser = blockedUserRepository.findByUserIdAndBlockedUserId(userId, blockUserId);
        if (checkBlockedUser.isPresent()) {
            throw new CommonException(ErrorCode.ALREADY_BLOCKED_USER);
        }

        BlockedUser createBlockedUser = BlockedUser.builder()
                .userId(user)
                .blockedUserId(blockedUser)
                .build();
        blockedUserRepository.save(createBlockedUser);
    }
}
