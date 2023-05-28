package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.Category;
import won.ecommerce.repository.category.CategoryRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;


    // 카테고리 존재 확인
    public Category checkCategory(Long id) {
        Optional<Category> findCategory = categoryRepository.findById(id);
        if (findCategory.isEmpty()) {
            throw new NoSuchElementException("카테고리를 다시 확인해 주세요.");
        }
        return findCategory.get();
    }
}
