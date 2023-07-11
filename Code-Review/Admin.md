## 👨🏻‍💻 Admin

### 회원가입
- Controller
    ```java
    @PostMapping("/join")
    public ResponseEntity<String> joinAdmin(@RequestBody @Valid JoinRequestDto request) {
        try {
            Long adminId = adminService.adminJoin(request);
            return ResponseEntity.ok().body(adminId.toString() + " 회원가입 되었습니다.");
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
    ``` java
    @Transactional
    public Long adminJoin(JoinRequestDto request) {
        duplicationCheckService.validateDuplicateEmail(request.getEmail());
        duplicationCheckService.validateDuplicateNickname(request.getNickname());
        duplicationCheckService.validateDuplicatePNum(request.getPNum());
        User admin = userService.createUser(request);
        admin.setStatus(UserStatus.ADMIN);
        userRepository.save(admin);
        return admin.getId();
    }
    ```

- Review

  | 종류                              |                설명                |
  |:--------------------------------:|:-------:|
  | Controller                      | Post 통신을 통해 회원가입에 필요한 정보를 전달받는다. |
  | JoinRequestDto                  | 회원가입에 필요한 정보|
  | Service<br/> adminService.adminJoin | 전달 받은 정보 중 이메일, 닉네임, 전화번호 중복검사<br/> userService.createUser 호출 <br/> 생성된 사용자 관리자 권한 부여, 저장 |
  | Service<br/> userService.createUser | 사용자 생성 |

### 사용자 정보 조회
- Controller
  ```java
  @GetMapping("/searchUsers/{id}")
  public ResponseEntity<?> searchUsers(@PathVariable("id") Long id, UserSearchCondition condition, Pageable pageable) {
      try {
          Page<SearchUsersDto> searchUsers = adminService.searchUsers(id, condition, pageable);
          return ResponseEntity.ok().body(searchUsers);
      } catch (IllegalAccessException e) {
          return createResponseEntity(e, NOT_ACCEPTABLE); // 권한 없음 예외
      }
  }
  ```

- UserSearchCondition
  ```java
  @Data
  public class UserSearchCondition {
    private UserStatus userStatus;
  }
  ```

- SearchUsersDto
  ```java
  @Data
  public class SearchUsersDto {
    private Long id;
    private UserStatus status;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String pNum;
    private String birth;
    private Address address;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    @QueryProjection
    public SearchUsersDto(Long id, UserStatus status, String email, String password, String name, String nickname, String pNum, String birth, Address address, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.status = status;
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.pNum = pNum;
        this.birth = birth;
        this.address = address;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }
  }
  ```

- Service
  - adminService.searchUsers
    ```java
    public Page<SearchUsersDto> searchUsers(Long id, UserSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkAdmin(id);
        return userRepository.searchUsersPage(condition, pageable);
    }
    ```
  - adminService.checkAdmin
    ```java
    public void checkAdmin(Long id) throws IllegalAccessException {
        Optional<User> findAdmin = userRepository.findById(id);
        if (findAdmin.isEmpty() || !findAdmin.get().getStatus().equals(UserStatus.ADMIN)) {
            throw new IllegalAccessException("조회할 권한이 없습니다.");
        }
    }
    ```

- Repository
  - UserRepositoryCustom
    ```java
    public interface UserRepositoryCustom {
        Page<SearchUsersDto> searchUsersPage(UserSearchCondition condition, Pageable pageable);
    }
    ```
  - UserRepositoryCustomImpl
    ```java
    @Override
    public Page<SearchUsersDto> searchUsersPage(UserSearchCondition condition, Pageable pageable) {
        List<SearchUsersDto> content = queryFactory
                .select(new QSearchUsersDto(
                        user.id,
                        user.status,
                        user.email,
                        user.password,
                        user.name,
                        user.nickname,
                        user.pNum,
                        user.birth,
                        user.address,
                        user.createdDate,
                        user.lastModifiedDate))
                .from(user)
                .where(userStatEq(condition.getUserStatus()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(userStatEq(condition.getUserStatus()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    
    private BooleanExpression userStatEq(UserStatus userStatus) {
        return userStatus != null ? user.status.eq(userStatus) : null;
    }
    ```
  
- Review

  |                  종류                   |                             설명                             |
  |:----------------------------------------------------------:|:-----------------------------------------------------------|
  |              Controller               |         Get 통신을 통해 사용자 고유번호, 검색조건, 페이지 정보를 전달받는다.          |
  |          UserSearchCondition          |                            검색조건                            |
  |           SearchUsersDto              |                        페이지를 구성하는 정보                        | 
  | Service<br/> adminService.searchUsers | 사용자 고유번호를 통해 관리자 검증<br/> userRepository.searchUsersPage 호출 |
  - userRepository.searchUsersPage
    ```
    사용자의 정보를 검색한다.
    검색된 정보는 SearchUsersDto 로 변환되며 검색조건을 추가할 수 있다.
    검색조건 : UserSearchCondition.getUserStatus 사용자 권한을 기준으로 검색
    이는 userStatusEq 로 구현되었으며 검색조건이 있으면 where 절을 추가한다.
    
    검색조건을 통해 필터링 된 정보들은 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.

    위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
      - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
      - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
    ```

### 일반 사용자 - 판매자 변경 요청 검색
- Controller
  ```java
  @GetMapping("/searchChangeStatusLogs/{id}")
  public ResponseEntity<?> searchLogs(@PathVariable("id") Long id, StatusLogSearchCondition condition, Pageable pageable) {
      try {
          Page<SearchStatusLogDto> searchLogs = adminService.searchLogs(id, condition, pageable);
          return ResponseEntity.ok().body(searchLogs);
      } catch (IllegalAccessException e1) {
          return createResponseEntity(e1, NOT_ACCEPTABLE); // 권환 없음 예외
      } catch (NoSuchElementException e2) {
          return createResponseEntity(e2, NOT_FOUND);
      }
  }
  ```

- StatusLogSearchCondition
  ```java
  @Data
  public class StatusLogSearchCondition {
    private Long userId;
    private Long adminId;
    private LogStatus logStat;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeGoe;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timeLoe;
  }
  ```

- SearchStatusLogDto
  ```java
  @Data
  public class SearchStatusLogDto {
    private Long logId;
    private Long userId;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;
    private UserStatus beforeStat;
    private UserStatus requestStat;
    private LogStatus logStat;
    private Long adminId;
    private LocalDateTime processingTime;
    private String cancelReason;

    @QueryProjection
    public SearchStatusLogDto(Long logId, Long userId, LocalDateTime createdDate, LocalDateTime lastModifiedDate, UserStatus beforeStat, UserStatus requestStat, LogStatus logStat, Long adminId, LocalDateTime processingTime, String cancelReason) {
        this.logId = logId;
        this.userId = userId;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.beforeStat = beforeStat;
        this.requestStat = requestStat;
        this.logStat = logStat;
        this.adminId = adminId;
        this.processingTime = processingTime;
        this.cancelReason = cancelReason;
    }
  }
  ```

- Service
  - adminService.searchLogs
    ```java
    public Page<SearchStatusLogDto> searchLogs(Long id, StatusLogSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        checkAdmin(id);
        return changeStatusLogService.searchLogs(condition, pageable);
    }
    ```
  - changeStatusLogService.searchLogs
    ```java
    public Page<SearchStatusLogDto> searchLogs(StatusLogSearchCondition condition, Pageable pageable) {
        return changeStatusLogRepository.searchLogsPage(condition, pageable);
    }
    ```

- Repository
  - ChangeStatusLogRepositoryCustom
    ```java
    public interface ChangeStatusLogRepositoryCustom {
      Page<SearchStatusLogDto> searchLogsPage(StatusLogSearchCondition condition, Pageable pageable);
    }
    ```
  - ChangeStatusLogRepositoryCustomImpl
    ```java
    @Override
    public Page<SearchStatusLogDto> searchLogsPage(StatusLogSearchCondition condition, Pageable pageable) {
        List<SearchStatusLogDto> content = queryFactory
                .select(new QSearchStatusLogDto(
                        changeStatusLog.id,
                        changeStatusLog.userId,
                        changeStatusLog.createdDate,
                        changeStatusLog.lastModifiedDate,
                        changeStatusLog.beforeStat,
                        changeStatusLog.requestStat,
                        changeStatusLog.logStat,
                        changeStatusLog.adminId,
                        changeStatusLog.processingTime,
                        changeStatusLog.cancelReason
                ))
                .from(changeStatusLog)
                .where(userIdEq(condition.getUserId()),
                        adminIdEq(condition.getAdminId()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()),
                        stateEq(condition.getLogStat())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(changeStatusLog.count())
                .from(changeStatusLog)
                .where(userIdEq(condition.getUserId()),
                        adminIdEq(condition.getAdminId()),
                        createTimeGoe(condition.getTimeGoe()),
                        createTimeLoe(condition.getTimeLoe()),
                        stateEq(condition.getLogStat())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression stateEq(LogStatus logStat) {
        return logStat != null ? changeStatusLog.logStat.eq(logStat) : null;
    }

    // createdDate <= timeLoe -> createdDate 보다 같거나 늦은 시간
    private BooleanExpression createTimeLoe(LocalDateTime timeLoe) {
        return timeLoe != null ? changeStatusLog.createdDate.loe(timeLoe) : null;
    }

    // createdDate >= timeGoe -> createdDate 보다 같거나 빠른 시간
    private BooleanExpression createTimeGoe(LocalDateTime timeGoe) {
        return timeGoe != null ? changeStatusLog.createdDate.goe(timeGoe) : null;
    }

    private BooleanExpression adminIdEq(Long adminId) {
        return adminId != null ? changeStatusLog.adminId.eq(adminId) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? changeStatusLog.userId.eq(userId) : null;
    }
    ```

- Review

  |                       종류                        |                              설명                               |
  |:-----------------------------------------------:|:-------------------------------------------------------------:|
  |                   Controller                    |           Get 통신을 통해 사용자 고유번호, 검색조건, 페이지 정보를 전달받는다            |
  |            StatusLogSearchCondition             |                             검색조건                              |
  |               SearchStatusLogDto                |                         페이지를 구성하는 정보                          |
  |      Service<br/> adminService.searchLogs       | 사용자 고유번호를 통해 관리자 검증<br/> changeStatusLogService.searchLogs 호춣 |
  | Service<br/> changeStatusLogService.searchLogs  |         changeStatusLogRepository.searchLogsPage 호출           |

  - changeStatusLogRepository.searchLogsPage
    ```
    사용자 - 판매자 변경 요청을 검색한다.
    검색된 정보는 SearchStatusLogDto 로 변환되며 검색조건을 추가할 수 있다.
    검색조건
      1. userId : 신청한 사용자 고유번호
      2. adminId : 신청을 처리한 관리자 고유번호
      3. logStat : 신청 상태(OK, CAMCEL, WAIT)
      4. timeGoe : 신청 일(이상)
      5. timeLoe : 신청 일(이하)
    검색조건은 각각 userIdEq, adminIdEq, stateEq, createTimeGoe, createTimeLoe 메서드로 구현되었으며
    해당하는 검색조건이 있다면 where절에 추가한다.
    
    검색조건을 통해 필터링된 정보들은 page, size 로 페이지 번호와 정보의 수를 조정해 사용자에게 표시된다.
    
    위 쿼리는 PageableExecutionUtils.getPage() 를 사용하여 count 쿼리가 생략 가능한 경우 생략해서 처리한다.
      - 페이지가 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
      - 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구한다.)
    ```

### 일반 사용자 - 판매자 변경 요청 처리
- Controller
  ```java
  @PostMapping("/changeStatus/{logId}")
  public ResponseEntity<String> changeStatus(@PathVariable("logId") Long logId, @RequestBody @Valid ChangeStatusRequestDto request) {
      try {
          adminService.changeStatus(logId, request.getAdminId(), request.getStat(), request.getCancelReason());
          return ResponseEntity.ok().body("요청이 성공적으로 처리되었습니다.");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND); // 존재하지 않는 요청, 관리자, 회원 에외
      } catch (IllegalStateException e2) {
          return createResponseEntity(e2, CONFLICT); // 이미 처리된 요청
      } catch (IllegalAccessException e3) {
          return createResponseEntity(e3, NOT_ACCEPTABLE); // 권환 없음 예외
      }
  }
  ```

- ChangeStatusRequestDto
  ```java
  @Data
  public class ChangeStatusRequestDto {
    @NotNull(message = "ADMIN ID(필수)")
    Long adminId;
    @NotNull(message = "응답(필수)")
    LogStatus stat;
    String cancelReason;
  }
  ```

- Service
  - adminService.changeStatus
    ```java
    @Transactional
    public void changeStatus(Long logId, Long adminId, LogStatus stat, String reason) throws IllegalAccessException {
        ChangeStatusLog findLog = changeStatusLogService.checkChangeStatusLog(logId);
        checkAdmin(adminId);
  
        Optional<User> findUser = userRepository.findById(findLog.getUserId());
        if (findUser.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 회원의 요청입니다.");
        }
        findLog.changeStatus(findUser.get(), stat, adminId, reason);
    }
    ```
  - changeStatusLogService.checkChangeStatusLog
    ```java
    public ChangeStatusLog checkChangeStatusLog(Long logId) {
        ChangeStatusLog findLog = findLogById(logId);
        if (findLog.getLogStat().equals(OK) || findLog.getLogStat().equals(CANCEL)) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }
        return findLog;
    }
    ```

- ChangeStatus.changeStatus
  ```java
  public void changeStatus(User user, LogStatus stat, Long adminId, String cancelReason) {
      if (stat.equals(OK)) {
          user.setStatus(this.getRequestStat());
          this.logStat = OK;
      } else {
          this.logStat = CANCEL;
          this.cancelReason = Objects.requireNonNullElse(cancelReason, "취소");
      }
      this.adminId = adminId;
      this.processingTime = LocalDateTime.now();
  }
  ```

- Review

  |                            종류                            |                        설명                         |
  |:-------------------------------------------------:|:--------------------------------------------------:|
  |                        Controller                        |  Post 통신을 통해 처리할 요청 고유번호와 요청 처리에 필요한 정보를 전달받는다.   |
  |                  ChangeStatusRequestDto                  |                   요청처리에 필요한 정보                    |
  | Service<br/> changeStatusLogService.checkChangeStatusLog |       전달받은 요청 고유번호를 통해 요청의 존재와 처리 유무를 검증한다.       |
  |          Service<br/> adminService.changeStatus          | 전달받은 adminId 를 통해 관리자 검증<br/> 요청한 사용자의 존재 검증<br/> |
  |                ChangeStatus.changeStatus                 | 요청상태를 전달받은 stat 으로 변경하고 처리시간과 처리한 관리자 고유번호를 기록한다. |

### 카테고리 생성
- Controller
  ```java
  @PostMapping("/createCategory/{adminId}")
  public ResponseEntity<String> createCategory(@PathVariable("adminId") Long id, @RequestBody @Valid CategoryCreateRequestDto request) {
      try {
          adminService.createCategory(id, request);
          return ResponseEntity.ok().body(request.getName() + " 카테고리가 생성 되었습니다.");
      } catch (IllegalAccessException e1) {
          return createResponseEntity(e1, NOT_ACCEPTABLE); // IllegalAccessException 권한 없음
      } catch (NoSuchElementException e2) {
          return createResponseEntity(e2, NOT_FOUND); // NoSuchElementException 부모 카테고리 없음
      } catch (IllegalStateException e3) {
          return createResponseEntity(e3, CONFLICT); // IllegalStateException 중복된 카테고리 이름
      }
  }
  ```

- CategoryCreateRequestDto
  ```java
  @Data
  public class CategoryCreateRequestDto {
    @NotBlank(message = "카테고리 이름(필수)")
    String name;
    Long parentId;
  }
  ```

- Service
  - adminService.createCategory
    ```java
    @Transactional
    public void createCategory(Long adminId, CategoryCreateRequestDto request) throws IllegalAccessException {
        checkAdmin(adminId); // IllegalAccessException 권한 없음
        categoryService.createCategory(request); //NoSuchElementException 부모 카테고리 없음, IllegalStateException 중복된 카테고리 이름
    }
    ```
  
  - categoryService.createCategory
    ```java
    public void createCategory(CategoryCreateRequestDto request) {
        checkDuplicateCategory(request.getName()); // IllegalStateException 중복된 카테고리 이름
        Category category = new Category(request.getName());
        if (request.getParentId() != null) {
            Category parentCategory = checkCategory(request.getParentId()); // NoSuchElementException 부모 카테고리 없음
            category.addParentCategory(parentCategory);
        }
        categoryRepository.save(category);
    }
    ```
  
  - categoryService.checkDuplicateCategory
    ```java
    public void checkDuplicateCategory(String categoryName) {
        Optional<Category> findCategory = categoryRepository.findByName(categoryName);
        if (findCategory.isPresent()) {
            throw new IllegalStateException("이미 존재하는 카테고리 이름입니다.");
        }
    }
    ```
  
  - categoryService.checkCategory
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

  |     종류     |                   상세                   |                                            설명                                             |
  |:----------:|:--------------------------------------:|:-----------------------------------------------------------------------------------------:|
  | Controller |  POST<br/> /createCategory/{adminId}   |                       Post 통신을 통해 사용자 고유번호와 카테고리 생성에 필요힌 정보를 전달받는다.                       |
  |    Dto     |        CategoryCreateRequestDto        |    카테고리 생성에 필요한 정보<br/> [카테고리 이름, 부모 카테고리 고유번호]<br/> 부모 카테고리 고유번호가 없을 경우 최상위 카테고리로 생성     |
  |  Service   |      adminService.createCategory       |              전달받은 사용자 고유번호를 통해 관리자 검증<br/> categoryService.createCategory 호출              |
  |  Service   | categoryService.checkDuplicateCategory |                                  동일한 이름을 가진 카테고리가 있는지 검증                                  |
  |  Service   |     categoryService.checkCategory      |                                전달받은 부모 카테고리 고유번호가 유효한지 검증                                 |
  |  Service   |    categoryService.createCategory      | 카테고리 중복 검증 후 카테고리를 생성한다.<br/> 만약 부모 카테고리 정보가 전달되었다면 부모 카테고리 검증 후 생성한 카테고리의 부모를 설정하고 저장한다. |

### 카테고리 내 상품 조회 (카테고리 삭제를 위한 기능)
- Controller
  ```java
  @GetMapping("/checkCategoryItem/{adminId}/{categoryId}")
  public ResponseEntity<?> checkCategoryItem(@PathVariable("adminId") Long adminId, @PathVariable("categoryId") Long categoryId) {
      try {
          List<CategoryItemDto> find = adminService.checkCategoryItem(adminId, categoryId);
          return ResponseEntity.ok().body(find);
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND); // NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
      } catch (IllegalAccessException e2) {
          return createResponseEntity(e2, NOT_ACCEPTABLE); // IllegalAccessException 권한 없음
      }
  }  
  ```

- CategoryItemDto
  ```java
  @Data
  public class CategoryItemDto {
      Long sellerId;
      String sellerName;
      String sellerEmail;
      String categoryName;
      Long itemId;
      String itemName;
  
      @QueryProjection
      public CategoryItemDto(Long sellerId, String sellerName, String sellerEmail, String categoryName, Long itemId, String itemName) {
          this.sellerId = sellerId;
          this.sellerName = sellerName;
          this.sellerEmail = sellerEmail;
          this.categoryName = categoryName;
          this.itemId = itemId;
          this.itemName = itemName;
      }
  } 
  ```

- Service
  - adminService.checkCategoryItem
    ```java
    public List<CategoryItemDto> checkCategoryItem(Long adminId, Long categoryId) throws IllegalAccessException {
        checkAdmin(adminId); // IllegalAccessException 권한 없음
        Category category = categoryService.checkCategory(categoryId);
        return categoryService.checkCategoryItem(category); // NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
    }
    ```
  
  - categoryService.checkCategoryItem
    ```java
    public  List<CategoryItemDto> checkCategoryItem(Category category) {
        List<CategoryItemDto> categoryItems = categoryRepository.categoryItem(category.getId());
        // sellerId, sellerName, sellerEmail
        // categoryName
        // itemId, itemName
        // 이 담긴 Dto 를 상품 갯수만큼의 size 를 가진 List 로 반환

        if (!categoryItems.isEmpty()) {
            return categoryItems;
        } else {
            throw new NoSuchElementException("카테고리에 등록된 상품이 없습니다.");  // 자신, 자식 모두 등록된 상품이 없다면
        }
    }
    ```
  
- Repository
  - CategoryRepositoryCustom
    ```java
    public interface CategoryRepositoryCustom {
        List<CategoryItemDto> categoryItem(Long categoryId);
    }
    ```
  - CategoryRepositoryCustomImpl
    ```java
    @Override
    public List<CategoryItemDto> categoryItem(Long categoryId) {
        return queryFactory
                .select(new QCategoryItemDto(
                        user.id,
                        user.name,
                        user.email,
                        category.name,
                        item.id,
                        item.name
                ))
                .from(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.category.id.eq(categoryId).or(item.category.parent.id.eq(categoryId)))
                .orderBy(user.id.asc())
                .fetch();
    }
    ```

- Review

  |     종류     |                         상세                         |                                              설명                                               |
  |:----------:|:---------------------------------------------------------------------------------------------:|:----------------------------------------------------------------------:|
  | Controller | GET<br/> /checkCategoryItem/{adminId}/{categoryId} |                          GET 통신을 통해 사용자 고유번호와 카테고리 고유번호를 전달받는다.<br/>                          |
  |    Dto     |                  CategoryItemDto                   |            카테고리 내 상품 정보<br/> [판매자 고유번호, 판매자 이름, 판매가 이메일, 카테고리 이름, 상품 고유번호, 상품 이름]             |
  |  Service   |           adminService.checkCategoryItem           | 사용자 고유번호를 통해 관리자 검증<br/>  카테고리 고유번호를 통해 카테고리 존제 검증 <br/> categoryService.checkCategoryItem 호출 |
  |  Service   |         categoryService.checkCategoryItem          |                              categoryRepository.categoryItem 호출                               |
    
  - categoryRepository.categoryItem
    ```
    상품들이 속한 카테고리의 고유번호 혹은 속한 카테고리의 부모 카테고리 고유번호가 전달받은 카테고리 고유번호와 같은 상품들을 검색한다.
    검색된 정보는 CategoryItemDto 로 변환되며 판매자의 고유번호를 기준으로 오름차순 정렬된다.
    ```

### 카테고리 내 상품의 판매자들에게 경고 메일 전송
- Controller
  ```java
  @GetMapping("/sendMailCategoryWarning/{adminId}/{categoryId}")
    public ResponseEntity<?> sendMailCategoryWarning(@PathVariable("adminId") Long adminId, @PathVariable("categoryId") Long categoryId) throws MessagingException {
        try {
            List<String> sellerNames = adminService.sendMailByCategoryItem(adminId, categoryId);
            return ResponseEntity.ok().body(sellerNames.toString() + " 에게 메일 전송 완료");
        } catch (NoSuchElementException e1) {
            return createResponseEntity(e1, NOT_FOUND); // NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
        } catch (IllegalAccessException e2) {
            return createResponseEntity(e2, NOT_ACCEPTABLE); // IllegalAccessException 권한 없음
        }
    }
  ```

- Service
  - adminService.sendMailByCategoryItem
    ```java
    @Transactional(readOnly = true)
    public List<String> sendMailByCategoryItem(Long adminId, Long categoryId) throws IllegalAccessException, MessagingException {
        checkAdmin(adminId);
        Category category = categoryService.checkCategory(categoryId);
        List<CategoryItemMailElementDto> elementDto = categoryService.categoryItemMailElement(categoryId);

        List<String> sellerNames = new ArrayList<>();
        for (CategoryItemMailElementDto element : elementDto) {
            String sellerName = element.getSellerName();
            mailService.sendCategoryWarningMail(element.getSellerEmail(), category.getName(), sellerName, element.getItemsName());
            sellerNames.add(sellerName);
        }

        return sellerNames;
    }
    ```
  - categoryService.categoryItemMailElement
    ```java
    public List<CategoryItemMailElementDto> categoryItemMailElement(Long categoryId) {
        List<CategoryItemMailElementDto> elementDto = categoryRepository.categoryItemMailElement(categoryId);
        if (!elementDto.isEmpty()) {
            return elementDto;
        } else {
            throw new NoSuchElementException("카테고리에 등록된 상품이 없습니다.");  // 자신, 자식 모두 등록된 상품이 없다면
        }
    }
    ```

- CategoryItemMailElementDto
  ```java
  @Data
  public class CategoryItemMailElementDto {
      Long sellerId;
      String sellerName;
      String sellerEmail;
      List<String> itemsName = new ArrayList<>();
  
      @QueryProjection
      public CategoryItemMailElementDto(Long sellerId, String sellerName, String sellerEmail, List<String> itemsName) {
          this.sellerId = sellerId;
          this.sellerName = sellerName;
          this.sellerEmail = sellerEmail;
          this.itemsName = itemsName;
      }
  }
  ```

- Repository
  - CategoryRepositoryCustom
    ```java
    public interface CategoryRepositoryCustom {
        List<CategoryItemMailElementDto> categoryItemMailElement(Long categoryId);
    }
    ```
  - CategoryRepositoryCustomImpl
    ```java
    public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public CategoryRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }
    
    @Override
    public List<CategoryItemMailElementDto> categoryItemMailElement(Long categoryId) {
        return queryFactory
                .selectFrom(item)
                .leftJoin(item.seller, user)
                .leftJoin(item.category, category)
                .where(item.category.id.eq(categoryId).or(item.category.parent.id.eq(categoryId)))
                .orderBy(user.id.asc())
                .transform(groupBy(user.id).list(new QCategoryItemMailElementDto(
                        user.id,
                        user.name,
                        user.email,
                        list(Projections.constructor(String.class, item.name))
                )));
        }   
    }
    ```

- Review

  |     종류     |                            상세                             |                                                            설명                                                             |
  |:----------:|:---------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------:|
  | Controller | GET<br/> /sendMailCategoryWarning/{adminId}/{categoryId}  |                                        GET 통신을 통해 사용자 고유번호와 카테고리 고유번호를 전달받는다.<br/>                                        |
  |    Dto     |                CategoryItemMailElementDto                 |                                카테고리 내 판매자별 상품 정보<br/> [판매자 고유번호, 판매자 이름, 판매가 이메일, 상품 이름들]                                 |
  |  Service   |            adminService.sendMailByCategoryItem            | 사용자 고유번호를 통해 관리자 검증<br/>  카테고리 고유번호를 통해 카테고리 존제 검증 <br/> categoryService.categoryItemMailElement 호출<br/> 판매자 단위로 경고 메일 전송 |
  |  Service   |             categoryService.checkCategoryItem             |                               categoryRepository.categoryItemMailElement 호출 <br/> 카테고리에 등록된 상품이 없을 경우 예외                  |

  - categoryRepository.categoryItemMailElement
    ```
    전달받은 카테고리에 속한 상품을 검색한다.
    querydsl 의 trasform groupBy 기능을 이용하여 판매자 고유번호를 기준으로 판매자 정보와 해당 카테고리 내의 판매자 상품들의 이름을 반환한다.
    ```
  - querydsl 의 transform
    ```
    결과 값을 불러온 후 메모리에서 원하는 자료형으로 변환할 수 있는 기능
    SpringBoot 3 버전 부터는 JPQLTemplates.DEFAULT 설정이 필요하다.
    ```

### 카테고리 내 상품 카테고리 일괄 변경 후 메일 발송
- Controller
  ```java
  PostMapping("/batchChangeItemCategory/{adminId}")
  public ResponseEntity<?> batchChangeItemCategory(@PathVariable("adminId") Long adminId, @RequestBody @Valid BatchChangeItemCategoryRequestDto request) throws MessagingException {
      try {
          Category category = categoryService.checkCategory(request.getCategoryId());
          Category changeCategory = categoryService.checkCategory(request.getChangeCategoryId());
          List<String> changeItemList = adminService.batchChangeItemCategory(adminId, category, changeCategory);
          return ResponseEntity.ok().body(changeItemList.toString() + "\n[" + category.getName() + "] 에서 [" + changeCategory.getName() + "] 로 변경 완료");
      } catch (NoSuchElementException e1) {
          return createResponseEntity(e1, NOT_FOUND); // NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때, 카테고리가 없을 때
      } catch (IllegalAccessException e2) {
          return createResponseEntity(e2, NOT_ACCEPTABLE); // IllegalAccessException 권한 없음
      }
  }
  ```

- BatchChangeItemCategoryRequestDto
  ```java
  @Data
  public class BatchChangeItemCategoryRequestDto {
      @NotNull(message = "기존 카테고리 Id (필수)")
      Long categoryId;
  
      @NotNull(message = "변경할 카테고리 Id (필수)")
      Long changeCategoryId;
  }
  ```

- Service
  - adminService.batchChangeItemCategory
    ```java
    @Transactional
    public List<String> batchChangeItemCategory(Long adminId, Category category, Category changeCategory) throws IllegalAccessException, MessagingException {
        // 관리자 권한 확인 IllegalAccessException 권한 없음,
        checkAdmin(adminId);
        //NoSuchElementException 자신, 자식 모두 등록된 상품이 없을때
        List<CategoryItemMailElementDto> elementDto = categoryService.categoryItemMailElement(category.getId());
        itemService.batchChangeItemCategory(category.getId(), changeCategory.getId());

        List<String> itemNames = new ArrayList<>();
        for (CategoryItemMailElementDto element : elementDto) {
            String sellerName = element.getSellerName();
            List<String> elementItemsName = element.getItemsName();
            // 판매자에게 경고 메일 전송
            mailService.sendCategoryNoticeMail(element.getSellerEmail(), category.getName(), changeCategory.getName(), sellerName, elementItemsName);
            itemNames.addAll(elementItemsName);
        }

        return itemNames;
    }
    ```
  - itemService.batchChangeItemCategory
    ```java
    public void batchChangeItemCategory(Long categoryId, Long changeCategoryId) {
        itemRepository.batchUpdateItemCategory(categoryId, changeCategoryId);
    }
    ```

- Repository
  - ItemRepositoryCustom
    ```java
    public interface ItemRepositoryCustom {
      void batchUpdateItemCategory(Long beforeCategoryId, Long changeCategoryId);
    }
    ```
  - ItemRepositoryCustomImpl
    ```java
    @Override
    public void batchUpdateItemCategory(Long categoryId, Long changeCategoryId) {
        List<Long> itemIds = queryFactory
                .select(item.id)
                .from(item)
                .leftJoin(item.category, category)
                .where(category.id.eq(categoryId).or(category.parent.id.eq(categoryId)))
                .fetch();

        queryFactory
                .update(item)
                .set(item.category.id, changeCategoryId)
                .where(item.id.in(itemIds))
                .execute();
    }
    ```

- Review

|     종류     |                      상세                      |                                                        설명                                                         |
|:----------:|:--------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|
| Controller | POST<br/> /batchChangeItemCategory/{adminId} |               POST 통신을 통해 사용자 고유번호와 카테고리 변경에 필요한 정보를 전달받는다.<br/>    카테고리 고유번호를 통해 카테고리 존제 검증                      |
|    Dto     |      BatchChangeItemCategoryRequestDto       |                                카테고리 변경에 필요한 정보<br/> [변경전 카테고리 고유번호, 변경후 카테고리 고유번호]                                |
|  Service   |     adminService.batchChangeItemCategory     | 사용자 고유번호를 통해 관리자 검증<br/> 변경전 카테고리에 속한 상품을 검색한다.<br/> itemService.batchChangeItemCategory 호출<br/> 판매자 단위로 안내 메일 전송 |
|  Service   |     itemService.batchChangeItemCategory      |                                     itemRepository.batchUpdateItemCategory 호출                                     |

  - itemRepository.batchUpdateItemCategory
    ```
    변경전 카테고리에 속한 상품들의 고유번호를 검색한다.
    해당 상품들의 카테고리를 변경후 카테고리로 변경한다.
    ```