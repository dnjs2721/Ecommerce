package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.User;
import won.ecommerce.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DuplicationCheckService {

    private final UserRepository userRepository;

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
}
