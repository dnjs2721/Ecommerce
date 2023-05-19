package won.ecommerce.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.repository.ChangeStatusLogRepository;
import won.ecommerce.service.dto.JoinRequestDto;
import won.ecommerce.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static won.ecommerce.entity.UserStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ChangeStatusLogRepository changeStatusLogRepository;
    private final EntityManager em;

    /**
     * 회원가입
     */
    @Transactional
    public Long join(User user) {
        validateDuplicateEmail(user.getEmail());
        validateDuplicateNickname(user.getNickname());
        validateDuplicatePNum(user.getPNum());
        userRepository.save(user);
        return user.getId();
    }

    // user 생성 - status 제외
    public User createdUser(JoinRequestDto request) {
        return User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(request.getPassword())
                .pNum(request.getPNum())
                .birth(request.getBirth())
                .address(new Address(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()))
                .build();
    }

    /**
     * 로그인
     */
    public Long login(String email, String password) {
        User user = findUserByEmail(email);
        if (user.getPassword().equals(password)) {
            return user.getId();
        } else {
            throw new IllegalArgumentException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * 이메일 중복 검사
     */
    public void validateDuplicateEmail(String email) {
        Optional<User> findUser = userRepository.findByEmail(email);
        if (findUser.isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    /**
     * 닉네임 중복 검사
     */
    public void validateDuplicateNickname(String nickname) {
        Optional<User> findUser = userRepository.findByNickname(nickname);
        if (findUser.isPresent()) {
            throw new IllegalStateException("다른 사용자가 사용중인 닉네임입니다.");
        }
    }

    /**
     * 휴대폰 번호 중복 검사
     */
    public void validateDuplicatePNum(String pNum) {
        Optional<User> findUser = userRepository.findBypNum(pNum);
        if (findUser.isPresent()) {
            throw new IllegalStateException("이미 등록된 휴대폰 번호입니다.");
        }
    }

    /**
     * 아이디(이메일) 찾기
     */
    public String findEmailByNameAndPNum(String name, String pNum) {
        String email = userRepository.findEmailByNameAndPNum(name, pNum);
        if (email == null) {
            throw new NoSuchElementException("가입되지 않은 회원 입니다. 이름 혹은 전화번호를 확인 해 주세요.");
        }
        return email;
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public String changePassword(String email, String newPassword) {
        User user = findUserByEmail(email);
        user.changePassword(newPassword);
        return email;
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public String deleteUser(String email, String password) {
        User user = findUserByEmail(email);
        if (password.equals(user.getPassword())) {
            userRepository.delete(user);
            return user.getName();
        } else {
            throw new IllegalArgumentException("잘못된 패스워드 입니다.");
        }
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일 입니다."));
    }
}
