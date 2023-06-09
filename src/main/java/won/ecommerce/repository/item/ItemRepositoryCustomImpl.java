package won.ecommerce.repository.item;

import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import retrofit2.http.PUT;
import won.ecommerce.entity.Category;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.QShoppingCartItem;
import won.ecommerce.entity.ShoppingCartItem;
import won.ecommerce.repository.dto.search.item.SortCondition;
import won.ecommerce.repository.dto.search.item.*;
import won.ecommerce.service.dto.item.ChangeItemInfoRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.*;
import static won.ecommerce.entity.QCategory.*;
import static won.ecommerce.entity.QCategory.category;
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
    public Page<SearchItemFromCommonDto> searchItemPageFromCommon(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable) {
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
                .orderBy(createOrderSpecifier(sortCondition).toArray(OrderSpecifier[]::new))
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
    public void batchUpdateItemCategory(Long categoryId, Long changeCategoryId) {
        List<Long> itemIds = queryFactory
                .select(item.id)
                .from(item)
                .leftJoin(item.category, category)
                .where(category.id.eq(categoryId).or(category.parent.id.eq(categoryId)))
                .fetch();

        queryFactory
                .update(item)
                .set(item.category.id, changeCategoryId)
                .where(item.id.in(itemIds))
                .execute();
    }

    @Override
    public void changeItemInfo(ChangeItemInfoRequestDto request) {
        JPAUpdateClause itemInfo = queryFactory
                .update(item)
                .where(item.id.eq(request.getItemId()));

        if (request.getChangePrice() != null) {
            itemInfo.set(item.price, request.getChangePrice());
            queryFactory
                    .update(shoppingCartItem)
                    .set(shoppingCartItem.itemPrice, request.getChangePrice())
                    .set(shoppingCartItem.totalItemPrice, shoppingCartItem.itemCount.multiply(request.getChangePrice()))
                    .where(shoppingCartItem.item.id.eq(request.getItemId()))
                    .execute();
        }
        if (request.getChangeStockQuantity() != null) {
            itemInfo.set(item.stockQuantity, request.getChangeStockQuantity());
        }
        if (request.getChangeCategoryId() != null) {
            itemInfo.set(item.category.id, request.getChangeCategoryId());
        }

        itemInfo.execute();
    }

    @Override
    public List<Item> findItemBySellerIdAndItemIds(Long sellerId, List<Long> itemIds) {
        return queryFactory
                .select(item)
                .from(item)
                .where(item.seller.id.eq(sellerId),
                        item.id.in(itemIds))
                .fetch();
    }

    @Override
    public List<ShoppingCartItem> getShoppingCartItem(List<Long> itemIds) {
        return queryFactory
                .select(shoppingCartItem)
                .from(shoppingCartItem)
                .where(shoppingCartItem.item.id.in(itemIds))
                .fetch();
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

    public List<OrderSpecifier<?>> createOrderSpecifier(SortCondition orderCondition) {
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
