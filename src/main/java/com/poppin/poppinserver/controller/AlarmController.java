//package com.poppin.poppinserver.controller;
//
//import com.poppin.poppinserver.annotation.UserId;
//import com.poppin.poppinserver.dto.common.ResponseDto;
//import com.poppin.poppinserver.service.AlarmService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@Slf4j
//@RequiredArgsConstructor
//@RequestMapping("/api/v1/alarm")
//public class AlarmController {
//
//    private AlarmService alarmService;
//
//    // 팝업 알림 보여주기
//    @GetMapping("/popup")
//    public ResponseDto<?> readPopupAlarm(@UserId Long userId, @RequestParam(name = "token") String token){
//        return ResponseDto.ok(alarmService.readPopupAlarmList(userId, token));
//    }
//
//    // 공지사항 알림 보여주기
////    @GetMapping("/info")
////    public ResponseDto<?> readInfoAlarm(){
////
////    }
//}
