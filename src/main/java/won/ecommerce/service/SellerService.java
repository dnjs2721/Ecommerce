package won.ecommerce.service;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import won.ecommerce.controller.dto.order.ChangeOrderStatusRequestDto;
import won.ecommerce.entity.*;
import won.ecommerce.repository.dto.search.exchangeRefundLog.ExchangeRefundLogSearchCondition;
import won.ecommerce.repository.dto.search.exchangeRefundLog.SearchExchangeRefundLogDto;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.dto.search.order.*;
import won.ecommerce.service.dto.item.ChangeItemInfoRequestDto;
import won.ecommerce.service.dto.item.ItemCreateRequestDto;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerService {
    private final ItemService itemService;
    private final UserService userService;
    private final OrdersService ordersService;
    private final ExchangeRefundLogService exchangeRefundLogService;
    private final PaymentService paymentService;

    /**
     * 판매 상품 등록
     */
    @Transactional
    public Item registerItem(Long sellerId, ItemCreateRequestDto request) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.createItem(seller, request);
    }

    /**
     * 판매자 본인 상품 조회
     */
    public Page<SearchItemDto> searchItems(Long sellerId, ItemSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원, 변경 정보없음 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.searchItems(sellerId, condition, pageable);
    }

    /**
     * 상품 정보 변경
     */
    @Transactional
    public String changeItemInfo(Long sellerId ,ChangeItemInfoRequestDto request) throws IllegalAccessException {
        checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.changeItemInfo(sellerId, request); // NoSuchElementException 존재하지 않는 상품. IllegalAccessException 판매자의 상품이 아닐 떄 예외`
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public List<String> deleteItem(Long sellerId, List<Long> itemIds) throws IllegalAccessException {
        User seller = checkSeller(sellerId);
        return itemService.deleteItem(seller, itemIds);
    }

    /**
     * 주문 조회 판매자
     */
    public Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkSeller(sellerId);
        return ordersService.searchOrdersForSeller(sellerId, condition, pageable);
    }

    /**
     * 주문 상세 조회 판매자
     */
    public List<SearchOrderItemForSellerDto> searchOrderDetailForSeller(Long sellerId, Long orderId) throws IllegalAccessException {
        checkSeller(sellerId); //NoSuchElementException
        return ordersService.searchOrderDetailForSeller(sellerId, orderId);
    }

    /**
     * 판매자 주문 상품 상태 변경
     */
    @Transactional
    public String changeOrderStatus(Long sellerId, ChangeOrderStatusRequestDto request) throws IllegalAccessException {
        checkSeller(sellerId); // NoSuchElementException, IllegalAccessException
        OrderItem orderItem = ordersService.checkSellerOrderItem(sellerId, request.getOrderItemId());// NoSuchElementException
        Item item = itemService.checkItem(orderItem.getItemId()); // NoSuchElementException

        return ordersService.changeOrderStatus(item, orderItem, request); // IllegalStateException
    }

    /**
     * 판매자 교환/환불 신청서 확인
     */
    public Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkSeller(sellerId);
        return exchangeRefundLogService.searchExchangeRefundLog(sellerId, condition, pageable);
    }

    /**
     * 판매자 교환/환불 신청 거부
     */
    @Transactional
    public void cancelERLog(Long erLogId, Boolean okOrCancel) {
        if (Boolean.FALSE.equals(okOrCancel)) {
            exchangeRefundLogService.changeLogStatusExchangeRefundLog(erLogId, LogStatus.CANCEL);
        } else {
            exchangeRefundLogService.changeLogStatusExchangeRefundLog(erLogId, LogStatus.OK);
        }
    }

    public void cancelOrderHome(Long sellerId, Long orderItemId, Model model) throws IllegalAccessException {
        checkSeller(sellerId);// NoSuchElementException
        OrderItem orderItem = ordersService.checkSellerOrderItem(sellerId, orderItemId); // IllegalAccessException
        User user = userService.checkUserById(orderItem.getBuyerId());
        model.addAttribute("reason", "판매자에 의한 취소");
        paymentService.cancelOrderHomeForSeller(user.getName(), user.getId(), orderItem, model);// IllegalStateException
    }

    // 판매자 확인
    public User checkSeller(Long sellerId) throws IllegalAccessException {
        User seller = userService.checkUserById(sellerId); // NoSuchElementException 가입되지 않은 회원 에외
        if (!seller.getStatus().equals(UserStatus.SELLER)) {
            throw new IllegalAccessException("판매자가 아닙니다. 먼저 판매자 신청을 해주세요.");
        }
        return seller;
    }
}
