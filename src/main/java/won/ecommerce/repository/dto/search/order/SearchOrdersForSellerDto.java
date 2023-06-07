package won.ecommerce.repository.dto.search.order;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.OrderStatus;

import java.time.LocalDateTime;

@Data
public class SearchOrdersForSellerDto {
    private Long orderId;
    private String buyerName;
    private String buyerPNum;
    private Address buyerAddress;
    private Integer orderPrice;
    private OrderStatus orderStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;

    @QueryProjection
    public SearchOrdersForSellerDto(Long orderId, String buyerName, String buyerPNum, Address buyerAddress, Integer orderPrice, OrderStatus orderStatus, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.buyerName = buyerName;
        this.buyerPNum = buyerPNum;
        this.buyerAddress = buyerAddress;
        this.orderPrice = orderPrice;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }
}
