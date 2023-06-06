package won.ecommerce.repository.deleted.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import static won.ecommerce.entity.QDeletedUser.deletedUser;

public class DeletedUserRepositoryCustomImpl implements DeletedUserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public DeletedUserRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void deleteUserByCreatedAtLessThanEqual(LocalDateTime localDateTime) {
        queryFactory
                .delete(deletedUser)
                .where(deletedUser.createdDate.loe(localDateTime))
                .execute();
    }
}
