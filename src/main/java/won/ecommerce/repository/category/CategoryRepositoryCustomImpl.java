package won.ecommerce.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;
import won.ecommerce.repository.dto.search.categoryItem.QCategoryItemDto;

import java.util.ArrayList;
import java.util.List;

import static won.ecommerce.entity.QCategory.category;
import static won.ecommerce.entity.QItem.item;
import static won.ecommerce.entity.QUser.user;

@Slf4j
public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public CategoryRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<CategoryItemDto> categoryItem(List<Long> subCategoryIds, Long categoryId) {
        List<Long> ids = new ArrayList<>(List.copyOf(subCategoryIds));
        ids.add(categoryId);

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
                .where(item.category.id.in(ids))
                .orderBy(user.id.asc())
                .fetch();
    }

}
