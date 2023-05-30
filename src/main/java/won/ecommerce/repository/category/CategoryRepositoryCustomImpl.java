package won.ecommerce.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import won.ecommerce.repository.dto.search.QSubCategoryItemDto;
import won.ecommerce.repository.dto.search.SubCategoryItemDto;

import java.util.List;

import static won.ecommerce.entity.QCategory.category;
import static won.ecommerce.entity.QItem.item;
import static won.ecommerce.entity.QUser.user;

public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public CategoryRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<SubCategoryItemDto> subCategoryItem(List<Long> subCategoryIds) {
        return queryFactory
                .select(new QSubCategoryItemDto(
                        user.id,
                        category.name,
                        item.id,
                        item.name
                ))
                .from(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.category.id.in(subCategoryIds))
                .orderBy(user.id.asc())
                .fetch();
    }

}
