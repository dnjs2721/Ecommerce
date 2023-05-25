package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.LogStat;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.ChangeStatusLogRepository;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;

import java.util.NoSuchElementException;
import java.util.Optional;

import static won.ecommerce.entity.LogStat.*;
import static won.ecommerce.entity.UserStatus.COMMON;
import static won.ecommerce.entity.UserStatus.SELLER;

@Service
@RequiredArgsConstructor
public class ChangeStatusLogService {
    private final ChangeStatusLogRepository changeStatusLogRepository;

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 작성
     */
    public Long createChangeStatusLog(User user) {
        UserStatus beforeStatus = user.getStatus();
        UserStatus requestStatus = null;
        if (beforeStatus.equals(COMMON)) {
            requestStatus = SELLER;
        } else if (beforeStatus.equals(SELLER)) {
            requestStatus = COMMON;
        }
        checkDuplicateRequest(user.getId(), WAIT);

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
     * COMMON-SELLER, SELLER-COMMON 변경 요청 로그 검색
     */
    public Page<SearchStatusLogDto> searchLogs(StatusLogSearchCondition condition, Pageable pageable) {
        return changeStatusLogRepository.searchLogsPage(condition, pageable);
    }

    /**
     * 로그 존재 유무와 처리 상태 확인
     */
    public ChangeStatusLog checkChangeStatusLog(Long logId) {
        ChangeStatusLog findLog = findLogById(logId);
        if (findLog.getLogStat().equals(OK) || findLog.getLogStat().equals(CANCEL)) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        return findLog;
    }

    /**
     * 변경 요청 중복 검사
     */
    public void checkDuplicateRequest(Long userId, LogStat stat) {
        Optional<ChangeStatusLog> findLog = changeStatusLogRepository.findByUserIdAndLogStat(userId, stat);
        if (findLog.isPresent()) {
            throw new IllegalStateException("[" + findLog.get().getId() + "]" + "이미 전송된 요청입니다.");
        }
    }

    /**
     * 로그 존재 유무 확인
     */
    public ChangeStatusLog findLogById(Long id) {
        Optional<ChangeStatusLog> findLog = changeStatusLogRepository.findById(id);
        if (findLog.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 요청입니다.");
        }
        return findLog.get();
    }
}
