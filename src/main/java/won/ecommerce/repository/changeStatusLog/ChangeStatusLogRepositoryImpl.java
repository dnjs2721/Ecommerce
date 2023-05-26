package won.ecommerce.repository.changeStatusLog;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.entity.LogStat;
import won.ecommerce.repository.dto.search.statusLog.QSearchStatusLogDto;
import won.ecommerce.repository.dto.search.statusLog.SearchStatusLogDto;
import won.ecommerce.repository.dto.search.statusLog.StatusLogSearchCondition;

import java.time.LocalDateTime;
import java.util.List;

import static won.ecommerce.entity.QChangeStatusLog.*;

public class ChangeStatusLogRepositoryImpl implements ChangeStatusLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public ChangeStatusLogRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<SearchStatusLogDto> searchLogsPage(StatusLogSearchCondition condition, Pageable pageable) {
        List<SearchStatusLogDto> content = queryFactory
                .select(new QSearchStatusLogDto(
                        changeStatusLog.id,
                        changeStatusLog.userId,
                        changeStatusLog.createdDate,
                        changeStatusLog.lastModifiedDate,
                        changeStatusLog.beforeStat,
                        changeStatusLog.requestStat,
                        changeStatusLog.logStat,
                        changeStatusLog.adminId,
                        changeStatusLog.processingTime,
                        changeStatusLog.cancelReason
                ))
                .from(changeStatusLog)
                .where(userIdEq(condition.getUserId()),
                        adminIdEq(condition.getAdminId()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()),
                        stateEq(condition.getLogStat())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(changeStatusLog.count())
                .from(changeStatusLog)
                .where(userIdEq(condition.getUserId()),
                        adminIdEq(condition.getAdminId()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()),
                        stateEq(condition.getLogStat())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression stateEq(LogStat logStat) {
        return logStat != null ? changeStatusLog.logStat.eq(logStat) : null;
    }

    // createdDate <= timeLoe -> createdDate 보다 같거나 늦은 시간
    private BooleanExpression createTimeLoe(LocalDateTime timeLoe) {
        return timeLoe != null ? changeStatusLog.createdDate.loe(timeLoe) : null;
    }

    // createdDate >= timeGoe -> createdDate 보다 같거나 빠른 시간
    private BooleanExpression createTimeGoe(LocalDateTime timeGoe) {
        return timeGoe != null ? changeStatusLog.createdDate.goe(timeGoe) : null;
    }

    private BooleanExpression adminIdEq(Long adminId) {
        return adminId != null ? changeStatusLog.adminId.eq(adminId) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? changeStatusLog.userId.eq(userId) : null;
    }
}
