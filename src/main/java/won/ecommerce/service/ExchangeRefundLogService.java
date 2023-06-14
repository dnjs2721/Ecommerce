package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import won.ecommerce.controller.dto.userDto.CreateExchangeRefundLogRequestDto;
import won.ecommerce.entity.ExchangeRefundLog;
import won.ecommerce.entity.ExchangeRefundStatus;
import won.ecommerce.entity.LogStatus;
import won.ecommerce.repository.orders.ExchangeRefundRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRefundLogService {
    private final ExchangeRefundRepository exchangeRefundRepository;

    /**
     * 교환/환불 신청 로그 생성
     */
    public void createExchangeRefundLog(Long userId, CreateExchangeRefundLogRequestDto request) {
        Optional<ExchangeRefundLog> findLog = exchangeRefundRepository.findByUserIdAndOrderItemIdAndLogStatus(userId, request.getOrderItemId(), LogStatus.WAIT);
        if (findLog.isPresent()) {
            ExchangeRefundLog exchangeRefundLog = findLog.get();
            if (exchangeRefundLog.getStatus().equals(request.getStatus())) {
                throw new IllegalStateException("이미 전송된 요청입니다.");
            } else {
                if (exchangeRefundLog.getStatus().equals(ExchangeRefundStatus.EXCHANGE)) {
                    throw new IllegalStateException("환불 신청이 전송된 주문입니다. 교환을 원하시면 환불 신청을 취소 해주세요.");
                } else if (exchangeRefundLog.getStatus().equals(ExchangeRefundStatus.REFUND)) {
                    throw new IllegalStateException("교환 신청이 전송된 주문입니다. 환불을 원하시면 교환 신청을 취소 해주세요.");
                }
            }
        }

        ExchangeRefundLog log = ExchangeRefundLog.builder()
                .userId(userId)
                .orderItemId(request.getOrderItemId())
                .reason(request.getReason())
                .status(request.getStatus())
                .build();
        exchangeRefundRepository.save(log);
    }

    /**
     * 대기중인 교환/환불 신청 확인
     */
    public ExchangeRefundLog searchWaitExchangeRefundLog(Long userId, Long orderItemId) {
        Optional<ExchangeRefundLog> findLog = exchangeRefundRepository.findByUserIdAndOrderItemIdAndLogStatus(userId, orderItemId, LogStatus.WAIT);
        if (findLog.isEmpty()) {
            throw new NoSuchElementException("대기중인 교환/환불 신청을 찾지 못했습니다.");
        }
        return findLog.get();
    }

    /**
     * 교환/환불 신청 상태 변경
     */
    public void changeLogStatusExchangeRefundLog(Long logId, LogStatus logStatus) {
        Optional<ExchangeRefundLog> findLog = exchangeRefundRepository.findById(logId);
        if (findLog.isEmpty()) {
            throw new NoSuchElementException("신청을 찾지 못했습니다.");
        }
        ExchangeRefundLog exchangeRefundLog = findLog.get();
        exchangeRefundLog.changeStatus(logStatus);
    }
}
