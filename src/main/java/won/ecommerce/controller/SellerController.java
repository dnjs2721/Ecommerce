package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.controller.dto.itemDto.DeleteItemRequestDto;
import won.ecommerce.controller.dto.order.ChangeOrderStatusRequestDto;
import won.ecommerce.entity.Item;
import won.ecommerce.repository.dto.search.exchangeRefundLog.ExchangeRefundLogSearchCondition;
import won.ecommerce.repository.dto.search.exchangeRefundLog.SearchExchangeRefundLogDto;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.dto.search.order.OrderSearchCondition;
import won.ecommerce.repository.dto.search.order.SearchOrderItemForSellerDto;
import won.ecommerce.repository.dto.search.order.SearchOrdersForSellerDto;
import won.ecommerce.service.SellerService;
import won.ecommerce.service.dto.item.ChangeItemInfoRequestDto;
import won.ecommerce.service.dto.item.ItemCreateRequestDto;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {
    private final SellerService sellerService;

    /**
     * 상품 등록
     */
    @PostMapping("/addItem/{sellerId}")
    public ResponseEntity<String> addItem(@RequestBody @Valid ItemCreateRequestDto request, @PathVariable("sellerId") Long sellerId) {
        try {
            Item item = sellerService.registerItem(sellerId, request);
            return ResponseEntity.ok().body("상품 " + item.getName() + "이 등록 되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND);
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, NOT_ACCEPTABLE);
        } catch (IllegalStateException e3) {
            return createResponseEntity(e3, CONFLICT);
        }
    }

    /**
     * 판매자 판매 상품 조회
     */
    @GetMapping("/itemSearch/{sellerId}")
    public ResponseEntity<?> searchItems(@PathVariable("sellerId") Long sellerId, ItemSearchCondition condition, Pageable pageable) {
        try {
            Page<SearchItemDto> searchItems = sellerService.searchItems(sellerId, condition, pageable);
            return ResponseEntity.ok().body(searchItems);
        } catch (IllegalAccessException e1) {
            return createResponseEntity(e1, NOT_ACCEPTABLE);
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND);
        }
    }

    /**
     * 상품 정보 변경
     */
    @PostMapping("/changeItemInfo/{sellerId}")
    public ResponseEntity<String> changeItemInfo(@PathVariable("sellerId") Long sellerId, @RequestBody @Valid ChangeItemInfoRequestDto request) {
        try {
            Item item = sellerService.changeItemInfo(sellerId, request);
            return ResponseEntity.ok().body(item.getName() + "의 정보가 변경되었습니다.");
        } catch (IllegalAccessException e1) {
            return createResponseEntity(e1, NOT_ACCEPTABLE);
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND);
        }
    }

    /**
     * 상품 삭제
     */
    @PostMapping("/deleteItem/{sellerId}")
    public ResponseEntity<String> deleteItem(@PathVariable("sellerId") Long sellerId, @RequestBody DeleteItemRequestDto request) {
        try {
            List<String> itemsName = sellerService.deleteItem(sellerId, request.getItemIds());
            return ResponseEntity.ok().body(itemsName.toString() + " 이(가) 삭제되었습니다.");
        } catch (IllegalAccessException e1) {
            return createResponseEntity(e1, NOT_ACCEPTABLE);
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND);
        } catch (IllegalArgumentException e3) {
            return createResponseEntity(e3, CONFLICT);
        }
    }

    /**
     * 주문 조회 판매자
     */
    @GetMapping("/searchOrders/{userId}")
    public ResponseEntity<?> searchOrdersForSeller(@PathVariable("userId") Long sellerId, OrderSearchCondition condition, Pageable pageable) {
        try {
            Page<SearchOrdersForSellerDto> content = sellerService.searchOrdersForSeller(sellerId, condition, pageable);
            return ResponseEntity.ok().body(content);
        } catch (IllegalAccessException e1) {
            return createResponseEntity(e1, NOT_ACCEPTABLE);
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND);
        }
    }

    /**
     * 주문 상세 조회 판매자
     */
    @GetMapping("/searchOrderDetail/{userId}/{orderId}")
    public ResponseEntity<?> searchOrderDetail(@PathVariable("userId") Long sellerId, @PathVariable("orderId") Long orderId) {
        try {
            List<SearchOrderItemForSellerDto> items = sellerService.searchOrderDetailForSeller(sellerId, orderId);
            return ResponseEntity.ok().body(items);
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND);
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, NOT_ACCEPTABLE);
        }
    }

    /**
     * 판매자 주문 상품 상태 변경
     */
    @PostMapping("/changeOrderStatus/{userId}")
    public ResponseEntity<String> changeOrderStatus(@PathVariable("userId") Long sellerId, @RequestBody @Valid ChangeOrderStatusRequestDto request) {
        try {
            String itemName = sellerService.changeOrderStatus(sellerId, request);
            return ResponseEntity.ok().body(itemName + " 의 주문상태가 변경되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND);
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, NOT_ACCEPTABLE);
        } catch (IllegalStateException e3) {
            return createResponseEntity(e3, CONFLICT);
        }
    }

    /**
     * 교환환불 신청서 확인
     */
    @GetMapping("/searchExchangeRefundLog/{userId}")
    public ResponseEntity<?> searchExchangeRefundLog(@PathVariable("userId") Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable) {
        try {
            Page<SearchExchangeRefundLogDto> searchLogs = sellerService.searchExchangeRefundLog(sellerId, condition, pageable);
            return ResponseEntity.ok().body(searchLogs);
        } catch (IllegalAccessException e1) {
            return createResponseEntity(e1, NOT_ACCEPTABLE);
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND);
        }
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
