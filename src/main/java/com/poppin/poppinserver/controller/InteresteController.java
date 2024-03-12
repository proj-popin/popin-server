package com.poppin.poppinserver.controller;

import com.poppin.poppinserver.annotation.UserId;
import com.poppin.poppinserver.dto.intereste.requeste.AddInteresteDto;
import com.poppin.poppinserver.dto.common.ResponseDto;
import com.poppin.poppinserver.service.InteresteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/intereste")
public class InteresteController {
    private final InteresteService interesteService;

    @PostMapping("/add-intereste")
    public ResponseDto<?> addIntereste(@RequestBody @Valid AddInteresteDto addInteresteDto, @UserId Long userId){
        return ResponseDto.ok(interesteService.userAddIntereste(addInteresteDto, userId));
    }
}
