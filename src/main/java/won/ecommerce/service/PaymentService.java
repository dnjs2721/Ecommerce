package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import won.ecommerce.config.PortOneApiConfig;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.OrderItem;
import won.ecommerce.entity.OrdersForBuyer;
import won.ecommerce.entity.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final SpringTemplateEngine templateEngine;
    private final PortOneApiConfig portOneApiConfig;

    public void payment(User user, OrdersForBuyer order) {
        List<OrderItem> orderItems = order.getOrderItems();
        int totalPrice = order.getTotalPrice();
        String itemsName = order.getOrderItemsName().toString();
        Context context = new Context();
        context.setVariable("itemsName", itemsName);
        context.setVariable("buyerName", user.getName());
        context.setVariable("buyerEmail", user.getEmail());
        context.setVariable("buyerPNUm", user.getPNum());
        context.setVariable("totalPrice", totalPrice);
        context.setVariable("IdentificationCode", portOneApiConfig.getIdentificationCode());
        templateEngine.process("payment", context);
    }
}
