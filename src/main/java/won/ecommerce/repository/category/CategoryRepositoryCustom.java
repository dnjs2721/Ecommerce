package won.ecommerce.repository.category;

import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;
import won.ecommerce.service.dto.category.CategoryItemMailElementDto;

import java.util.List;
import java.util.Map;

public interface CategoryRepositoryCustom {
    List<CategoryItemDto> categoryItem(Long categoryId);
    List<CategoryItemMailElementDto> categoryItemMailElement(Long categoryId);
}
