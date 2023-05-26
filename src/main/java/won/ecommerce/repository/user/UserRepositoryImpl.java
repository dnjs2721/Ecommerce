package won.ecommerce.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.dto.search.user.QSearchUsersDto;
import won.ecommerce.repository.dto.search.user.SearchUsersDto;
import won.ecommerce.repository.dto.search.user.UserSearchCondition;

import java.util.List;

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

    @Override
    public Page<SearchUsersDto> searchUsersPage(UserSearchCondition condition, Pageable pageable) {
        List<SearchUsersDto> content = queryFactory
                .select(new QSearchUsersDto(
                        user.id,
                        user.status,
                        user.email,
                        user.password,
                        user.name,
                        user.nickname,
                        user.pNum,
                        user.birth,
                        user.address,
                        user.createdDate,
                        user.lastModifiedDate))
                .from(user)
                .where(userStatEq(condition.getUserStatus()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(userStatEq(condition.getUserStatus()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression userStatEq(UserStatus userStatus) {
        return userStatus != null ? user.status.eq(userStatus) : null;
    }
}
