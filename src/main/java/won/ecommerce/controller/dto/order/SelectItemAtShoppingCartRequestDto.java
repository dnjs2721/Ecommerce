package won.ecommerce.controller.dto.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SelectItemAtShoppingCartRequestDto {
    List<Long> shoppingCartItemIds = new ArrayList<>();
}
