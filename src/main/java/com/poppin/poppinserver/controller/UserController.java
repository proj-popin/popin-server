package com.poppin.poppinserver.controller;

import com.poppin.poppinserver.annotation.UserId;
import com.poppin.poppinserver.dto.common.ResponseDto;
import com.poppin.poppinserver.dto.user.request.CreateUserTasteDto;
import com.poppin.poppinserver.dto.user.request.UserInfoDto;
import com.poppin.poppinserver.service.PopupService;
import com.poppin.poppinserver.service.ReviewService;
import com.poppin.poppinserver.service.UserService;
import com.poppin.poppinserver.type.EOperationStatus;
import com.poppin.poppinserver.type.EPopupSort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {
    private final UserService userService;
    private final ReviewService reviewService;
    private final PopupService popupService;

    @PostMapping("/popup-taste")
    public ResponseDto<?> createUserTaste(
            @UserId Long userId,
            @RequestBody @Valid CreateUserTasteDto userTasteDto
    ) {
        return ResponseDto.created(userService.createUserTaste(userId, userTasteDto));
    }

    @GetMapping("/popup-taste")
    public ResponseDto<?> readUserTaste(@UserId Long userId) {
        return ResponseDto.ok(userService.readUserTaste(userId));
    }

    @PutMapping("/popup-taste")
    public ResponseDto<?> updateUserTaste(
            @UserId Long userId,
            @RequestBody @Valid CreateUserTasteDto userTasteDto
    ) {
        return ResponseDto.ok(userService.updateUserTaste(userId, userTasteDto));
    }

    @GetMapping("")
    public ResponseDto<?> readUser(@UserId Long userId) {
        return ResponseDto.ok(userService.readUser(userId));
    }

    @GetMapping("/settings")
    public ResponseDto<?> readUserProfile(@UserId Long userId) {
        return ResponseDto.ok(userService.readUserProfile(userId));
    }

    @PostMapping("/image")
    public ResponseDto<?> createUserProfileImage(@UserId Long userId, @RequestPart(value = "profileImage") MultipartFile profileImage) {
        return ResponseDto.ok(userService.createProfileImage(userId, profileImage));
    }

    @PutMapping("/image")
    public ResponseDto<?> updateUserProfileImage(@UserId Long userId, @RequestPart(value = "profileImage") MultipartFile profileImage) {
        return ResponseDto.ok(userService.updateProfileImage(userId, profileImage));
    }

    @DeleteMapping("/image")
    public ResponseDto<?> deleteUserProfileImage(@UserId Long userId) {
        userService.deleteProfileImage(userId);
        return ResponseDto.ok("프로필 이미지가 삭제되었습니다.");
    }

    @PatchMapping("/settings")
    public ResponseDto<?> updateUserNicknameAndBirthDate(
            @UserId Long userId,
            @RequestBody UserInfoDto userInfoDto
    ) {
        return ResponseDto.ok(userService.updateUserNicknameAndBirthDate(userId, userInfoDto));
    }

    @DeleteMapping("/withdrawal")
    public ResponseDto<?> deleteUser(@UserId Long userId) {
        userService.deleteUser(userId);
        return ResponseDto.ok("회원 탈퇴가 완료되었습니다.");
    }

    /*작성완료 후기 조회*/
    @GetMapping("/review/finish")
    public ResponseDto<?> getFinishReviewList(@UserId Long userId ){
        return ResponseDto.ok(userService.getFinishReviewList(userId));
    }

    /*작성완료 인증후기 보기*/
    @GetMapping("/review/finish/certi")
    public ResponseDto<?> getCertifiedReview(@UserId Long userId, @RequestParam(value = "reviewId") Long reviewId, @RequestParam(value = "popupId") Long popupId){
        return ResponseDto.ok(userService.getCertifiedReview(userId, reviewId, popupId));
    }

    /*작성완료 미인증후기 보기*/
    @GetMapping("/review/finish/uncerti")
    public ResponseDto<?> getUncertifiedReview(@UserId Long userId, @RequestParam(value = "reviewId") Long reviewId, @RequestParam(value = "popupId") Long popupId){
        return ResponseDto.ok(userService.getUncertifiedReview(userId, reviewId, popupId));
    }

    /*마이페이지 - 후기 작성하기 - 방문한 팝업 조회*/
    @GetMapping("popup/v/certi")
    public ResponseDto<?> getCertifiedPopupList(@UserId Long userId){
        return ResponseDto.ok(userService.getCertifiedPopupList(userId));
    }

    /*마이페이지 - 후기 작성하기 - 방문한 팝업 후기 작성*/
    @PostMapping(value = "review/w/certi", consumes = {MediaType.APPLICATION_JSON_VALUE , MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseDto<?> createMPCertiReview(
            @UserId Long userId,
            @RequestParam("popupId") Long popupId,
            @RequestParam("text") String text,
            @RequestParam("visitDate") String visitDate,
            @RequestParam("satisfaction") String satisfaction,
            @RequestParam("congestion") String congestion,
            @RequestParam("nickname") String nickname,
            @RequestPart(value = "images" ) List<MultipartFile> images)
    {
        return ResponseDto.ok(reviewService.writeCertifiedReview(userId, popupId,text,visitDate,satisfaction,congestion,nickname, images));
    }

    /*마이페이지 - 후기 작성하기 - 일반 후기 작성*/
    @PostMapping(value = "review/w/uncerti", consumes = {MediaType.APPLICATION_JSON_VALUE , MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseDto<?> createMPUncertiReview(
            @UserId Long userId,
            @RequestParam("popupId") Long popupId,
            @RequestParam("text") String text,
            @RequestParam("visitDate") String visitDate,
            @RequestParam("satisfaction") String satisfaction,
            @RequestParam("congestion") String congestion,
            @RequestParam("nickname") String nickname,
            @RequestPart(value = "images" ) List<MultipartFile> images)
    {
        return ResponseDto.ok(reviewService.writeUncertifiedReview(userId, popupId,text,visitDate,satisfaction,congestion,nickname, images));
    }

    /*마이페이지 - 일반후기 팝업 검색*/
    @GetMapping("/popup/search")
    public ResponseDto<?> searchPopupName(@RequestParam("text") String text,
                                          @RequestParam("taste") String taste,
                                          @RequestParam("prepered") String prepered,
                                          @RequestParam("oper") EOperationStatus oper,
                                          @RequestParam("order") EPopupSort order,
                                          @RequestParam("page") int page,
                                          @RequestParam("size") int size,
                                          @UserId Long userId) {
        return ResponseDto.ok(popupService.readSearchingList(text, taste, prepered, oper, order, page, size, userId));
    }

    /*마이페이지 - 자주 묻는 질문 조회*/
    @GetMapping("/support/faqs")
    public ResponseDto<?> readFAQs() {
        return ResponseDto.ok(userService.readFAQs());
    }

    /*마이페이지 - 한글 닉네임 랜덤 생성*/
    @GetMapping("/random-nickname")
    public ResponseDto<?> generateRandomNickname() {
        return ResponseDto.ok(userService.generateRandomNickname());
    }

//    @PostMapping("/support/question")
//    public ResponseDto<?> createUserQna(@UserId Long userId,
//                                        @RequestPart(value = "images") MultipartFile images,
//                                        @RequestParam("title") String title,
//                                        @RequestParam("content") String content) {
//        userService.createUserQna(userId, title, content, images);
//        return ResponseDto.created("문의가 성공적으로 접수되었습니다.");
//    }
}
