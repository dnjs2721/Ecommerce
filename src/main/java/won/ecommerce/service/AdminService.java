package won.ecommerce.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.Category;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;
import won.ecommerce.repository.user.UserRepository;
import won.ecommerce.repository.dto.search.statusLog.SearchStatusLogDto;
import won.ecommerce.repository.dto.search.user.SearchUsersDto;
import won.ecommerce.repository.dto.search.statusLog.StatusLogSearchCondition;
import won.ecommerce.repository.dto.search.user.UserSearchCondition;
import won.ecommerce.service.dto.category.CategoryCreateRequestDto;
import won.ecommerce.service.dto.category.CategoryItemMailElementDto;
import won.ecommerce.service.dto.user.JoinRequestDto;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final DuplicationCheckService duplicationCheckService;
    private final ChangeStatusLogService changeStatusLogService;
    private final CategoryService categoryService;
    private final EmailService mailService;
    private final ItemService itemService;

    @Transactional
    public Long adminJoin(JoinRequestDto request) {
        duplicationCheckService.validateDuplicateEmail(request.getEmail());
        duplicationCheckService.validateDuplicateNickname(request.getNickname());
        duplicationCheckService.validateDuplicatePNum(request.getPNum());
        User admin = userService.createUser(request);
        admin.setStatus(UserStatus.ADMIN);
        userRepository.save(admin);
        return admin.getId();
    }

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
        checkAdmin(adminId); // IllegalAccessException 권한 없음
        categoryService.createCategory(request); //NoSuchElementException 부모 카테고리 없음, IllegalStateException 중복된 카테고리 이름
    }

    /**
     * 카테고리 상품 조회
     */
    public List<CategoryItemDto> checkCategoryItem(Long adminId, Category category) throws IllegalAccessException {
        checkAdmin(adminId); // IllegalAccessException 권한 없음
        return categoryService.checkCategoryItem(category); // NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
    }

    /**
     * 카테고리 상품의 판매자에게 경고 메일 전송
     */
    public List<String> sendMailByCategoryItem(Long adminId, Category category) throws IllegalAccessException, MessagingException {
        // 관리자 권한 확인과, 자신, 자식에 등록된 상품을 조회한다.
        // IllegalAccessException 권한 없음, NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
        List<CategoryItemDto> findItems = checkCategoryItem(adminId, category);

        // db 에서 가지고 온 데이터를 가공
        Map<Long, CategoryItemMailElementDto> elementMap = categoryService.categoryItemMailElement(findItems);

        // Key 들(판매자 id)를 통해 반복문 실행
        Set<Long> sellerIds = elementMap.keySet();
        // 판매자 이름을 담기위한 List
        List<String> sellerNames = new ArrayList<>();

        for (Long sellerId : sellerIds) {
            CategoryItemMailElementDto element = elementMap.get(sellerId); // 판매자 id 를 통해 해당하는 Value(Dto) 를 가지고 온다.
            String sellerName = element.getSellerName();
            String sellerEmail = element.getSellerEmail();
            // 판매자에게 경고 메일 전송
            mailService.sendCategoryWarningMail(sellerEmail, category.getName(), sellerName, element.getItemsName());
            // 메일 발송한 사용자 이름 저장
            sellerNames.add(sellerName);
        }

        // 사용자 이름 반환
        return sellerNames;
    }

    /**
     * 카테고리 내 상품 카테고리 일괄 변경 후 메일 발송
     */
    @Transactional
    public List<String> batchChangeItemCategory(Long adminId, Category category, Category changeCategory) throws IllegalAccessException, MessagingException {
        // 관리자 권한 확인과, 자신, 자식에 등록된 상품을 조회한다.
        // IllegalAccessException 권한 없음, NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
        List<CategoryItemDto> findItems = checkCategoryItem(adminId, category);

        List<String> itemNames = itemService.batchChangeItemCategory(findItems, changeCategory);

        // db 에서 가지고 온 데이터를 가공
        Map<Long, CategoryItemMailElementDto> elementMap = categoryService.categoryItemMailElement(findItems);

        // Key 들(판매자 id)를 통해 반복문 실행
        Set<Long> sellerIds = elementMap.keySet();

        for (Long sellerId : sellerIds) {
            CategoryItemMailElementDto element = elementMap.get(sellerId); // 판매자 id 를 통해 해당하는 Value(Dto) 를 가지고 온다.
            String sellerName = element.getSellerName();
            String sellerEmail = element.getSellerEmail();
            // 판매자에게 경고 메일 전송
            mailService.sendCategoryNoticeMail(sellerEmail, category.getName(), changeCategory.getName(), sellerName, itemNames);
        }

        return itemNames;
    }

    /**
     * 카테고리 삭제
     */
    @Transactional
    public String deleteCategory(Long adminId, Long categoryId) throws IllegalAccessException {
        checkAdmin(adminId); // IllegalAccessException
        return categoryService.deleteCategory(categoryId);
        // NoSuchElementException 카테고리 존재 확인
        // IllegalStateException 카테고리 내에 등록된 상품 존재
    }


    // 관리자 존재 유무, 권한 확인
    public void checkAdmin(Long id) throws IllegalAccessException {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
    }
}
