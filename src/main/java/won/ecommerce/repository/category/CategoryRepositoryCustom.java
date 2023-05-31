package won.ecommerce.repository.category;

import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;

import java.util.List;

public interface CategoryRepositoryCustom {
    List<CategoryItemDto> categoryItem(List<Long> subCategoryIds);
}
