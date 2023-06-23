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
- Service
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
- Review
    ```
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

  