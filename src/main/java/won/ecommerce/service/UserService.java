package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.service.dto.ChangeUserInfoRequestDto;
import won.ecommerce.service.dto.JoinRequestDto;
import won.ecommerce.repository.user.UserRepository;

import java.util.NoSuchElementException;

import static io.micrometer.common.util.StringUtils.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DuplicationCheckService duplicationCheckService;
    private final ChangeStatusLogService changeStatusLogService;

    /**
     * 회원가입
     */
    @Transactional
    public Long join(User user) {
        duplicationCheckService.validateDuplicateEmail(user.getEmail());
        duplicationCheckService.validateDuplicateNickname(user.getNickname());
        duplicationCheckService.validateDuplicatePNum(user.getPNum());
        userRepository.save(user);
        return user.getId();
    }

    /**
     * 로그인
     */
    public Long login(String email, String password) throws IllegalAccessException {
        User user = findUserByEmail(email);
        if (user.getPassword().equals(password)) {
            return user.getId();
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
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
    public String changePassword(String email, String password, String newPassword) throws IllegalAccessException {
        User user = findUserByEmail(email);
        if (user.getPassword().equals(password)) {
            if (user.getPassword().equals(newPassword)) {
                throw new IllegalStateException("현재 사용중인 패스워드와 같습니다.");
            }
            user.changePassword(newPassword);
            return email;
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public String deleteUser(String email, String password) throws IllegalAccessException {
        User user = findUserByEmail(email);
        if (password.equals(user.getPassword())) {
            userRepository.delete(user);
            return user.getName();
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * 정보 수정
     * 닉네임, 주소
     */
    @Transactional
    public void changeUserInfo(ChangeUserInfoRequestDto request) throws IllegalAccessException {
        User user = findUserByEmail(request.getEmail()); // NoSuchElementException
        if (request.getPassword().equals(user.getPassword())) {
            Address address = changeUserInfoAddress(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()); // IllegalArgumentException, 주소 형태 확인
            if (request.getNickname() != null) {
                String newNickname = changeUserInfoNickname(request.getNickname(), user.getNickname()); // IllegalStateException,닉네임 사용가능 유무 확인
                user.changeNickname(newNickname);
            }
            if (address != null) user.changeAddress(address);
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 작성
     */
    @Transactional
    public Long createChangeStatusLog(long userId) {
        User user = findUserById(userId);
        return changeStatusLogService.createChangeStatusLog(user);
    }

    /**
     * 닉네임 검사
     */
    public String changeUserInfoNickname(String newNickname, String nickname) {
        String ignoreNickname = "admin";
        if (nickname.equals(newNickname)) {
            throw new IllegalStateException("현재 사용중인 닉네임입니다.");
        }
        if (newNickname.toUpperCase().matches("(.*)"+ignoreNickname.toUpperCase()+"(.*)")
                || newNickname.toLowerCase().matches("(.*)"+ignoreNickname.toLowerCase()+"(.*)") ) {
            throw new IllegalStateException("사용할 수 없는 닉네임입니다.");
        }
        duplicationCheckService.validateDuplicateNickname(newNickname); // IllegalStateException
        return newNickname;
    }

    /**
     * 주소 검사
     */
    public Address changeUserInfoAddress(String region, String city, String street, String detail, String zipcode) {
        if (isBlank(region) && isBlank(city) && isBlank(street) && isBlank(detail) && isBlank(zipcode)) {
            return null;
        } else if (isNotBlank(region) && isNotBlank(city) && isNotBlank(street) && isNotBlank(detail) && isNotBlank(zipcode)) {
            return new Address(region, city, street, detail, zipcode);
        } else {
            throw new IllegalArgumentException("잘못된 주소형태 입니다.");
        }
    }

    // 사용중인 이메일인지 검사 메소드
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일 입니다."));
    }

    // 가입된 회원 검증 메서드
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("가입되지 않은 회원입니다."));
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
}
