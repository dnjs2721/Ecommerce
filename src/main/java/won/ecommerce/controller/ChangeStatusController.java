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
import won.ecommerce.controller.dto.chagngeStatusDto.ChangeStatusRequestDto;
import won.ecommerce.controller.dto.chagngeStatusDto.CreateChangeStatusLogRequestDto;
import won.ecommerce.service.ChangeStatusService;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ChangeStatusController {

    private final ChangeStatusService changeStatusService;

    /**
     * Status 변경 요청 작성
     */
    @PostMapping("/createChangeStatusLog")
    public ResponseEntity<String> createChangeStatusLog(@RequestBody @Valid CreateChangeStatusLogRequestDto request) {
        try {
            Long logId = changeStatusService.createChangeStatusLog(request.getEmail());
            return ResponseEntity.ok().body(logId.toString() + "." + request.getEmail() + " 님의 요청이 전송되었습니다.");
        } catch (NoSuchElementException e1) {
            return NoSuchElementException(e1);
        } catch (IllegalStateException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
        }
    }

    /**
     * Status 변경
     */
    @PostMapping("/changeStatus")
    public ResponseEntity<String> changeStatus(@RequestBody @Valid ChangeStatusRequestDto request) {
        try {
            changeStatusService.changeStatus(request.getLogId(), request.getAdminId(), request.getStat());
            return ResponseEntity.ok().body("요청이 성공적으로 처리되었습니다.");
        } catch (NoSuchElementException e1) {
            return NoSuchElementException(e1);
        } catch (IllegalStateException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
        }
    }

    public ResponseEntity<String> NoSuchElementException(NoSuchElementException e) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }
}
