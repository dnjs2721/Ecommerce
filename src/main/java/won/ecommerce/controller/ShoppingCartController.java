package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.shoppingCartDto.ChangeShoppingCartItemCountRequestDto;
import won.ecommerce.controller.dto.shoppingCartDto.DeleteShoppingCartItemRequestDto;
import won.ecommerce.controller.dto.shoppingCartDto.ShoppingCartItemRequestDto;
import won.ecommerce.repository.dto.search.shoppingCart.SearchShoppingCartDto;
import won.ecommerce.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class ShoppingCartController {

    private final UserService userService;

    /**
     * 장바구니 상품 추가
     */
    @PostMapping("/addShoppingCartItem/{userId}")
    public ResponseEntity<String> addShoppingCartItem(@PathVariable("userId") Long userId, @RequestBody @Valid ShoppingCartItemRequestDto request) {
        try {
            String itemName = userService.addShoppingCartItem(userId, request.getItemId(), request.getItemCount());
            return ResponseEntity.ok().body(itemName + " 이(가) 장바구니에 추가 되었습니다.");
        } catch (NoSuchElementException e) { // 사용자 없음, 상품 없음 예외
            return createResponseEntity(e, NOT_FOUND);
        }
    }

    /**
     * 장바구니 상품 수량 변경
     */
    @PostMapping("/changeShoppingCartItemCount/{userId}")
    public ResponseEntity<String> changeShoppingCartItemCount(@PathVariable("userId") Long userId, @RequestBody @Valid ChangeShoppingCartItemCountRequestDto request) {
        try {
            String itemName = userService.changeShoppingCartItemCount(userId, request.getShoppingCartItemId(), request.getChangCount());
            return ResponseEntity.ok().body(itemName + " 수량 변경 완료.");
        } catch (NoSuchElementException e1) { // 사용자 없음, 장바구니 상품 없음 예외
            return createResponseEntity(e1, NOT_FOUND);
        } catch (IllegalAccessException e2) { // 사용자의 장바구니 상품이 아님
            return createResponseEntity(e2, NOT_ACCEPTABLE);
        }
    }

    /**
     * 장바구니 선택 상품 삭제
     */
    @PostMapping("/deleteShoppingCartItem/{userId}")
    public ResponseEntity<String> deleteShoppingCartItem(@PathVariable("userId") Long userId, @RequestBody DeleteShoppingCartItemRequestDto request) {
        try {
            List<String> itemsName = userService.deleteShoppingCartItem(userId, request.getShoppingCartItemsIds());
            return ResponseEntity.ok().body(itemsName.toString() + " 이 장바구니에서 삭제되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND);
        } catch (IllegalArgumentException e2) {
            return createResponseEntity(e2, CONFLICT);
        }
    }

    /**
     * 장바구니 비우기
     */
    @PostMapping("/deleteAllShoppingCartItem/{userId}")
    public ResponseEntity<String> deleteAllShoppingCartItem(@PathVariable("userId") Long userId) {
        try {
            String userName = userService.deleteAllShoppingCartItem(userId);
            return ResponseEntity.ok().body(userName + " 님의 장바구니가 비워졌습니다.");
        } catch (NoSuchElementException e) { // 장바구니에 등록된 상품 없음
            return createResponseEntity(e, NOT_FOUND);
        }
    }

    /**
     * 장바구니 전체 가격 조회
     */
    @GetMapping("/getShoppingCartTotalPrice/{userId}")
    public ResponseEntity<String> getShoppingCartTotalPrice(@PathVariable("userId") Long userId) {
        try {
            int shoppingCartTotalPrice = userService.getShoppingCartTotalPrice(userId);
            return ResponseEntity.ok().body(shoppingCartTotalPrice + " 원");
        } catch (NoSuchElementException e) { // 사용자 없음
            return createResponseEntity(e, NOT_FOUND);
        }
    }

    /**
     * 장바구니 전체 상품 조회 페이지
     */
    @GetMapping("/getShoppingCartItems/{userId}")
    public ResponseEntity<?> getShoppingCartItems(@PathVariable("userId") Long userId, Pageable pageable) {
        try {
            Page<SearchShoppingCartDto> shoppingCartItems = userService.getShoppingCartItems(userId, pageable);
            return ResponseEntity.ok().body(shoppingCartItems);
        } catch (NoSuchElementException e) { // 사용자 없음
            return createResponseEntity(e, NOT_FOUND);
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
