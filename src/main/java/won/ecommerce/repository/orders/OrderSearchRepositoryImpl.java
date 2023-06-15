package won.ecommerce.repository.orders;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.entity.OrderItemStatus;
import won.ecommerce.repository.dto.search.order.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.*;
import static won.ecommerce.entity.QOrderItem.orderItem;
import static won.ecommerce.entity.QOrdersForBuyer.ordersForBuyer;
import static won.ecommerce.entity.QOrdersForSeller.ordersForSeller;
import static won.ecommerce.entity.QUser.user;

public class OrderSearchRepositoryImpl implements OrderSearchRepository{

    private final JPAQueryFactory queryFactory;
    public OrderSearchRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable) {
        List<SearchOrdersForBuyerDto> content = queryFactory
                .select(new QSearchOrdersForBuyerDto(
                        ordersForBuyer.id,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(orderItem.totalPrice.sum())
                                        .from(orderItem)
                                        .where(orderItem.buyerOrderId.eq(ordersForBuyer.id),
                                                orderItem.orderItemStatus.ne(OrderItemStatus.CANCEL)), "orderPrice"),
                        ordersForBuyer.createdDate))
                .from(ordersForBuyer)
                .where(ordersForBuyer.buyer.id.eq(buyerId),
                        orderTimeGoeForBuyer(condition.getTimeGoe()),
                        orderTimeLoeForBuyer(condition.getTimeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(ordersForBuyer.count())
                .from(ordersForBuyer)
                .where(ordersForBuyer.buyer.id.eq(buyerId),
                        orderTimeGoeForBuyer(condition.getTimeGoe()),
                        orderTimeLoeForBuyer(condition.getTimeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable) {
        List<SearchOrdersForSellerDto> content = queryFactory
                .select(new QSearchOrdersForSellerDto(
                        ordersForSeller.id,
                        ordersForSeller.buyerName,
                        ordersForSeller.buyerPNum,
                        ordersForSeller.buyerAddress,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(orderItem.totalPrice.sum())
                                        .from(orderItem)
                                        .where(orderItem.sellerOrderId.eq(ordersForSeller.id),
                                                orderItem.orderItemStatus.ne(OrderItemStatus.CANCEL)), "orderPrice"),
                        ordersForSeller.createdDate))
                .from(ordersForSeller)
                .where(ordersForSeller.seller.id.eq(sellerId),
                        orderTimeGoeForSeller(condition.getTimeGoe()),
                        orderTimeLoeForSeller(condition.getTimeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(ordersForSeller.count())
                .from(ordersForSeller)
                .where(ordersForSeller.seller.id.eq(sellerId),

                        orderTimeGoeForSeller(condition.getTimeGoe()),
                        orderTimeLoeForSeller(condition.getTimeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<SearchOrderItemForSellerDto> searchOrderItemsForSeller(Long orderId) {
        return queryFactory
                .select(new QSearchOrderItemForSellerDto(
                        orderItem.id,
                        orderItem.itemId,
                        orderItem.itemName,
                        orderItem.price,
                        orderItem.count,
                        orderItem.totalPrice,
                        orderItem.orderItemStatus,
                        orderItem.comment
                        ))
                .from(orderItem)
                .where(orderItem.sellerOrderId.eq(orderId))
                .fetch();
    }

    @Override
    public List<SearchOrderItemsForBuyerDto> searchOrderItemsForBuyer(Long orderId) {
        return queryFactory
                .select(new QSearchOrderItemsForBuyerDto(
                        orderItem.id,
                        orderItem.itemId,
                        user.name,
                        orderItem.itemName,
                        orderItem.price,
                        orderItem.count,
                        orderItem.totalPrice,
                        orderItem.orderItemStatus,
                        orderItem.comment
                ))
                .from(orderItem)
                .leftJoin(user).on(orderItem.sellerId.eq(user.id))
                .where(orderItem.buyerOrderId.eq(orderId))
                .fetch();
    }

//    statusEq(condition.getStatus(), ordersForSeller.orderStatus),
    private BooleanExpression statusEq(String status, EnumPath<OrderItemStatus> orderStatus) {
        if (!hasText(status)) {
            return null;
        } else {
            switch (status) {
                case "결재대기" -> {
                    return orderStatus.eq(OrderItemStatus.WAITING_FOR_PAYMENT);
                }
                case "결재완료" -> {
                    return orderStatus.eq(OrderItemStatus.COMPLETE_PAYMENT);
                }
                case "배송준비중" -> {
                    return orderStatus.eq(OrderItemStatus.WAITING_FOR_DELIVERY);
                }
                case "배송중" -> {
                    return orderStatus.eq(OrderItemStatus.SHIPPING);
                }
                case "배송완료" -> {
                    return orderStatus.eq(OrderItemStatus.DELIVERY_COMPLETE);
                }
                case "취소" -> {
                    return orderStatus.eq(OrderItemStatus.CANCEL);
                }
                default -> {
                    return null;
                }
            }
        }
    }

    private BooleanExpression orderTimeGoeForBuyer(LocalDateTime timeGoe) {
        return timeGoe != null ? ordersForBuyer.createdDate.goe(timeGoe) : null;
    }
    private BooleanExpression orderTimeLoeForBuyer(LocalDateTime timeLoe) {
        return timeLoe != null ? ordersForBuyer.createdDate.loe(timeLoe) : null;
    }

    private BooleanExpression orderTimeGoeForSeller(LocalDateTime timeGoe) {
        return timeGoe != null ? ordersForSeller.createdDate.goe(timeGoe) : null;
    }
    private BooleanExpression orderTimeLoeForSeller(LocalDateTime timeLoe) {
        return timeLoe != null ? ordersForSeller.createdDate.loe(timeLoe) : null;
    }
}
