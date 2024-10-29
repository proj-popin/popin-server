package com.poppin.poppinserver.inform.controller;

import com.poppin.poppinserver.core.annotation.UserId;
import com.poppin.poppinserver.core.dto.ResponseDto;
import com.poppin.poppinserver.core.type.EInformProgress;
import com.poppin.poppinserver.inform.service.AdminUserInformService;
import com.poppin.poppinserver.inform.service.UserInformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-inform")
public class UserInformQueryController {
    private final AdminUserInformService adminUserInformService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    public ResponseDto<?> readUserInform(@RequestParam("informId") Long userInformId) {
        return ResponseDto.ok(adminUserInformService.readUserInform(userInformId));
    } // 제보 조회



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseDto<?> readUserInformList(@RequestParam(value = "page") int page,
                                             @RequestParam(value = "size") int size,
                                             @RequestParam(value = "prog") EInformProgress progress,
                                             @UserId Long adminId) {
        return ResponseDto.ok(adminUserInformService.readUserInformList(page, size, progress));
    } // 제보 목록 조회
}
