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