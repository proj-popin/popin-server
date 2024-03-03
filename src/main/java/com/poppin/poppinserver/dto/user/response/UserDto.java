package com.poppin.poppinserver.dto.user.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.poppin.poppinserver.domain.User;
import lombok.Builder;

@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserDto(
    Long id,
    String email,
    String nickname,
    String provider,
    String role,
    Boolean agreedToPrivacyPolicy,
    Boolean agreedToServiceTerms,
    Boolean agreedToGPS,
    String createdAt
) {
    public static UserDto fromEntity(User user){
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .provider(user.getProvider().toString())
                .role(user.getRole().toString())
                .agreedToPrivacyPolicy(user.getAgreedToPrivacyPolicy())
                .agreedToServiceTerms(user.getAgreedToServiceTerms())
                .agreedToGPS(user.getAgreedToGPS())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}