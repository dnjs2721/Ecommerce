package won.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.order.SelectItemAtShoppingCartRequestDto;
import won.ecommerce.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final UserService userService;

    /**
     * 장바구니 전체 상품 주문
     */
    @GetMapping("/allItemAtShoppingCart/{userId}")
    public ResponseEntity<String> orderAllItemAtShoppingCart(@PathVariable("userId") Long userId) {
        try {
            List<String> itemsName = userService.orderAllItemAtShoppingCart(userId);
            return ResponseEntity.ok().body("상품 " + itemsName.toString() + " 이 주문되었습니다. 상태(결제대기)");
        } catch (NoSuchElementException e) {
            return createResponseEntity(e, NOT_FOUND);
        }
    }

    /**
     * 장바구니 상품 선택 주문
     */
    @PostMapping("/selectItemAtShoppingCart/{userId}")
    public ResponseEntity<String> orderSelectItemAtShoppingCart(@PathVariable("userId") Long userId, @RequestBody SelectItemAtShoppingCartRequestDto request) {
        try {
            List<String> itemsName = userService.orderSelectItemAtShoppingCart(userId, request.getShoppingCartItemIds());
            return ResponseEntity.ok().body("상품 " + itemsName.toString() + " 이 주문되었습니다. 상태(결제대기)");
        } catch (NoSuchElementException e) {
            return createResponseEntity(e, NOT_FOUND);
        } catch (IllegalArgumentException e2) {
            return createResponseEntity(e2, CONFLICT);
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
