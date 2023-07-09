## 🛒 ShoppingCart

### ShoppingCart Entity
   ```java
   @Entity
   @Getter
   @NoArgsConstructor
   public class ShoppingCart extends BaseTimeEntity{
       @Id
       @GeneratedValue
       private Long id;
   
       @OneToOne(mappedBy = "shoppingCart")
       private User user;
   
       @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.REMOVE)
       private List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();
   
       public int getTotalPrice() {
           List<ShoppingCartItem> findItems = this.getShoppingCartItems();
           int totalPrice = 0;
           for (ShoppingCartItem shoppingCartItem : findItems) {
               totalPrice += shoppingCartItem.getTotalItemPrice();
           }
           return totalPrice;
       }
   }
   ```

### ShoppingCartItem Entity
   ```java
   @Entity
   @Getter
   @NoArgsConstructor(access = AccessLevel.PROTECTED)
   public class ShoppingCartItem extends BaseTimeEntity {
       @Id
       @GeneratedValue
       private Long id;
   
       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "shoppingCartId")
       private ShoppingCart shoppingCart;
   
       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "itemId")
       private Item item;
       private int itemCount;
       private int itemPrice;
       private int totalItemPrice;
   
       public ShoppingCartItem(ShoppingCart shoppingCart, Item item, int itemCount) {
           this.shoppingCart = shoppingCart;
           this.item = item;
           this.itemCount = itemCount;
           this.itemPrice = item.getPrice();
           this.totalItemPrice = item.getPrice() * itemCount;
       }
   
       public void changeItemCount(int itemCount) {
           this.itemCount = itemCount;
           this.totalItemPrice = this.itemPrice * itemCount;
       }
   }
   ```

### 장바구니 상품 추가
- Controller
    ```java
    @PostMapping("/addShoppingCartItem/{userId}")
    public ResponseEntity<String> addShoppingCartItem(@PathVariable("userId") Long userId, @RequestBody @Valid ShoppingCartItemRequestDto request) {
        try {
            String itemName = userService.addShoppingCartItem(userId, request.getItemId(), request.getItemCount());
            return ResponseEntity.ok().body(itemName + " 이(가) 장바구니에 추가 되었습니다.");
        } catch (NoSuchElementException e) { // 사용자 없음, 상품 없음 예외
            return createResponseEntity(e, NOT_FOUND);
        }
    }
    ```

- ShoppingCartItemRequestDto
  ```java
  @Data
  public class ShoppingCartItemRequestDto {
      @NotNull(message = "상품 Id(필수)")
      Long itemId;
  
      int itemCount = 1;
  }
  ```

- Service
  ```java
  @Transactional
  public String addShoppingCartItem(Long userId, Long itemId, int itemCount) {
      User user = checkUserById(userId); // NoSuchElementException
      Item item = itemService.checkItem(itemId); // NoSuchElementException 상품 없음
      shoppingCartService.addItem(user.getShoppingCart(), item, itemCount);
      return item.getName();
  }
  ```

- Service - shoppingCartService.addItem
  ```java
  public void addItem(ShoppingCart shoppingCart, Item item, int itemCount) {
        Optional<ShoppingCartItem> optionalShoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndItemId(shoppingCart.getId(), item.getId());
        if (optionalShoppingCartItem.isEmpty()) {
            ShoppingCartItem shoppingCartItem = new ShoppingCartItem(shoppingCart, item, itemCount);
            shoppingCartItemRepository.save(shoppingCartItem);
        } else {
            ShoppingCartItem shoppingCartItem = optionalShoppingCartItem.get();
            shoppingCartItem.changeItemCount(itemCount + shoppingCartItem.getItemCount());
        }
  }
  ```

- Review
  ```
  Post 통신을 통해 장바구니 상품 추가에 필요한 정보를 전달받는다.
  정보를 전달 받을 때 상품 고유번호와 상품 수량을 전달 받게 되는데 상품 수량이 없다면 1 로 설정된다.
  전달 받은 정보 중 사용자, 상품 고유번호를 통해 사용자, 상품의 존재를 확인하고 없다면 예외를 반환한다.
  사용자와 상품이 모두 존재한다면 사용자의 장바구니에 동일한 상품이 있는지 검색한다.
  만약 동일한 상품이 없다면 사용자의 장바구니, 상품, 수량을 이용해 장바구니 상품을 생성하고 저장한다.
  동일한 상품이 있다면 장바구니 상품의 수량에 전달받은 수량만큼 더한 뒤 저장한다.
  ```

### 장바구니 상품 수량 변경
- Controller
  ```java
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
  ```

- ChangeShoppingCartItemCountRequestDto
  ```java
  @Data
  public class ChangeShoppingCartItemCountRequestDto {
    @NotNull(message = "쇼핑 카트 상품 Id")
    Long shoppingCartItemId;

    @NotNull(message = "변경할 수량")
    Integer changCount;
  }
  ```

- Service
  ```java
  @Transactional
  public String changeShoppingCartItemCount(Long userId, Long shoppingCartItemId, int changeCount) throws IllegalAccessException {
      User user = checkUserById(userId); // NoSuchElementException
      return shoppingCartService.changeCount(user.getShoppingCart().getId(), shoppingCartItemId, changeCount);
  }
  ```

- Service - shoppingCartService.changeCount
  ```java
  public String changeCount(Long shoppingCartId, Long shoppingCartItemId, int changeCount) throws IllegalAccessException {
        Optional<ShoppingCartItem> optionalShoppingCartItem = shoppingCartItemRepository.findById(shoppingCartItemId);
        if (optionalShoppingCartItem.isEmpty()) {
            throw new NoSuchElementException("잘못된 장바구니 상품입니다.");
        }
        ShoppingCartItem shoppingCartItem = optionalShoppingCartItem.get();
        if (!shoppingCartItem.getShoppingCart().getId().equals(shoppingCartId)) {
            throw new IllegalAccessException("사용자의 장바구니 상품이 아닙니다.");
        }
        shoppingCartItem.changeItemCount(changeCount);

        return shoppingCartItem.getItem().getName();
  }
  ```

- Review
  ```
  Post 통신을 통해 장바구니 상품 수량 변경에 필요한 정보를 전달받습니다.
  전달받은 장바구니 상품 고유번호, 장바구니 고유번호를 통해 장바구니 상품의 존재 여부와 해당 장바구니의 상품인지에 대한 검사가 이루어진다.
  장바구니 상품이 존재하고, 해당 장바구니의 상품일 경우 전달받은 수량 값으로 변경한다.
  ```

### 장바구니 선택 상품 삭제
- Controller
  ```java
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
  ```

- DeleteShoppingCartItemRequestDto
  ```java
  @Data
  public class DeleteShoppingCartItemRequestDto {
    List<Long> shoppingCartItemsIds = new ArrayList<>();
  }
  ```

- Service
  ```java
  @Transactional
  public List<String> deleteShoppingCartItem(Long userId, List<Long> shoppingCartItemIds) {
      User user = checkUserById(userId);
      return shoppingCartService.deleteShoppingCartItemByListIds(user, shoppingCartItemIds);
  }
  ```

- Service - shoppingCartService.deleteShoppingCartItemByListIds
  ```java
  public List<String> deleteShoppingCartItemByListIds(User user, List<Long> shoppingCartItemIds) {
        List<ShoppingCartItem> shoppingCartItems = findShoppingCartItemByShoppingCartIdAndIds(user, shoppingCartItemIds);
        List<String> itemsName = new ArrayList<>();

        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            itemsName.add(shoppingCartItem.getItem().getName());
        }
        shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);

        return itemsName;
  }
  
  public List<ShoppingCartItem> findShoppingCartItemByShoppingCartIdAndIds(User user, List<Long> shoppingCartItemIds){
        Long shoppingCartId = user.getShoppingCart().getId();
        List<ShoppingCartItem> shoppingCartItems =
                shoppingCartItemRepository.findShoppingCartItemByShoppingCartIdAndIds(shoppingCartId, shoppingCartItemIds);
        if ((shoppingCartItemIds.size() != shoppingCartItems.size()) || shoppingCartItemIds.isEmpty()) {
            throw new IllegalArgumentException("잘못된 장바구니 상품 정보입니다.");
        }
        return shoppingCartItems;
  }
  ```

- ShoppingCartItemRepositoryCustom
  ```java
  public interface ShoppingCartItemRepositoryCustom{
    List<ShoppingCartItem> findShoppingCartItemByShoppingCartIdAndIds(Long shoppingCartId, List<Long> shoppingCartItemIds);
  }
  ```

- ShoppingCartItemRepositoryCustomImpl
  ```java
  @Override
  public List<ShoppingCartItem> findShoppingCartItemByShoppingCartIdAndIds(Long shoppingCartId, List<Long> shoppingCartItemIds) {
      return queryFactory
              .select(shoppingCartItem)
              .from(shoppingCartItem)
              .where(shoppingCartItem.shoppingCart.id.eq(shoppingCartId),
                      shoppingCartItem.id.in(shoppingCartItemIds))
              .fetch();
  }
  ```

- Review
  ```
  Post 통신을 통해 장바구니 상품 삭제에 필요한 정보(장바구니 상품 고유번호)를 받아온다.
  장바구니 상품 고유번호는 List 형식으로 전달받아 단건, 복수건 삭제 모두 가능하다.
  전달받은 사용자의 고유번호를 통해 사용자를 검증한다.
  검증된 사용자의 장바구니 고유번호와 전달받은 장바구니 상품 고유번호들을 이용하여 사용자의 장바구니 상품인지 검증한다.
  검증에 문제가 없었다면 장바구니 상품 고유번호를 가진 장바구니 상품들을 삭제하고
  삭제된 상품의 이름을 List<String> 형식으로 반환한다.
  ```

### 장바구니 비우기
- Controller
  ```java
  @PostMapping("/deleteAllShoppingCartItem/{userId}")
  public ResponseEntity<String> deleteAllShoppingCartItem(@PathVariable("userId") Long userId) {
      try {
          String userName = userService.deleteAllShoppingCartItem(userId);
          return ResponseEntity.ok().body(userName + " 님의 장바구니가 비워졌습니다.");
      } catch (NoSuchElementException e) { // 장바구니에 등록된 상품 없음
          return createResponseEntity(e, NOT_FOUND);
      }
  }
  ```

- Service 
  - userService.deleteAllShoppingCartItem
    ```java
    @Transactional
    public String deleteAllShoppingCartItem(Long userId) {
        User user = checkUserById(userId); //NoSuchElementException
        shoppingCartService.deleteAllItems(user.getShoppingCart()); //NoSuchElementException 등록된 상품 없음
        return user.getName();
    }
    ```
  - shoppingCartService.deleteAllItems
    ```java
    public void deleteAllItems(ShoppingCart shoppingCart) {
          List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
          if (shoppingCartItems.isEmpty()) {
              throw new NoSuchElementException("장바구니에 담긴 상품이 없습니다.");
          }
          deleteShoppingCartItemByList(shoppingCartItems);
    }
    ```
  - shoppingCartService.deleteShoppingCartItemByList
    ```java
    public void deleteShoppingCartItemByList(List<ShoppingCartItem> shoppingCartItems) {
          List<Long> shoppingCartItemIds = new ArrayList<>();
          for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
              shoppingCartItemIds.add(shoppingCartItem.getId());
          }
          shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);
    }
    ```
- Review

  |                              종류                             |      설명                                  | 
  |:----------------------------------------:|:-----------------------------------------------------------:|
  |                          Controller                          |  Post 통신을 통해 장바구니 비우기에 필요한 정보를 전달 받는다.   |
  |      Service<br/>userService.deleteAllShoppingCartItem       |           사용자 고유번호를 통해 사용자 검증            |
  |        Service<br/>shoppingCartService.deleteAllItems        |       사용자의 장바구니를 통해 장바구니 상품을 불러온다.       |
  | Service<br/>shoppingCartService.deleteShoppingCartItemByList | 장바구니 상품들의 고유번호를 가지는 List를 생성한 후 batch 삭제 |

### 장바구니 전체 가격 조회
- Controller
  ```java
  @GetMapping("/getShoppingCartTotalPrice/{userId}")
  public ResponseEntity<String> getShoppingCartTotalPrice(@PathVariable("userId") Long userId) {
      try {
          int shoppingCartTotalPrice = userService.getShoppingCartTotalPrice(userId);
          return ResponseEntity.ok().body(shoppingCartTotalPrice + " 원");
      } catch (NoSuchElementException e) { // 사용자 없음
          return createResponseEntity(e, NOT_FOUND);
      }
  }
  ```
- Service
  - userService.getShoppingCartTotalPrice
    ```java
    public int getShoppingCartTotalPrice(Long userId) {
        User user = checkUserById(userId); //NoSuchElementException
        return user.getShoppingCart().getTotalPrice();
    }
    ```
- Review

  |      종류      |                 설명                  |
  |:------------:|:-----------------------------------:|
  |  Controller  |     Get 통신을 통해 사용자 고유번호를 전달받는다.     |
  |   Service<br/>userService.getShoppingCartTotalPrice    | 사용자 고유번호를 통해 사용자 검증 후 장바구니 전체 가격 반환 |

### 장바구니 전체 상품 조회
- Controller
  ```java
  @GetMapping("/getShoppingCartItems/{userId}")
  public ResponseEntity<?> getShoppingCartItems(@PathVariable("userId") Long userId, Pageable pageable) {
      try {
          Page<SearchShoppingCartDto> shoppingCartItems = userService.getShoppingCartItems(userId, pageable);
          return ResponseEntity.ok().body(shoppingCartItems);
      } catch (NoSuchElementException e) { // 사용자 없음
          return createResponseEntity(e, NOT_FOUND);
      }
  }
  ```

- SearchShoppingCartDto
  ```java
  @Data
  public class SearchShoppingCartDto {
      private Long shoppingCartItemId;
      private String itemName;
      private String itemSellerNickName;
      private int itemCount;
      private int itemPrice;
      private int totalItemPrice;
  
      @QueryProjection
      public SearchShoppingCartDto( Long shoppingCartItemId, String itemName, String itemSellerNickName, int itemCount, int itemPrice, int totalItemPrice) {
          this.shoppingCartItemId = shoppingCartItemId;
          this.itemName = itemName;
          this.itemSellerNickName = itemSellerNickName;
          this.itemCount = itemCount;
          this.itemPrice = itemPrice;
          this.totalItemPrice = totalItemPrice;
      }
  }
  ```

- Service
  - userService.getShoppingCartItems
    ```java
    public Page<SearchShoppingCartDto> getShoppingCartItems(Long userId, Pageable pageable) {
        User user = checkUserById(userId); //NoSuchElementException
        return shoppingCartService.getShoppingCartItems(user.getShoppingCart().getId(), pageable);
    }
    ```
  - shoppingCartService.getShoppingCartItems
    ```java
    public Page<SearchShoppingCartDto> getShoppingCartItems(Long shoppingCartId, Pageable pageable) {
        return shoppingCartItemRepository.searchShoppingCart(shoppingCartId, pageable);
    }
    ```

- Repository
  - ShoppingCartItemRepositoryCustom
    ```java
    public interface ShoppingCartItemRepositoryCustom{
        Page<SearchShoppingCartDto> searchShoppingCart(Long shoppingCartId, Pageable pageable);
    }
    ```
  - ShoppingCartItemRepositoryCustomImpl
    ```java
    @Override
    public Page<SearchShoppingCartDto> searchShoppingCart(Long shoppingCartId, Pageable pageable) {
        List<SearchShoppingCartDto> content = queryFactory
                .select(new QSearchShoppingCartDto(
                        shoppingCartItem.id,
                        item.name,
                        user.nickname,
                        shoppingCartItem.itemCount,
                        shoppingCartItem.itemPrice,
                        shoppingCartItem.totalItemPrice
                ))
                .from(shoppingCartItem)
                .leftJoin(shoppingCartItem.item, item)
                .leftJoin(shoppingCartItem.item.seller, user)
                .leftJoin(shoppingCartItem.shoppingCart, shoppingCart)
                .where(shoppingCart.id.eq(shoppingCartId))
                .orderBy(shoppingCartItem.createdDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.
                select(shoppingCartItem.count())
                .from(shoppingCartItem)
                .leftJoin(shoppingCartItem.item, item)
                .leftJoin(shoppingCartItem.item.seller, user)
                .leftJoin(shoppingCartItem.shoppingCart, shoppingCart)
                .where(shoppingCart.id.eq(shoppingCartId))
                .orderBy(shoppingCartItem.createdDate.asc());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    ```
- Review

  | 종류                                                   |                              설명                               |
  |:-------------------------------------------------------------:|:-----------------------------------:|
  | Controller                                           | Get 통신을 통해 사용자 고유번호, 페이지 정보를 전달받는다.<br/> Dto로 이루어진 페이지를 반환한다. |
  | SearchShoppingCartDto                                |                         페이지를 이루는 Dto                          |
  | Service<br/>userService.getShoppingCartTotalPrice    |                      사용자 고유번호를 통해 사용자 검증                      |
  | Service<br/>shoppingCartService.getShoppingCartItems |       shoppingCartItemRepository.searchShoppingCart 호출        |
  - shoppingCartItemRepository.searchShoppingCart
    ```
    전달받은 사용자의 장바구니 고유번호와 일치하는 장바구니 상품들을 검색한다.
    검색된 정보는 SearchShoppingCartDto 로 변환되며 생성일을 기준으로 오름차순 정렬된다.
    정보들은 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.
    
    위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
      - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
      - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
    ```