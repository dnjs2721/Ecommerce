package won.ecommerce.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import won.ecommerce.controller.dto.EmailRequestDto;
import won.ecommerce.controller.dto.ValidateEmailRequestDto;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.service.EmailService;
import won.ecommerce.service.UserService;
import won.ecommerce.service.dto.JoinRequestDto;

import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    @PostMapping("/members/checkDuplicationUser")
    public ResponseEntity<String> checkDuplicationUser(@RequestBody @Valid EmailRequestDto request) {
        try {
            userService.validateDuplicateUser(request.getEmail());
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body("이미 가입된 이메일 입니다.");
        }
        return ResponseEntity.ok().body(request.getEmail() + " 은 사용가능 합니다.");
    }

    @PostMapping("/members/email")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequestDto request) throws MessagingException, UnsupportedEncodingException {
        String authCode = emailService.sendEmail(request.getEmail());
        return ResponseEntity.ok().body(authCode);
    }

    @PostMapping("/members/validateEmail")
    public ResponseEntity<String> validateEmail(@RequestBody @Valid ValidateEmailRequestDto request) {
        int value = emailService.validateCode(request.getEmail(), request.getAuthCode());
        switch (value) {
            case 0 -> {
                return ResponseEntity.badRequest().body("만료된 인증코드 혹은 잘못된 이메일 입니다.");
            }
            case 1 -> {
                return ResponseEntity.ok().body("이메일 인증 성공");
            }
            case 2 -> {
                return ResponseEntity.badRequest().body("잘못된 인증번호 입니다.");
            }
        }
        return null;
    }

    @PostMapping("/members/join")
    public ResponseEntity<String> joinUser(@RequestBody @Valid JoinRequestDto request) {
        try {
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .pNum(request.getPNum())
                    .birth(request.getBirth())
                    .address(new Address(request.getRegion(), request.getCity(), request.getStreet(), request.getZipcode()))
                    .status(UserStatus.COMMON)
                    .build();
            Long memberId = userService.join(user);

            return ResponseEntity.ok().body(memberId.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("중복 회원");
        }
    }
}
