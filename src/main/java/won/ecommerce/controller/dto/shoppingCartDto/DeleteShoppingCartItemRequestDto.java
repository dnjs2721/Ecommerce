package won.ecommerce.controller.dto.shoppingCartDto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeleteShoppingCartItemRequestDto {
    List<Long> shoppingCartItemsIds = new ArrayList<>();
}
