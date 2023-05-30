package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.EnumType.*;
import static won.ecommerce.entity.LogStatus.CANCEL;
import static won.ecommerce.entity.LogStatus.OK;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeStatusLog extends BaseTimeEntity{
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    @Enumerated(STRING)
    private UserStatus beforeStat;
    @Enumerated(STRING)
    private UserStatus requestStat;
    @Enumerated(STRING)
    private LogStatus logStat;
    private Long adminId;
    private LocalDateTime processingTime;

    private String cancelReason;

    @Builder
    public ChangeStatusLog(Long userId, UserStatus beforeStat, UserStatus requestStat, LogStatus logStat) {
        this.userId = userId;
        this.beforeStat = beforeStat;
        this.requestStat = requestStat;
        this.logStat = logStat;
    }

    public void changeStatus(User user, String stat, Long adminId, String cancelReason) {
        if (stat.equals("OK")) {
            user.setStatus(this.getRequestStat());
            this.logStat = OK;
        } else {
            this.logStat = CANCEL;
            this.cancelReason = Objects.requireNonNullElse(cancelReason, "취소");
        }
        this.adminId = adminId;
        this.processingTime = LocalDateTime.now();
    }
}
