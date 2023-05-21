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
import won.ecommerce.controller.dto.adminDto.ChangeStatusRequestDto;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.SearchUsersDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;
import won.ecommerce.repository.dto.UserSearchCondition;
import won.ecommerce.service.AdminService;
import won.ecommerce.service.UserService;
import won.ecommerce.service.dto.JoinRequestDto;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    /**
     * 회원가입 - 관리자
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinAdmin(@RequestBody @Valid JoinRequestDto request) {
        try {
            User admin = userService.createdUser(request);
            admin.setStatus(UserStatus.ADMIN);
            Long memberId = userService.join(admin);
            return ResponseEntity.ok().body(memberId.toString() + " 회원가입 되었습니다.");
        } catch (IllegalStateException e) {
            return createResponseEntity(e, CONFLICT); // 닉네임, 이메일, 휴대폰 번호 중복 예외
        }
    }

    /**
     * 사용자 정보 조회 - 관리자
     * http://localhost:8080/api/users/searchUsers/{id}?userStatus=SELLER
     * /searchUsers/{id} -> id 로 관리자인지 확인
     * userStatus 검색가능
     */
    @GetMapping("/searchUsers/{id}")
    public ResponseEntity<?> searchUsers(@PathVariable("id") Long id, UserSearchCondition condition, Pageable pageable) {
        try {
            Page<SearchUsersDto> searchUsers = adminService.searchUsers(id, condition, pageable);
            return ResponseEntity.ok().body(searchUsers);
        } catch (IllegalAccessException e) {
            return createResponseEntity(e, NOT_ACCEPTABLE); // 권한 없음 예외
        }
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 로그 검색
     * http://localhost:8080/api/users/searchChangeStatusLogs/{id}?userId=2&adminId=1&timeGoe=2023-05-20T02:33:00&timeLoe=2023-05-20T02:36:30&logStat=WAIT&size=3&page=0
     * /searchChangeStatusLogs/{id} -> id 로 관리자인지 확인
     * adminId, userId, timeGoe, timeLoe, logStat, size, page 지정 가능 -> 동적쿼리
     */
    @GetMapping("/searchChangeStatusLogs/{id}")
    public ResponseEntity<?> searchLogs(@PathVariable("id") Long id, StatusLogSearchCondition condition, Pageable pageable) {
        try {
            Page<SearchStatusLogDto> searchLogs = adminService.searchLogs(id, condition, pageable);
            return ResponseEntity.ok().body(searchLogs);
        } catch (IllegalAccessException e) {
            return createResponseEntity(e, NOT_ACCEPTABLE); // 권환 없음 예외
        }
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경
     * /changeStatus/{logId} -> logId 로 요청 선택
     */
    @PostMapping("/changeStatus/{logId}")
    public ResponseEntity<String> changeStatus(@PathVariable("logId") Long logId, @RequestBody @Valid ChangeStatusRequestDto request) {
        try {
            adminService.changeStatus(logId, request.getAdminId(), request.getStat());
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
