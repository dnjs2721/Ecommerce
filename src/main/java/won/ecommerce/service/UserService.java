package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.repository.dto.SearchUsersDto;
import won.ecommerce.repository.dto.UserSearchCondition;
import won.ecommerce.service.dto.ChangeUserInfoRequestDto;
import won.ecommerce.service.dto.JoinRequestDto;
import won.ecommerce.repository.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

import static io.micrometer.common.util.StringUtils.*;

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

    /**
     * 정보 수정
     * 닉네임, 주소
     */
    @Transactional
    public void changeUserInfo(ChangeUserInfoRequestDto request) {
        User user = findUserByEmail(request.getEmail()); // NoSuchElementException
        if (request.getPassword().equals(user.getPassword())) {
            Address address = changeUserInfoAddress(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()); // IllegalArgumentException
            if (request.getNickname() != null) {
                String newNickname = changeUserInfoNickname(request.getNickname(), user.getNickname()); // IllegalStateException
                user.changeNickname(newNickname);
            }
            if (address != null) {
                user.changeAddress(address);
            }
        } else {
            throw new IllegalArgumentException("잘못된 패스워드 입니다.");
        }
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
        validateDuplicateNickname(newNickname); // IllegalStateException
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

    /**
     * 사용자 조회 - 관리자
     */
    public Page<SearchUsersDto> searchUsers(Long id, UserSearchCondition condition, Pageable pageable) {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new NoSuchElementException("조회할 권한이 없습니다.");
        }
        return userRepository.searchUsersPage(condition, pageable);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일 입니다."));
    }
}
