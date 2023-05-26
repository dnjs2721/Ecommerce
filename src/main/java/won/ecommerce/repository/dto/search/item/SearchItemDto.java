package won.ecommerce.repository.dto.search.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.Category;

import java.time.LocalDateTime;

@Data
public class SearchItemDto {
    private Long itemId;
    private Long sellerId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
    private String name;
    private int price;
    private int stockQuantity;
    private String category;

    @QueryProjection
    public SearchItemDto(Long itemId, Long sellerId, LocalDateTime createdDate, LocalDateTime lastModifiedDate, String name, int price, int stockQuantity, String category) {
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }
}
