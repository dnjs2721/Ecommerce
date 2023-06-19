## 🙍🏻‍ User ( 사용자 )

- 회원가입
    - **API** : `/api/users/join`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```jsonc
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
        ```jsonc
        {id} 회원가입 되었습니다.
        ```
        - 409 *CONFLICT*
        ```jsonc
        이미 가입된 이메일입니다.
        ```
        ```jsonc
        사용할 수 없는 닉네임입니다.
        ```
        ```jsonc
        다른 사용자가 사용중인 닉네임입니다.
        ```
        ```jsonc
        이미 등록된 휴대폰 번호입니다.
        ```
<br/>

- 로그인
    - **API** : `/api/users/login`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```jsonc
    {
      "email": user의 로그인 ID에 해당,
      "password": 비밀번호
    }
    ```
  
    - **Response**
      - 200 OK
      ```jsonc
      {id} 로그인 성공.
      ``` 
      - 401 *UNAUTHORIZED*
      ```jsonc
      잘못된 패스워드 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      가입되지 않은 이메일 입니다.
      ```
<br/>

- 아이디(이메일) 찾기
    - **API** : `/api/users/findEmail`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```jsonc
    {
      "name": 사용자 이름,
      "pnum": 사용자 전화번호
    }
    ```
  
    - **Response**
      - 200 OK
      ```jsonc
      {name}의 아이디(이메일)은 {email} 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      가입되지 않은 회원 입니다. 이름 혹은 전화번호를 확인 해 주세요.
      ```
<br/>

- 비밀번호 변경
    - **API** : `/api/users/changePassword`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```jsonc
    {
      "email" : 사용자 email,
      "password" : 기존 비밀번호,
      "newPassword" : 새로운 비밀번호
    }
    ```

    - **Response**
      - 200 OK
      ```jsonc
      {email} 님의 비밀번호가 성공적으로 변경 되었습니다.
      ```
      - 401 *UNAUTHORIZED*
      ```jsonc
      잘못된 패스워드 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      가입되지 않은 이메일 입니다.
      ```
      - 409 *CONFLICT*
      ```jsonc
      현재 사용중인 패스워드와 같습니다.
      ```
<br/>

- 회원탈퇴
    - **API** : `/api/users/deleteUser`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```jsonc
    {
      "email": 사용자 이메일,
      "password": 패스워드
    }
    ```

    - **Response**
      - 200 OK
      ```jsonc
      {name} 님 정상적으로 회원탈퇴 되었습니다.
      ```
      - 401 *UNAUTHORIZED*
      ```jsonc
      잘못된 패스워드 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      가입되지 않은 이메일 입니다.
      ```
<br/>

- 사용자 정보 변경
    - **API** : `/api/users/changeUserInfo`
    - **Method : POST**
    - **Body :  raw (json)**
    - **Request**

    ```jsonc
    {
      "email": 사용자 이메일,
      "password": 패스워드,
      "nickname": 닉네임, -> 미입력시 변경 안함, 중복체크, 예외체크
      "region" : 시/도, -> 미입력시 변경 안함, 주소 항목 미비시 예외
      "city": 읍/면/구,
      "street": 도로명,
      "detail": 상세주소,
      "zipcode": 우편번호
    }
    ```

    - **Response**
      - 200 OK
      ```jsonc
      정보를 성공적으로 변경하였습니다.
      ```
      - 401 *UNAUTHORIZED*
      ```jsonc
      잘못된 패스워드 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      가입되지 않은 이메일 입니다.
      ```
      - 409 *CONFLICT*
      ```jsonc
      현재 사용중인 닉네임입니다.
      ```
      ```jsonc
      사용할 수 없는 닉네임입니다.
      ```
      ```jsonc
      잘못된 주소형태 입니다.
      ```
<br/>

- COMMON-SELLER, SELLER-COMMON 변경 요청 작성
    - **API** : `/api/users/createChangeStatusLog/{userId}`
    - **Method : GET**
    - **Request**
  
      ```jsonc
      "userId" : userId 를 통하여 중복 요청 체크
      ```

    - **Response**
      - 200 OK
      ```jsonc
      {logId} 요청이 전송되었습니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      가입되지 않은 회원입니다.
      ```
      - 409 *CONFLICT*
      ```jsonc
      {logId} 이미 전송된 요청입니다.
      ```
<br/>

- 상품 조회
  - **API** : `/api/users/searchItem/?Params`
  - **Method : GET**
  - **Request**

      ```jsonc
      {Params} 동적
      {
        orderName1 : price or name
        orderDirect1 : ASC or DESC
        orderName2 : price or name
        orderDirect2 : ASC or DESC
        priceGoe : 가격범위 이상
        priceLoe : 가격범위 이하
        sellerNickName : 판매자 닉네임
        itemName : 상품 이름
        categoryId : 카테고리
        size : 페이지 최대 표시 수
        page : 페이지 번호
      }
      ```

  - **Response**
      - 200 OK
      ```
      Page
      ```
      - 404 *NOT_FOUND*
      ```
      가입되지 않은 회원입니다.
      ```
<br/>

- 주문 조회
    - **API** : `/api/users/searchOrders/{userId}`
    - **Method : GET**
  
    - **Request**
     
        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```
        ```jsonc
        {Params} 동적
        {
        status : 주문 상태
        timeGoe : 주문 날짜 이상
        timeLoe : 주문 날짜 이하
        size : 페이지 최대 표시 수
        page : 페이지 번호
        }
        ```

    - **Response**
        - 200 OK
        ```
        Page
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
<br/>

- 주문 상세 조회
    - **API** : `/api/users/searchOrderDetail/{userId}/{orderId}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        "orderId" : orderId 를 통하여 주문 존재, 사용자의 주문인지 확인
        ```

    - **Response**
        - 200 OK
        ``` jsonc
        List<SearchOrderItemsForBuyerDto>
        [
            {
            "orderItmeId" : 주문 상품 번호,
            "itemId" : 상품 번호,
            "sellerName" : 판매자 이름,
            "itemName" : 상품 이름,
            "price" : 상품 가격,
            "count" : 주문한 상품 수량,
            "totalPrice" : 주문 상품 총 가격
            "orderItemStatus" : 주문 상품 상태
            }
        ]
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        잘못된 주문번호 입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        사용자의 주문이 아닙니다.
        ```
<br/>

- 교환/환불 신청서 생성
    - **API** : `/api/users/exchangeRefundLog/create/{userId}`
    - **Method : POST**
    - **Body :  raw (json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        {
        "orderItemId" : 주문상품 id,
        "status" : 교환/환불 종류 "REFUND" or "EXCHANGE",
        "reason" : 교환/환불 이유
        }
        ```

    - **Response**
        - 200 OK
        ```
        교환/환불 신청이 전송되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        잘못된 주문상품번호 입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        사용자의 주문상품이 아닙니다.
        ```
        - 409 *CONFLICT*
        ```
        교환/환불을 신청할수 있는 상태가 아닙니다. 배송완료 후 신청 해주세요.
        ```
        ```
        이미 전송된 요청입니다.
        ```
        ```
        환불 신청이 전송된 주문입니다. 교환을 원하시면 환불 신청을 취소 해주세요.
        ```
        ```
        교환 신청이 전송된 주문입니다. 환불을 원하시면 교환 신청을 취소 해주세요.
        ```
<br/>

- 대기중인 교환/환불 신청 취소
    - **API** : `/api/users/exchangeRefundLog/cancel/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```
        ```jsonc
        {
        "orderItemId" : 주문상품 Id
        }
        ```

    - **Response**
        - 200 OK
        ```
        요청이 성공적으로 취소되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        잘못된 주문상품번호 입니다.
        ```
        ```
        대기중인 교환/환불 신청을 찾지 못했습니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        사용자의 주문상품이 아닙니다.
        ```
<br/>