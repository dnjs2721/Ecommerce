package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import won.ecommerce.controller.dto.userDto.CreateExchangeRefundLogRequestDto;
import won.ecommerce.entity.*;
import won.ecommerce.repository.deleted.user.DeletedUserRepository;
import won.ecommerce.repository.dto.search.item.SortCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchFromCommonCondition;
import won.ecommerce.repository.dto.search.item.SearchItemFromCommonDto;
import won.ecommerce.repository.dto.search.order.OrderSearchCondition;
import won.ecommerce.repository.dto.search.order.SearchOrderItemsForBuyerDto;
import won.ecommerce.repository.dto.search.order.SearchOrdersForBuyerDto;
import won.ecommerce.repository.dto.search.shoppingCart.SearchShoppingCartDto;
import won.ecommerce.service.dto.user.ChangeUserInfoRequestDto;
import won.ecommerce.service.dto.user.JoinRequestDto;
import won.ecommerce.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static io.micrometer.common.util.StringUtils.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DeletedUserRepository deletedUserRepository;
    private final DuplicationCheckService duplicationCheckService;
    private final ChangeStatusLogService changeStatusLogService;
    private final ItemService itemService;
    private final ShoppingCartService shoppingCartService;
    private final OrdersService ordersService;
    private final PaymentService paymentService;
    private final ExchangeRefundLogService exchangeRefundLogService;

    /**
     * 회원가입
     */
    @Transactional
    public Long join(User user) {
        duplicationCheckService.validateDuplicateEmail(user.getEmail());
        duplicationCheckService.validateDuplicateNickname(user.getNickname());
        duplicationCheckService.validateDuplicatePNum(user.getPNum());
        userRepository.save(user);
        return user.getId();
    }

    /**
     * 로그인
     */
    public Long login(String email, String password) throws IllegalAccessException {
        User user = checkUserByEmail(email);
        if (user.getPassword().equals(password)) {
            return user.getId();
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * 아이디(이메일) 찾기
     */
    public String findEmailByNameAndPNum(String name, String pNum) {
        String email = userRepository.findEmailByNameAndPNum(name, pNum);
        if (email == null) {
            throw new NoSuchElementException("가입되지 않은 회원 입니다. 이름 혹은 전화번호를 확인 해 주세요.");
        }
        return email;
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public String changePassword(String email, String password, String newPassword) throws IllegalAccessException {
        User user = checkUserByEmail(email);
        if (user.getPassword().equals(password)) {
            if (user.getPassword().equals(newPassword)) {
                throw new IllegalStateException("현재 사용중인 패스워드와 같습니다.");
            }
            user.changePassword(newPassword);
            return email;
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public String deleteUser(String email, String password) throws IllegalAccessException {
        User user = checkUserByEmail(email);
        if (password.equals(user.getPassword())) {
            shoppingCartService.deleteShoppingCart(user.getShoppingCart());
            saveDeletedUser(user);
            userRepository.delete(user);
            return user.getName();
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * 정보 수정
     * 닉네임, 주소
     */
    @Transactional
    public void changeUserInfo(ChangeUserInfoRequestDto request) throws IllegalAccessException {
        User user = checkUserByEmail(request.getEmail()); // NoSuchElementException
        if (request.getPassword().equals(user.getPassword())) {
            Address address = changeUserInfoAddress(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()); // IllegalArgumentException, 주소 형태 확인
            if (request.getNickname() != null) {
                String newNickname = changeUserInfoNickname(request.getNickname(), user.getNickname()); // IllegalStateException,닉네임 사용가능 유무 확인
                user.changeNickname(newNickname);
            }
            if (address != null) user.changeAddress(address);
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }

    /**
     * COMMON-SELLER, SELLER-COMMON 변경 요청 작성
     */
    @Transactional
    public Long createChangeStatusLog(long userId) {
        User user = checkUserById(userId);
        return changeStatusLogService.createChangeStatusLog(user);
    }

    /**
     * 상품 조회
     */
    public Page<SearchItemFromCommonDto> searchItems(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable) {
        return itemService.searchItemFromCommon(condition, sortCondition, pageable);
    }

    /**
     * 장바구니 상품 추가
     */
    @Transactional
    public String addShoppingCartItem(Long userId, Long itemId, int itemCount) {
        User user = checkUserById(userId); // NoSuchElementException
        Item item = itemService.checkItem(itemId); // NoSuchElementException 상품 없음
        shoppingCartService.addItem(user.getShoppingCart(), item, itemCount);
        return item.getName();
    }

    /**
     * 장바구니 상품 수량 변경
     */
    @Transactional
    public String changeShoppingCartItemCount(Long userId, Long shoppingCartItemId, int changeCount) throws IllegalAccessException {
        User user = checkUserById(userId); // NoSuchElementException
        return shoppingCartService.changeCount(user.getShoppingCart().getId(), shoppingCartItemId, changeCount);
    }

    /**
     * 장바구니 선택 상품 삭제
     */
    @Transactional
    public List<String> deleteShoppingCartItem(Long userId, List<Long> shoppingCartItemIds) {
        User user = checkUserById(userId);
        return shoppingCartService.deleteShoppingCartItemByListIds(user, shoppingCartItemIds);
    }
    /**
     * 장바구니 비우기
     */
    @Transactional
    public String deleteAllShoppingCartItem(Long userId) {
        User user = checkUserById(userId); //NoSuchElementException
        shoppingCartService.deleteAllItems(user.getShoppingCart()); //NoSuchElementException 등록된 상품 없음
        return user.getName();
    }

    /**
     * 장바구니 전체 가격 조회
     */
    public int getShoppingCartTotalPrice(Long userId) {
        User user = checkUserById(userId); //NoSuchElementException
        return shoppingCartService.getTotalPrice(user.getShoppingCart());
    }

    /**
     * 장바구니 전체 상품 조회
     */
    public Page<SearchShoppingCartDto> getShoppingCartItems(Long userId, Pageable pageable) {
        User user = checkUserById(userId); //NoSuchElementException
        return shoppingCartService.getShoppingCartItems(user.getShoppingCart().getId(), pageable);
    }

    /**
     * 장바구니 전체 상품 주문
     */
    @Transactional
    public List<String> orderAllItemAtShoppingCart(Long userId) {
        User user = checkUserById(userId);
        return shoppingCartService.orderAllItemAtShoppingCart(user);
    }

    /**
     * 장바구니 상품 선택 주문
     */
    @Transactional
    public List<String> orderSelectItemAtShoppingCart(Long userId, List<Long> shoppingCartItemId) {
        User user = checkUserById(userId);
        return shoppingCartService.orderSelectItemAtShoppingCart(user, shoppingCartItemId);
    }

    /**
     * 상품 단건 주문
     */
    @Transactional
    public String orderSingleItem(Long buyerId, Long itemId, int itemCount) {
        User buyer = checkUserById(buyerId); // NoSuchElementException
        Item item = itemService.checkItem(itemId); // NoSuchElementException
        return ordersService.orderSingleItem(buyer, item, itemCount);
    }

    /**
     * 주문 조회 사용자
     */
    public Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable) {
        User user = checkUserById(buyerId);
        return ordersService.searchOrdersForBuyer(buyerId, condition, pageable);
    }

    /**
     * 주문 상세 조회 사용자
     */
    public List<SearchOrderItemsForBuyerDto> searchOrderDetailForBuyer(Long buyerId, Long orderId) throws IllegalAccessException {
        User user = checkUserById(buyerId);
        return ordersService.searchOrderDetailForBuyer(buyerId, orderId);
    }

    /**
     * 결제
     */
    @Transactional
    public void payment(Long userId, Long orderId, Model model) throws IllegalAccessException {
        User user = checkUserById(userId);
        paymentService.payment(user, orderId, model);
    }

    /**
     * 주문 상품 취소 홈
     */
    public void cancelOrderHome(Long buyerId, Long orderItemId, Model model) throws IllegalAccessException {
        User buyer = checkUserById(buyerId);// NoSuchElementException

        OrderItem orderItem = ordersService.checkBuyerOrderItem(buyerId, orderItemId); // IllegalAccessException
        paymentService.cancelOrderHome(buyer.getName(), buyerId, orderItem, model);// IllegalStateException
    }

    /**
     * 교환/환불 신청 로그 생성
     */
    @Transactional
    public void createExchangeRefundLog(Long userId, CreateExchangeRefundLogRequestDto request) throws IllegalAccessException {
        checkUserById(userId);// NoSuchElementException
        OrderItem orderItem = ordersService.checkBuyerOrderItem(userId, request.getOrderItemId());// IllegalAccessException
        if (!orderItem.getOrderItemStatus().equals(OrderItemStatus.DELIVERY_COMPLETE)) {
            throw new IllegalStateException("교환/환불을 신청할수 있는 상태가 아닙니다. 배송완료 후 신청 해주세요.");
        }
        exchangeRefundLogService.createExchangeRefundLog(userId, orderItem.getSellerId(), request);  // IllegalStateException
    }

    /**
     * 대기중인 교환/환불 신청 확인
     */
    public ExchangeRefundLog searchWaitExchangeRefundLog(Long userId, Long orderItemId) {
        checkUserById(userId); // NoSuchElementException
        return exchangeRefundLogService.searchWaitExchangeRefundLog(userId, orderItemId); // NoSuchElementException
    }

    /**
     * 대기중인 교환/환불 신청 취소
     */
    @Transactional
    public void cancelExchangeRefund(Long userId, Long orderItemId, LogStatus logStatus) {
        ExchangeRefundLog exchangeRefundLog = searchWaitExchangeRefundLog(userId, orderItemId); // NoSuchElementException
        exchangeRefundLogService.changeLogStatusExchangeRefundLog(exchangeRefundLog.getId(), logStatus); // NoSuchElementException
    }


    /**
     * 닉네임 검사
     */
    public String changeUserInfoNickname(String newNickname, String nickname) {
        String ignoreNickname = "admin";
        if (nickname.equals(newNickname)) {
            throw new IllegalStateException("현재 사용중인 닉네임입니다.");
        }
        if (newNickname.toUpperCase().matches("(.*)"+ignoreNickname.toUpperCase()+"(.*)")
                || newNickname.toLowerCase().matches("(.*)"+ignoreNickname.toLowerCase()+"(.*)") ) {
            throw new IllegalStateException("사용할 수 없는 닉네임입니다.");
        }
        duplicationCheckService.validateDuplicateNickname(newNickname); // IllegalStateException
        return newNickname;
    }

    /**
     * 주소 검사
     */
    public Address changeUserInfoAddress(String region, String city, String street, String detail, String zipcode) {
        if (isBlank(region) && isBlank(city) && isBlank(street) && isBlank(detail) && isBlank(zipcode)) {
            return null;
        } else if (isNotBlank(region) && isNotBlank(city) && isNotBlank(street) && isNotBlank(detail) && isNotBlank(zipcode)) {
            return new Address(region, city, street, detail, zipcode);
        } else {
            throw new IllegalArgumentException("잘못된 주소형태 입니다.");
        }
    }

    // 사용중인 이메일인지 검사 메소드
    public User checkUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("가입되지 않은 이메일 입니다."));
    }

    // 가입된 회원 검증 메서드
    public User checkUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("가입되지 않은 회원입니다."));
    }

    // user 생성 - status 제외
    public User createUser(JoinRequestDto request) {
        ShoppingCart shoppingCart = shoppingCartService.createShoppingCart();
        User user = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(request.getPassword())
                .pNum(request.getPNum())
                .birth(request.getBirth())
                .address(new Address(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()))
                .build();
        user.setShoppingCart(shoppingCart);
        return user;
    }

    // 회원탈퇴시 정보 저장
    public void saveDeletedUser(User user) {
        DeletedUser deletedUser = DeletedUser.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userNickname(user.getNickname())
                .userEmail(user.getEmail())
                .userPassword(user.getPassword())
                .userPNum(user.getPNum())
                .userBirth(user.getBirth())
                .userAddress(user.getAddress())
                .userStatus(user.getStatus())
                .build();

        deletedUserRepository.save(deletedUser);
    }

    /**
     * DeletedUser 보관 기간 지난 정보 삭제
     * 매일 자정(0시 0분 0초)에 보관 기간이 7일 이상 지난 데이터 삭제
     */
    @Transactional
    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpireDeletedItem() {
        deletedUserRepository.deleteUserByCreatedAtLessThanEqual(LocalDateTime.now().minusDays(7));
    }
}
