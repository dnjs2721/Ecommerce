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
import won.ecommerce.controller.dto.mailDto.*;
import won.ecommerce.service.EmailService;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {

    private final EmailService emailService;

    /**
     * 인증코드 이메일 전송
     */
    @PostMapping("/sendMail")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequestDto request) throws MessagingException, UnsupportedEncodingException {
        String authCode = emailService.sendAuthCode(request.getEmail());
        return ResponseEntity.ok().body(authCode);
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
            return NoSuchElementException(e1);
        } catch (IllegalArgumentException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
        }
    }

    public ResponseEntity<String> NoSuchElementException(NoSuchElementException e) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }
}
