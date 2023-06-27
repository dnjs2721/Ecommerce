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