package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.*;
import static won.ecommerce.entity.LogStat.CANCEL;
import static won.ecommerce.entity.LogStat.OK;

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

    private Long adminId;

    @Enumerated(STRING)
    private LogStat logStat;

    private LocalDateTime processingTime;

    @Builder
    public ChangeStatusLog(Long id, Long userId, UserStatus beforeStat, UserStatus requestStat, LogStat logStat) {
        this.id = id;
        this.userId = userId;
        this.beforeStat = beforeStat;
        this.requestStat = requestStat;
        this.logStat = logStat;
    }

    public void setLogStat(LogStat logStat) {
        this.logStat = logStat;
    }

    public void changeStatus(User user, String stat, Long adminId) {
        if (stat.equals("OK")) {
            user.setStatus(this.getRequestStat());
            this.setLogStat(OK);
        } else {
            this.setLogStat(CANCEL);
        }
        this.adminId = adminId;
        this.processingTime = LocalDateTime.now();
    }
}
