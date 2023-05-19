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
import won.ecommerce.controller.dto.userDto.*;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.service.UserService;
import won.ecommerce.service.dto.JoinRequestDto;

import java.util.NoSuchElementException;

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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 회원가입 - 관리자
     */
    @PostMapping("/joinAdmin")
    public ResponseEntity<String> joinAdmin(@RequestBody @Valid JoinRequestDto request) {
        try {
            User admin = userService.createdUser(request);
            admin.setStatus(UserStatus.ADMIN);
            Long memberId = userService.join(admin);

            return ResponseEntity.ok().body(memberId.toString() + " 회원가입 되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
            return NoSuchElementException(e1);
        } catch (IllegalArgumentException e2) {
            return ResponseEntity.badRequest().body(e2.getMessage());
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
            return NoSuchElementException(e);
        }
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequestDto request) {
        try {
            String email = userService.changePassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok().body(email + " 님의 비밀번호가 성공적으로 변경 되었습니다.");
        } catch (NoSuchElementException e) {
            return NoSuchElementException(e);
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
        } catch (NoSuchElementException e) {
            return NoSuchElementException(e);
        }
    }

    public ResponseEntity<String> NoSuchElementException(NoSuchElementException e) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }
}
