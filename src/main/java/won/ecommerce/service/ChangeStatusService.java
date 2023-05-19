package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.LogStat;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.ChangeStatusLogRepository;
import won.ecommerce.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static won.ecommerce.entity.LogStat.*;
import static won.ecommerce.entity.UserStatus.COMMON;
import static won.ecommerce.entity.UserStatus.SELLER;

@Service
@RequiredArgsConstructor
public class ChangeStatusService {
    private final ChangeStatusLogRepository changeStatusLogRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Status 변경 요청 작성
     */
    @Transactional
    public Long createChangeStatusLog(String email) {
        User user = userService.findUserByEmail(email);
        UserStatus beforeStatus = user.getStatus();
        UserStatus requestStatus = null;
        if (beforeStatus.equals(COMMON)) {
            requestStatus = SELLER;
        } else if (beforeStatus.equals(SELLER)) {
            requestStatus = COMMON;
        }

        Optional<ChangeStatusLog> findLog = changeStatusLogRepository.findByUserIdAndLogStat(user.getId(), WAIT);
        if (findLog.isPresent()) {
            throw new IllegalStateException("이미 전송된 요청입니다.");
        }

        ChangeStatusLog log = ChangeStatusLog.builder()
                .userId(user.getId())
                .beforeStat(beforeStatus)
                .requestStat(requestStatus)
                .logStat(WAIT)
                .build();

        changeStatusLogRepository.save(log);

        return log.getId();
    }

    /**
     * Status 변경
     */
    @Transactional
    public void changeStatus(Long logId, Long adminId, String stat) {
        Optional<ChangeStatusLog> findLog = changeStatusLogRepository.findById(logId);
        Optional<User> findAdmin = userRepository.findById(adminId);
        if (findLog.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 요청입니다.");
        }
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new NoSuchElementException("존재하지 않는 관리자입니다.");
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
