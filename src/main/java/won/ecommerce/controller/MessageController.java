package won.ecommerce.controller;

import jakarta.mail.AuthenticationFailedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.messageDto.*;
import won.ecommerce.service.MessageService;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {
    private final MessageService messageService;

    /**
     * 인증코드 메시지 전송
     */
    @GetMapping("/sendMessage/{pNum}")
    public ResponseEntity<String> sendMessage(@PathVariable("pNum") String pNum) {
        messageService.sendMessage(pNum);
        return ResponseEntity.ok().body("인증코드가 발송 되었습니다.");
    }

    /**
     * 메세지 인증코드 검증
     */
    @PostMapping("/validateMessage")
    public ResponseEntity<String> validateMessage(@RequestBody @Valid ValidateMessageRequestDto request) {
        try {
            messageService.validateCode(request.getPNum(), request.getAuthCode());
            return ResponseEntity.ok().body(request.getPNum() + " 인증 성공");
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
