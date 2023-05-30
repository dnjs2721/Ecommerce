package won.ecommerce.repository.dto.search.statusLog;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.LogStatus;
import won.ecommerce.entity.UserStatus;

import java.time.LocalDateTime;

@Data
public class SearchStatusLogDto {
    private Long logId;
    private Long userId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
    private UserStatus beforeStat;
    private UserStatus requestStat;
    private LogStatus logStat;
    private Long adminId;
    private LocalDateTime processingTime;
    private String cancelReason;

    @QueryProjection
    public SearchStatusLogDto(Long logId, Long userId, LocalDateTime createdDate, LocalDateTime lastModifiedDate, UserStatus beforeStat, UserStatus requestStat, LogStatus logStat, Long adminId, LocalDateTime processingTime, String cancelReason) {
        this.logId = logId;
        this.userId = userId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.beforeStat = beforeStat;
        this.requestStat = requestStat;
        this.logStat = logStat;
        this.adminId = adminId;
        this.processingTime = processingTime;
        this.cancelReason = cancelReason;
    }
}
