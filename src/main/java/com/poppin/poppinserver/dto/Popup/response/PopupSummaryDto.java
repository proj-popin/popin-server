package com.poppin.poppinserver.dto.Popup.response;

import com.poppin.poppinserver.domain.Popup;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record PopupSummaryDto(
        @NotNull
        Long id,
        @NotNull
        String image_url,
        @NotNull
        String name,
        @NotNull
        String introduce
) {
    public static List<PopupSummaryDto> fromEntityList(List<Popup> popups){
        List<PopupSummaryDto> dtoList = new ArrayList<>();

        for(Popup popup : popups){
            PopupSummaryDto popupSummaryDto =
                    PopupSummaryDto.builder()
                            .id(popup.getId())
                            .image_url("https://ifh.cc/v-szgh3Z") // 임시 url
                            .name(popup.getName())
                            .introduce(popup.getIntroduce())
                            .build();

            dtoList.add(popupSummaryDto);
        }

        return dtoList;
    }
}
