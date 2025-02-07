package com.poppin.poppinserver.popup.dto.popup.response;

import com.poppin.poppinserver.popup.domain.Popup;
import com.poppin.poppinserver.popup.domain.PosterImage;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record VisitedPopupDto(

        @NotNull
        String id,

        @NotNull
        String name,

        @NotNull
        String mainImageUrl,

        @NotNull
        String openDate,

        @NotNull
        String closeDate

) {
    public static VisitedPopupDto fromEntity(Popup popup) {

        List<String> imageUrls = popup.getPosterImages()
                .stream()
                .map(PosterImage::getPosterUrl)
                .sorted()
                .toList();

        return VisitedPopupDto.builder()
                .id(popup.getId().toString())
                .name(popup.getName())
                .mainImageUrl(imageUrls.get(0)) // 메인 이미지
                .openDate(popup.getOpenDate().toString())
                .closeDate(popup.getCloseDate().toString())
                .build();
    }
}