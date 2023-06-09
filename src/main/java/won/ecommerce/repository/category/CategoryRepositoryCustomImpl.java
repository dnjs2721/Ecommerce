package won.ecommerce.repository.category;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;
import won.ecommerce.repository.dto.search.categoryItem.QCategoryItemDto;
import won.ecommerce.service.dto.category.CategoryItemMailElementDto;
import won.ecommerce.service.dto.category.QCategoryItemMailElementDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static won.ecommerce.entity.QCategory.category;
import static won.ecommerce.entity.QItem.item;
import static won.ecommerce.entity.QUser.user;
import static com.querydsl.core.group.GroupBy.list;

@Slf4j
public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public CategoryRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    @Override
    public List<CategoryItemDto> categoryItem(Long categoryId) {
        return queryFactory
                .select(new QCategoryItemDto(
                        user.id,
                        user.name,
                        user.email,
                        category.name,
                        item.id,
                        item.name
                ))
                .from(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.category.id.eq(categoryId).or(item.category.parent.id.eq(categoryId)))
                .orderBy(user.id.asc())
                .fetch();
    }

    @Override
    public List<CategoryItemMailElementDto> categoryItemMailElement(Long categoryId) {
        return queryFactory
                .selectFrom(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.category.id.eq(categoryId).or(item.category.parent.id.eq(categoryId)))
                .orderBy(user.id.asc())
                .transform(groupBy(user.id).list(new QCategoryItemMailElementDto(
                        user.id,
                        user.name,
                        user.email,
                        list(Projections.constructor(String.class, item.name))
                )));
    }
}
