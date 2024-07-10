package com.poppin.poppinserver.dto.alarm.response;

import com.poppin.poppinserver.domain.InformAlarm;
import lombok.Builder;

import java.time.LocalDate;

// 1 depth
@Builder
public record InformAlarmListResponseDto(
        Long id, // 공지사항 id
        String title,
        String body,
        LocalDate createdAt,
        String iconUrl,
        Boolean isRead
) {

    public static InformAlarmListResponseDto fromEntity(InformAlarm alarm, Boolean isRead){

        return InformAlarmListResponseDto.builder()
                .id(alarm.getId())
                .title(alarm.getTitle())
                .body(alarm.getBody())
                .createdAt(alarm.getCreatedAt())
                .iconUrl(alarm.getIcon())
                .isRead(isRead)
                .build();
    }
}
