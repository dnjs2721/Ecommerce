package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.dto.search.SubCategoryItemDto;
import won.ecommerce.repository.user.UserRepository;
import won.ecommerce.repository.dto.search.statusLog.SearchStatusLogDto;
import won.ecommerce.repository.dto.search.user.SearchUsersDto;
import won.ecommerce.repository.dto.search.statusLog.StatusLogSearchCondition;
import won.ecommerce.repository.dto.search.user.UserSearchCondition;
import won.ecommerce.service.dto.CategoryCreateRequestDto;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ChangeStatusLogService changeStatusLogService;
    private final CategoryService categoryService;

    /**
     * 사용자 조회 - 관리자
     */
    public Page<SearchUsersDto> searchUsers(Long id, UserSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkAdmin(id);
        return userRepository.searchUsersPage(condition, pageable);
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 로그 검색
     */
    public Page<SearchStatusLogDto> searchLogs(Long id, StatusLogSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkAdmin(id);
        return changeStatusLogService.searchLogs(condition, pageable);
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경
     */
    @Transactional
    public void changeStatus(Long logId, Long adminId, String stat, String reason) throws IllegalAccessException {
        ChangeStatusLog findLog = changeStatusLogService.checkChangeStatusLog(logId);
        checkAdmin(adminId);

        Optional<User> findUser = userRepository.findById(findLog.getUserId());
        if (findUser.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 회원의 요청입니다.");
        }
        findLog.changeStatus(findUser.get(), stat, adminId, reason);
    }

    /**
     * Category 생성
     */
    @Transactional
    public void createCategory(Long adminId, CategoryCreateRequestDto request) throws IllegalAccessException {
        checkAdmin(adminId);
        categoryService.createCategory(request); //NoSuchElementException 부모 카테고리 없음, IllegalStateException 중보된 카테고리 이름
    }

    /**
     * 자식 카테고리 상품 조회
     */
    public List<SubCategoryItemDto> checkSubCategoryItem(Long adminId, Long parentCategoryId) throws IllegalAccessException {
        checkAdmin(adminId);
        return categoryService.checkSubCategoryItem(parentCategoryId);
    }

    /**
     * 자식 카테고리 상품의 판매자에게 메일 전송
     */

    // 관리자 존재 유무, 권한 확인
    public void checkAdmin(Long id) throws IllegalAccessException {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
    }
}
