package won.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.service.DuplicationCheckService;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/check")
public class DuplicationCheckController {
    private final DuplicationCheckService duplicationCheckService;

    /**
     * 이메일 중복 검사
     */
    @GetMapping("/DuplicationEmail/{userEmail}")
    public ResponseEntity<String> checkDuplicationEmail(@PathVariable("userEmail") String userEmail) {
        try {
            duplicationCheckService.validateDuplicateEmail(userEmail);
            return ResponseEntity.ok().body(userEmail + " 은 사용가능한 이메일입니다.\n이메일 인증을 해주세요.");
        } catch (IllegalStateException e) {
            return createResponseEntity(e, CONFLICT); // 중복 예외
        }
    }

    /**
     * 닉네임 중복 검사
     */
    @GetMapping("/DuplicationNickname/{nickname}")
    public ResponseEntity<String> checkDuplicationNickname(@PathVariable("nickname") String nickname) {
        try {
            duplicationCheckService.validateDuplicateNickname(nickname);
            return ResponseEntity.ok().body(nickname + " 은 사용가능한 닉네임입니다.");
        } catch (IllegalStateException e) {
            return createResponseEntity(e, CONFLICT); // 중복 예외
        }
    }

    /**
     * 휴대폰 번호 중복 검사
     */
    @GetMapping ("/DuplicationPNum/{pNum}")
    public ResponseEntity<String> checkDuplicationPNum(@PathVariable("pNum") String pNum) {
        try {
            duplicationCheckService.validateDuplicatePNum(pNum);
            return ResponseEntity.ok().body(pNum + " 은 사용가능한 번호입니다.\n전화번호 인증을 해주세요.");
        } catch (IllegalStateException e) {
            return createResponseEntity(e, CONFLICT); // 중복 예외
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
