package com.poppin.poppinserver.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AuthSignUpDto (
        // 이메일 형식
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "올바른 이메일 형식이 아닙니다.")
        String email,

        // 영문/숫자 포함, 특수문자 포함, 8자 이상 20자 이하
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$", message = "올바른 비밀번호 형식이 아닙니다.")
        String password,

        @NotBlank
        String passwordConfirm,

        // 한글/영문 1자 이상 10자 이하(공백 포함), 공백만 입력 불가
        @NotEmpty
        @Size(min = 1, max = 10)
        @Pattern(regexp = "^(?=.*[가-힣A-Za-z])[가-힣A-Za-z\\s]*$", message = "올바른 닉네임 형식이 아닙니다.")
        String nickname,

        // 개인정보 보호정책 동의 여부
        @NotNull
        Boolean agreedToPrivacyPolicy,

        // 서비스 이용 약관
        @NotNull
        Boolean agreedToServiceTerms
) { }
