package won.ecommerce.repository.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.Category;

import java.time.LocalDateTime;

@Data
public class ItemSearchCondition {
    private String itemName;
    private Integer priceGoe;
    private Integer priceLoe;
    private Integer stockQuantityGoe;
    private Integer stockQuantityLoe;
    private String category;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
}
