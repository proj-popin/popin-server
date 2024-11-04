package com.poppin.poppinserver.user.service;

import com.poppin.poppinserver.alarm.domain.AlarmSetting;
import com.poppin.poppinserver.alarm.domain.InformAlarm;
import com.poppin.poppinserver.alarm.domain.InformIsRead;
import com.poppin.poppinserver.alarm.domain.PopupAlarm;
import com.poppin.poppinserver.alarm.domain.type.ENotificationCategory;
import com.poppin.poppinserver.alarm.dto.DestinationResponseDto;
import com.poppin.poppinserver.alarm.dto.NotificationResponseDto;
import com.poppin.poppinserver.alarm.repository.InformIsReadRepository;
import com.poppin.poppinserver.alarm.repository.PopupAlarmRepository;
import com.poppin.poppinserver.alarm.service.FCMTokenService;
import com.poppin.poppinserver.core.constant.Constant;
import com.poppin.poppinserver.core.exception.CommonException;
import com.poppin.poppinserver.core.exception.ErrorCode;
import com.poppin.poppinserver.core.util.HeaderUtil;
import com.poppin.poppinserver.core.util.JwtUtil;
import com.poppin.poppinserver.core.util.OAuth2Util;
import com.poppin.poppinserver.core.util.PasswordUtil;
import com.poppin.poppinserver.core.util.RandomCodeUtil;
import com.poppin.poppinserver.interest.domain.Interest;
import com.poppin.poppinserver.interest.repository.InterestRepository;
import com.poppin.poppinserver.popup.dto.popup.response.PopupScrapDto;
import com.poppin.poppinserver.popup.repository.BlockedPopupRepository;
import com.poppin.poppinserver.user.domain.User;
import com.poppin.poppinserver.user.domain.type.EAccountStatus;
import com.poppin.poppinserver.user.domain.type.ELoginProvider;
import com.poppin.poppinserver.user.domain.type.EUserRole;
import com.poppin.poppinserver.user.domain.type.EVerificationType;
import com.poppin.poppinserver.user.dto.auth.request.AccountRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.AppStartRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.AuthLoginRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.EmailVerificationRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.FcmTokenRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.PasswordResetRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.PasswordUpdateRequestDto;
import com.poppin.poppinserver.user.dto.auth.request.PasswordVerificationRequestDto;
import com.poppin.poppinserver.user.dto.auth.response.AccessTokenDto;
import com.poppin.poppinserver.user.dto.auth.response.AccountStatusResponseDto;
import com.poppin.poppinserver.user.dto.auth.response.AuthCodeResponseDto;
import com.poppin.poppinserver.user.dto.auth.response.JwtTokenDto;
import com.poppin.poppinserver.user.dto.user.response.UserActivityResponseDto;
import com.poppin.poppinserver.user.dto.user.response.UserInfoResponseDto;
import com.poppin.poppinserver.user.dto.user.response.UserNoticeResponseDto;
import com.poppin.poppinserver.user.dto.user.response.UserNotificationResponseDto;
import com.poppin.poppinserver.user.dto.user.response.UserPreferenceSettingDto;
import com.poppin.poppinserver.user.dto.user.response.UserRelationDto;
import com.poppin.poppinserver.user.oauth.OAuth2UserInfo;
import com.poppin.poppinserver.user.oauth.apple.AppleOAuthService;
import com.poppin.poppinserver.user.repository.BlockedUserQueryRepository;
import com.poppin.poppinserver.user.repository.UserCommandRepository;
import com.poppin.poppinserver.user.repository.UserQueryRepository;
import com.poppin.poppinserver.user.usecase.UserQueryUseCase;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserQueryRepository userQueryRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final OAuth2Util oAuth2Util;
    private final AppleOAuthService appleOAuthService;
    private final MailService mailService;
    private final UserAlarmSettingService userAlarmSettingService;
    private final UserPreferenceSettingService userPreferenceSettingService;
    private final FCMTokenService fcmTokenService;
    private final InformIsReadRepository informIsReadRepository;
    private final PopupAlarmRepository popupAlarmRepository;
    private final InterestRepository interestRepository;
    private final UserCommandRepository userCommandRepository;
    private final BlockedPopupRepository blockedPopupRepository;
    private final BlockedUserQueryRepository blockedUserQueryRepository;
    private final UserQueryUseCase userQueryUseCase;

    // 유저 이메일 중복 확인 메서드
    private void checkDuplicatedEmail(String email) {
        userQueryRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new CommonException(ErrorCode.DUPLICATED_EMAIL);
                });
    }

    // 유저 비밀번호 및 비밀번호 확인 일치 여부 검증 메서드
    private void checkPasswordMatch(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new CommonException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    // 유저 닉네임 중복 확인 메서드
    private void checkDuplicatedNickname(String nickname) {
        userQueryRepository.findByNickname(nickname)
                .ifPresent(user -> {
                    throw new CommonException(ErrorCode.DUPLICATED_NICKNAME);
                });
    }

    @Transactional
    public Object authSocialLogin(String token, String provider, FcmTokenRequestDto fcmTokenRequestDto) {
        String accessToken = refineToken(token);
        String loginProvider = provider.toUpperCase();
        log.info("loginProvider : " + loginProvider);
        OAuth2UserInfo oAuth2UserInfoDto = getOAuth2UserInfo(loginProvider, accessToken);

        return processUserLogin(
                oAuth2UserInfoDto,
                ELoginProvider.valueOf(loginProvider),
                fcmTokenRequestDto.fcmToken()
        );
    }

//    @Transactional
//    public UserInfoResponseDto socialSignUp(String accessToken,
//                                            SocialRegisterRequestDto socialRegisterRequestDto) {  // 소셜 로그인 후 회원 등록 및 토큰 발급
//        String token = refineToken(accessToken);    // poppin access token
//
//        Long userId = jwtUtil.getUserIdFromToken(token);    // 토큰으로부터 id 추출
//
//        // 소셜 회원가입 시, id와 provider로 유저 정보를 찾음
//        User user = userRepository.findByIdAndELoginProvider(userId,
//                        ELoginProvider.valueOf(socialRegisterRequestDto.provider()))
//                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
//
//        // 닉네임 등록 -> 소셜 회원가입 완료
//        user.register(socialRegisterRequestDto.nickname());
//
//        final JwtTokenDto jwtTokenDto = jwtUtil.generateToken(user.getId(), user.getRole());
//        user.updateRefreshToken(jwtTokenDto.refreshToken());
//        AlarmSetting alarmSetting = userAlarmSettingService.getUserAlarmSetting(socialRegisterRequestDto.fcmToken());
//
//        UserPreferenceSettingDto userPreferenceSettingDto = userPreferenceSettingService.readUserPreferenceSettingCreated(
//                user.getId()
//        );
//
//        UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.fromUserEntity(
//                user,
//                alarmSetting,
//                jwtTokenDto,
//                userPreferenceSettingDto
//        );
//        return userInfoResponseDto;
//    }

    private OAuth2UserInfo getOAuth2UserInfo(String provider, String accessToken) {
        if (provider.equals(ELoginProvider.KAKAO.toString())) {
            return oAuth2Util.getKakaoUserInfo(accessToken);
        } else if (provider.equals(ELoginProvider.NAVER.toString())) {
            return oAuth2Util.getNaverUserInfo(accessToken);
        } else if (provider.equals(ELoginProvider.GOOGLE.toString())) {
            return oAuth2Util.getGoogleUserInfo(accessToken);
        } else if (provider.equals(ELoginProvider.APPLE.toString())) {
            return appleOAuthService.getAppleUserInfo(accessToken);
        } else {
            throw new CommonException(ErrorCode.INVALID_OAUTH2_PROVIDER);
        }
    }

    private String refineToken(String accessToken) {
        if (accessToken.startsWith(Constant.BEARER_PREFIX)) {
            return accessToken.substring(Constant.BEARER_PREFIX.length());
        } else {
            return accessToken;
        }
    }

    private Object processUserLogin(OAuth2UserInfo oAuth2UserInfo, ELoginProvider provider, String fcmToken) {
        Optional<User> user = userQueryRepository.findByEmailAndRole(oAuth2UserInfo.email(), EUserRole.USER);
        // 회원 탈퇴 여부 확인
        if (user.isPresent() && user.get().getIsDeleted()) {
            throw new CommonException(ErrorCode.DELETED_USER_ERROR);
        }

        // 이미 가입된 계정이 있는지 확인
        if (user.isPresent() && !user.get().getProvider().equals(provider)) {
            throw new CommonException(ErrorCode.DUPLICATED_SOCIAL_ID);
        }

        // USER 권한 + 이메일 정보가 DB에 존재 -> 팝핀 토큰 발급 및 로그인 상태 변경
        if (user.isPresent() && user.get().getProvider().equals(provider)) {
            JwtTokenDto jwtTokenDto = jwtUtil.generateToken(user.get().getId(), EUserRole.USER);
            userCommandRepository.updateRefreshTokenAndLoginStatus(user.get().getId(), jwtTokenDto.refreshToken(),
                    true);
            AlarmSetting alarmSetting = userAlarmSettingService.getUserAlarmSetting(fcmToken);
            UserPreferenceSettingDto userPreferenceSettingDto = userPreferenceSettingService.readUserPreferenceSettingCreated(
                    user.get().getId()
            );
            // 유저가 읽은 공지사항 알람 리스트 조회
            List<String> checkedNoticeIds = informIsReadRepository.findReadInformAlarmIdsByFcmToken(
                    fcmToken).stream().map(
                    Object::toString
            ).toList();

            // 유저가 가장 최근에 읽은 공지사항 알람 시간 조회
            String informLastCheckedTime = informIsReadRepository.findLastReadTimeByFcmToken(
                    fcmToken);

            UserNoticeResponseDto userNoticeResponseDto = UserNoticeResponseDto.builder()
                    .lastCheck(informLastCheckedTime)
                    .checkedNoticeIds(checkedNoticeIds)
                    .build();

            // TODO: 여기부터 수정 필요
            DestinationResponseDto destinationResponseDto = DestinationResponseDto.fromProperties(
                    null, null, null, null,
                    null, null, null, null, null
            );

            List<PopupAlarm> userPopupAlarm = popupAlarmRepository.findByFcmToken(fcmToken);
            List<InformIsRead> userInformIsRead = informIsReadRepository.findAllByFcmToken(fcmToken);

            List<NotificationResponseDto> popupNotificationResponseDtoList = userPopupAlarm.stream().map(
                    popupAlarm -> NotificationResponseDto.fromProperties(
                            String.valueOf(popupAlarm.getId()), String.valueOf(user.get().getId()), null,
                            String.valueOf(ENotificationCategory.POPUP),
                            popupAlarm.getTitle(), popupAlarm.getBody(), null, popupAlarm.getIsRead(),
                            String.valueOf(popupAlarm.getCreatedAt()), String.valueOf(popupAlarm.getPopupId()), null,
                            destinationResponseDto
                    )
            ).toList();

            List<NotificationResponseDto> noticeNotificationResponseDtoList = userInformIsRead.stream()
                    .map(informIsRead -> {
                        InformAlarm informAlarm = informIsRead.getInformAlarm();
                        Boolean isRead = informIsRead.getIsRead();

                        return NotificationResponseDto.fromProperties(
                                String.valueOf(informAlarm.getId()),
                                String.valueOf(user.get().getId()),
                                null,
                                String.valueOf(ENotificationCategory.NOTICE),
                                informAlarm.getTitle(),
                                informAlarm.getBody(),
                                null,
                                isRead,
                                String.valueOf(informAlarm.getCreatedAt()),
                                null,
                                String.valueOf(informAlarm.getId()),
                                destinationResponseDto
                        );
                    }).toList();

            UserNotificationResponseDto userNotificationResponseDto = UserNotificationResponseDto.fromDtoList(
                    popupNotificationResponseDtoList,
                    noticeNotificationResponseDtoList
            );

            List<Interest> userInterestPopupList = interestRepository.findByUserId(user.get().getId());

            List<PopupScrapDto> popupScrapDtoList = userInterestPopupList.stream().map(
                    interest -> PopupScrapDto.fromInterest(interest)
            ).toList();

            UserActivityResponseDto userActivities = UserActivityResponseDto.fromProperties(
                    popupScrapDtoList,
                    userNotificationResponseDto
            );

            List<String> blockedPopups = blockedPopupRepository.findAllByUserId(user.get()).stream()
                    .map(blockedPopup -> blockedPopup.getId().toString())
                    .toList();
            List<String> blockedUsers = blockedUserQueryRepository.findAllByUserId(user.get()).stream()
                    .map(blockedUser -> blockedUser.getId().toString())
                    .toList();
            UserRelationDto userRelationDto = UserRelationDto.ofBlockedUserIdsAndPopupIds(blockedUsers, blockedPopups);

            // TODO: 여기까지 수정 필요

            UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.fromUserEntity(
                    user.get(),
                    alarmSetting,
                    jwtTokenDto,
                    userPreferenceSettingDto,
                    userNoticeResponseDto,
                    userActivities,
                    userRelationDto
            );
            return userInfoResponseDto;
        } else {
            // 비밀번호 랜덤 생성 후 암호화해서 DB에 저장
            User newUser = userQueryRepository.findByEmail(oAuth2UserInfo.email())
                    .orElseGet(() -> userQueryRepository.save(
                            User.toGuestEntity(oAuth2UserInfo,
                                    bCryptPasswordEncoder.encode(PasswordUtil.generateRandomPassword()),
                                    provider))
                    );
            // 유저에게 GUEST 권한 주기
            JwtTokenDto jwtTokenDto = jwtUtil.generateToken(newUser.getId(), EUserRole.GUEST);
            String accessToken = jwtTokenDto.accessToken();
            userCommandRepository.updateRefreshTokenAndLoginStatus(newUser.getId(), jwtTokenDto.refreshToken(), true);
            return new AccessTokenDto(accessToken);
        }
    }

    // 비밀번호 재설정 메서드
    @Transactional
    public void resetPassword(Long userId, PasswordUpdateRequestDto passwordRequestDto) {
        User user = userQueryRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        // 비밀번호와 비밀번호 확인 일치 여부 검증
        checkPasswordMatch(passwordRequestDto.password(), passwordRequestDto.passwordConfirm());

        // 기존 쓰던 비밀번호로 설정해도 무방
        user.updatePassword(bCryptPasswordEncoder.encode(passwordRequestDto.password()));
    }

    // 이메일 확인 코드 전송 메서드
    public AuthCodeResponseDto sendEmailVerificationCode(EmailVerificationRequestDto emailVerificationRequestDto) {
        EVerificationType verificationType = EVerificationType.valueOf(
                emailVerificationRequestDto.verificationType().toUpperCase()
        );

        validateEmail(verificationType, emailVerificationRequestDto.email());

        String authCode = RandomCodeUtil.generateVerificationCode();
        mailService.sendEmail(emailVerificationRequestDto.email(), "[Poppin] 이메일 인증코드", authCode);
        return AuthCodeResponseDto.builder()
                .authCode(authCode)
                .build();
    }

    private void validateEmail(EVerificationType verificationType, String email) {
        boolean userExists = userQueryRepository.findByEmail(email).isPresent();

        if (verificationType.equals(EVerificationType.SIGN_UP) && userExists) {
            // 회원가입 시에 이메일 중복 -> 중복 이메일 Exception 반환
            throw new CommonException(ErrorCode.DUPLICATED_EMAIL);
        } else if (verificationType.equals(EVerificationType.PASSWORD_RESET) && !userExists) {
            // 비밀번호 재설정 시에 이메일 없음 -> 이메일 없음 Exception 반환
            throw new CommonException(ErrorCode.NOT_FOUND_USER);
        }
    }


    public Boolean verifyPassword(Long userId, PasswordVerificationRequestDto passwordVerificationRequestDto) {
        User user = userQueryRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        if (!bCryptPasswordEncoder.matches(passwordVerificationRequestDto.password(), user.getPassword())) {
            throw new CommonException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        return Boolean.TRUE;
    }

    // 토큰 재발급 메서드 (자동 로그인)
    @Transactional
    public UserInfoResponseDto refresh(String refreshToken, FcmTokenRequestDto fcmTokenRequestDto) {
        String token = refineToken(refreshToken);
        String fcmToken = fcmTokenRequestDto.fcmToken();

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userQueryRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        if (!user.getRefreshToken().equals(token)) {
            throw new CommonException(ErrorCode.INVALID_TOKEN_ERROR);
        }

        JwtTokenDto jwtTokenDto = jwtUtil.generateToken(userId, user.getRole());
        AlarmSetting alarmSetting = userAlarmSettingService.getUserAlarmSetting(fcmToken);

        // FCM 토큰 검증
        fcmTokenService.verifyFCMToken(Long.valueOf(user.getId()), fcmToken);
        userCommandRepository.updateRefreshTokenAndLoginStatus(user.getId(), jwtTokenDto.refreshToken(), true);
        UserPreferenceSettingDto userPreferenceSettingDto = userPreferenceSettingService.readUserPreferenceSettingCreated(
                user.getId());

        // 유저가 읽은 공지사항 알람 리스트 조회
        List<String> checkedNoticeIds = informIsReadRepository.findReadInformAlarmIdsByFcmToken(
                fcmToken).stream().map(
                Object::toString
        ).toList();

        // 유저가 가장 최근에 읽은 공지사항 알람 시간 조회
        String informLastCheckedTime = informIsReadRepository.findLastReadTimeByFcmToken(
                fcmToken);

        UserNoticeResponseDto userNoticeResponseDto = UserNoticeResponseDto.builder()
                .lastCheck(informLastCheckedTime)
                .checkedNoticeIds(checkedNoticeIds)
                .build();

        // TODO: 여기부터 수정 필요
        DestinationResponseDto destinationResponseDto = DestinationResponseDto.fromProperties(
                null, null, null, null,
                null, null, null, null, null
        );

        List<PopupAlarm> userPopupAlarm = popupAlarmRepository.findByFcmToken(fcmToken);
        List<InformIsRead> userInformIsRead = informIsReadRepository.findAllByFcmToken(fcmToken);

        List<NotificationResponseDto> popupNotificationResponseDtoList = userPopupAlarm.stream().map(
                popupAlarm -> NotificationResponseDto.fromProperties(
                        String.valueOf(popupAlarm.getId()), String.valueOf(user.getId()), null,
                        String.valueOf(ENotificationCategory.POPUP),
                        popupAlarm.getTitle(), popupAlarm.getBody(), null, popupAlarm.getIsRead(),
                        String.valueOf(popupAlarm.getCreatedAt()), String.valueOf(popupAlarm.getPopupId()), null,
                        destinationResponseDto
                )
        ).toList();

        List<NotificationResponseDto> noticeNotificationResponseDtoList = userInformIsRead.stream()
                .map(informIsRead -> {
                    InformAlarm informAlarm = informIsRead.getInformAlarm();
                    Boolean isRead = informIsRead.getIsRead();

                    return NotificationResponseDto.fromProperties(
                            String.valueOf(informAlarm.getId()),
                            String.valueOf(user.getId()),
                            null,
                            String.valueOf(ENotificationCategory.NOTICE),
                            informAlarm.getTitle(),
                            informAlarm.getBody(),
                            null,
                            isRead,
                            String.valueOf(informAlarm.getCreatedAt()),
                            null,
                            String.valueOf(informAlarm.getId()),
                            destinationResponseDto
                    );
                }).toList();

        UserNotificationResponseDto userNotificationResponseDto = UserNotificationResponseDto.fromDtoList(
                popupNotificationResponseDtoList,
                noticeNotificationResponseDtoList
        );

        List<Interest> userInterestPopupList = interestRepository.findByUserId(user.getId());

        List<PopupScrapDto> popupScrapDtoList = userInterestPopupList.stream().map(
                PopupScrapDto::fromInterest
        ).toList();

        UserActivityResponseDto userActivities = UserActivityResponseDto.fromProperties(
                popupScrapDtoList,
                userNotificationResponseDto
        );

        List<String> blockedPopups = blockedPopupRepository.findAllByUserId(user).stream()
                .map(blockedPopup -> blockedPopup.getId().toString())
                .toList();
        List<String> blockedUsers = blockedUserQueryRepository.findAllByUserId(user).stream()
                .map(blockedUser -> blockedUser.getId().toString())
                .toList();
        UserRelationDto userRelationDto = UserRelationDto.ofBlockedUserIdsAndPopupIds(blockedUsers, blockedPopups);

        // TODO: 여기까지 수정 필요

        return UserInfoResponseDto.fromUserEntity(
                user,
                alarmSetting,
                jwtTokenDto,
                userPreferenceSettingDto,
                userNoticeResponseDto,
                userActivities,
                userRelationDto
        );
    }

    // 헤더에 BASIC으로 로그인
    @Transactional
    public UserInfoResponseDto authSignIn(String authorizationHeader, FcmTokenRequestDto fcmTokenRequestDto) {
        String encoded = HeaderUtil.refineHeader(authorizationHeader, Constant.BASIC_PREFIX);
        String[] decoded = new String(Base64.getDecoder().decode(encoded)).split(":");
        String email = decoded[0];
        String password = decoded[1];
        String fcmToken = fcmTokenRequestDto.fcmToken();

        User user = userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new CommonException(ErrorCode.INVALID_LOGIN);
        }

        AlarmSetting alarmSetting = userAlarmSettingService.getUserAlarmSetting(fcmToken);

        // FCM 토큰 검증
        fcmTokenService.verifyFCMToken(Long.valueOf(user.getId()), fcmToken);

        JwtTokenDto jwtTokenDto = jwtUtil.generateToken(user.getId(), user.getRole());
        userCommandRepository.updateRefreshTokenAndLoginStatus(user.getId(), jwtTokenDto.refreshToken(), true);
        UserPreferenceSettingDto userPreferenceSettingDto = userPreferenceSettingService.readUserPreferenceSettingCreated(
                user.getId());

        // 유저가 읽은 공지사항 알람 리스트 조회
        List<String> checkedNoticeIds = informIsReadRepository.findReadInformAlarmIdsByFcmToken(
                fcmToken).stream().map(
                Object::toString
        ).toList();

        // 유저가 가장 최근에 읽은 공지사항 알람 시간 조회
        String informLastCheckedTime = informIsReadRepository.findLastReadTimeByFcmToken(
                fcmToken);

        UserNoticeResponseDto userNoticeResponseDto = UserNoticeResponseDto.builder()
                .lastCheck(informLastCheckedTime)
                .checkedNoticeIds(checkedNoticeIds)
                .build();

        // TODO: 여기부터 수정 필요
        DestinationResponseDto destinationResponseDto = DestinationResponseDto.fromProperties(
                null, null, null, null,
                null, null, null, null, null
        );

        List<PopupAlarm> userPopupAlarm = popupAlarmRepository.findByFcmToken(fcmToken);
        List<InformIsRead> userInformIsRead = informIsReadRepository.findAllByFcmToken(fcmToken);

        List<NotificationResponseDto> popupNotificationResponseDtoList = userPopupAlarm.stream().map(
                popupAlarm -> NotificationResponseDto.fromProperties(
                        String.valueOf(popupAlarm.getId()), String.valueOf(user.getId()), null,
                        String.valueOf(ENotificationCategory.POPUP),
                        popupAlarm.getTitle(), popupAlarm.getBody(), null, popupAlarm.getIsRead(),
                        String.valueOf(popupAlarm.getCreatedAt()), String.valueOf(popupAlarm.getPopupId()), null,
                        destinationResponseDto
                )
        ).toList();

        List<NotificationResponseDto> noticeNotificationResponseDtoList = userInformIsRead.stream()
                .map(informIsRead -> {
                    InformAlarm informAlarm = informIsRead.getInformAlarm();
                    Boolean isRead = informIsRead.getIsRead();

                    return NotificationResponseDto.fromProperties(
                            String.valueOf(informAlarm.getId()),
                            String.valueOf(user.getId()),
                            null,
                            String.valueOf(ENotificationCategory.NOTICE),
                            informAlarm.getTitle(),
                            informAlarm.getBody(),
                            null,
                            isRead,
                            String.valueOf(informAlarm.getCreatedAt()),
                            null,
                            String.valueOf(informAlarm.getId()),
                            destinationResponseDto
                    );
                }).toList();

        UserNotificationResponseDto userNotificationResponseDto = UserNotificationResponseDto.fromDtoList(
                popupNotificationResponseDtoList,
                noticeNotificationResponseDtoList
        );

        List<Interest> userInterestPopupList = interestRepository.findByUserId(user.getId());

        List<PopupScrapDto> popupScrapDtoList = userInterestPopupList.stream().map(
                interest -> PopupScrapDto.fromInterest(interest)
        ).toList();

        UserActivityResponseDto userActivities = UserActivityResponseDto.fromProperties(
                popupScrapDtoList,
                userNotificationResponseDto
        );

        List<String> blockedPopups = blockedPopupRepository.findAllByUserId(user).stream()
                .map(blockedPopup -> blockedPopup.getId().toString())
                .toList();
        List<String> blockedUsers = blockedUserQueryRepository.findAllByUserId(user).stream()
                .map(blockedUser -> blockedUser.getId().toString())
                .toList();
        UserRelationDto userRelationDto = UserRelationDto.ofBlockedUserIdsAndPopupIds(blockedUsers, blockedPopups);

        // TODO: 여기까지 수정 필요

        UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.fromUserEntity(
                user,
                alarmSetting,
                jwtTokenDto,
                userPreferenceSettingDto,
                userNoticeResponseDto,
                userActivities,
                userRelationDto
        );

        return userInfoResponseDto;
    }

    // Body에 email, password 받는 버전
    @Transactional
    public UserInfoResponseDto authLogin(AuthLoginRequestDto authLoginRequestDto) {
        String email = authLoginRequestDto.email();
        String password = authLoginRequestDto.password();
        String fcmToken = authLoginRequestDto.fcmToken();

        User user = userQueryRepository.findByEmail(email)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new CommonException(ErrorCode.INVALID_LOGIN);
        }

        AlarmSetting alarmSetting = userAlarmSettingService.getUserAlarmSetting(fcmToken);

        // FCM 토큰 검증
        fcmTokenService.verifyFCMToken(Long.valueOf(user.getId()), fcmToken);

        JwtTokenDto jwtTokenDto = jwtUtil.generateToken(user.getId(), user.getRole());
        userCommandRepository.updateRefreshTokenAndLoginStatus(user.getId(), jwtTokenDto.refreshToken(), true);
        UserPreferenceSettingDto userPreferenceSettingDto = userPreferenceSettingService.readUserPreferenceSettingCreated(
                user.getId());

        // 유저가 읽은 공지사항 알람 리스트 조회
        List<String> checkedNoticeIds = informIsReadRepository.findReadInformAlarmIdsByFcmToken(
                fcmToken).stream().map(
                Object::toString
        ).toList();

        // 유저가 가장 최근에 읽은 공지사항 알람 시간 조회
        String informLastCheckedTime = informIsReadRepository.findLastReadTimeByFcmToken(
                fcmToken);

        UserNoticeResponseDto userNoticeResponseDto = UserNoticeResponseDto.builder()
                .lastCheck(informLastCheckedTime)
                .checkedNoticeIds(checkedNoticeIds)
                .build();

        // TODO: 여기부터 수정 필요
        DestinationResponseDto destinationResponseDto = DestinationResponseDto.fromProperties(
                null, null, null, null,
                null, null, null, null, null
        );

        List<PopupAlarm> userPopupAlarm = popupAlarmRepository.findByFcmToken(fcmToken);
        List<InformIsRead> userInformIsRead = informIsReadRepository.findAllByFcmToken(fcmToken);

        List<NotificationResponseDto> popupNotificationResponseDtoList = userPopupAlarm.stream().map(
                popupAlarm -> NotificationResponseDto.fromProperties(
                        String.valueOf(popupAlarm.getId()), String.valueOf(user.getId()), null,
                        String.valueOf(ENotificationCategory.POPUP),
                        popupAlarm.getTitle(), popupAlarm.getBody(), null, popupAlarm.getIsRead(),
                        String.valueOf(popupAlarm.getCreatedAt()), String.valueOf(popupAlarm.getPopupId()), null,
                        destinationResponseDto
                )
        ).toList();

        List<NotificationResponseDto> noticeNotificationResponseDtoList = userInformIsRead.stream()
                .map(informIsRead -> {
                    InformAlarm informAlarm = informIsRead.getInformAlarm();
                    Boolean isRead = informIsRead.getIsRead();

                    return NotificationResponseDto.fromProperties(
                            String.valueOf(informAlarm.getId()),
                            String.valueOf(user.getId()),
                            null,
                            String.valueOf(ENotificationCategory.NOTICE),
                            informAlarm.getTitle(),
                            informAlarm.getBody(),
                            null,
                            isRead,
                            String.valueOf(informAlarm.getCreatedAt()),
                            null,
                            String.valueOf(informAlarm.getId()),
                            destinationResponseDto
                    );
                }).toList();

        UserNotificationResponseDto userNotificationResponseDto = UserNotificationResponseDto.fromDtoList(
                popupNotificationResponseDtoList,
                noticeNotificationResponseDtoList
        );

        List<Interest> userInterestPopupList = interestRepository.findByUserId(user.getId());

        List<PopupScrapDto> popupScrapDtoList = userInterestPopupList.stream().map(
                interest -> PopupScrapDto.fromInterest(interest)
        ).toList();

        UserActivityResponseDto userActivities = UserActivityResponseDto.fromProperties(
                popupScrapDtoList,
                userNotificationResponseDto
        );

        List<String> blockedPopups = blockedPopupRepository.findAllByUserId(user).stream()
                .map(blockedPopup -> blockedPopup.getId().toString())
                .toList();
        List<String> blockedUsers = blockedUserQueryRepository.findAllByUserId(user).stream()
                .map(blockedUser -> blockedUser.getId().toString())
                .toList();
        UserRelationDto userRelationDto = UserRelationDto.ofBlockedUserIdsAndPopupIds(blockedUsers, blockedPopups);

        // TODO: 여기까지 수정 필요

        UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.fromUserEntity(
                user,
                alarmSetting,
                jwtTokenDto,
                userPreferenceSettingDto,
                userNoticeResponseDto,
                userActivities,
                userRelationDto
        );

        return userInfoResponseDto;
    }

    @Transactional
    public void resetPasswordNoAuth(PasswordResetRequestDto passwordResetRequestDto) {
        User user = userQueryRepository.findByEmail(passwordResetRequestDto.email())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        if (!passwordResetRequestDto.password().equals(passwordResetRequestDto.passwordConfirm())) {
            throw new CommonException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        user.updatePassword(bCryptPasswordEncoder.encode(passwordResetRequestDto.password()));
    }

    // OS와 앱 버전 확인 메서드
    public Boolean appStart(AppStartRequestDto appStartRequestDto) {
        String platform = appStartRequestDto.os();
        String appVersion = appStartRequestDto.appVersion();
        if (platform.equals(Constant.iOS) && appVersion.equals(Constant.iOS_APP_VERSION)) {
            return Boolean.TRUE;
        }
        if (platform.equals(Constant.ANDROID) && appVersion.equals(Constant.ANDROID_APP_VERSION)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transactional(readOnly = true)
    public AccountStatusResponseDto getAccountStatus(AccountRequestDto accountRequestDto) {
        Optional<User> user = userQueryRepository.findByEmail(accountRequestDto.email());
        EAccountStatus accountStatus;
        if (user.isPresent()) {
            accountStatus = EAccountStatus.LOGIN;
        } else {
            accountStatus = EAccountStatus.SIGNUP;
        }
        return AccountStatusResponseDto.fromEnum(accountStatus);
    }
}
