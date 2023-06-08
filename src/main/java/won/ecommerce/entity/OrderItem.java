package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private Long buyerOrderId;
    private Long sellerOrderId;
    private Long buyerId;
    private Long sellerId;
    private Long itemId;
    private String itemName;
    private int price;
    private int count;
    private int totalPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private String comment;

    @Builder
    public OrderItem(Long buyerOrderId, Long sellerOrderId,  Long buyerId, Long sellerId, Long itemId, String itemName, int price, int count) {
        this.buyerOrderId = buyerOrderId;
        this.sellerOrderId = sellerOrderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.totalPrice = price * count;
        this.orderStatus = OrderStatus.WAITING_FOR_PAYMENT;
    }

    public void changeStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
