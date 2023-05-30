package won.ecommerce.repository.dto.search.statusLog;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.LogStatus;

import java.time.LocalDateTime;

@Data
public class StatusLogSearchCondition {
    private Long userId;
    private Long adminId;
    private LogStatus logStat;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
}
