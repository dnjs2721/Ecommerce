package won.ecommerce.repository.exchangeRefund;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.entity.ExchangeRefundStatus;
import won.ecommerce.entity.LogStatus;
import won.ecommerce.repository.dto.search.exchangeRefundLog.ExchangeRefundLogSearchCondition;
import won.ecommerce.repository.dto.search.exchangeRefundLog.QSearchExchangeRefundLogDto;
import won.ecommerce.repository.dto.search.exchangeRefundLog.SearchExchangeRefundLogDto;

import java.time.LocalDateTime;
import java.util.List;

import static won.ecommerce.entity.QExchangeRefundLog.*;

public class ExchangeRefundRepositoryCustomImpl implements ExchangeRefundRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public ExchangeRefundRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable) {
        List<SearchExchangeRefundLogDto> content = queryFactory
                .select(new QSearchExchangeRefundLogDto(
                        exchangeRefundLog.createdDate,
                        exchangeRefundLog.id,
                        exchangeRefundLog.userId,
                        exchangeRefundLog.orderItemId,
                        exchangeRefundLog.reason,
                        exchangeRefundLog.status,
                        exchangeRefundLog.logStatus,
                        exchangeRefundLog.processingTime
                ))
                .from(exchangeRefundLog)
                .where(exchangeRefundLog.sellerId.eq(sellerId),
                        createdDateGoe(condition.getTimeGoe()),
                        createdDateLoe(condition.getTimeLoe()),
                        statusEq(condition.getStatus()),
                        logStatusEq(condition.getLogStatus()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(exchangeRefundLog.count())
                .from(exchangeRefundLog)
                .where(exchangeRefundLog.sellerId.eq(sellerId),
                        createdDateGoe(condition.getTimeGoe()),
                        createdDateLoe(condition.getTimeLoe()),
                        statusEq(condition.getStatus()),
                        logStatusEq(condition.getLogStatus()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression createdDateGoe(LocalDateTime timeGoe) {
        return timeGoe != null ? exchangeRefundLog.createdDate.goe(timeGoe) : null;
    }
    private BooleanExpression createdDateLoe(LocalDateTime timeLoe) {
        return timeLoe != null ? exchangeRefundLog.createdDate.loe(timeLoe) : null;
    }
    private BooleanExpression statusEq(ExchangeRefundStatus status) {
        return status != null ? exchangeRefundLog.status.eq(status) : null;
    }
    private BooleanExpression logStatusEq(LogStatus logStatus) {
        return logStatus != null ? exchangeRefundLog.logStatus.eq(logStatus) : null;
    }
}
