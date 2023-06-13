package won.ecommerce.service;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import won.ecommerce.config.PortOneApiConfig;
import won.ecommerce.entity.*;
import won.ecommerce.exception.VerifyIamportException;

import static won.ecommerce.entity.OrderItemStatus.*;
import static won.ecommerce.entity.OrderItemStatus.CANCEL;
import static won.ecommerce.entity.OrderItemStatus.WAITING_FOR_PAYMENT;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PortOneApiConfig portOneApiConfig;
    private final OrdersService ordersService;
    private final ItemService itemService;

    /**
     * 결제
     */
    public void payment(User user, Long orderId, Model model) throws IllegalAccessException {
        OrdersForBuyer order = ordersService.checkBuyerOrder(user.getId(), orderId);
        if (order.getTotalPrice() == 0) {
            throw new IllegalStateException("결제 금액이 0인 주문입니다.");
        }
        if (order.getImpUid() != null) {
            throw new IllegalStateException("이미 처리된 주문입니다.");
        }
        ordersService.checkImpUid(order);
        int totalPrice = order.getTotalPrice();
        String itemsName = order.getOrderItemsName().toString();
        model.addAttribute("orderId", order.getId());
        model.addAttribute("itemsName", itemsName);
        model.addAttribute("buyerName", user.getName());
        model.addAttribute("buyerEmail", user.getEmail());
        model.addAttribute("buyerPNUm", user.getPNum());
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("identificationCode", portOneApiConfig.getIdentificationCode());
        model.addAttribute("CID", portOneApiConfig.getCID());
    }

    /**
     * 결제 검증
     */
    @Transactional
    public void verifyIamPort(IamportResponse<Payment> iamportResponse, int amount, Long orderId, String impUid) {
        OrdersForBuyer orderForBuyer = ordersService.getOrderForBuyer(orderId);
        if (iamportResponse.getResponse().getAmount().intValue() != amount) {
            throw new VerifyIamportException("PortOne 서버 결제 금액이 다릅니다.");
        }
        if (amount != orderForBuyer.getTotalPrice()) {
            throw new VerifyIamportException("주문서와 결제 금액이 다릅니다.");
        }
        ordersService.changeOrderStatusToCompletePayment(orderForBuyer, impUid);
    }

    /**
     * 주문 상품 취소 홈
     */
    public void cancelOrderHome(String buyerName, Long buyerId, OrderItem orderItem, Model model) {
        OrderItemStatus orderItemStatus = orderItem.getOrderItemStatus();
        if (orderItemStatus.equals(WAITING_FOR_PAYMENT)) {
            model.addAttribute("itemName", orderItem.getItemName());
            model.addAttribute("orderItemId", orderItem.getId());
            model.addAttribute("state", "order");
        } else if (orderItemStatus.equals(COMPLETE_PAYMENT)) {
            model.addAttribute("itemName", orderItem.getItemName());
            model.addAttribute("userId", buyerId);
            model.addAttribute("orderItemId", orderItem.getId());
            model.addAttribute("state", "payment");
            model.addAttribute("paymentUid", orderItem.getImpUid());
            model.addAttribute("amount", orderItem.getTotalPrice());
            model.addAttribute("orderItemId", orderItem.getId());
            model.addAttribute("buyerName", buyerName);
        } else {
            ordersService.cancelOrderHome(orderItemStatus);
        }
    }

    // 주문 취소, 재고 변경
    @Transactional
    public void cancelOrderItem(Long orderItemId) {
        OrderItem orderItem = ordersService.checkOrderItem(orderItemId);
        Item item = itemService.checkItem(orderItem.getItemId());
        orderItem.changeStatus(CANCEL);
        orderItem.setComment("구매자에 의한 취소");
        item.increaseStockQuantity(orderItem.getCount());
    }

    public int getOrderItemTotalPrice(Long orderItemId) {
        OrderItem orderItem = ordersService.checkOrderItem(orderItemId);
        return orderItem.getTotalPrice();
    }
}
