package won.ecommerce.repository.category;

import won.ecommerce.repository.dto.search.SubCategoryItemDto;

import java.util.List;

public interface CategoryRepositoryCustom {
    List<SubCategoryItemDto> subCategoryItem(List<Long> subCategoryIds);
}
