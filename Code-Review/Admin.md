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

  |                            종류                            |                         설명                         |
  |:--------------------------------------------------------:|:--------------------------------------------------:|
  |                        Controller                        |   Post 통신을 통해 처리할 요청 고유번호와 요청 처리에 필요한 정보를 전달받는다.   |
  |                  ChangeStatusRequestDto                  |                    요청처리에 필요한 정보                    |
  | Service<br/> changeStatusLogService.checkChangeStatusLog |       전달받은 요청 고유번호를 통해 요청의 존재와 처리 유무를 검증한다.        |
  |          Service<br/> adminService.changeStatus          | 전달받은 adminId 를 통해 관리자 검증<br/> 요청한 사용자의 존재 검증<br/>  |
  |                ChangeStatus.changeStatus                 | 요청상태를 전달받은 stat 으로 변경하고 처리시간과 처리한 관리자 고유번호를 기록한다.  |