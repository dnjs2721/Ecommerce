## 🙍🏻‍ User ( 사용자 )

### User Entity
   ```java
    @Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class User extends BaseTimeEntity {
        @Id
        @GeneratedValue
        private Long id;
        private String name;
        private String nickname;
        private String email;
        private String password;
        private String pNum;
        private String birth;
        @Embedded
        private Address address;
        @Enumerated(EnumType.STRING)
        private UserStatus status;
        @OneToMany(mappedBy = "seller")
        private final List<Item> sellItems = new ArrayList<>();
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "shoppingCartId")
        private ShoppingCart shoppingCart;
        @OneToMany(mappedBy = "buyer")
        private final List<OrdersForBuyer> ordersForBuyer = new ArrayList<>();
        @OneToMany(mappedBy = "seller")
        private final List<OrdersForSeller> ordersForSeller = new ArrayList<>();
    
    
        @Builder
        public User(String name, String nickname, String email, String password, String pNum, String birth, Address address) {
            this.name = name;
            this.nickname = nickname;
            this.email = email;
            this.password = password;
            this.pNum = pNum;
            this.birth = birth;
            this.address = address;
        }
    
        public void changePassword(String password) {
            this.password = password;
        }
    
        public void changeNickname(String nickname) {
            this.nickname = nickname;
        }
    
        public void changeAddress(Address address) {
            this.address = address;
        }
    
        public void setStatus(UserStatus status) {
            this.status = status;
        }
    
        public void setShoppingCart(ShoppingCart shoppingCart) {
            this.shoppingCart = shoppingCart;
        }
    }
   ```

### 회원가입
- Controller
    ```java
    @PostMapping("/join")
    public ResponseEntity<String> joinUser(@RequestBody @Valid JoinRequestDto request) {
        try {
            Long userId = userService.join(request);
            return ResponseEntity.ok().body(userId.toString() + " 회원가입 되었습니다.");
        } catch (IllegalStateException e) {
            return createResponseEntity(e, CONFLICT); // 닉네임, 이메일, 휴대폰 번호 중복 예외
        }
    }
    ```
- JoinRequestDto
    ```java
    @Data
    public class JoinRequestDto {
        @NotBlank(message = "사용자 이름(필수)")
        private String name;
        @NotBlank(message = "닉네임(필수)")
        private String nickname;
        @NotBlank(message = "이메일(필수)")
        @Email
        private String email;
        @NotBlank(message = "이메일(필수)")
        private String password;
        @NotBlank(message = "전화번호(필수)")
        private String pNum;
        @NotBlank(message = "생알(필수)")
        private String birth;
        @NotBlank(message = "주소(필수)")
        private String region;
        @NotBlank(message = "주소(필수)")
        private String city;
        @NotBlank(message = "주소(필수)")
        private String street;
        @NotBlank(message = "주소(필수)")
        private String detail;
        @NotBlank(message = "주소(필수)")
        private String zipcode;
    }
    ```

- Service - join
    ```java
    @Transactional
    public Long join(JoinRequestDto request) {
        duplicationCheckService.validateDuplicateEmail(request.getEmail());
        checkIgnoreNickName(request.getNickname());
        duplicationCheckService.validateDuplicateNickname(request.getNickname());
        duplicationCheckService.validateDuplicatePNum(request.getPNum());
        User user = createUser(request);
        user.setStatus(UserStatus.COMMON);
        userRepository.save(user);
        return user.getId();
    }
    ```

- Service - createUser
    ```java
    public User createUser(JoinRequestDto request) {
        ShoppingCart shoppingCart = shoppingCartService.createShoppingCart();
        User user = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(request.getPassword())
                .pNum(request.getPNum())
                .birth(request.getBirth())
                .address(new Address(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()))
                .build();
        user.setShoppingCart(shoppingCart);
        return user;
    }
    ```

- Review
    ```
    Post 통신을 통해 가입 희망자가 입력한 정보들을 전달 받는다.
    서비스 로직에서 중복(이메일, 휴대폰, 닉네임), 사용불가 닉네임 검사가 이루어진다. 
    이 과정중 예외가 발생하면 예외를 반환하고 그렇지 않은 경우 사용자 정보를 이용하여 사용자를 생성한다.
    ```
    ```
    Service 의 createUser 를 통해 사용자를 생성한다.
    이 때 사용자를 생성하면서 사용자에게 고유한 장바구니를 부여한다.
    일반 사용자용 회원가입이기 때문에 생성한 사용자의 상태를 COMMON 으로 설정한다.
    이렇게 생성된 사용자를 DB에 저장하고 Controller 로 생성된 사용자의 고유번호를 반환한다.
    ```

### 로그인
- Controller
  ```java
  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto request) {
      try {
          Long id = userService.login(request.getEmail(), request.getPassword());
          return ResponseEntity.ok().body(id.toString() + " 로그인 성공");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
      } catch (IllegalAccessException e2) {
          return createResponseEntity(e2, UNAUTHORIZED); // 비밀번호 오류 예외
      }
  }
  ```

- LoginRequestDto
  ```java
  @Data
  public class LoginRequestDto {
    @Email
    @NotBlank(message = "이메일(필수)")
    private String email;

    @NotBlank(message = "비밀번호(필수)")
    private String password;
  }
  ```

- Service
  ```java
  public Long login(String email, String password) throws IllegalAccessException {
        User user = checkUserByEmail(email);
        if (user.getPassword().equals(password)) {
            return user.getId();
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
  }
  ```

- Review
  ```
  Post 통신을 통해 사용자가 입력한 로그인 정보를 전달 받는다.
  Service 에서 사용자가 입력한 이메일을 가진 사용자가 있는지 검사하며 사용자가 없으면 예외를 반환한다.
  동일한 이메일을 가진 사용자 정보의 암호와 로그인 정보의 암호가 일치 하는지 검사한다.
  암호가 일치하면 사용자의 고유번호(id) 를 반환하고 그렇지 않다면 예외를 반환한다.
  ```

### 아이디(이메일) 찾기
- Controller
  ```java
  @PostMapping("/findEmail")
    public ResponseEntity<String> findEmail(@RequestBody @Valid FindEmailRequestDto request) {
        try {
            String email = userService.findEmailByNameAndPNum(request.getName(), request.getPNum());
            return ResponseEntity.ok().body(request.getName() + "님의 아이디(이메일)은 " + email + " 입니다.");
        } catch (NoSuchElementException e) {
            return createResponseEntity(e, NOT_FOUND); // 등록된 사용자 없음 예외
        }
  }  
  ```

- FindEmailRequestDto
  ```java
  @Data
  public class FindEmailRequestDto {
      @NotBlank(message = "이름(필수)")
      private String name;
  
      @NotBlank(message = "전화번호(필수)")
      private String pNum;
  }
  ```

- Service
  ```java
  public String findEmailByNameAndPNum(String name, String pNum) {
        String email = userRepository.findEmailByNameAndPNum(name, pNum);
        if (email == null) {
            throw new NoSuchElementException("가입되지 않은 회원 입니다. 이름 혹은 전화번호를 확인 해 주세요.");
        }
        return email;
  }  
  ```

- Repository
  - UserRepositoryCustom
    ```java
    public interface UserRepositoryCustom {
        String findEmailByNameAndPNum(String name, String pNum);
    }
    ```
  - UserRepositoryCustomImpl
    ```java
    public class UserRepositoryImpl implements UserRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    public UserRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }
    
    @Override
    public String findEmailByNameAndPNum(String name, String pNum) {
        return queryFactory
                .select(user.email)
                .from(user)
                .where(user.name.eq(name),
                        user.pNum.eq(pNum))
                .fetchOne();
        }
    }
    ```

- Review
  ```
  Post 통신을 통해 아이디 찾기에 필요한 정보를 전달 받는다.
  Service 에서 전달받은 정보를 이용하여 이메일을 찾는다. User 전체가 아닌 email 만을 필요로 하기에
  사용자의 이름과, 전화번호 가 일치하는 사용자의 email 을 선택하는 query 를 작성하여 구현하였다.  
  해당하는 이메일이 있다면 이메일을 Controller 로 반환하고 없다면 예외를 반환한다.
  ```
  
### 비밀번호 변경
- Controller
  ```java
  @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequestDto request) {
        try {
            String email = userService.changePassword(request.getEmail(), request.getPassword(), request.getNewPassword());
            return ResponseEntity.ok().body(email + " 님의 비밀번호가 성공적으로 변경 되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, UNAUTHORIZED); // 비밀번호 오류 예외
        } catch (IllegalStateException e3) {
            return createResponseEntity(e3, CONFLICT); // 동일한 패스워드 예외
        }
  }  
  ```

- ChangePasswordRequestDto
  ```java
  @Data
  public class ChangePasswordRequestDto {
    @Email
    @NotBlank(message = "이메일(필수)")
    private String email;
    @NotBlank(message = "기존 비밀번호(필수)")
    private String password;
    @NotBlank(message = "새 비밀번호(필수)")
    private String newPassword;
  }
  ```

- Service
  ```java
   @Transactional
    public String changePassword(String email, String password, String newPassword) throws IllegalAccessException {
        User user = checkUserByEmail(email);
        if (user.getPassword().equals(password)) {
            if (user.getPassword().equals(newPassword)) {
                throw new IllegalStateException("현재 사용중인 패스워드와 같습니다.");
            }
            user.changePassword(newPassword);
            return email;
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
  }
  ```

- Review
  ```
  Post 통신을 통해 비밀번호 변경에 필요한 정보를 전달받는다.
  Service 로직에서 전달받은 정보 중 이메일을 통해 사용자의 유무를 파악하고 등록된 사용자가 없다면 예외를 반환한다.
  사용자가 있으면 전달 받은 기존 비밀번호와 저장된 비밀번호를 비교검증 한다.
  일치하지 않으면 예외를 반환하고 현재 비밀번호와 변경할 비밀번호가 같으면 예외를 반환한다.
  비밀번호 검증을 통과화면 저장된 비밀번호를 변경하고 Controller로 이메일을 반환한다.
  ```

### 회원 탈퇴
- Controller
  ```java
  @PostMapping("/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestBody @Valid DeleteUserRequestDto request) {
        try {
            String userName = userService.deleteUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok().body(userName + " 님 정상적으로 회원탈퇴 되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, UNAUTHORIZED); // 비밀번호 오류 예외
        }
  }
  ```

- DeleteUserRequestDto
  ```java
  @Data
  public class DeleteUserRequestDto {
    @NotBlank(message = "이메일(필수)")
    @Email
    String email;
    @NotBlank(message = "패스워드(필수)")
    String password;
  }
  ```

- Service
  ```java
  @Transactional
    public String deleteUser(String email, String password) throws IllegalAccessException {
        User user = checkUserByEmail(email);
        if (password.equals(user.getPassword())) {
            if (user.getStatus().equals(UserStatus.SELLER)) {
                List<Item> sellItems = user.getSellItems();
                List<Long> sellItemIds = new ArrayList<>();
                for (Item item : sellItems) {
                    sellItemIds.add(item.getId());
                }
                itemService.deleteItem(user, sellItemIds);
                ordersService.deleteSellerOrder(user);
            }
            ordersService.deleteBuyerOrder(user);
            shoppingCartService.deleteShoppingCart(user.getShoppingCart());
            saveDeletedUser(user);
            userRepository.delete(user);
            return user.getName();
        } else {
            throw new IllegalAccessException("잘못된 패스워드 입니다.");
        }
    }
  ```

- Service - saveDeletedUser
  ```java
  public void saveDeletedUser(User user) {
        DeletedUser deletedUser = DeletedUser.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userNickname(user.getNickname())
                .userEmail(user.getEmail())
                .userPassword(user.getPassword())
                .userPNum(user.getPNum())
                .userBirth(user.getBirth())
                .userAddress(user.getAddress())
                .userStatus(user.getStatus())
                .build();

        deletedUserRepository.save(deletedUser);
    }
  ```

- Review
  ```
  Post 통신을 통해 회원 탈퇴에 필요한 정보를 전달받는다.
  Service 로직에서 전달받은 정보 중 이메일을 통해 사용자의 유무를 파악하고 등록된 사용자가 없다면 예외를 반환한다.
  사용자가 있으면 전달 받은 기존 비밀번호와 저장된 비밀번호를 비교 검증 한다. 비밀번호가 일치하지 않다면 예외를 반환하고,
  막약 탈퇴 하는 사용자가 판매자라면 다음의 로직이 실행된다.
  ```
  ```
  1. 판매자 판매 물품 삭제
    a. 다른 사용자 장바구니에 등록된 물품 정보 삭제
    b. 삭제되는 물품 정보를 일정기간 보관을 위해 저장
    b. 판매자 판매 물품 삭제
  2. 판매 내역 삭제 (상품별 주문 내역은 남는다.)
  ```
  ```
  판매자 로직이 끝나면 구매자 주문 내역을 삭제하고, 탈퇴하는 사용자의 장바구니를 비우고 삭제한다.
  그 후 탈퇴 하는 사용자의 정보를 일정기간 보관하기 위해 저장하고 사용자를 삭제한다.
  정상적으로 끝났을 경우 Controller 에 사용자의 이름을 전달한다.
  ```

- Note
  ```
  회원을 바로 삭제 하려고 하면 외래 키 무결성을 위반한다.
  기본적으로 사용자에 대한 장바구니, 장바구니 물품, 주문목록, 판매자일경우 추가로 판매상품, 판매내역이 있다.
  이를 해결하기 위하여 JPA의 cascade = CascadeType.REMOVE 을 사용 할 수 있지만 장바구니-장바구니 상품을 제외하고 사용하지 않았다.
  cascade 를 사용하여 영속성 전이 특성을 부여하면 User 엔티티 삭제만으로 연관된 엔티티를 편하게 삭제할 수 있지만
  연관되는 엔티티의 수 만큼 select, delete 쿼리가 발생된다. 
  최대 : 1(사용자) + 1(장바구니) + 장바구니 물품의 수 + 주문목록의 수 + 판매상품의 수 + 장바구니에 등록된 판매상품의 수 + 판매내역의 수
  이를 방지하기 위하여 cascade 를 사용하지 않고 외래 키 무결성 위반을 해결하였다.
  자세한 내용은 Service[ShoppingCart, Item, Orders] 참조.
  ```

### 정보 수정 (닉네임. 주소)
- Controller
  ```java
  @PostMapping("/changeUserInfo")
    public ResponseEntity<String> changeUserInfo(@RequestBody @Valid ChangeUserInfoRequestDto request) {
        try {
            userService.changeUserInfo(request);
            return ResponseEntity.ok().body("정보를 성공적으로 변경하였습니다.");
        } catch (IllegalStateException | IllegalArgumentException e1) {
            return createResponseEntity(e1, CONFLICT); // 닉네임 중복, 사용불가 닉네임, 잘못된 주소형태 예외
        } catch (NoSuchElementException e2) {
            return createResponseEntity(e2, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalAccessException e3) {
            return createResponseEntity(e3, UNAUTHORIZED); // 비밀번호 오류 예외
        }
    }
  ```

- ChangeUserInfoRequestDto
  ```java
  @Data
  public class ChangeUserInfoRequestDto {
    @NotBlank(message = "이메일(필수)")
    @Email
    String email;
    @NotBlank(message = "패스워드(필수)")
    String password;
    String nickname;
    String region;
    String city;
    String street;
    String detail;
    String zipcode;
  }
  ```

- Service
  ```java
  @Transactional
  public void changeUserInfo(ChangeUserInfoRequestDto request) throws IllegalAccessException {
      User user = checkUserByEmail(request.getEmail()); // NoSuchElementException
      if (request.getPassword().equals(user.getPassword())) {
          Address address = changeUserInfoAddress(request.getRegion(), request.getCity(), request.getStreet(), request.getDetail(), request.getZipcode()); // IllegalArgumentException, 주소 형태 확인
          if (request.getNickname() != null) {
              String newNickname = changeUserInfoNickname(request.getNickname(), user.getNickname()); // IllegalStateException,닉네임 사용가능 유무 확인
              user.changeNickname(newNickname);
          }
          if (address != null) user.changeAddress(address);
      } else {
          throw new IllegalAccessException("잘못된 패스워드 입니다.");
      }
  }
  ```

- Service - changeUserInfoAddress
  ```java
  public Address changeUserInfoAddress(String region, String city, String street, String detail, String zipcode) {
        if (isBlank(region) && isBlank(city) && isBlank(street) && isBlank(detail) && isBlank(zipcode)) {
            return null;
        } else if (isNotBlank(region) && isNotBlank(city) && isNotBlank(street) && isNotBlank(detail) && isNotBlank(zipcode)) {
            return new Address(region, city, street, detail, zipcode);
        } else {
            throw new IllegalArgumentException("잘못된 주소형태 입니다.");
        }
    }
  ```

- Service - changeUserInfoNickname
  ```java
  public String changeUserInfoNickname(String newNickname, String nickname) {
        if (nickname.equals(newNickname)) {
            throw new IllegalStateException("현재 사용중인 닉네임입니다.");
        }
        checkIgnoreNickName(newNickname); // IllegalStateException
        duplicationCheckService.validateDuplicateNickname(newNickname); // IllegalStateException
        return newNickname;
    }
  ```

- Service - checkIgnoreNickName
  ```java
  public void checkIgnoreNickName(String nickName) {
        String ignoreNickname = "admin";
        if (nickName.toUpperCase().matches("(.*)"+ignoreNickname.toUpperCase()+"(.*)")
                || nickName.toLowerCase().matches("(.*)"+ignoreNickname.toLowerCase()+"(.*)") ) {
            throw new IllegalStateException("사용할 수 없는 닉네임입니다.");
        }
    }
  ```

- Review
  ```
  Post 통신을 통해 정보 수정에 필요한 정보를 전달받는다.
  Service 로직에서 전달받은 정보 중 이메일을 통해 사용자의 유무를 파악하고 등록된 사용자가 없다면 예외를 반환한다.
  사용자가 있으면 전달 받은 기존 비밀번호와 저장된 비밀번호를 비교 검증 한다. 비밀번호가 일치하지 않다면 예외를 반환한다.
  받아 온 정보 중 주소 관련 정보가 없다면 주소는 변경되지 않으며 주소 정보는 있지만 완전하지 않다면 예외를 반환하고
  정보의 주소가 완전하다면 사용자의 정보를 수정한다.
  닉네임의 경우 받아 온 정보 중 닉네임이 있다면 닉네임 검증이 먼저 이루어진다.
  현재 사용중인 닉네임, 다른 사용자가 사용중인 닉네임, 닉네임 중 admin(대소문자 구분 없이) 이 포함되어 있다면 예외를 반환한다.
  정상적인 닉네임이라면 닉네임을 수정한다.
  ```
  
### 일반 사용자 - 판매자 변경 요청 전송
- Controller
  ```java
  @GetMapping("/createChangeStatusLog/{userId}")
    public ResponseEntity<String> createChangeStatusLog(@PathVariable("userId") Long userId) {
        try {
            Long logId = userService.createChangeStatusLog(userId);
            return ResponseEntity.ok().body("[" + logId + "]" + " 요청이 전송되었습니다.");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // 등록된 사용자 없음 예외
        } catch (IllegalStateException e2) {
            return createResponseEntity(e2, CONFLICT); // 이미 등록된 요청 예외
        }
    }
  ```

- Service
  ```java
  @Transactional
    public Long createChangeStatusLog(long userId) {
        User user = checkUserById(userId);
        return changeStatusLogService.createChangeStatusLog(user);
    }
  ```

- Review
  ```
  Get 통신을 통해 사용자의 고유번호를 전달받는다.
  Service 로직에서 전달받은 고유번호를 통해 사용자의 유무를 파악하고 등록된 사용자가 없다면 예외를 반환한다.
  사용자가 존재한다면 사용자의 현재 권한을 확인하고 현재 권한 변경 신청을 한 상태인지 검증한다.
  이미 신청한 상태이면 예외를 반환하고 그렇지 않다면 사용자 <-> 판매자 신청서를 생성하고 저장한다.
  자세한 내용은 ChangeStatusLogService 에서 확인
  ```

### 상품 조회
- Controller
  ```java
   @GetMapping("/searchItem")
    public ResponseEntity<?> searchItem(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable) {
        Page<SearchItemFromCommonDto> findItems = userService.searchItems(condition, sortCondition, pageable);
        return ResponseEntity.ok().body(findItems);
    }
  ```

- ItemSearchFromCommonCondition
  ```java
  @Data
  public class ItemSearchFromCommonCondition {
    private String sellerNickName;
    private String itemName;
    private Integer priceGoe;
    private Integer priceLoe;
    private Long categoryId;
  }
  ```

- SortCondition
  ```java
  @Data
  public class SortCondition {
    private String orderName1;
    private String orderDirect1;
    private String orderName2;
    private String orderDirect2;
    private String orderName3;
    private String orderDirect3;
  }
  ```

- Service
  ```java 
  public Page<SearchItemFromCommonDto> searchItems(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable) {
        return itemService.searchItemFromCommon(condition, sortCondition, pageable);
    }
  ```

- itemService.searchItemFromCommon()
  ```java
  public Page<SearchItemFromCommonDto> searchItemFromCommon(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable) {
        return itemRepository.searchItemPageFromCommon(condition, sortCondition,pageable);
  }
  ```
  
- SearchItemFromCommonDto
  ```java
  @Data
  public class SearchItemFromCommonDto {
    private SellerInfoDto seller;
    private String category;
    private String itemName;
    private int price;

    @QueryProjection
    public SearchItemFromCommonDto(SellerInfoDto seller, String category, String itemName, int price) {
        this.seller = seller;
        this.category = category;
        this.itemName = itemName;
        this.price = price;
    }
  }
  ```
- SellerInfoDto
  ```java
  @Data
  public class SellerInfoDto {
     private String sellerNickname;
     private String sellerEmail;
     private String sellerPNum;
  
     @QueryProjection
     public SellerInfoDto(String sellerNickname, String sellerEmail, String sellerPNum) {
         this.sellerNickname = sellerNickname;
         this.sellerEmail = sellerEmail;
         this.sellerPNum = sellerPNum;
     }
  }
  ```

- ItemRepositoryCustom
  ```java
  Page<SearchItemFromCommonDto> searchItemPageFromCommon(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable);
  ```

- ItemRepositoryCustomImpl
  ```java
  @Override
  public Page<SearchItemFromCommonDto> searchItemPageFromCommon(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable) {
      List<SearchItemFromCommonDto> content = queryFactory
              .select(new QSearchItemFromCommonDto(
                      new QSellerInfoDto(
                              user.nickname,
                              user.email,
                              user.pNum),
                      category.name,
                      item.name,
                      item.price
              ))
              .from(item)
              .leftJoin(item.seller, user)
              .leftJoin(item.category, category)
              .where(item.stockQuantity.goe(1),
                      itemNameEq(condition.getItemName()),
                      sellerNickNameEq(condition.getSellerNickName()),
                      priceGoe(condition.getPriceGoe()),
                      priceLoe(condition.getPriceLoe()),
                      categoryEQ(condition.getCategoryId()))
              .orderBy(createOrderSpecifier(sortCondition).toArray(OrderSpecifier[]::new))
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();

      JPAQuery<Long> countQuery = queryFactory
              .select(item.count())
              .from(item)
              .leftJoin(item.seller, user)
              .leftJoin(item.category, category)
              .where(item.stockQuantity.goe(1),
                      itemNameEq(condition.getItemName()),
                      sellerNickNameEq(condition.getSellerNickName()),
                      priceGoe(condition.getPriceGoe()),
                      priceLoe(condition.getPriceLoe()),
                      categoryEQ(condition.getCategoryId()));

      return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
  
  private BooleanExpression sellerNickNameEq(String sellerNickName) {
        return hasText(sellerNickName) ? user.nickname.like("%" + sellerNickName + "%") : null;
  }
  
  private BooleanExpression categoryEQ(Long categoryId) {
        return categoryId != null ? item.category.id.eq(categoryId) : null;
  }
  
  private BooleanExpression priceLoe(Integer priceLoe) {
        return priceLoe != null ? item.price.loe(priceLoe) : null;
  }

  private BooleanExpression priceGoe(Integer priceGoe) {
        return priceGoe != null ? item.price.goe(priceGoe) : null;
  }
  
  private BooleanExpression itemNameEq(String itemName) {
        return hasText(itemName) ? item.name.like("%"+itemName+"%") : null;
  }
  
  public List<OrderSpecifier<?>> createOrderSpecifier(SortCondition orderCondition) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        checkOrderCondition(orderSpecifiers, orderCondition.getOrderName1(), orderCondition.getOrderDirect1());
        checkOrderCondition(orderSpecifiers, orderCondition.getOrderName2(), orderCondition.getOrderDirect2());
        checkOrderCondition(orderSpecifiers, orderCondition.getOrderName3(), orderCondition.getOrderDirect3());
        return orderSpecifiers;
  }
  
  public void checkOrderCondition(List<OrderSpecifier<?>> orderSpecifiers,String orderName, String orderDirect) {
        if (hasText(orderName)) {
            if (orderName.equals("price")) {
                if (hasText(orderDirect) && orderDirect.equals("ASC")) {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, item.price));
                } else {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, item.price));
                }
            } else if (orderName.equals("name")) {
                if (hasText(orderDirect) && orderDirect.equals("ASC")) {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, item.name));
                } else {
                    orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, item.name));
                }
            }
        }
  }
  ```

- Review
  ```
  GET 통신을 통해 /searchItem?{Params} 형태로 검색조건(ItemSearchFromCommonCondition), 정렬조건(SortCondition), 페이지 정보(Pageable) 를 전달받는다.
  상품 검색은 회원이 아닌 사용자도 검색을 할 수 있기에 회원 검증 로직을 포함하지 않는다.
  검색 결과는 SearchItemFromCommonDto 정보로 이루어진 페이지이다.
  Params 가 없을때는 재고가 1개 이상인 모든 상품들을 검색한다. 
  아래는 Params 에 들어갈 수 있는 값의 종류이다.
  Params
    - sellerNickName : 판매자 닉네임
    - itemName : 상품 이름
    - priceGoe : 상품 가격(이상)
    - priceLoe : 상품 가격(이하)
    - cateGroyId : 카테고리 고유번호
    - orderName1 : 정렬조건 1
    - orderDirect1 : 1의 오름, 내림 차순
    - orderName2 : 정렬조건 2
    - orderDirect2 : 2의 오름, 내림 차순
    - orderName3 : 정렬조건 3
    - orderDirect3 : 3의 오름, 내림 차순
    - page : 페이지 번호
    - size : 한페이지에 표시할 정보의 수
  ```
  ```
  ItemRepositoryCustomImpl
  Params 의 값을 동적으로 처리하기 위하여 querydsl 을 통하여 쿼리를 작성하였다.
  
  검색조건을 통해 검색된 정보는 SearchItemFromCommonDto 로 변환되며 판매자 관련 정보(SellerInfoDto) 가 포함되어 있다.
  검색은 기본적으로 상품의 재고가 1개 이상인 제품들을 검색하며 상품이름, 판매자 닉네임, 가격범위, 카테고리 설정이 가능하다.
  각각 itemNameEq, sellerNickNameEq, [priceGoe, priceLoe], categoryEQ 메서드로 구현하였다.
  위 메서드들은 해당하는 Params 값이 있다면 쿼리의 where 절에 조건을 추가하고 없다면 null 을 반환하여 where 절에 추가하지 않는다.
  상품이름과 판매자 닉네임은 like 문을 사용하였으며, priceGoe 와 Loe 둘 다 사용하면 Between 효과를 볼 수 있다.
  
  위 조건들을 통해 검색된 정보들은 정렬조건을 통해 정렬된다.
  정렬은 createOrderSpecifier, checkOrderCondition 메서드로 구현하였으며 가격과 상품이름을 기준으로 할 수 있다.
  정렬 순서는 기본적으로 내림차순으로 정렬되며 오름차순으로 변경이 가능하다.
  정렬조건이 2개 이상일 경우 orderName 뒤 숫자의 우선순위를 가진다.
  
  검색조건과 정렬조건을 통해 필터링된 정보들은 페이지 형태를 가지며 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.
  
  위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
    - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
    - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
  ```

### 주문 조회
- Controller
  ```java
  @GetMapping("/searchOrders/{userId}")
  public ResponseEntity<?> searchOrdersForBuyer(@PathVariable("userId") Long buyerId, OrderSearchCondition condition, Pageable pageable) {
      try {
          Page<SearchOrdersForBuyerDto> content = userService.searchOrdersForBuyer(buyerId, condition, pageable);
          return ResponseEntity.ok().body(content);
      } catch (NoSuchElementException e) {
          return createResponseEntity(e, NOT_FOUND);
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
  public Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable) {
        User user = checkUserById(buyerId);
        return ordersService.searchOrdersForBuyer(buyerId, condition, pageable);
  }
  ```

- Service - searchOrdersForBuyer
  ```java
  public Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable) {
        return buyerRepository.searchOrdersForBuyer(buyerId, condition, pageable);
  }
  ```

- SearchOrdersForBuyerDto
  ```java
  @Data
  public class SearchOrdersForBuyerDto {
      private Long orderId;
      private int orderPrice;
      @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
      private LocalDateTime orderDate;
  
      @QueryProjection
      public SearchOrdersForBuyerDto(Long orderId, int orderPrice, LocalDateTime orderDate) {
          this.orderId = orderId;
          this.orderPrice = orderPrice;
          this.orderDate = orderDate;
      }
  }
  ```

- OrderSearchRepository
  ```java
  public interface OrderSearchRepository {
     Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable);
  }
  ```

- OrderSearchRepositoryImpl
  ```java
  @Override
  public Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable) {
      List<SearchOrdersForBuyerDto> content = queryFactory
              .select(new QSearchOrdersForBuyerDto(
                      ordersForBuyer.id,
                      ExpressionUtils.as(
                              JPAExpressions
                                      .select(orderItem.totalPrice.sum())
                                      .from(orderItem)
                                      .where(orderItem.buyerOrderId.eq(ordersForBuyer.id),
                                              orderItem.orderItemStatus.ne(OrderItemStatus.CANCEL)), "orderPrice"),
                      ordersForBuyer.createdDate))
              .from(ordersForBuyer)
              .where(ordersForBuyer.buyer.id.eq(buyerId),
                      orderTimeGoeForBuyer(condition.getTimeGoe()),
                      orderTimeLoeForBuyer(condition.getTimeLoe()))
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();

      JPAQuery<Long> countQuery = queryFactory
              .select(ordersForBuyer.count())
              .from(ordersForBuyer)
              .where(ordersForBuyer.buyer.id.eq(buyerId),
                      orderTimeGoeForBuyer(condition.getTimeGoe()),
                      orderTimeLoeForBuyer(condition.getTimeLoe()));

      return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }
  
  private BooleanExpression orderTimeGoeForBuyer(LocalDateTime timeGoe) {
        return timeGoe != null ? ordersForBuyer.createdDate.goe(timeGoe) : null;
    }
  
  private BooleanExpression orderTimeLoeForBuyer(LocalDateTime timeLoe) {
      return timeLoe != null ? ordersForBuyer.createdDate.loe(timeLoe) : null;
  }
  ```

- Review
  ```
  GET 통신을 통해 /searchOrders/{userId}?{Params} 형태로 사용자 고윺번호(userId), 검색조건(OrderSearchCondition), 페이지 정보(Pageable) 를 전달받는다.
  전달받은 사용자의 고유번호를 통해 사용자의 존재를 확인하고 없다면 예외를 반환한다. 사용자가 있다면 검색조건, 페이지 정보를 참고하여 결과를 반환한다.
  검색 결과는 SearchOrdersForBuyerDto 정보로 이루어진 페이지이다.
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
  검색조건을 통해 검색된 정보는 SearchOrdersForBuyerDto 로 변환된다.
  전달받은 사용자의 고유번호를 통해 사용자의 주문들을 페이지 형태로 반환한다. 이때 주문 시간 범위 설정이 가능하다.
  주문 시간 범위는 orderTimeGoeForBuyer, orderTimeLoeForBuyer 메서드로 구현하였으며
  해당하는 Params 값이 있다면 쿼리의 where 절에 조건을 추가하고 없다면 null 을 반환하여 where 절에 추가하지 않는다.
  Goe 와 Loe 둘 다 사용하면 Between 효과를 볼 수 있다.
  
  검색결과에는 주문의 고유번호, 해당 주문의 전체 가격, 주문 시간을 가지고 있다.
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
    public ResponseEntity<?> searchOrderDetail(@PathVariable("userId") Long buyerId, @PathVariable("orderId") Long orderId) {
        try {
            List<SearchOrderItemsForBuyerDto> items = userService.searchOrderDetailForBuyer(buyerId, orderId);
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
  public List<SearchOrderItemsForBuyerDto> searchOrderDetailForBuyer(Long buyerId, Long orderId) throws IllegalAccessException {
        User user = checkUserById(buyerId);
        return ordersService.searchOrderDetailForBuyer(buyerId, orderId);
  }
  ```

- Service - orderService.searchOrderDetailForBuyer
  ```java
  public List<SearchOrderItemsForBuyerDto> searchOrderDetailForBuyer(Long buyerId, Long orderId) throws IllegalAccessException {
        checkBuyerOrder(buyerId, orderId);
        return buyerRepository.searchOrderItemsForBuyer(orderId);
  }
  ```
  
- SearchOrderItemsForBuyerDto
  ```java
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public class SearchOrderItemsForBuyerDto {
      Long orderItemId;
      Long itemId;
      String sellerName;
      String itemName;
      int price;
      int count;
      int totalPrice;
      OrderItemStatus orderItemStatus;
      String cancelReason;
  
      @QueryProjection
      public SearchOrderItemsForBuyerDto(Long orderItemId, Long itemId, String sellerName, String itemName, int price, int count, int totalPrice, OrderItemStatus orderItemStatus, String cancelReason) {
          this.orderItemId = orderItemId;
          this.itemId = itemId;
          this.sellerName = sellerName;
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
    List<SearchOrderItemsForBuyerDto> searchOrderItemsForBuyer(Long orderId);
  }
  ```

- OrderSearchRepositoryImpl
  ```java
  @Override
  public List<SearchOrderItemsForBuyerDto> searchOrderItemsForBuyer(Long orderId) {
      return queryFactory
              .select(new QSearchOrderItemsForBuyerDto(
                      orderItem.id,
                      orderItem.itemId,
                      user.name,
                      orderItem.itemName,
                      orderItem.price,
                      orderItem.count,
                      orderItem.totalPrice,
                      orderItem.orderItemStatus,
                      orderItem.comment
              ))
              .from(orderItem)
              .leftJoin(user).on(orderItem.sellerId.eq(user.id))
              .where(orderItem.buyerOrderId.eq(orderId))
              .fetch();
  }
  ```

- Review
  ```
  GET 통신을 통해 /searchOrderDetail/{userId}/{orderId} 형태로 사용자 고유번호(userId), 주문 고유번호(orderId) 를 전달 받는다.
  전달받은 사용자 고유번호를 통해 사용자의 존재를 확인하고 없다면 예외를 반환한다. 사용자가 있다면 주문 고유번호를 통해 주문의 존재와 주문이 사용자의 것인지 확인한다.
  만약 주문이 없거나 사용자의 주문이 아닌 경우 예외를 반환한다. 주문이 존재하고 사용자의 주문이 맞다면 해당 주문에 대해 상세 정보를 반환한다.
  ```
  ```
  OrderSearchRepositoryImpl
  전달 받은 주문의 고유 번호를 통해 주문의 상세 정보를 반환한다. 주문의 상세 정보는 SearchOrderItemsForBuyerDto 의 형태로 반환된다.
  주문의 상세 정보는 주문상품의 고유번호, 상품의 고유번호, 판매자의 이름, 상품의 이름, 상품 가격, 주문 수량, 주문 상품의 총 가격, 주문 상품의 상태, 코멘트 를 담고있다.
  이 때 comment 는 상품을 취소 하는 이유이며 취소 상태가 아닐 경우 표시되지 않는다.
  ```

### 교환/환불 신청
- Controller
  ```java
  @PostMapping("/exchangeRefundLog/create/{userId}")
  public ResponseEntity<String> createExchangeRefundLog(@PathVariable("userId") Long buyerId, @RequestBody @Valid CreateExchangeRefundLogRequestDto request) {
      try {
          userService.createExchangeRefundLog(buyerId, request);
          return ResponseEntity.ok().body("교환/환불 신청이 전송되었습니다.");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND);
      } catch (IllegalAccessException e2) {
          return createResponseEntity(e2, NOT_ACCEPTABLE);
      } catch (IllegalStateException e3) {
          return createResponseEntity(e3, CONFLICT);
      }
  }
  ```

- CreateExchangeRefundLogRequestDto
  ```java
  @Data
  public class CreateExchangeRefundLogRequestDto {
    @NotNull(message = "주문상품 Id")
    Long orderItemId;
    @NotNull(message = "교환/환불 종류")
    ExchangeRefundStatus status;
    @NotBlank(message = "교환/환불 이유")
    String reason;
  }
  ```

- Service
  ```java
  @Transactional
  public void createExchangeRefundLog(Long userId, CreateExchangeRefundLogRequestDto request) throws IllegalAccessException {
      checkUserById(userId);// NoSuchElementException
      OrderItem orderItem = ordersService.checkBuyerOrderItem(userId, request.getOrderItemId());// IllegalAccessException
      if (!orderItem.getOrderItemStatus().equals(OrderItemStatus.DELIVERY_COMPLETE)) {
          throw new IllegalStateException("교환/환불을 신청할수 있는 상태가 아닙니다. 배송완료 후 신청 해주세요.");
      }
      exchangeRefundLogService.createExchangeRefundLog(userId, orderItem.getSellerId(), request);  // IllegalStateException
  }
  ```

- Service - exchangeRefundLogService.createExchangeRefundLog
  ```java
  public void createExchangeRefundLog(Long userId, Long sellerId, CreateExchangeRefundLogRequestDto request) {
        Optional<ExchangeRefundLog> findLog = exchangeRefundRepository.findByUserIdAndOrderItemIdAndLogStatus(userId, request.getOrderItemId(), LogStatus.WAIT);
        if (findLog.isPresent()) {
            ExchangeRefundLog exchangeRefundLog = findLog.get();
            if (exchangeRefundLog.getStatus().equals(request.getStatus())) {
                throw new IllegalStateException("이미 전송된 요청입니다.");
            } else {
                if (exchangeRefundLog.getStatus().equals(ExchangeRefundStatus.EXCHANGE)) {
                    throw new IllegalStateException("환불 신청이 전송된 주문입니다. 교환을 원하시면 환불 신청을 취소 해주세요.");
                } else if (exchangeRefundLog.getStatus().equals(ExchangeRefundStatus.REFUND)) {
                    throw new IllegalStateException("교환 신청이 전송된 주문입니다. 환불을 원하시면 교환 신청을 취소 해주세요.");
                }
            }
        }

        ExchangeRefundLog log = ExchangeRefundLog.builder()
                .sellerId(sellerId)
                .userId(userId)
                .orderItemId(request.getOrderItemId())
                .reason(request.getReason())
                .status(request.getStatus())
                .build();
        exchangeRefundRepository.save(log);
  }
  ```

- Review
  ```
  Post 통신으로 사용자 고유번호와 교환/환불 신청에 필요한 정보를 전달받는다.
  전달받은 정보 중 사용자 고유번호를 통해 사용자의 존재를 확인하고 없다면 예외를 반환한다. 사용자가 있다면 전달받은 정보 중 주문 상품 고유번호를 통해
  주문 상품의 존재와 주문 상품이 사용자의 것인지 확인한다. 주문 상품이 존재하지 않거나 사용자의 주문 상품이 아닌경우 예외를 반환한다.
  주문 상품이 존재하고 주문 상품이 사용자의 것이라면, 주문 상품의 상태를 확인한다. 주문 상품의 상태가 배송 완료 상태가 아니라면 예외를 반환한다.
  주문 상품의 상태가 배송 완료 상태라면 교환/환불 신청서를 작성한다.
  교환/환불 신청서를 작성할 때 해당 주문 상품의 대기중인 교환/환불 신청이 있는지 확인하고 이미 전송된 요청이 있다면 예외를 반환한다.
  이미 전송된 요청이 없다면 판매자 고유변호, 사용자 고유번호, 주문 상품 고유번호, 교환/환불 이유, 교환/환불 종류 를 가진 신청서를 작성하고 저장한다.
  ```
  
### 대기중인 교환/환불 신청 확인
- Controller
  ```java
  @GetMapping("/exchangeRefundLog/searchWait/{userId}/{orderItemId}")
  public ResponseEntity<?> searchWaitExchangeRefundLog(@PathVariable("userId") Long buyerId, @PathVariable("orderItemId") Long orderItemId) {
      try {
          ExchangeRefundLog exchangeRefundLog = userService.searchWaitExchangeRefundLog(buyerId, orderItemId);
          return ResponseEntity.ok().body(exchangeRefundLog);
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND);
      } catch (IllegalAccessException e2) {
          return createResponseEntity(e2, NOT_ACCEPTABLE);
      }
  }
  ```

- Service
  ```java
  public ExchangeRefundLog searchWaitExchangeRefundLog(Long userId, Long orderItemId) throws IllegalAccessException {
        checkUserById(userId); // NoSuchElementException
        ordersService.checkBuyerOrderItem(userId, orderItemId);
        return exchangeRefundLogService.searchWaitExchangeRefundLog(userId, orderItemId); // NoSuchElementException
  }
  ```

- Service - exchangeRefundLogService.searchWaitExchangeRefundLog
  ```java
  public ExchangeRefundLog searchWaitExchangeRefundLog(Long userId, Long orderItemId) {
        Optional<ExchangeRefundLog> findLog = exchangeRefundRepository.findByUserIdAndOrderItemIdAndLogStatus(userId, orderItemId, LogStatus.WAIT);
        if (findLog.isEmpty()) {
            throw new NoSuchElementException("대기중인 교환/환불 신청을 찾지 못했습니다.");
        }
        return findLog.get();
  }
  ```

- Review
  ```
  Get 통신으로 /exchangeRefundLog/searchWait/{userId}/{orderItemId} 형태로 사용자 고유번호(userId), 주문 상품 고유번호(orderItemId) 를 전달 받는다.
  전달받은 정보 중 사용자 고유번호를 통해 사용자의 존재를 확인하고 없다면 예외를 반환한다. 사용자가 있다면 전달받은 정보 중 주문 상품 고유번호를 통해
  주문 상품의 존재와 주문 상품이 사용자의 것인지 확인한다. 주문 상품이 존재하지 않거나 사용자의 주문 상품이 아닌경우 예외를 반환한다.
  주문 상품이 존재하고 주문 상품이 사용자의 것이라면 해당 주문의 대기중인 신청을 확인한다.
  대기중인 신청이 없다면 예외를 반환하고, 있다면 대기중인 신청을 반환한다.
  ```
  
### 대기중인 교환/환불 신청 취소
- Controller
  ```java
  @PostMapping("/exchangeRefundLog/cancel/{userId}")
  public ResponseEntity<String> cancelExchangeRefund(@PathVariable("userId") Long buyerId, @RequestBody @Valid cancelExchangeRefundRequestDto request) {
      try {
          userService.cancelExchangeRefund(buyerId, request.getOrderItemId(), LogStatus.CANCEL);
          return ResponseEntity.ok().body("요청이 성공적으로 취소되었습니다.");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND);
      } catch (IllegalAccessException e2) {
          return createResponseEntity(e2, NOT_ACCEPTABLE);
      }
  }
  ```

- cancelExchangeRefundRequestDto
  ```java
  @Data
  public class cancelExchangeRefundRequestDto {
    @NotNull(message = "주문상품 Id")
    Long orderItemId;
  }
  ```

- Service
  ```java
  @Transactional
  public void cancelExchangeRefund(Long userId, Long orderItemId, LogStatus logStatus) throws IllegalAccessException {
      ExchangeRefundLog exchangeRefundLog = searchWaitExchangeRefundLog(userId, orderItemId); // NoSuchElementException
      exchangeRefundLog.changeStatus(logStatus);
  }
  ```

- Review
  ```
  Post 통신을 통해 대기중인 교환/환불 신청 취소에 필요한 정보를 전달 받는다.
  전달 받은 정보중 사용자의 고유번호를 통해 사용자의 존재를 확인하고 없다면 예외를 반환한다. 사용자가 존재한다면 앞서 Review 하였던
  exchangeRefundLogService.searchWaitExchangeRefundLog 를 통해 찾은 신청서의 상태를 전달 받은 logStatus 로 변경한다.
  이때는 취소하는 상황이기에 logStatus 는 CANCEL 이 되며 해당 신청서의 처리 시간은 현재 시간으로 설정된다.
  ```