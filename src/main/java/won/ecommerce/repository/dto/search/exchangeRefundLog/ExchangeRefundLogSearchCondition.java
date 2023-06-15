package won.ecommerce.repository.dto.search.exchangeRefundLog;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.ExchangeRefundStatus;
import won.ecommerce.entity.LogStatus;

import java.time.LocalDateTime;

@Data
public class ExchangeRefundLogSearchCondition {
    private Long userId;
    private ExchangeRefundStatus status;
    private LogStatus logStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
}
