package won.ecommerce.controller;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.mailDto.*;
import won.ecommerce.service.EmailService;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {

    private final EmailService emailService;

    /**
     * 인증코드 이메일 전송
     */
    @GetMapping("/sendMail/{userEmail}")
    public ResponseEntity<String> sendEmail(@PathVariable("userEmail") String userEmail) throws MessagingException {
        emailService.sendAuthCode(userEmail);
        return ResponseEntity.ok().body("인증코드가 발송 되었습니다.");
    }

    /**
     * 이메일 인증코드 검증
     */
    @PostMapping("/validateEmail")
    public ResponseEntity<String> validateEmail(@RequestBody @Valid ValidateEmailRequestDto request) {
        try {
            emailService.validateCode(request.getEmail(), request.getAuthCode());
            return ResponseEntity.ok().body(request.getEmail() + " 인증 성공");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, HttpStatus.NOT_FOUND);
        } catch (AuthenticationFailedException e2) {
            return createResponseEntity(e2, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
