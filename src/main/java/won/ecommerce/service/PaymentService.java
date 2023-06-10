package won.ecommerce.service;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import won.ecommerce.config.PortOneApiConfig;
import won.ecommerce.entity.OrderItem;
import won.ecommerce.entity.OrdersForBuyer;
import won.ecommerce.entity.User;
import won.ecommerce.exception.VerifyIamportException;
import won.ecommerce.repository.orders.OrdersForBuyerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PortOneApiConfig portOneApiConfig;
    private final OrdersService ordersService;

    public void payment(User user, OrdersForBuyer order, Model model) {
        List<OrderItem> orderItems = order.getOrderItems();
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

    @Transactional
    public void verifyIamPort(IamportResponse<Payment> iamportResponse, int amount, Long orderId) {
        OrdersForBuyer orderForBuyer = ordersService.getOrderForBuyer(orderId);
        if (iamportResponse.getResponse().getAmount().intValue() != amount) {
            throw new VerifyIamportException("PortOne 서버 결제 금액이 다릅니다.");
        }
        if (amount != orderForBuyer.getTotalPrice()) {
            throw new VerifyIamportException("주문서와 결제 금액이 다릅니다.");
        }
        ordersService.changeOrderStatusToCompletePayment(orderForBuyer);
    }

}
