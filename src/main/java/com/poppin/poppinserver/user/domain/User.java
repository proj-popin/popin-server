package com.poppin.poppinserver.user.domain;

import com.poppin.poppinserver.alarm.domain.UserAlarmKeyword;
import com.poppin.poppinserver.core.constant.Constant;
import com.poppin.poppinserver.core.type.ELoginProvider;
import com.poppin.poppinserver.core.type.EUserRole;
import com.poppin.poppinserver.interest.domain.Interest;
import com.poppin.poppinserver.popup.domain.PreferedPopup;
import com.poppin.poppinserver.popup.domain.TastePopup;
import com.poppin.poppinserver.popup.domain.WhoWithPopup;
import com.poppin.poppinserver.user.dto.auth.request.AuthSignUpDto;
import com.poppin.poppinserver.user.oauth.OAuth2UserInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_login", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isLogin;

    @Column(name = "agreed_to_privacy_policy", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean agreedToPrivacyPolicy;

    @Column(name = "agreed_to_service_terms", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean agreedToServiceTerms;

    @Column(name = "agreed_to_gps", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean agreedToGPS;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isDeleted;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private EUserRole role;

    @Column(name = "login_provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private ELoginProvider provider;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "review_cnt", nullable = false)
    private Long reviewCnt;

    @Column(name = "visited_popup_cnt", nullable = false)
    private Long visitedPopupCnt;

    @Column(name = "require_special_care", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean requiresSpecialCare;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Interest> interestes = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<UserAlarmKeyword> userAlarmKeywords = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "prefered_popup_id")
    private PreferedPopup preferedPopup;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "taste_popup_id")
    private TastePopup tastePopup;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "who_with_popup_id")
    private WhoWithPopup whoWithPopup;

    @Column(name = "reported_cnt", nullable = false)
    private Long reportedCnt;

    @Builder
    public User(String email, String password, String nickname,
                ELoginProvider eLoginProvider, EUserRole role,
                Boolean agreedToPrivacyPolicy, Boolean agreedToServiceTerms, Boolean agreedToGPS
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = eLoginProvider;
        this.role = role;
        this.agreedToPrivacyPolicy = agreedToPrivacyPolicy;
        this.agreedToServiceTerms = agreedToServiceTerms;
        this.agreedToGPS = agreedToGPS;
        this.createdAt = LocalDateTime.now();
        this.isLogin = false;
        this.refreshToken = null;
        this.deletedAt = null;
        this.isDeleted = false;
        this.profileImageUrl = null;
        this.requiresSpecialCare = false;
        this.reviewCnt = 0L;
        this.visitedPopupCnt = 0L;
        this.reportedCnt = 0L;
    }

    public static User toUserEntity(AuthSignUpDto authSignUpDto, String encodedPassword, ELoginProvider eLoginProvider) {
        return User.builder()
                .email(authSignUpDto.email())
                .password(encodedPassword)
                .nickname(authSignUpDto.nickname())
                .eLoginProvider(eLoginProvider)
                .role(EUserRole.USER)
                .agreedToPrivacyPolicy(authSignUpDto.agreedToPrivacyPolicy())
                .agreedToServiceTerms(authSignUpDto.agreedToServiceTerms())
                .agreedToGPS(false)
                .build();
    }

    public static User toGuestEntity(OAuth2UserInfo oAuth2UserInfo, String encodedPassword, ELoginProvider eLoginProvider) {
        return User.builder()
                .email(oAuth2UserInfo.email())
                .password(encodedPassword)
                .eLoginProvider(eLoginProvider)
                .role(EUserRole.GUEST)
                .agreedToPrivacyPolicy(true)
                .agreedToServiceTerms(true)
                .agreedToGPS(false)
                .build();
    }

    public void register(String nickname) {
        this.nickname = nickname;
        this.role = EUserRole.USER;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updatePopupTaste(PreferedPopup preferedPopup, TastePopup tastePopup, WhoWithPopup whoWithPopup) {
        this.preferedPopup = preferedPopup;
        this.tastePopup = tastePopup;
        this.whoWithPopup = whoWithPopup;
    }

    public void updatePopupTaste(PreferedPopup preferedPopup) {
        this.preferedPopup = preferedPopup;
    }

    public void updatePopupTaste(TastePopup tastePopup) {
        this.tastePopup = tastePopup;
    }

    public void updatePopupTaste(WhoWithPopup whoWithPopup) {
        this.whoWithPopup = whoWithPopup;
    }

    public void updateUserNickname(String nickname) {
        if (nickname != null && !nickname.isEmpty())
            this.nickname = nickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void deleteProfileImage() {
        this.profileImageUrl = null;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now().plusDays(Constant.MEMBER_INFO_RETENTION_PERIOD);
    }

    public void requiresSpecialCare() {
        this.requiresSpecialCare = true;
    }

    public void recover() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public void addReportCnt(){
        this.reportedCnt++;
    }

    public void addReviewCnt(){
        this.reviewCnt++;
    }

    public void addVisitedPopupCnt(){
        this.visitedPopupCnt++;
    }
}
