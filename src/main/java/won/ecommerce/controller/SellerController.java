package won.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import won.ecommerce.entity.Item;
import won.ecommerce.repository.dto.ItemSearchCondition;
import won.ecommerce.repository.dto.SearchItemDto;
import won.ecommerce.service.SellerService;
import won.ecommerce.service.dto.ItemCreateRequestDto;

import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {
    private final SellerService sellerService;

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

    @GetMapping("/itemSearch/{sellerId}")
    public ResponseEntity<?> searchItems(@PathVariable("sellerId") Long sellerId, ItemSearchCondition condition, Pageable pageable) {
        Page<SearchItemDto> searchItems = sellerService.searchItems(sellerId, condition, pageable);
        return ResponseEntity.ok().body(searchItems);
    }

    public ResponseEntity<String> createResponseEntity(Exception e, HttpStatus httpStatus) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(e.getMessage(), headers, httpStatus);
    }
}
