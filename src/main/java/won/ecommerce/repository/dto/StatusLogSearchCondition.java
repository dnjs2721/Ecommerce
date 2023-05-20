package won.ecommerce.repository.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.LogStat;

import java.time.LocalDateTime;

@Data
public class StatusLogSearchCondition {
    private Long userId;
    private Long adminId;
    private LogStat logStat;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
}
