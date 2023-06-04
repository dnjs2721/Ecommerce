package won.ecommerce.repository.shoppingCart;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import won.ecommerce.entity.QShoppingCartItem;

import java.util.List;

import static won.ecommerce.entity.QShoppingCartItem.shoppingCartItem;

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
}
