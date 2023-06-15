package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRefundLog extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private Long sellerId;
    private Long userId;
    private Long orderItemId;
    private String reason;
    @Enumerated(EnumType.STRING)
    private ExchangeRefundStatus status;
    @Enumerated(EnumType.STRING)
    private LogStatus logStatus;
    private LocalDateTime processingTime;

    @Builder
    public ExchangeRefundLog(Long sellerId, Long userId, Long orderItemId, String reason, ExchangeRefundStatus status) {
        this.sellerId = sellerId;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.reason = reason;
        this.status = status;
        this.logStatus = LogStatus.WAIT;
    }

    public void changeStatus(LogStatus logStatus) {
        this.logStatus = logStatus;
        this.processingTime = LocalDateTime.now();
    }
}
