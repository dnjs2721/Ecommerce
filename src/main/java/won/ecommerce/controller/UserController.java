package won.ecommerce.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import won.ecommerce.controller.dto.*;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.service.EmailService;
import won.ecommerce.service.UserService;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    /**
     * 이메일 중복 검사
     */
    @PostMapping("/members/checkDuplicationEmail")
    public ResponseEntity<String> checkDuplicationEmail(@RequestBody @Valid EmailRequestDto request) {
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
    @PostMapping("/members/checkDuplicationNickname")
    public ResponseEntity<String> checkDuplicationNickname(@RequestBody @Valid DuplicationNicknameRequestDto request) {
        try {
            userService.validateDuplicateNickname(request.getNickname());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body(request.getNickname() + " 은 사용가능 합니다.");
    }

    /**
     * 이메일 전송
     */
    @PostMapping("/members/email")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequestDto request) throws MessagingException, UnsupportedEncodingException {
        String authCode = emailService.sendEmail(request.getEmail());
        return ResponseEntity.ok().body(authCode);
    }

    /**
     * 이메일 인증코드 검증
     */
    @PostMapping("/members/validateEmail")
    public ResponseEntity<String> validateEmail(@RequestBody @Valid ValidateEmailRequestDto request) {
        try {
            emailService.validateCode(request.getEmail(), request.getAuthCode());
            return ResponseEntity.ok().body(request.getEmail() + " 인증 성공");
        } catch (NoSuchElementException e1) {
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<>(e1.getMessage(), headers, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
        }
    }

    /**
     * 회원가입
     */
    @PostMapping("/members/join")
    public ResponseEntity<String> joinUser(@RequestBody @Valid JoinRequestDto request) {
        try {
            User user = User.builder()
                    .name(request.getName())
                    .nickname(request.getNickname())
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .pNum(request.getPNum())
                    .birth(request.getBirth())
                    .address(new Address(request.getRegion(), request.getCity(), request.getStreet(), request.getZipcode()))
                    .status(UserStatus.COMMON)
                    .build();
            Long memberId = userService.join(user);

            return ResponseEntity.ok().body(memberId.toString() + " 회원가입 되었습니다.");
        } catch (IllegalStateException | IllegalArgumentException e1) {
            return ResponseEntity.badRequest().body(e1.getMessage());
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/members/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto request) {
        try {
            Long id = userService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok().body(id.toString() + " 로그인 성공");
        } catch (NoSuchElementException e1) {
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<>(e1.getMessage(), headers, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
        }
    }

    /**
     * 아이디(이메일) 찾기
     */
    @PostMapping("members/findEmail")
    public ResponseEntity<String> findEmail(@RequestBody @Valid FindEmailRequestDto request) {
        try {
            String email = userService.findEmailByNameAndPNum(request.getName(), request.getPNum());
            return ResponseEntity.ok().body(request.getName() + "님의 아이디(이메일)은 " + email + " 입니다.");
        } catch (NoSuchElementException e) {
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
        }
    }
}
