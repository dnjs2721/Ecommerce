package won.ecommerce.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import won.ecommerce.entity.QUser;

import static won.ecommerce.entity.QUser.*;

public class UserRepositoryImpl implements UserRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public String findEmailByNameAndPNum(String name, String pNum) {
        return queryFactory
                .select(user.email)
                .from(user)
                .where(user.name.eq(name),
                        user.pNum.eq(pNum))
                .fetchOne();
    }
}
