package won.ecommerce.repository.deleted.item;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import static won.ecommerce.entity.QDeletedItem.deletedItem;

public class DeletedItemRepositoryCustomImpl implements DeletedItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public DeletedItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void deleteItemByCreatedAtLessThanEqual(LocalDateTime localDateTime) {
        queryFactory
                .delete(deletedItem)
                .where(deletedItem.createdDate.loe(localDateTime))
                .execute();
    }
}
