package com.poppin.poppinserver.constant;

import java.util.List;

public class Constant {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION = "accessToken";
    public static final String REAUTHORIZATION = "refreshToken";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String USER_ROLE_CLAIM_NAME = "role";
    public static final String USER_EMAIL_CLAIM_NAME = "email";
    public static final List<String> NO_NEED_AUTH_URLS = List.of(
            "/api/v1/auth/sign-up", // 회원가입
            "/api/v1/auth/sign-in", // 로그인
            "/api/v1/popup/hot-list",   // 인기 팝업 목록 조회
            "/api/v1/popup/new-list",   // 새로 오픈 팝업 목록 조회
            "/api/v1/popup/closing-list"    // 종료 임박 팝업 목록 조회
    );
}
