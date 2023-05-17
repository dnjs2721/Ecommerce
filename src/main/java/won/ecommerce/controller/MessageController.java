package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(@RequestBody @Valid MessageRequestDto request) {
        messageService.sendMessage(request.getPNum());
        String authCode = messageService.checkCode(request.getPNum());
        return ResponseEntity.ok().body(authCode);
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
            HttpHeaders headers = new HttpHeaders();
            return new ResponseEntity<>(e1.getMessage(), headers, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
        }
    }
}
