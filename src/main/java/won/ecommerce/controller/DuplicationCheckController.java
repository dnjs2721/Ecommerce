package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import won.ecommerce.controller.dto.duplicationCheckDto.*;
import won.ecommerce.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/check")
public class DuplicationCheckController {
    private final UserService userService;

    /**
     * 이메일 중복 검사
     */
    @PostMapping("/DuplicationEmail")
    public ResponseEntity<String> checkDuplicationEmail(@RequestBody @Valid DuplicationEmailRequestDto request) {
        try {
            userService.validateDuplicateEmail(request.getEmail());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body(request.getEmail() + " 은 사용가능 합니다.");
    }

    /**
     * 닉네임 중복 검사
     */
    @PostMapping("/DuplicationNickname")
    public ResponseEntity<String> checkDuplicationNickname(@RequestBody @Valid DuplicationNicknameRequestDto request) {
        try {
            userService.validateDuplicateNickname(request.getNickname());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body(request.getNickname() + " 은 사용가능 합니다.");
    }

    /**
     * 휴대폰 번호 중복 검사
     */
    @PostMapping("/DuplicationPNum")
    public ResponseEntity<String> checkDuplicationPNum(@RequestBody @Valid DuplicationPNumRequestDto request) {
        try {
            userService.validateDuplicatePNum(request.getPNum());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body(request.getPNum() + " 은 사용가능 합니다.");
    }
}
