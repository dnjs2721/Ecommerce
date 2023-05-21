## 🙍🏻‍♂️ Admin ( 관리자 )


- 회원가입
    - **API** : `/api/admin/join`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```json
    {
      "name" : 이름,
      "nickname": 닉네임,
      "email": user의 로그인 ID에 해당,
      "password": 비밀번호,
      "pnum": 전화번호,
      "birth" : 생일,
      "region" : 시/도,
      "city": 읍/면/구,
      "street": 도로명,
      "detail": 상세주소,
      "zipcode": 우편번호
    }
    ```

    - **Response**
        - 200 OK
        ```json
        {id} 회원가입 되었습니다.
        ```
        - 409 *CONFLICT*
        ```json
        이미 가입된 이메일입니다.
        ```
        ```json
        다른 사용자가 사용중인 닉네임입니다.
        ```
        ```json
        이미 등록된 휴대폰 번호입니다.
        ```
<br/>

- 사용자 조회
    - **API** : `/api/admin/searchUsers/{id}?Params`
    - **Method : GET**
    - **Request**
    ```json
    "id" : id 를 통해 관리자인지 체크
    ```
    ```Params
    {Params} 동적
    {
      userStatus : COMMON, SELLER, ADMIN 검색 가능
      size : 페이지 최대 표시 수
      page : 페이지 번호
    }
    ```
    - **Response**
      - 200 OK
      ```json
      Page
      ```
      - 406 *NOT_ACCEPTABLE*
      ```json
      조회할 권한이 없습니다.
      ```
<br/>

- COMMON-SELLER, SELLER-COMMON 변경 요청 로그 검색
    - **API** : `/api/admin/searchChangeStatusLogs/{id}?Params`
    - **Method : GET**
    - **Request**
    ```json
    "id" : id 를 통해 관리자인지 체크
    ```
    ```Params
    {Params} 동적
    {
      userId : 요청한 사용자의 Id
      adminId : 요청을 처리한 관리자의 Id
      timeGoe : 크거나 같은 요청시간
      timeLoe : 작거나 같은 요청시간
      logStat : 처리 상태 -> WAIT, CANCLE, OK 검색 가능
      size : 페이지 최대 표시 수
      page : 페이지 번호
    }
    ```
    - **Response**
      - 200 OK
      ```json
      Page
      ```
      - 406 *NOT_ACCEPTABLE*
      ```json
      조회할 권한이 없습니다.
      ```
<br/>

- COMMON-SELLER, SELLER-COMMON 변경
    - **API** : `/api/admin/changeStatus/{logId}`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**
    ```json
    "logId" 를 통해 요청 선택
    ```
    ```json
    {
      "adminId" : 관리자 Id,
      "stat" : 처리 종류 -> OK, CANCEL
    }
    ```

    - **Response**
      - 200 OK
      ```json
      요청이 성공적으로 처리되었습니다.
      ```
      - 404 *NOT_FOUND*
      ```json
      존재하지 않는 요청입니다.
      ```
      ```json
      존재하지 않는 회원의 요청입니다.
      ```
      ```json
      존재하지 않는 관리자입니다.
      ```
      - 406 *NOT_ACCEPTABLE*
      ```json
      조회할 권한이 없습니다.
      ```
      - 409 *CONFLICT*
      ```json
      이미 처리된 요청입니다.
      ```