package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.ChangeStatusLogRepository;
import won.ecommerce.repository.UserRepository;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.SearchUsersDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;
import won.ecommerce.repository.dto.UserSearchCondition;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static won.ecommerce.entity.LogStat.*;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ChangeStatusLogRepository changeStatusLogRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 조회 - 관리자
     */
    public Page<SearchUsersDto> searchUsers(Long id, UserSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
        return userRepository.searchUsersPage(condition, pageable);
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 로그 검색
     */
    public Page<SearchStatusLogDto> searchLogs(Long id, StatusLogSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
        return changeStatusLogRepository.searchLogsPage(condition, pageable);
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경
     */
    @Transactional
    public void changeStatus(Long logId, Long adminId, String stat) throws IllegalAccessException {
        Optional<ChangeStatusLog> findLog = changeStatusLogRepository.findById(logId);
        Optional<User> findAdmin = userRepository.findById(adminId);
        if (findLog.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 요청입니다.");
        }
        if (findAdmin.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 관리자입니다.");
        }
        if (!findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
        if (findLog.get().getLogStat().equals(OK) || findLog.get().getLogStat().equals(CANCEL)) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        Optional<User> findUser = userRepository.findById(findLog.get().getUserId());
        if (findUser.isPresent()) {
            if (stat.equals("OK")) {
                UserStatus requestStat = findLog.get().getRequestStat();
                findUser.get().setStatus(requestStat);
                findLog.get().setLogStat(OK);
            } else {
                findLog.get().setLogStat(CANCEL);
            }
            findLog.get().setAdminId(adminId);
            findLog.get().setProcessingTime(LocalDateTime.now());
        } else {
            throw new NoSuchElementException("존재하지 않는 회원의 요청입니다.");
        }
    }
}
