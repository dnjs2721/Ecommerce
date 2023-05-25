package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.UserRepository;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.SearchUsersDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;
import won.ecommerce.repository.dto.UserSearchCondition;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ChangeStatusLogService changeStatusLogService;

    /**
     * 사용자 조회 - 관리자
     */
    public Page<SearchUsersDto> searchUsers(Long id, UserSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        findUserByIdAndCheckAdmin(id);
        return userRepository.searchUsersPage(condition, pageable);
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 로그 검색
     */
    public Page<SearchStatusLogDto> searchLogs(Long id, StatusLogSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        findUserByIdAndCheckAdmin(id);
        return changeStatusLogService.searchLogs(condition, pageable);
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경
     */
    @Transactional
    public void changeStatus(Long logId, Long adminId, String stat, String reason) throws IllegalAccessException {
        ChangeStatusLog findLog = changeStatusLogService.checkChangeStatusLog(logId);
        findUserByIdAndCheckAdmin(adminId);

        Optional<User> findUser = userRepository.findById(findLog.getUserId());
        if (findUser.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 회원의 요청입니다.");
        }
        findLog.changeStatus(findUser.get(), stat, adminId, reason);
    }

    // 관리자 존재 유무, 권한 확인
    public void findUserByIdAndCheckAdmin(Long id) throws IllegalAccessException {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
    }
}
