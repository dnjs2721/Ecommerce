## 💳 Order

### OrderItem Entity
   ```java
    @Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class OrderItem extends BaseTimeEntity {
        @Id
        @GeneratedValue
        private Long id;
        private Long buyerOrderId;
        private Long sellerOrderId;
        private Long buyerId;
        private Long sellerId;
        private Long itemId;
        private String itemName;
        private int price;
        private int count;
        private int totalPrice;
        @Enumerated(EnumType.STRING)
        private OrderItemStatus orderItemStatus;
        private String comment;
        private String impUid;
    
        @Builder
        public OrderItem(Long buyerOrderId, Long sellerOrderId,  Long buyerId, Long sellerId, Long itemId, String itemName, int price, int count) {
            this.buyerOrderId = buyerOrderId;
            this.sellerOrderId = sellerOrderId;
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.itemId = itemId;
            this.itemName = itemName;
            this.price = price;
            this.count = count;
            this.totalPrice = price * count;
            this.orderItemStatus = OrderItemStatus.WAITING_FOR_PAYMENT;
        }
    
        public void changeStatus(OrderItemStatus orderItemStatus) {
            this.orderItemStatus = orderItemStatus;
        }
    
        public void setComment(String comment) {
            this.comment = comment;
        }
    
        public void setImpUid(String impUid) {
            this.impUid = impUid;
        }
    }
   ```

### OrdersForBuyer Entity
   ```java
    @Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class OrdersForBuyer extends BaseTimeEntity {
        @Id
        @GeneratedValue
        private Long id;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "buyerId")
        private User buyer;
        @OneToMany(mappedBy = "buyerOrderId")
        private final List<OrderItem> orderItems = new ArrayList<>();
    
        private String impUid;
    
        public OrdersForBuyer(User buyer) {
            this.buyer = buyer;
        }
    
        public int getTotalPrice() {
            int totalPrice = 0;
            for (OrderItem orderItem : this.orderItems) {
                if (orderItem.getOrderItemStatus() != OrderItemStatus.CANCEL) {
                    totalPrice += orderItem.getTotalPrice();
                }
            }
            return totalPrice;
        }
    
        public List<String> getOrderItemsName() {
            List<String> itemsName = new ArrayList<>();
            for (OrderItem orderItem : this.orderItems) {
                if (orderItem.getOrderItemStatus() != OrderItemStatus.CANCEL) {
                    itemsName.add(orderItem.getItemName());
                }
            }
            return itemsName;
        }
    
        public void setImpUid(String impUid) {
            this.impUid = impUid;
        }
    }
   ```

### OrdersForSeller Entity
   ```java
    @Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class OrdersForSeller extends BaseTimeEntity {
        @Id
        @GeneratedValue
        private Long id;
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sellerId")
        private User seller;
        private String buyerName;
        private String buyerPNum;
        @Embedded
        private Address buyerAddress;
        @OneToMany(mappedBy = "sellerOrderId")
        private final List<OrderItem> orderItems = new ArrayList<>();
        private String impUid;
    
        @Builder
        public OrdersForSeller(User seller, String buyerName, String buyerPNum, Address buyerAddress) {
            this.seller = seller;
            this.buyerName = buyerName;
            this.buyerPNum = buyerPNum;
            this.buyerAddress = buyerAddress;
        }
    
        public void setImpUid(String impUid) {
            this.impUid = impUid;
        }
    }
   ```

### 장바구니 전체 상품 주문
- Controller
    ```java
    @GetMapping("/allItemAtShoppingCart/{userId}")
    public ResponseEntity<String> orderAllItemAtShoppingCart(@PathVariable("userId") Long userId) {
        try {
            List<String> itemsName = userService.orderAllItemAtShoppingCart(userId);
            return ResponseEntity.ok().body("상품 " + itemsName.toString() + " 이 주문되었습니다. 상태(결제대기)");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND);
        } catch (NotEnoughStockException e2) {
            return createResponseEntity(e2, CONFLICT);
        }
    }
    ```

- Service
  - userService.orderAllItemAtShoppingCart
    ```java
    @Transactional
    public List<String> orderAllItemAtShoppingCart(Long userId) {
        User user = checkUserById(userId);
        return shoppingCartService.orderAllItemAtShoppingCart(user);
    }
    ```
  - shoppingCartService.orderAllItemAtShoppingCart
    ```java
    public List<String> orderAllItemAtShoppingCart(User user) {
        ShoppingCart shoppingCart = user.getShoppingCart();

        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        if (shoppingCartItems.isEmpty()) {
            throw new NoSuchElementException("장바구니에 담긴 상품이 없습니다.");
        }

        List<String> itemsName = new ArrayList<>();
        Map<Item, Integer> itemAndCountMap = createItemAndCountMap(shoppingCartItems, itemsName);

        ordersService.createOrders(user, itemAndCountMap); // 구매자용, 판매자용 주문 생성
        deleteShoppingCartItemByList(shoppingCartItems); // 장바구니 비우기

        return itemsName;
    }
    ```
  - shoppingCartService.createItemAndCountMap
    ```java
    public Map<Item, Integer> createItemAndCountMap(List<ShoppingCartItem> shoppingCartItems, List<String> itemsName) {
        Map<Item, Integer> itemsAndCount = new HashMap<>();

        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            Item item = shoppingCartItem.getItem();
            int shoppingCartItemCount = shoppingCartItem.getItemCount();

            if (shoppingCartItemCount > item.getStockQuantity()) {
                throw new NotEnoughStockException(item.getName() + "의 재고가 부족합니다.");
            }

            itemsAndCount.put(item, shoppingCartItemCount);

            itemsName.add(item.getName());
        }

        return itemsAndCount;
    }
    ```
  - ordersService.createOrders
    ```java
    public void createOrders(User buyer, Map<Item, Integer> allItemsAndCount) {
        Map<User, List<Map<Item, Integer>>> sellerAndItem = new HashMap<>(); // 판매자용 주문을 만들기 위한 맵

        Set<Item> items = allItemsAndCount.keySet(); // 맵의 keySet 을 통해 items 을 받아온다.
        for (Item item : items) { // items 순회
            User seller = item.getSeller(); // 판매자 조회

            Map<Item, Integer> itemAndCount = new HashMap<>();

            int shoppingCartItemCount = allItemsAndCount.get(item);

            itemAndCount.put(item, shoppingCartItemCount);

            // 판매자 별로 상품들 분류
            if (!sellerAndItem.containsKey(seller)) { // sellerAndItem 에 판매자 Key 가 없다면
                sellerAndItem.put(seller, new ArrayList<>(List.of(itemAndCount)));
            } else { // sellerAndItem 에 판매자 Key 가 있다면
                sellerAndItem.get(seller).add(itemAndCount);
            }
        }

        OrdersForBuyer orderForBuyer = createOrderForBuyer(buyer); // 구매자용 주문 생성

        // 판매자별로 판매자용 주문 생성
        Set<User> sellers = sellerAndItem.keySet();
        for (User seller : sellers) {
            OrdersForSeller orderForSeller = createOrderForSeller(seller, buyer); // 판매자 주문 생성

            List<Map<Item, Integer>> sellerItemsAndCount = sellerAndItem.get(seller); // 주문 상품 리스트에서 판매자것만 리스트로 가져온다.
            for (Map<Item, Integer> sellerItemAndCount : sellerItemsAndCount) { // 주문 상품 중 판매자 것만 순회
                Set<Item> sellerItems = sellerItemAndCount.keySet(); // 맵의 keySet 을 통해 item 을 받아온다 Set 이지만 하나밖에 없다.
                for (Item sellerItem : sellerItems) { // for 문 이지만 item 은 하나밖에 없다.
                    int shoppingCartItemCount = sellerItemAndCount.get(sellerItem); // 상품 수량을 가지고 온다.
                    int stockQuantity = sellerItem.getStockQuantity();

                    sellerItem.decreaseStockQuantity(shoppingCartItemCount);
                    // 주문상품 생성
                    // 구매자의 주문 id 와 판매자의 주문 id 를 갖는다.
                    // 한 주문에 대하여
                    //      구매자의 주문 id 를 통해 구매자는 판매자와 상관없이 한번에 조회 가능하다.
                    //      판매자의 주문 id 를 통해 판매자는 자신의 상품에 대한 주문을 조회 가능하다.
                    // 판매자 혹은 구매자가 회원 탈퇴 하여도 주문 상품 내역은 남는다. 상품 삭제도 동일
                    // buyerId, sellerId, itemId 를 통해 탈퇴 혹은 삭제 한 객체의 정보 조회 가능 (DeletedUser, DeletedItem)
                    createOrderItem(orderForBuyer.getId(), orderForSeller.getId(), buyer.getId(), seller.getId(), sellerItem, shoppingCartItemCount);
                }
            }
        }
    }
    ```
  - orderService.createOrderForBuyer
    ```java
    public OrdersForBuyer createOrderForBuyer(User buyer) {
        OrdersForBuyer ordersForBuyer = new OrdersForBuyer(buyer);

        buyerRepository.save(ordersForBuyer);

        return ordersForBuyer;
    }
    ```
  - orderService.createOrderForSeller
    ```java
    public OrdersForSeller createOrderForSeller(User seller, User buyer) {
        OrdersForSeller ordersForSeller = OrdersForSeller.builder()
                .seller(seller)
                .buyerName(buyer.getName())
                .buyerPNum(buyer.getPNum())
                .buyerAddress(buyer.getAddress())
                .build();

        sellerRepository.save(ordersForSeller);

        return ordersForSeller;
    }
    ```
  - orderService.createOrderItem
    ```java
    public void createOrderItem(Long buyerOrderId, Long sellerOrderId, Long buyerId, Long sellerId, Item item, int count) {
        OrderItem orderItem = OrderItem.builder()
                .buyerOrderId(buyerOrderId)
                .sellerOrderId(sellerOrderId)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .itemId(item.getId())
                .itemName(item.getName())
                .price(item.getPrice())
                .count(count)
                .build();

        orderItemRepository.save(orderItem);
    }
    ```

- Review

  |     종류     |                       상세                       |                                                                  설명                                                                   |
  |:----------:|:----------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------:|
  | Controller |    GET<br/> /allItemAtShoppingCart/{userId}    |                                                      GET 통신을 통해 사용자 고유번호를 전달받는다.                                                      |
  |  Service   |     userService.orderAllItemAtShoppingCart     |                             사용지 고유번호를 통해 사용자 존재 검증<br/> shoppingCartService.orderAllItemAtShoppingCart 호출                             |
  |  Service   | shoppingCartService.orderAllItemAtShoppingCart | 사용자의 장바구니 상품을 검색<br/> 장바구니 상품 정보를 재가공(shoppingCartService.createItemAndCountMap)<br/> 주문 생성(ordersService.createOrders)<br/> 장바구니 비우기 |
  |  Service   |   shoppingCartService.createItemAndCountMap    |                           사용자의 장바구니에 담긴 상품의 재고를 체크하고 재고가 충분하다면<br/> Map<Key : 상품, Value : 주문수량> 형태로 가공하여 반환                           |

  - ordersService.createOrders
    ```
    Map<Key : 상품, Value : 주문수량>]을 이용하여 구매자 주문서, 판매자 주문서, 주문상품을 생성한다.
    
    전달받은 정보를 판매자 기준으로 재가공한다. -> Map<Key: 판매자, Value : List<Map<Key : 상품, Value : 주문수량>>>
    
    구매자 정보를 이용하여 구매자 주문서를 생성한다.
    
    판매자 기준으로 재가공된 정보의 Key를 불러온다. -> 판매자 집합
    판매자 집합을 순회
      - 각 판매자의 주문서 생성
      - 각 판매자의 Value를 가져온다 -> List<Map<Key : 상품, Value : 주문수량>> -> 상품과 주문수량의 리스트
        - 상품과 주문수량의 리스트를 순회하며 상품의 재고를 다시 검증하고, 재고가 충분하다면 상품의 재고를 감소시킨 뒤
          구매자 주문서, 판매자 주문서, 구매자 고유번호, 판매자 고유번호, 상품, 수량을 이용하여 주문상품을 생성한다.
    ```
  
### 장바구니 상품 선택 주문
- Controller
  ```java
  @PostMapping("/selectItemAtShoppingCart/{userId}")
  public ResponseEntity<String> orderSelectItemAtShoppingCart(@PathVariable("userId") Long userId, @RequestBody SelectItemAtShoppingCartRequestDto request) {
      try {
          List<String> itemsName = userService.orderSelectItemAtShoppingCart(userId, request.getShoppingCartItemIds());
          return ResponseEntity.ok().body("상품 " + itemsName.toString() + " 이 주문되었습니다. 상태(결제대기)");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND);
      } catch (IllegalArgumentException | NotEnoughStockException e2) {
          return createResponseEntity(e2, CONFLICT);
      }
  }
  ```

- SelectItemAtShoppingCartRequestDto
  ```java
  @Data
  public class SelectItemAtShoppingCartRequestDto {
    List<Long> shoppingCartItemIds = new ArrayList<>();
  }
  ```

- Service
  - userService.orderSelectItemAtShoppingCart
    ```java
    @Transactional
    public List<String> orderSelectItemAtShoppingCart(Long userId, List<Long> shoppingCartItemId) {
        User user = checkUserById(userId);
        return shoppingCartService.orderSelectItemAtShoppingCart(user, shoppingCartItemId);
    }
    ```
  - shoppingCartService.orderSelectItemAtShoppingCart
    ```java
    public List<String> orderSelectItemAtShoppingCart(User user, List<Long> shoppingCartItemIds) {

        List<ShoppingCartItem> shoppingCartItems = findShoppingCartItemByShoppingCartIdAndIds(user, shoppingCartItemIds);

        List<String> itemsName = new ArrayList<>();

        Map<Item, Integer> itemAndCountMap = createItemAndCountMap(shoppingCartItems, itemsName);

        ordersService.createOrders(user, itemAndCountMap);
        shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);

        return itemsName;
    }
    ```

 
- Review

  |     종류     |                               상세                                |                                                      설명                                                       |
  |:----------:|:---------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------:|
  | Controller |          POST<br/> /selectItemAtShoppingCart/{userId}           |                                   Post 통신을 통해 사용자 고유번호와 주문에 필요한 정보를 전달받는다.                                    |
  |    Dto     |               SelectItemAtShoppingCartRequestDto                |                                         주문에 필요한 정보 : 장바구니 상품 고유번호 리스트                                         |
  |  Service   |            userService.orderSelectItemAtShoppingCart            |               사용자 고유번호를 통해 사용자 존재 검증<br/> shoppingCartService.orderSelectItemAtShoppingCart 호출                |
  |  Service   |        shoppingCartService.orderSelectItemAtShoppingCart        | 전달받은 장바구니 상품들이 사용자의 상품인지 검증<br/> 장바구니 상품 정보를 재가공<br/> 재가공한 정보를 이용해 구매자, 판매자, 상품 주문서 생성<br/> 장바구니에서 해당 상품들 삭제  |

### 단건주문 
- Controller
  ```java
  @PostMapping("/singleItem/{userId}")
  public ResponseEntity<String> orderSingleItem(@PathVariable("userId") Long userId, @RequestBody @Valid OrderSingleItemRequestDto request) {
      try {
          String itemName = userService.orderSingleItem(userId, request.getItemId(), request.getItemCount());
          return ResponseEntity.ok().body(itemName + " 이(가) 주문되었습니다. 상태(결재대기)");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND);
      } catch (NotEnoughStockException e2) {
          return createResponseEntity(e2, CONFLICT);
      }
  }
  ```

- OrderSingleItemRequestDto
  ```java
  @Data
  public class OrderSingleItemRequestDto {
    @NotNull(message = "상품 Id")
    Long itemId;
    @NotNull(message = "상품 수량")
    Integer itemCount;
  }
  ```

- Service
  - userService.orderSingleItem
    ```java
    @Transactional
    public String orderSingleItem(Long buyerId, Long itemId, int itemCount) {
        User buyer = checkUserById(buyerId); // NoSuchElementException
        Item item = itemService.checkItem(itemId); // NoSuchElementException
        return ordersService.orderSingleItem(buyer, item, itemCount);
    }
    ```
  
  - ordersService.orderSingleItem
    ```java
    public String orderSingleItem(User buyer, Item item, int itemCount) {
        item.decreaseStockQuantity(itemCount); // NotEnoughStockException
        User seller = item.getSeller();
        OrdersForBuyer orderForBuyer = createOrderForBuyer(buyer);
        OrdersForSeller orderForSeller = createOrderForSeller(seller, buyer);
        createOrderItem(orderForBuyer.getId(), orderForSeller.getId(), buyer.getId(), seller.getId(), item, itemCount);

        return item.getName();
    }
    ```

- Review

  |     종류     |           상세                   |                                              설명                                              |
  |:----------:|:------------------------------:|:--------------------------------------------------------------------------------------------:|
  | Controller | POST<br/> /singleItem/{userId} |                           Post 통신을 통해 사용자 고유번호와 주문에 필요한 정보를 전달받는다.                           |
  |    Dto     |   OrderSingleItemRequestDto    |                                 주문에 필요한 정보 : 상품 고유번호, 주문 수량                                  |
  |  Service   |  userService.orderSingleItem   | 사용자 고유번호를 통해 사용자 존재 검증<br/> 상품 고유번호를 통해 상품 존재 검증<br/> shoppingCartService.orderSingleItem 호출 |
  |  Service   | ordersService.orderSingleItem  |                              상품 재고 감소<br/> 구매자, 판매자, 상품 주문서 생성                               |
