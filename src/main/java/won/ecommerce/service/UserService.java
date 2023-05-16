package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.User;
import won.ecommerce.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    /**
     * 회원가입
     */
    @Transactional
    public Long join(User user) {
        validateDuplicateEmail(user.getEmail());
        validateDuplicateNickname(user.getNickname());
        userRepository.save(user);
        return user.getId();
    }

    /**
     * 로그인
     */
    public Long login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일 입니다. 가입 후 로그인 해 주시길 바랍니다."));
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
            throw new IllegalArgumentException("다른 사용자가 사용중인 닉네임입니다.");
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
}
