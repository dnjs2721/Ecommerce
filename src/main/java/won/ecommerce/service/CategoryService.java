package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.Category;
import won.ecommerce.entity.Item;
import won.ecommerce.repository.category.CategoryRepository;
import won.ecommerce.repository.dto.search.SubCategoryItemDto;
import won.ecommerce.service.dto.CategoryCreateRequestDto;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 생성
     */
    public void createCategory(CategoryCreateRequestDto request) {
        Category category = checkDuplicateCategory(request.getName());
        Category parentCategory = checkCategory(request.getParentId());
        category.addParentCategory(parentCategory);
        categoryRepository.save(category);
    }

    /**
     * 하위 카테고리 상품 체크
     */
    public  List<SubCategoryItemDto> checkSubCategoryItem(Long parentCategoryId) {
        Category category = checkCategory(parentCategoryId);
        List<Long> childIds = checkChildCategories(category);
        List<SubCategoryItemDto> subCategoryItems = categoryRepository.subCategoryItem(childIds);

        if (!subCategoryItems.isEmpty()) {
            return subCategoryItems;
        } else {
            throw new NoSuchElementException("카테고리에 등록된 상품이 없습니다.");
        }
    }

    /**
     * 하위 카테고리 상품 메일 전송 Element
     */
    public Map<Long, List<String>> subCategoryItemMailElement(List<SubCategoryItemDto> subCategoryItems) {
        Map<Long, List<String>> sellerIdAndItemsName = new HashMap<>();
        for (SubCategoryItemDto subCategoryItem : subCategoryItems) {
            if (!sellerIdAndItemsName.containsKey(subCategoryItem.getSellerId())) {
                sellerIdAndItemsName.put(subCategoryItem.getSellerId(), new ArrayList<>(List.of(subCategoryItem.getItemName())));
            } else {
                sellerIdAndItemsName.get(subCategoryItem.getSellerId()).add(subCategoryItem.getItemName());
            }
        }
        return sellerIdAndItemsName;
    }

    /**
     * 하위 카테고리 상품 전체 id
     */
    public  List<Long> subCategoryItemsId(List<SubCategoryItemDto> subCategoryItems) {
        List<Long> itemIds = new ArrayList<>();
        for (SubCategoryItemDto subCategoryItem : subCategoryItems) {
            itemIds.add(subCategoryItem.getItemId());
        }
        return itemIds;
    }

    // 카테고리 존재 확인
    public Category checkCategory(Long id) {
        Optional<Category> findCategory = categoryRepository.findById(id);
        if (findCategory.isEmpty()) {
            throw new NoSuchElementException("카테고리를 다시 확인해 주세요.");
        }
        return findCategory.get();
    }

    // 카테고리 이름 중복 확인
    public Category checkDuplicateCategory(String categoryName) {
        Optional<Category> findCategory = categoryRepository.findByName(categoryName);
        if (findCategory.isPresent()) {
            throw new IllegalStateException("이미 존재하는 카테고리 이름입니다.");
        }
        return findCategory.get();
    }

    public List<Long> checkChildCategories(Category category) {
        List<Category> child = category.getChild();
        List<Long> childIds = new ArrayList<>();
        for (Category childCategory : child) {
            childIds.add(childCategory.getId());
        }
        return childIds;
    }

    public List<Long> checkIncludeItemsByCategory(Category category) {
        List<Item> items = category.getItems();
        List<Long> itemsIds = new ArrayList<>();
        for (Item item : items) {
            itemsIds.add(item.getId());
        }
        return itemsIds;
    }

}
