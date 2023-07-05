## 🙍🏻‍♂️ Seller API

### 상품 등록
- Controller
    ```java
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
    ```

- ItemCreateRequestDto
    ```java
    @Data
    public class ItemCreateRequestDto {
       @NotBlank(message = "상품 이름(필수)")
       String name;
       @NotNull(message = "상품 가격(필수)")
       int price;
       @NotNull(message = "상품 재고(필수)")
       int stockQuantity;
       @NotNull(message = "카테고리 id(필수)")
       Long categoryId;
    }
    ```

- Service
    ```java
    @Transactional
    public Item registerItem(Long sellerId, ItemCreateRequestDto request) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.createItem(seller, request);
    }
  
    public User checkSeller(Long sellerId) throws IllegalAccessException {
        User seller = userService.checkUserById(sellerId); // NoSuchElementException 가입되지 않은 회원 에외
        if (!seller.getStatus().equals(UserStatus.SELLER)) {
            throw new IllegalAccessException("판매자가 아닙니다. 먼저 판매자 신청을 해주세요.");
        }
        return seller;
    }
    ```

- Service - itemService.createItem
    ```java
    public Item createItem(User seller, ItemCreateRequestDto request){
        Item sellItem = Item.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        duplicationItemCheck(seller, request.getName()); // IllegalStateException 중복 아이템 예외

        Category category = categoryService.checkCategory(request.getCategoryId()); // NoSuchElementException 등록되지 않은 카테고리
        sellItem.setCategory(category);
        sellItem.setSeller(seller);

        itemRepository.save(sellItem);

        return sellItem;
    }
  
    public void duplicationItemCheck(User seller, String itemName) {
        Optional<Item> bySellerAndName = itemRepository.findBySellerAndName(seller, itemName);
        if (bySellerAndName.isPresent()) {
            throw new IllegalStateException("이미 판매자가 판매중인 상품입니다.");
        }
   }
   ```

- Service - categoryService.checkCategory
    ```java
    public Category checkCategory(Long id) {
        Optional<Category> findCategory = categoryRepository.findById(id);
        if (findCategory.isEmpty()) {
            throw new NoSuchElementException("카테고리를 다시 확인해 주세요.");
        }
        return findCategory.get();
    }
    ```

- Review
    ```
     Post 통신으로 상품등록에 필요한 정보를 전달받는다.
     전달받은 정보 중 sellerId 를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
     사용자가 존재하고, 판매자일 경우 판매상품을 생성한다.
     판매 상품을 생성할 때 판매자가 동일한 이름을 가진 상품을 판매중이거나 설정한 카테고리가 존재하지 않는 경우 예외를 반환한다.
     판매중인 상품중 동일한 이름을 가진 상품이 없고, 올바른 카테고리를 설정하였다면 상품을 생성하고 생성한 상품의 판매자를 사용자로 설정하고 카테고리를 설정한다.
     모든 작업이 끝난후 상품을 저장하고 상품을 반환한다.
    ```

### 판매자 판매 상품 조회
- Controller
  ```java
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
  ```

- ItemSearchCondition
  ```java
  @Data
  public class ItemSearchCondition {
    private String itemName;
    private Integer priceGoe;
    private Integer priceLoe;
    private Integer stockQuantityGoe;
    private Integer stockQuantityLoe;
    private Long categoryId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
  }
  ```

- Service
  ```java
  public Page<SearchItemDto> searchItems(Long sellerId, ItemSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원, 변경 정보없음 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.searchItems(sellerId, condition, pageable);
  }
  ```
- Service - itemService.searchItems
  ```java
  public Page<SearchItemDto> searchItems(Long sellerId, ItemSearchCondition condition, Pageable pageable){
        return itemRepository.searchItemPage(sellerId, condition, pageable);
  }
  ```

- SearchItemDto
  ```java
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
  ```

- ItemRepositoryCustom
  ```java
  public interface ItemRepositoryCustom {
    Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable);
  }
  ```

- ItemRepositoryCustomImpl
  ```java
  @Override
  public Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable) {
      List<SearchItemDto> content = queryFactory
              .select(new QSearchItemDto(
                      item.id,
                      item.seller.id,
                      item.createdDate,
                      item.lastModifiedDate,
                      item.name,
                      item.price,
                      item.stockQuantity,
                      category.name
              ))
              .from(item)
              .leftJoin(item.category, category)
              .where(item.seller.id.eq(sellerId),
                      itemNameEq(condition.getItemName()),
                      priceGoe(condition.getPriceGoe()),
                      priceLoe(condition.getPriceLoe()),
                      stockQuantityGoe(condition.getStockQuantityGoe()),
                      stockQuantityLoe(condition.getStockQuantityLoe()),
                      categoryEQ(condition.getCategoryId()),
                      createTimeGoe(condition.getTimeGoe()),
                      createTimeLoe(condition.getTimeLoe()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    JPAQuery<Long> countQuery = queryFactory
            .select(item.count())
            .from(item)
            .leftJoin(item.category, category)
            .where(item.seller.id.eq(sellerId),
                    itemNameEq(condition.getItemName()),
                    priceGoe(condition.getPriceGoe()),
                    priceLoe(condition.getPriceLoe()),
                    stockQuantityGoe(condition.getStockQuantityGoe()),
                    stockQuantityLoe(condition.getStockQuantityLoe()),
                    categoryEQ(condition.getCategoryId()),
                    createTimeGoe(condition.getTimeGoe()),
                    createTimeLoe(condition.getTimeLoe()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
  
  private BooleanExpression itemNameEq(String itemName) {
        return hasText(itemName) ? item.name.like("%"+itemName+"%") : null;
  }
  
  private BooleanExpression priceGoe(Integer priceGoe) {
      return priceGoe != null ? item.price.goe(priceGoe) : null;
  }
  
  private BooleanExpression priceLoe(Integer priceLoe) {
        return priceLoe != null ? item.price.loe(priceLoe) : null;
  }

  private BooleanExpression stockQuantityGoe(Integer stockQuantityGoe) {
      return stockQuantityGoe != null ? item.stockQuantity.goe(stockQuantityGoe) : null;
  }
  
  private BooleanExpression stockQuantityLoe(Integer stockQuantityLoe) {
        return stockQuantityLoe != null ? item.stockQuantity.loe(stockQuantityLoe) : null;
  }
  
  private BooleanExpression categoryEQ(Long categoryId) {
      return categoryId != null ? item.category.id.eq(categoryId) : null;
  }
  
  private BooleanExpression createTimeGoe(LocalDateTime timeGoe) {
      return timeGoe != null ? item.createdDate.goe(timeGoe) : null;
  }
  
  private BooleanExpression createTimeLoe(LocalDateTime timeLoe) {
        return timeLoe != null ? item.createdDate.loe(timeLoe) : null;
  }
  ```

- Review
  ```
  GET 통신을 통해 /itemSearch/{sellerId}?{Params} 형태로 판매자 고유번호(sellerId), 검색조건(ItemSearchCondition), 페이지 정보(Pageable) 를 전달받는다.
  전달받은 판매자 고유번호를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
  사용자가 존재하고, 판매자일 경우 판매자의 판매 상품들을 조회한다.
  검색 결과는 SearchItemDto 정보로 이루어진 페이지이다.
  아래는 Params 에 들어갈 수 있는 값의 종류이다.
  Params
    - itemName : 상품 이름
    - priceGoe : 상품 가격(이상)
    - priceLoe : 상품 가격(이하)
    - stockQuantityGoe : 상품 재고(이상)
    - stockQuantityLoe : 상품 재고(이하)
    - cateGroyId : 카테고리 고유번호
    - timeGoe : 상품 등록 시간(이상)
    - timeLoe : 상품 등록 시간(이하)
    - page : 페이지 번호
    - size : 한페이지에 표시할 정보의 수
  ```
  ```
  ItemRepositoryCustomImpl
  Params 의 값을 동적으로 처리하기 위하여 querydsl 을 통하여 쿼리를 작성하였다.

  검색조건을 통해 검색된 정보는 SearchItemDto 로 변환된다.
  검색은 상품의 판매자 고유번호와 전달받은 판매자 고유번호가 일치하는 상품들을 전체 검색한다. 상품이름, 가격범위, 재고범위, 카테고리, 상품 등록 시간 범위 설정이 가능하다.
  각각 itemNameEq, [priceGoe, priceLoe], [stockQuantityGoe, stockQuantityLoe], categoryEQ, [timeGoe, timeLoe] 메서드로 구현하였다.
  위 메서드들은 해당하는 Params 값이 있다면 쿼리의 where 절에 조건을 추가하고 없다면 null 을 반환하여 where 절에 추가하지 않는다.
  상품이름은 like 문을 사용하였으며, Goe 와 Loe 둘 다 사용하면 Between 효과를 볼 수 있다.

  검색조건을 통해 필터링된 정보들은 페이지 형태를 가지며 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.

  위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
  - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
  - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
  ```

### 상품 정보 변경
- Controller
  ```java
  @PostMapping("/changeItemInfo/{sellerId}")
  public ResponseEntity<String> changeItemInfo(@PathVariable("sellerId") Long sellerId, @RequestBody @Valid ChangeItemInfoRequestDto request) {
      try {
          String itemName = sellerService.changeItemInfo(sellerId, request);
          return ResponseEntity.ok().body(itemName + "의 정보가 변경되었습니다.");
      } catch (IllegalAccessException e1) {
          return createResponseEntity(e1, NOT_ACCEPTABLE);
      } catch (NoSuchElementException e2) {
          return createResponseEntity(e2, NOT_FOUND);
      }
  }
  ```

- ChangeItemInfoRequestDto
  ```java
  @Data
  public class ChangeItemInfoRequestDto {
     @NotNull(message = "상품 ID(필수)")
     Long itemId;
     Integer changePrice;
     Integer changeStockQuantity;
     Long changeCategoryId;
  }
  ```

- Service
  ```java
  @Transactional
  public String changeItemInfo(Long sellerId ,ChangeItemInfoRequestDto request) throws IllegalAccessException {
      checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원 에외, IllegalAccessException 판매자 아닐 때 예외
      return itemService.changeItemInfo(sellerId, request); // NoSuchElementException 존재하지 않는 상품. IllegalAccessException 판매자의 상품이 아닐 떄 예외`
  }
  ```

- Service - itemService.changeItemInfo
  ```java
  public String changeItemInfo(Long sellerId, ChangeItemInfoRequestDto request) throws IllegalAccessException {
       Item item = checkItem(request.getItemId());
       if (!item.getSeller().getId().equals(sellerId)) {
           throw new IllegalAccessException("판매자의 상품이 아닙니다.");
       }
       if (request.getChangePrice() == null && request.getChangeStockQuantity() == null && request.getChangeCategoryId() == null) {
           throw new NoSuchElementException("변경할 정보가 없습니다.");
       }
       if (request.getChangeCategoryId() != null){
           Category category = categoryService.checkCategory(request.getChangeCategoryId()); // NoSuchElementException 없는 카테고리 예외
       }
       itemRepository.changeItemInfo(request);
       return item.getName();
  }
  
  public Item checkItem(Long itemId) {
        Optional<Item> findItem = itemRepository.findById(itemId);
        if (findItem.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 상품입니다.");
        }
        return findItem.get();
  }
  ```

- ItemRepositoryCustom
  ```java
  public interface ItemRepositoryCustom {
    void changeItemInfo(ChangeItemInfoRequestDto request);
  }
  ```

- ItemRepositoryCustomImpl
  ```java
  @Override
  public void changeItemInfo(ChangeItemInfoRequestDto request) {
      JPAUpdateClause itemInfo = queryFactory
              .update(item)
              .where(item.id.eq(request.getItemId()));

      if (request.getChangePrice() != null) {
          itemInfo.set(item.price, request.getChangePrice());
          queryFactory
                  .update(shoppingCartItem)
                  .set(shoppingCartItem.itemPrice, request.getChangePrice())
                  .set(shoppingCartItem.totalItemPrice, shoppingCartItem.itemCount.multiply(request.getChangePrice()))
                  .where(shoppingCartItem.item.id.eq(request.getItemId()))
                  .execute();
      }
      if (request.getChangeStockQuantity() != null) {
          itemInfo.set(item.stockQuantity, request.getChangeStockQuantity());
      }
      if (request.getChangeCategoryId() != null) {
          itemInfo.set(item.category.id, request.getChangeCategoryId());
      }

      itemInfo.execute();
  }
  ```

- Review
  ```
  Post 통신으로 상품정보 수정에 필요한 정보를 전달받는다.
  전달받은 정보 중 sellerId 를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
  사용자가 존재하고, 판매자일 경우 상품정보를 수정한다. 상품 정보는 가격, 재고, 카테고리 변경이 가능하다.
  상품정보를 수정할 때 전달받은 상품 고유번호를 통해 상품의 존재를 확인하고 사용자가 판매중인 상품인지 확인한다. 상품이 존재하지 않거나 판매자의 상품이 아닌경우 예외를 반환한다.
  상품이 존재하고 판매자의 상품인 경우 전달받은 수정 내역을 검증한다. 만약 가격, 재고, 카테고리의 변경이 없다면 예외를 반환한다.
  카테고리 변경의 경우 먼저 변경할 카테고리가 존재하는지 확인 하며 존재하지 않을 경우 예외를 반환한다.
  ```
  ```
  ItemRepositoryCustomImpl
  전달 받은 상품 고유번호를 가진 상품의 정보를 수정한다.
  변경 가격, 변경 재고, 변경 카테고리에 값이 있다면 set 절을 추가한다. 
  이 때 가격이 변경이 될때는 해당 상품에 대한 장바구니 상품들의 상품당 가격, 주문 상품 전체 가격이 함께 변경된다.
  모든 작업이 끝난 후 변경된 상품의 이름을 반환한다.
  ```
  
### 상품 삭제
- Controller
  ```java
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
  ```

- DeleteItemRequestDto
  ```java
  @Data
  public class DeleteItemRequestDto {
    List<Long> itemIds = new ArrayList<>();
  }
  ```

- Service
  ```java
  @Transactional
  public List<String> deleteItem(Long sellerId, List<Long> itemIds) throws IllegalAccessException {
      User seller = checkSeller(sellerId);
      return itemService.deleteItem(seller, itemIds);
  }
  ```

- Service - itemService.deleteItem
  ```java
  public List<String> deleteItem(User seller, List<Long> itemIds){
        List<Item> findItems = itemRepository.findItemBySellerIdAndItemIds(seller.getId(), itemIds);
        if (itemIds.isEmpty() || (findItems.size() != itemIds.size())) {
            throw new IllegalArgumentException("잘못된 상품 정보입니다.");
        }
        List<String> itemsName = new ArrayList<>();
        List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();
        for (Item item : findItems) {
            shoppingCartItems.addAll(item.getShoppingCartItems());
            itemsName.add(item.getName());
        }
        if (!shoppingCartItems.isEmpty()) {
            shoppingCartService.deleteShoppingCartItemByList(shoppingCartItems);
        }
        saveDeletedItem(seller, findItems);
        itemRepository.deleteAllByIdInBatch(itemIds);
        return itemsName;
  }
  ```

- Service - shoppingCartService.deleteShoppingCartItemByList
  ```java
  public void deleteShoppingCartItemByList(List<ShoppingCartItem> shoppingCartItems) {
        List<Long> shoppingCartItemIds = new ArrayList<>();
        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            shoppingCartItemIds.add(shoppingCartItem.getId());
        }
        shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);
  }
  ```

- ItemRepositoryCustom
  ```java
  public interface ItemRepositoryCustom {
    List<Item> findItemBySellerIdAndItemIds(Long sellerId, List<Long> itemIds);
    List<ShoppingCartItem> getShoppingCartItem(List<Long> itemIds);
  }
  ```

- ItemRepositoryCustomImpl
  ```java
  @Override
  public List<Item> findItemBySellerIdAndItemIds(Long sellerId, List<Long> itemIds) {
      return queryFactory
              .select(item)
              .from(item)
              .where(item.seller.id.eq(sellerId),
                      item.id.in(itemIds))
              .fetch();
  }
  
  @Override
  public List<ShoppingCartItem> getShoppingCartItem(List<Long> itemIds) {
      return queryFactory
              .select(shoppingCartItem)
              .from(shoppingCartItem)
              .where(shoppingCartItem.item.id.in(itemIds))
              .fetch();
  }
  ```

- Review
  ```
  Post 통신을 통해 상품 삭제에 필요한 정보를 받아온다. - 판매자 고유번호, 상품 고유번호(List)
  전달받은 정보 중 sellerId 를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
    
  상품 고유번호가 없거나, 전달받은 상품 고유번호와 판매자 고유번호가 일치하는 상품이 전달받은 상품 고유번호의 갯수와 맞지 않는 경우 예외를 반환한다.
  이를 통해 전달 받은 상품이 판매자의 상품인지 검사한다.
  판매자의 상품인지 검사가 완료되었으면 결과 반환을 위해 상품의 이름을 List 형태로 생성한다.
    
  삭제될 상품이 다른 사용자의 장바구니에 존재하면 안되기에 삭제될 상품에 대한 장바구니 상품들을 삭제한다.
    
  삭제되는 상품의 정보를 일정기간 보관하기 위해 저장하고 상품을 삭제한다.
  정상적으로 끝났을 경우 Controller 에 삭제된 상품의 이름을 전달한다.
  ```

### 주문 조회
- Controller
  ```java
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
  ```

- OrderSearchCondition
  ```java
  @Data
  public class OrderSearchCondition {
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
  }
  ```

- Service
  ```java
  public Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkSeller(sellerId);
        return ordersService.searchOrdersForSeller(sellerId, condition, pageable);
  }
  ```

- Service - ordersService.searchOrdersForSeller
  ```java
  public Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable) {
        return sellerRepository.searchOrdersForSeller(sellerId, condition, pageable);
  }
  ```

- SearchOrdersForSellerDto
  ```java
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public class SearchOrderItemForSellerDto {
      Long orderItemId;
      Long itemId;
      String itemName;
      int price;
      int count;
      int totalPrice;
      OrderItemStatus orderItemStatus;
      String cancelReason;
  
      @QueryProjection
      public SearchOrderItemForSellerDto(Long orderItemId, Long itemId, String itemName, int price, int count, int totalPrice, OrderItemStatus orderItemStatus, String cancelReason) {
          this.orderItemId = orderItemId;
          this.itemId = itemId;
          this.itemName = itemName;
          this.price = price;
          this.count = count;
          this.totalPrice = totalPrice;
          this.orderItemStatus = orderItemStatus;
          this.cancelReason = cancelReason;
      }
  }
  ```

- OrderSearchRepository
  ```java
  public interface OrderSearchRepository {
    Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable);
  }
  ```

- OrderSearchRepositoryImpl
  ```java
  @Override
  public Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable) {
      List<SearchOrdersForSellerDto> content = queryFactory
              .select(new QSearchOrdersForSellerDto(
                      ordersForSeller.id,
                      ordersForSeller.buyerName,
                      ordersForSeller.buyerPNum,
                      ordersForSeller.buyerAddress,
                      ExpressionUtils.as(
                              JPAExpressions
                                      .select(orderItem.totalPrice.sum())
                                      .from(orderItem)
                                      .where(orderItem.sellerOrderId.eq(ordersForSeller.id),
                                              orderItem.orderItemStatus.ne(OrderItemStatus.CANCEL)), "orderPrice"),
                      ordersForSeller.createdDate))
              .from(ordersForSeller)
              .where(ordersForSeller.seller.id.eq(sellerId),
                      orderTimeGoeForSeller(condition.getTimeGoe()),
                      orderTimeLoeForSeller(condition.getTimeLoe()))
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();

      JPAQuery<Long> countQuery = queryFactory
              .select(ordersForSeller.count())
              .from(ordersForSeller)
              .where(ordersForSeller.seller.id.eq(sellerId),
                      orderTimeGoeForSeller(condition.getTimeGoe()),
                      orderTimeLoeForSeller(condition.getTimeLoe()));

      return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
  
  private BooleanExpression orderTimeGoeForSeller(LocalDateTime timeGoe) {
        return timeGoe != null ? ordersForSeller.createdDate.goe(timeGoe) : null;
  }
  private BooleanExpression orderTimeLoeForSeller(LocalDateTime timeLoe) {
      return timeLoe != null ? ordersForSeller.createdDate.loe(timeLoe) : null;
  }
  ```

- Review
  ```
  GET 통신을 통해 /searchOrders/{userId}?{Params} 형태로 사용자 고윺번호(userId), 검색조건(OrderSearchCondition), 페이지 정보(Pageable) 를 전달받는다.
  전달받은 사용자의 고유번호를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
  검색 결과는 SearchOrdersForSellerDto 정보로 이루어진 페이지이다.
  아래는 Params 에 들어갈 수 있는 값의 종류이다.
  Params
    - timeGoe : 주문 시간(이상)
    - timeLoe : 주문 시간(이하)
    - page : 페이지 번호
    - size : 한페이지에 표시할 정보의 수
  ```
  ```
  OrderSearchRepositoryImpl
  Params 의 값을 동적으로 처리하기 위하여 querydsl 을 통하여 쿼리를 작성하였다.
  검색조건을 통해 검색된 정보는 SearchOrdersForSellerDto 로 변환된다.
  전달받은 사용자의 고유번호를 통해 사용자의 주문들을 페이지 형태로 반환한다. 이때 주문 시간 범위 설정이 가능하다.
  주문 시간 범위는 orderTimeGoeForSeller, orderTimeLoeForSeller 메서드로 구현하였으며
  해당하는 Params 값이 있다면 쿼리의 where 절에 조건을 추가하고 없다면 null 을 반환하여 where 절에 추가하지 않는다.
  Goe 와 Loe 둘 다 사용하면 Between 효과를 볼 수 있다.
  
  검색결과에는 주문의 고유번호, 구매자 이름, 구매자 전화번호, 구매자 주소, 해당 주문의 전체 가격, 주문 시간을 가지고 있다.
  해당 주문의 전체 가격은 서브쿼리로 구현하였다. 해당 주문의 고유번호를 가지고 있는 주문 상품들 중 주문 상품 상태가 CANCEL인 것들을 제외하고 전체가격을 계산한다.
  
  검색조건을 통해 필터링 된 정보들은 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.
  
  위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
  - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
  - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
  ```

### 주문 상세 조회
- Controller
  ```java
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
  ```

- Service
  ```java
  public List<SearchOrderItemForSellerDto> searchOrderDetailForSeller(Long sellerId, Long orderId) throws IllegalAccessException {
        User seller = checkSeller(sellerId); //NoSuchElementException
        return ordersService.searchOrderDetailForSeller(sellerId, orderId);
  }
  ```

- Service - ordersService.searchOrderDetailForSeller
  ```java
  public List<SearchOrderItemForSellerDto> searchOrderDetailForSeller(Long sellerId, Long orderId) throws IllegalAccessException {
        checkSellerOrder(sellerId, orderId);
        return sellerRepository.searchOrderItemsForSeller(orderId);
  }
  
  public void checkSellerOrder(Long sellerId, Long orderId) throws IllegalAccessException {
        OrdersForSeller ordersForSeller = getOrdersForSeller(orderId);
        if (!ordersForSeller.getSeller().getId().equals(sellerId)) {
            throw new IllegalAccessException("사용자의 주문이 아닙니다.");
        }
  }
  ```

- SearchOrderItemForSellerDto
  ```java
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public class SearchOrderItemForSellerDto {
      Long orderItemId;
      Long itemId;
      String itemName;
      int price;
      int count;
      int totalPrice;
      OrderItemStatus orderItemStatus;
      String cancelReason;
  
      @QueryProjection
      public SearchOrderItemForSellerDto(Long orderItemId, Long itemId, String itemName, int price, int count, int totalPrice, OrderItemStatus orderItemStatus, String cancelReason) {
          this.orderItemId = orderItemId;
          this.itemId = itemId;
          this.itemName = itemName;
          this.price = price;
          this.count = count;
          this.totalPrice = totalPrice;
          this.orderItemStatus = orderItemStatus;
          this.cancelReason = cancelReason;
      }
  }
  ```

- OrderSearchRepository
  ```java
  public interface OrderSearchRepository {
    List<SearchOrderItemForSellerDto> searchOrderItemsForSeller(Long orderId);
  }
  ```

- OrderSearchRepositoryImpl
  ```java
  @Override
  public List<SearchOrderItemForSellerDto> searchOrderItemsForSeller(Long orderId) {
      return queryFactory
              .select(new QSearchOrderItemForSellerDto(
                      orderItem.id,
                      orderItem.itemId,
                      orderItem.itemName,
                      orderItem.price,
                      orderItem.count,
                      orderItem.totalPrice,
                      orderItem.orderItemStatus,
                      orderItem.comment
                      ))
              .from(orderItem)
              .where(orderItem.sellerOrderId.eq(orderId))
              .fetch();
  }
  ```

- Review
  ```
  GET 통신을 통해 /searchOrderDetail/{userId}/{orderId} 형태로 사용자 고유번호(userId), 주문 고유번호(orderId) 를 전달 받는다.
  전달받은 사용자 고유번호를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
  사용자가 존재하고, 판매자라면 주문 고유번호를 통해 주문의 존재와 주문이 사용자의 것인지 확인한다.
  만약 주문이 없거나 사용자의 주문이 아닌 경우 예외를 반환한다. 주문이 존재하고 사용자의 주문이 맞다면 해당 주문에 대해 상세 정보를 반환한다.
  ```
  ```
  OrderSearchRepositoryImpl
  전달 받은 주문의 고유 번호를 통해 주문의 상세 정보를 반환한다. 주문의 상세 정보는 SearchOrderItemForSellerDto 의 형태로 반환된다.
  주문의 상세 정보는 주문상품의 고유번호, 상품의 고유번호, 상품의 이름, 상품 가격, 주문 수량, 주문 상품의 총 가격, 주문 상품의 상태, 코멘트 를 담고있다.
  이 때 comment 는 상품을 취소 하는 이유이며 취소 상태가 아닐 경우 표시되지 않는다.
  ```

### 주문 상품 상태 변경
- Controller
  ```java
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
  ```

- ChangeOrderStatusRequestDto
  ```java
  @Data
  public class ChangeOrderStatusRequestDto {
    @NotNull(message = "주문상품 ID")
    Long orderItemId;
    OrderItemStatus orderItemStatus;
    String comment;
  }
  ```

- Service
  ```java
  @Transactional
  public String changeOrderStatus(Long sellerId, ChangeOrderStatusRequestDto request) throws IllegalAccessException {
      checkSeller(sellerId); // NoSuchElementException, IllegalAccessException
      OrderItem orderItem = ordersService.checkSellerOrderItem(sellerId, request.getOrderItemId());// NoSuchElementException
      Item item = itemService.checkItem(orderItem.getItemId()); // NoSuchElementException

      return ordersService.changeOrderStatus(item, orderItem, request); // IllegalStateException
  }
  ```

- Service - ordersService.changeOrderStatus
  ```java
  public String changeOrderStatus(Item item, OrderItem orderItem, ChangeOrderStatusRequestDto request){
        OrderItemStatus orderItemStatus = request.getOrderItemStatus();
        if (orderItemStatus.equals(CANCEL)) {
            orderItem.setComment("판매자에 의한 취소 " + request.getComment());
            item.increaseStockQuantity(orderItem.getCount());
        } else if (orderItemStatus.equals(WAITING_FOR_PAYMENT)) {
            throw new IllegalStateException("해당 단계로 변경할 수 없습니다.");
        }
        orderItem.changeStatus(orderItemStatus);

        return orderItem.getItemName();
  }
  ```

- Review
  ```
  Post 통신을 통해 주문 상품 상태 변경에 필요한 정보를 전달 받는다.
  전달받은 사용자 고유번호를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
  사용자가 존재하고, 판매자라면 주문 상품 고유번호와 사용자 고유번호를 통해 주문 상품 존재와 주문 상품이 사용자의 것인지 확인한다.
  만약 주문 상품이 존재하지 않거나 사용자의 주문 상품이 아니라면 예외를 반환한다.
  주문 상품 확인을 한 뒤 주문 상품 정보 중 상품 고유번호를 통해 상품 존재를 확인한다. 만약 상품이 존재하지 않다면 예외를 반환한다.
  상품존재까지 확인이 끝났다면 주문 상품 상태를 변경한다.
  주문 상품 상태를 취소로 변경할 때는 이유를 설정하며, 결제대기 상태로는 변경이 불가능하다.
  ```

### 교환/환불 신청서 확인
- Controller
  ```java
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
  ```

- ExchangeRefundLogSearchCondition
  ```java
  @Data
  public class ExchangeRefundLogSearchCondition {
    private ExchangeRefundStatus status;
    private LogStatus logStatus;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
  }
  ```

- Service
  ```java
  public Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkSeller(sellerId);
        return exchangeRefundLogService.searchExchangeRefundLog(sellerId, condition, pageable);
  }
  ```

- Service - exchangeRefundLogService.searchExchangeRefundLog
  ```java
  public Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable) {
        return exchangeRefundRepository.searchExchangeRefundLog(sellerId, condition, pageable);
  }
  ```

- SearchExchangeRefundLogDto
  ```java
  @Data
  public class SearchExchangeRefundLogDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    private Long logId;
    private Long userId;
    private Long orderItemId;
    private String reason;
    private ExchangeRefundStatus status;
    private LogStatus logStatus;
    private LocalDateTime processingTime;

    @QueryProjection
    public SearchExchangeRefundLogDto(LocalDateTime createdDate, Long logId, Long userId, Long orderItemId, String reason, ExchangeRefundStatus status, LogStatus logStatus, LocalDateTime processingTime) {
        this.createdDate = createdDate;
        this.logId = logId;
        this.userId = userId;
        this.orderItemId = orderItemId;
        this.reason = reason;
        this.status = status;
        this.logStatus = logStatus;
        this.processingTime = processingTime;
    }
  }
  ```

- ExchangeRefundRepositoryCustom
  ```java
  public interface ExchangeRefundRepositoryCustom {
    Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable);
  }
  ```

- ExchangeRefundRepositoryCustomImpl
  ```java
  @Override
  public Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable) {
      List<SearchExchangeRefundLogDto> content = queryFactory
              .select(new QSearchExchangeRefundLogDto(
                      exchangeRefundLog.createdDate,
                      exchangeRefundLog.id,
                      exchangeRefundLog.userId,
                      exchangeRefundLog.orderItemId,
                      exchangeRefundLog.reason,
                      exchangeRefundLog.status,
                      exchangeRefundLog.logStatus,
                      exchangeRefundLog.processingTime
              ))
              .from(exchangeRefundLog)
              .where(exchangeRefundLog.sellerId.eq(sellerId),
                      createdDateGoe(condition.getTimeGoe()),
                      createdDateLoe(condition.getTimeLoe()),
                      statusEq(condition.getStatus()),
                      logStatusEq(condition.getLogStatus()))
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();

      JPAQuery<Long> countQuery = queryFactory
              .select(exchangeRefundLog.count())
              .from(exchangeRefundLog)
              .where(exchangeRefundLog.sellerId.eq(sellerId),
                      createdDateGoe(condition.getTimeGoe()),
                      createdDateLoe(condition.getTimeLoe()),
                      statusEq(condition.getStatus()),
                      logStatusEq(condition.getLogStatus()));

      return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private BooleanExpression createdDateGoe(LocalDateTime timeGoe) {
      return timeGoe != null ? exchangeRefundLog.createdDate.goe(timeGoe) : null;
  }
  private BooleanExpression createdDateLoe(LocalDateTime timeLoe) {
      return timeLoe != null ? exchangeRefundLog.createdDate.loe(timeLoe) : null;
  }
  private BooleanExpression statusEq(ExchangeRefundStatus status) {
      return status != null ? exchangeRefundLog.status.eq(status) : null;
  }
  private BooleanExpression logStatusEq(LogStatus logStatus) {
      return logStatus != null ? exchangeRefundLog.logStatus.eq(logStatus) : null;
  }
  ```

- Review
  ```
  GET 통신을 통해 /searchExchangeRefundLog/{userId}?{Params} 형태로 사용자 고윺번호(userId), 검색조건(ExchangeRefundLogSearchCondition), 페이지 정보(Pageable) 를 전달받는다.
  전달받은 사용자의 고유번호를 통해 사용자의 존재, 판매자 권한을 확인한다. 만약 사용자가 존재하지 않거나 판매자가 아닌경우 예외를 반환한다.
  검색 결과는 SearchExchangeRefundLogDto 정보로 이루어진 페이지이다.
  아래는 Params 에 들어갈 수 있는 값의 종류이다.
  Params
    - status : 신청 종류
    - logStatus : 신청서 상태
    - timeGoe : 신청 시간(이상)
    - timeLoe : 신청 시간(이하)
    - page : 페이지 번호
    - size : 한페이지에 표시할 정보의 수
  ```
  ```
  ExchangeRefundRepositoryCustomImpl
  Params 의 값을 동적으로 처리하기 위하여 querydsl 을 통하여 쿼리를 작성하였다.
  검색조건을 통해 검색된 정보는 SearchExchangeRefundLogDto 로 변환된다.
  전달받은 사용자의 고유번호를 통해 사용자의 교환/환불 신청들을 페이지 형태로 반환한다. 
  이때 교환/환불 종류, 신청 상태, 신청 시간 범위 설정이 가능하다.
  각각 statusEq, logStatusEq, createdDateGoe, createdDateLoe 메서드로 구현하였으며
  해당하는 Params 값이 있다면 쿼리의 where 절에 조건을 추가하고 없다면 null 을 반환하여 where 절에 추가하지 않는다.
  Goe 와 Loe 둘 다 사용하면 Between 효과를 볼 수 있다.
  
  검색결과에는 신청시간, 신청 고유번호, 신청자 고유번호, 신청 주문 상품 고유번호, 이유, 종류, 상태, 처리시간을 가지고 있다.
  
  검색조건을 통해 필터링 된 정보들은 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.
  
  위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
  - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
  - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
  ```