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