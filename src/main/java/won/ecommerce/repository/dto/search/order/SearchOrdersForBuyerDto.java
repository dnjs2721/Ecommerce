package won.ecommerce.repository.dto.search.order;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.OrderStatus;

import java.time.LocalDateTime;


@Data
public class SearchOrdersForBuyerDto {
    private Long orderId;
    private Integer orderPrice;
    private OrderStatus status;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;

    @QueryProjection
    public SearchOrdersForBuyerDto(Long orderId, Integer orderPrice, OrderStatus status, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.orderPrice = orderPrice;
        this.status = status;
        this.orderDate = orderDate;
    }
}
