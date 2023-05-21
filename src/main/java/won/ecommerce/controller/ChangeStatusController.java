package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.chagngeStatusDto.ChangeStatusRequestDto;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;
import won.ecommerce.service.ChangeStatusService;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class ChangeStatusController {

    private final ChangeStatusService changeStatusService;

    /**
     * Status 변경 요청 작성
     */
    @GetMapping("/createChangeStatusLog/{userId}")
    public ResponseEntity<String> createChangeStatusLog(@PathVariable("userId") Long userId) {
        try {
            Long logId = changeStatusService.createChangeStatusLog(userId);
            return ResponseEntity.ok().body("[" + logId.toString() + "]" + " 요청이 전송되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalStateException e2) {
            return createResponseEntity(e2, CONFLICT); // 이미 등록된 요청 예외
        }
    }

    /**
     * Status 변경 요청 로그 검색
     * http://localhost:8080/api/users/searchChangeStatusLogs/{id}?userId=2&adminId=1&timeGoe=2023-05-20T02:33:00&timeLoe=2023-05-20T02:36:30&logStat=WAIT&size=3&page=0
     * /searchChangeStatusLogs/{id} -> id 로 관리자인지 확인
     * adminId, userId, timeGoe, timeLoe, logStat, size, page 지정 가능 -> 동적쿼리
     */
    @GetMapping("/searchChangeStatusLogs/{id}")
    public ResponseEntity<?> searchLogs(@PathVariable("id") Long id, StatusLogSearchCondition condition, Pageable pageable) {
        try {
            Page<SearchStatusLogDto> searchLogs = changeStatusService.searchLogs(id, condition, pageable);
            return ResponseEntity.ok().body(searchLogs);
        } catch (IllegalAccessException e) {
            return createResponseEntity(e, NOT_ACCEPTABLE); // 권환 없음 예외
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
            return createResponseEntity(e1, NOT_FOUND); // 존재하지 않는 요청, 관리자, 회원 에외
        } catch (IllegalStateException e2) {
            return createResponseEntity(e2, CONFLICT); // 이미 처리된 요청
        } catch (IllegalAccessException e3) {
            return createResponseEntity(e3, NOT_ACCEPTABLE); // 권환 없음 예외
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
