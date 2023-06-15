package won.ecommerce.repository.dto.search.exchangeRefundLog;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.ExchangeRefundStatus;
import won.ecommerce.entity.LogStatus;

import java.time.LocalDateTime;

@Data
public class SearchExchangeRefundLogDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    private Long logId;
    private Long userId;
    private Long orderItemId;
    private String reason;
    private ExchangeRefundStatus status;
    private LogStatus logStatus;
    private LocalDateTime processingTime;

    @QueryProjection
    public SearchExchangeRefundLogDto(LocalDateTime createdDate, Long logId, Long userId, Long orderItemId, String reason, ExchangeRefundStatus status, LogStatus logStatus, LocalDateTime processingTime) {
        this.createdDate = createdDate;
        this.logId = logId;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.reason = reason;
        this.status = status;
        this.logStatus = logStatus;
        this.processingTime = processingTime;
    }
}
