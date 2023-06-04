package won.ecommerce.repository.shoppingCart;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.entity.QItem;
import won.ecommerce.entity.QShoppingCart;
import won.ecommerce.entity.QShoppingCartItem;
import won.ecommerce.entity.QUser;
import won.ecommerce.repository.dto.search.shoppingCart.QSearchShoppingCartDto;
import won.ecommerce.repository.dto.search.shoppingCart.SearchShoppingCartDto;

import java.util.List;

import static won.ecommerce.entity.QItem.*;
import static won.ecommerce.entity.QShoppingCart.*;
import static won.ecommerce.entity.QShoppingCartItem.shoppingCartItem;
import static won.ecommerce.entity.QUser.*;

public class ShoppingCartItemRepositoryCustomImpl implements ShoppingCartItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public ShoppingCartItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        queryFactory
                .delete(shoppingCartItem)
                .where(shoppingCartItem.id.in(ids))
                .execute();
    }

    @Override
    public Page<SearchShoppingCartDto> searchShoppingCart(Long shoppingCartId, Pageable pageable) {
        List<SearchShoppingCartDto> content = queryFactory
                .select(new QSearchShoppingCartDto(
                        item.name,
                        user.nickname,
                        shoppingCartItem.itemCount,
                        shoppingCartItem.itemPrice,
                        shoppingCartItem.totalItemPrice
                ))
                .from(shoppingCartItem)
                .leftJoin(shoppingCartItem.item, item)
                .leftJoin(shoppingCartItem.item.seller, user)
                .leftJoin(shoppingCartItem.shoppingCart, shoppingCart)
                .where(shoppingCart.id.eq(shoppingCartId))
                .orderBy(shoppingCartItem.createdDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.
                select(shoppingCartItem.count())
                .from(shoppingCartItem)
                .leftJoin(shoppingCartItem.item, item)
                .leftJoin(shoppingCartItem.item.seller, user)
                .leftJoin(shoppingCartItem.shoppingCart, shoppingCart)
                .where(shoppingCart.id.eq(shoppingCartId))
                .orderBy(shoppingCartItem.createdDate.asc());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
