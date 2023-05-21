package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.userDto.*;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.dto.SearchUsersDto;
import won.ecommerce.repository.dto.UserSearchCondition;
import won.ecommerce.service.UserService;
import won.ecommerce.service.dto.JoinRequestDto;
import won.ecommerce.service.dto.ChangeUserInfoRequestDto;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * 회원가입 - 일반 사용자
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinUser(@RequestBody @Valid JoinRequestDto request) {
        try {
            User user = userService.createdUser(request);
            user.setStatus(UserStatus.COMMON);
            Long memberId = userService.join(user);
            return ResponseEntity.ok().body(memberId.toString() + " 회원가입 되었습니다.");
        } catch (IllegalStateException e) {
            return createResponseEntity(e, CONFLICT); // 닉네임, 이메일, 휴대폰 번호 중복 예외
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto request) {
        try {
            Long id = userService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok().body(id.toString() + " 로그인 성공");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, UNAUTHORIZED); // 비밀번호 오류 예외
        }
    }

    /**
     * 아이디(이메일) 찾기
     */
    @PostMapping("/findEmail")
    public ResponseEntity<String> findEmail(@RequestBody @Valid FindEmailRequestDto request) {
        try {
            String email = userService.findEmailByNameAndPNum(request.getName(), request.getPNum());
            return ResponseEntity.ok().body(request.getName() + "님의 아이디(이메일)은 " + email + " 입니다.");
        } catch (NoSuchElementException e) {
            return createResponseEntity(e, NOT_FOUND); // 등록된 사용자 없음 예외
        }
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequestDto request) {
        try {
            String email = userService.changePassword(request.getEmail(), request.getPassword(), request.getNewPassword());
            return ResponseEntity.ok().body(email + " 님의 비밀번호가 성공적으로 변경 되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, UNAUTHORIZED); // 비밀번호 오류 예외
        }
    }

    /**
     * 회원 탈퇴
     */
    @PostMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestBody @Valid DeleteUserRequestDto request) {
        try {
            String userName = userService.deleteUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok().body(userName + " 님 정상적으로 회원탈퇴 되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, UNAUTHORIZED); // 비밀번호 오류 예외
        }
    }

    /**
     * 정보 수정
     * 닉네임, 주소
     */
    @PostMapping("/changeUserInfo")
    public ResponseEntity<String> changeUserInfo(@RequestBody @Valid ChangeUserInfoRequestDto request) {
        try {
            userService.changeUserInfo(request);
            return ResponseEntity.ok().body("정보를 성공적으로 변경하였습니다.");
        } catch (IllegalStateException | IllegalArgumentException e1) {
            return createResponseEntity(e1, CONFLICT); // 닉네임 중복, 사용불가 닉네임, 잘못된 주소형태 예외
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e3) {
            return createResponseEntity(e3, UNAUTHORIZED); // 비밀번호 오류 예외
        }
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 작성
     */
    @GetMapping("/createChangeStatusLog/{userId}")
    public ResponseEntity<String> createChangeStatusLog(@PathVariable("userId") Long userId) {
        try {
            Long logId = userService.createChangeStatusLog(userId);
            return ResponseEntity.ok().body("[" + logId + "]" + " 요청이 전송되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalStateException e2) {
            return createResponseEntity(e2, CONFLICT); // 이미 등록된 요청 예외
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
