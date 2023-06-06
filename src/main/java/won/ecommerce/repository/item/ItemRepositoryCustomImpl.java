package won.ecommerce.repository.item;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.entity.Category;
import won.ecommerce.repository.dto.search.item.OrderCondition;
import won.ecommerce.repository.dto.search.item.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.*;
import static won.ecommerce.entity.QCategory.*;
import static won.ecommerce.entity.QItem.*;
import static won.ecommerce.entity.QShoppingCartItem.*;
import static won.ecommerce.entity.QUser.*;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable) {
        List<SearchItemDto> content = queryFactory
                .select(new QSearchItemDto(
                        item.id,
                        item.seller.id,
                        item.createdDate,
                        item.lastModifiedDate,
                        item.name,
                        item.price,
                        item.stockQuantity,
                        category.name
                ))
                .from(item)
                .leftJoin(item.category, category)
                .where(item.seller.id.eq(sellerId),
                        itemNameEq(condition.getItemName()),
                        priceGoe(condition.getPriceGoe()),
                        priceLoe(condition.getPriceLoe()),
                        stockQuantityGoe(condition.getStockQuantityGoe()),
                        stockQuantityLoe(condition.getStockQuantityLoe()),
                        categoryEQ(condition.getCategoryId()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .leftJoin(item.category, category)
                .where(item.seller.id.eq(sellerId),
                        itemNameEq(condition.getItemName()),
                        priceGoe(condition.getPriceGoe()),
                        priceLoe(condition.getPriceLoe()),
                        stockQuantityGoe(condition.getStockQuantityGoe()),
                        stockQuantityLoe(condition.getStockQuantityLoe()),
                        categoryEQ(condition.getCategoryId()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<SearchItemFromCommonDto> searchItemPageFromCommon(ItemSearchFromCommonCondition condition, OrderCondition orderCondition, Pageable pageable) {
        List<SearchItemFromCommonDto> content = queryFactory
                .select(new QSearchItemFromCommonDto(
                        new QSellerInfoDto(
                                user.nickname,
                                user.email,
                                user.pNum),
                        category.name,
                        item.name,
                        item.price
                ))
                .from(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.stockQuantity.goe(1),
                        itemNameEq(condition.getItemName()),
                        sellerNickNameEq(condition.getSellerNickName()),
                        priceGoe(condition.getPriceGoe()),
                        priceLoe(condition.getPriceLoe()),
                        categoryEQ(condition.getCategoryId()))
                .orderBy(createOrderSpecifier(orderCondition).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(item.count())
                .from(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.stockQuantity.goe(1),
                        itemNameEq(condition.getItemName()),
                        sellerNickNameEq(condition.getSellerNickName()),
                        priceGoe(condition.getPriceGoe()),
                        priceLoe(condition.getPriceLoe()),
                        categoryEQ(condition.getCategoryId()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public void batchUpdateItemCategory(List<Long> itemIds, Category category) {
        queryFactory
                .update(item)
                .set(item.category, category)
                .where(item.id.in(itemIds))
                .execute();
    }

    @Override
    public void changePrice(Long itemId, int price) {
        queryFactory
                .update(item)
                .set(item.price, price)
                .where(item.id.eq(itemId))
                .execute();

        queryFactory
                .update(shoppingCartItem)
                .set(shoppingCartItem.itemPrice, price)
                .set(shoppingCartItem.totalItemPrice, shoppingCartItem.itemCount.multiply(price))
                .where(shoppingCartItem.item.id.eq(itemId))
                .execute();
    }

    private BooleanExpression sellerNickNameEq(String sellerNickName) {
        return hasText(sellerNickName) ? user.nickname.like("%" + sellerNickName + "%") : null;
    }

    private BooleanExpression createTimeLoe(LocalDateTime timeLoe) {
        return timeLoe != null ? item.createdDate.loe(timeLoe) : null;
    }

    private BooleanExpression createTimeGoe(LocalDateTime timeGoe) {
        return timeGoe != null ? item.createdDate.goe(timeGoe) : null;
    }

    private BooleanExpression categoryEQ(Long categoryId) {
        return categoryId != null ? item.category.id.eq(categoryId) : null;
    }

    private BooleanExpression stockQuantityLoe(Integer stockQuantityLoe) {
        return stockQuantityLoe != null ? item.stockQuantity.loe(stockQuantityLoe) : null;
    }

    private BooleanExpression stockQuantityGoe(Integer stockQuantityGoe) {
        return stockQuantityGoe != null ? item.stockQuantity.goe(stockQuantityGoe) : null;
    }

    private BooleanExpression priceLoe(Integer priceLoe) {
        return priceLoe != null ? item.price.loe(priceLoe) : null;
    }

    private BooleanExpression priceGoe(Integer priceGoe) {
        return priceGoe != null ? item.price.goe(priceGoe) : null;
    }

    private BooleanExpression itemNameEq(String itemName) {
        return hasText(itemName) ? item.name.like("%"+itemName+"%") : null;
    }

    public List<OrderSpecifier<?>> createOrderSpecifier(OrderCondition orderCondition) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        checkOrderCondition(orderSpecifiers, orderCondition.getOrderName1(), orderCondition.getOrderDirect1());
        checkOrderCondition(orderSpecifiers, orderCondition.getOrderName2(), orderCondition.getOrderDirect2());
        checkOrderCondition(orderSpecifiers, orderCondition.getOrderName3(), orderCondition.getOrderDirect3());
        return orderSpecifiers;
    }
    public void checkOrderCondition(List<OrderSpecifier<?>> orderSpecifiers,String orderName, String orderDirect) {
        if (hasText(orderName)) {
            if (orderName.equals("price")) {
                if (hasText(orderDirect) && orderDirect.equals("ASC")) {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, item.price));
                } else {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, item.price));
                }
            } else if (orderName.equals("name")) {
                if (hasText(orderDirect) && orderDirect.equals("ASC")) {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, item.name));
                } else {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, item.name));
                }
            }
        }
    }
}
