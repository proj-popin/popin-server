package com.poppin.poppinserver.dto.popup.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.poppin.poppinserver.domain.Popup;
import com.poppin.poppinserver.domain.PreferedPopup;
import com.poppin.poppinserver.domain.TastePopup;
import com.poppin.poppinserver.domain.WhoWithPopup;
import com.poppin.poppinserver.dto.popup.request.CreatePreferedDto;
import com.poppin.poppinserver.dto.popup.request.CreateTasteDto;
import com.poppin.poppinserver.dto.popup.request.CreateWhoWithDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PopupDto(
        Long id,
        String posterUrl,
        String name,
        String introduce,
        String location,
        Integer entranceFee,
        Integer availableAge,
        Boolean parkingAvailable,
        Integer reopenDemandCnt,
        Integer interesteCnt,
        Integer viewCnt,
        String createdAt,
        String editedAt,
        String openDate,
        String closeDate,
        String openTime,
        String closeTime,
        String category,
        String operationStatus,
        PreferedDto prefered,
        TasteDto taste,
        WhoWithDto whoWith
) {
    public static PopupDto fromEntity(Popup popup){

        PreferedDto preferedDto = PreferedDto.fromEntity(popup.getPreferedPopup());
        TasteDto tasteDto = TasteDto.fromEntity(popup.getTastePopup());
        WhoWithDto whoWithDto = WhoWithDto.fromEntity(popup.getWhoWithPopup());

        return PopupDto.builder()
                .id(popup.getId())
                .posterUrl(popup.getPosterUrl())
                .name(popup.getName())
                .introduce(popup.getIntroduce())
                .location(popup.getLocation())
                .entranceFee(popup.getEntranceFee())
                .availableAge(popup.getEntranceFee())
                .parkingAvailable(popup.getParkingAvailable())
                .reopenDemandCnt(popup.getReopenDemandCnt())
                .interesteCnt(popup.getInterestCnt())
                .viewCnt(popup.getViewCnt())
                .createdAt(popup.getCreatedAt().toString())
                .editedAt(popup.getEditedAt().toString())
                .openDate(popup.getOpenDate().toString())
                .closeDate(popup.getCloseDate().toString())
                .openTime(popup.getOpenTime().toString())
                .closeTime(popup.getCloseTime().toString())
                .category(popup.getCategory())
                .operationStatus(popup.getOperationStatus())
                .prefered(preferedDto)
                .taste(tasteDto)
                .whoWith(whoWithDto)
                .build();
    }
}
