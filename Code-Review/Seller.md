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
  변경 가격, 변경 재고, 변경 카테고리에 값이 있다면 set 절을 추가한다. 이 때 가격이 변경이 될때는 해당 상품에 대한 장바구니 상품들의
  상품당 가격, 주문 상품 전체 가격이 함께 변경된다.
  모든 작업이 끝난 후 변경된 상품의 이름을 반환한다.
  ```