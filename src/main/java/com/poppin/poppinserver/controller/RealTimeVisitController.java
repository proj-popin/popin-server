package com.poppin.poppinserver.controller;

import com.poppin.poppinserver.annotation.UserId;
import com.poppin.poppinserver.dto.RealTimeVisit.request.AddVisitorsDto;
import com.poppin.poppinserver.dto.common.ResponseDto;
import com.poppin.poppinserver.service.RealTimeVisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/rtvisit")
public class RealTimeVisitController {

    private final RealTimeVisitService realTimeVisitService;

    @GetMapping ("/show-visitors")
    public ResponseDto<?> showRealTimeVisitorsCnt(@UserId Long userId, AddVisitorsDto addVisitorsDto){ return ResponseDto.ok(realTimeVisitService.showRealTimeVisitors(userId, addVisitorsDto)); }

    @PostMapping("/add-visitors")
    public  ResponseDto<?> addRealTimeVisitors(@UserId Long userId, AddVisitorsDto addVisitorsDto){
        log.info(userId.toString());
        return ResponseDto.ok(realTimeVisitService.addRealTimeVisitors(userId, addVisitorsDto));
    }

}
