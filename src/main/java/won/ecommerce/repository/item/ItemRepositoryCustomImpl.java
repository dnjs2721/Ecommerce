package won.ecommerce.repository.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.QSearchItemDto;
import won.ecommerce.repository.dto.search.item.SearchItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.*;
import static won.ecommerce.entity.QCategory.*;
import static won.ecommerce.entity.QItem.*;

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
                        categoryEQ(condition.getCategory()),
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
                        categoryEQ(condition.getCategory()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression createTimeLoe(LocalDateTime timeLoe) {
        return timeLoe != null ? item.createdDate.loe(timeLoe) : null;
    }

    private BooleanExpression createTimeGoe(LocalDateTime timeGoe) {
        return timeGoe != null ? item.createdDate.goe(timeGoe) : null;
    }

    private BooleanExpression categoryEQ(String category) {
        return hasText(category) ? item.category.name.eq(category) : null;
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
        return hasText(itemName) ? item.name.eq(itemName) : null;
    }
}
