package com.poppin.poppinserver.dto.managerInform.response;

import com.poppin.poppinserver.domain.ManagerInform;
import com.poppin.poppinserver.dto.userInform.response.UserInformSummaryDto;
import com.poppin.poppinserver.type.EInformProgress;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public record ManagerInformSummaryDto(
      Long id,
      EInformProgress progress,
      String popupName,
      String informerName,
      String informedAt,
      String adminName,
      String executedAt
) {
    public static List<ManagerInformSummaryDto> fromEntityList(List<ManagerInform> managerInforms){
        List<ManagerInformSummaryDto> dtoList = new ArrayList<>();

        for(ManagerInform managerInform : managerInforms){

            String executedAt = null;
            if(managerInform.getExecutedAt() != null){
                executedAt = managerInform.getExecutedAt().toString();
            }

            String adminName = null;
            if(managerInform.getAdminId() != null){
                adminName = managerInform.getAdminId().getNickname();
            }

            ManagerInformSummaryDto managerInformSummaryDto =
                    ManagerInformSummaryDto.builder()
                            .id(managerInform.getId())
                            .progress(managerInform.getProgress())
                            .popupName(managerInform.getPopupId().getName())
                            .informerName(managerInform.getInformerId().getNickname())
                            .informedAt(managerInform.getInformedAt().toString())
                            .adminName(adminName)
                            .executedAt(executedAt)
                            .build();

            dtoList.add(managerInformSummaryDto);
        }

        return dtoList;
    }
}

