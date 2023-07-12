package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.Category;
import won.ecommerce.repository.category.CategoryRepository;
import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;
import won.ecommerce.service.dto.category.CategoryCreateRequestDto;
import won.ecommerce.service.dto.category.CategoryItemMailElementDto;

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
        checkDuplicateCategory(request.getName()); // IllegalStateException 중복된 카테고리 이름
        Category category = new Category(request.getName());
        if (request.getParentId() != null) {
            Category parentCategory = checkCategory(request.getParentId()); // NoSuchElementException 부모 카테고리 없음
            category.addParentCategory(parentCategory);
        }
        categoryRepository.save(category);
    }

    /**
     * 카테고리 상품 체크
     */
    public  List<CategoryItemDto> checkCategoryItem(Category category) {
        // sellerId, sellerName, sellerEmail
        // categoryName
        // itemId, itemName
        // 이 담긴 Dto 를 상품 갯수만큼의 size 를 가진 List 로 반환
        List<CategoryItemDto> categoryItems = categoryRepository.categoryItem(category.getId());

        if (!categoryItems.isEmpty()) {
            return categoryItems;
        } else {
            throw new NoSuchElementException("카테고리에 등록된 상품이 없습니다.");  // 자신, 자식 모두 등록된 상품이 없다면
        }
    }

    /**
     * 카테고리 상품 메일 전송 Element
     */
//    public Map<Long, CategoryItemMailElementDto> categoryItemMailElement(List<CategoryItemDto> categoryItems) {
//        // Map 을 통해 sellerId 를 기준으로 데이터 가공
//        Map<Long, CategoryItemMailElementDto> sellerInfoAndItemName = new HashMap<>();
//        for (CategoryItemDto categoryItem : categoryItems) {
//            Long sellerId = categoryItem.getSellerId();
//            // 만약 Key 에 sellerId 가 없다면 새로운 Key 를 생성하고 판매자 이름과, 이메일, 상품 이름을 가진 Dto 를 Value 로 설정
//            if (!sellerInfoAndItemName.containsKey(sellerId)) {
//                sellerInfoAndItemName.put(sellerId, new CategoryItemMailElementDto(
//                        categoryItem.getSellerName(), categoryItem.getSellerEmail(), categoryItem.getItemName()));
//            } else { // Key 에 이미 sellerId 가 있다면 상품 이름 리스트에 현재 상품의 이름을 추가한다.
//                sellerInfoAndItemName.get(sellerId).getItemsName().add(categoryItem.getItemName());
//            }
//        }
//        // 판매자 Id 를 기준으로 가공된 데이터를 반환
//        return sellerInfoAndItemName;
//    }
    public List<CategoryItemMailElementDto> categoryItemMailElement(Long categoryId) {
        List<CategoryItemMailElementDto> elementDto = categoryRepository.categoryItemMailElement(categoryId);
        if (!elementDto.isEmpty()) {
            return elementDto;
        } else {
            throw new NoSuchElementException("카테고리에 등록된 상품이 없습니다.");  // 자신, 자식 모두 등록된 상품이 없다면
        }
    }

    /**
     * 카테고리 삭제
     */
    public String deleteCategory(Long categoryId) {
        Category category = checkCategory(categoryId); // NoSuchElementException 카테고리 존재 확인

        List<CategoryItemDto> categoryItems = categoryRepository.categoryItem(categoryId);
        if (!categoryItems.isEmpty()) {
            throw new IllegalStateException("카테고리 내에 등록된 상품이 있습니다. 변경 혹은 삭제후 다시 시도해 주세요.");
        }

        List<Long> childIds = checkChildCategories(category);

        if (!childIds.isEmpty()) { // 외래키 제약조건을 해결하기 위해 자식 카테고리가 있다면 자식 카테고리부터 일괄 삭제
            categoryRepository.deleteAllByIdInBatch(childIds);
        }

        categoryRepository.deleteById(categoryId); // 카테고리 삭제

        return category.getName();
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
    public void checkDuplicateCategory(String categoryName) {
        Optional<Category> findCategory = categoryRepository.findByName(categoryName);
        if (findCategory.isPresent()) {
            throw new IllegalStateException("이미 존재하는 카테고리 이름입니다.");
        }
    }

    // 자식 카테고리들의 Id를 반환
    public List<Long> checkChildCategories(Category category) {
        List<Category> child = category.getChild();
        List<Long> childIds = new ArrayList<>();
        for (Category childCategory : child) {
            childIds.add(childCategory.getId());
        }
        return childIds;
    }
}
