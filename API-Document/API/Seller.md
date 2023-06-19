## 🙍🏻‍♂️ Seller ( 판매자 )

- 상품 등록
    - **API** : `/api/seller/addItem/{sellerId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "sellerId" : sellerId 를 통하여 판매자 확인
        ```
        ```jsonc
        {
        "name" : 상품 이름,
        "price" : 상품 가격,
        "stockQuantity" : 상품 재고,
        "categoryId" : 상품 카테고리
        }
        ```

    - **Response**
        - 200 OK
        ```
        상품 {상품이름} 이 등록 되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        카테고리를 다시 확인해 주세요.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
        - 409 *CONFLICT*
        ```
        이미 판매자가 판매중인 상품입니다.
        ```
<br/>

- 판매자 판매 상품 조회
    - **API** : `/api/seller/itemSearch/{sellerId}?Params`
    - **Method : GET**

    - **Request**

        ```jsonc
        "sellerId" : sellerId 를 통하여 판매자 확인
        ```
        ```jsonc
        {Params} 동적
        {
        "itemName" : 상품 이름,
        "priceGoe" : 상품 가격 이상,
        "priceLoe" : 상품 가격 이하,
        "stockQuantityGoe" : 상품 재고 이상,
        "stockQuantityLoe" : 상품 재고 이하,
        "categoryId" : 상품 카테고리
        "timeGoe" : 등록 날짜 이상,
        "timeLoe" : 등록 날짜 이하,
        "size" : 페이지 최대 표시 수,
        "page" : 페이지 번호
        }
        ```

    - **Response**
        - 200 OK
        ```
        Page<SearchItemDto>
        SearchItemDto
        {
            "itemId": 상품 Id,
            "sellerId": 판매자 Id,
            "createdDate": 생성날짜,
            "lastModifiedDate": 수정날짜,
            "name": 상품이름,
            "price": 상품가격,
            "stockQuantity": 재고,
            "category": 카테고리 이름"
        }
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
<br/>

- 상품 정보 변경
    - **API** : `/api/seller/changeItemInfo/{sellerId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "sellerId" : sellerId 를 통하여 판매자 확인
        ```
        ```jsonc
        {
        "itemId" : 상품 아이디,
        "changePrice" : 변경할 상품 가격,
        "changeStockQuantity" : 변경할 상품 재고,
        "changeCategoryId" : 변경할 상품 카테고리
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품이름} 의 정보가 변경되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        존재하지 않는 상품입니다.
        ```
        ```
        변경할 정보가 없습니다.
        ```
        ```
        카테고리를 다시 확인해 주세요.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
        ```
        판매자의 상품이 아닙니다.
        ```
<br/>

- 상품 삭제
    - **API** : `/api/seller/deleteItem/{sellerId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "sellerId" : sellerId 를 통하여 판매자 확인
        ```
        ```jsonc
        {
        "itemIds" : 상품 ID 번호
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품이름들} 이(가) 삭제되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
        - 409 *CONFLICT*
        ```
        잘못된 상품 정보입니다.
        ```
<br/>

- 주문 조회
    - **API** : `/api/seller/searchOrders/{userId}?Params`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 판매자 확인
        ```
        ```jsonc
        {Params} 동적
        {
        "status" : 상품 이름,
        "timeGoe" : 등록 날짜 이상,
        "timeLoe" : 등록 날짜 이하,
        "size" : 페이지 최대 표시 수,
        "page" : 페이지 번호
        }
        ```

    - **Response**
        - 200 OK
        ```
        Page<SearchOrdersForSellerDto>
        SearchOrdersForSellerDto
        {
            "orderId": 주문 Id,
            "buyerName": 구매자 이름,
            "buyerPNum": 구매자 전화번호,
            "buyerAddress": 구매자 주소,
            "orderPrice": 주문 가격,
            "orderDate": 주문 날짜
        }
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
<br/>

- 주문 상세 조회
    - **API** : `/api/seller/searchOrderDetail/{userId}/{orderId}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 판매자 확인
        "orderId" : orderId 를 통하여 주문 확인
        ```

    - **Response**
        - 200 OK
        ```
        List<SearchOrderItemForSellerDto> 
        SearchOrderItemForSellerDto
        {
            "orderItemId": 주문 상품 Id,
            "itemId": 상품 Id,
            "itemName": 상품 이름,
            "price": 상품 가격,
            "count": 주문 수량,
            "totalPrice": 주문 가격,
            "orderItemStatus" : 주문상품 상태,
            "cancelReason" : 취소이유
        }
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
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
        ```
        사용자의 주문이 아닙니다.
        ```
<br/>

- 주문 상품 상태 변경
    - **API** : `/api/seller/changeOrderStatus/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 판매자 확인
        ```
        ```jsonc
        {
        "orderItemId" : 주문 상품 Id,
        "orderItemStatus" : 변경할 주문 상품 상태,
        "comment" : 주문 상품 취소시 이유
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품이름} 의 주문상태가 변경되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        존재하지 않는 상품입니다.
        ```
        ```
        잘못된 주문상품번호 입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
        ```
        사용자의 주문상품이 아닙니다.
        ```
        - 409 *CONFLICT*
        ```
        해당 단계로 변경할 수 없습니다.
        ```
<br/>

- 교환/환불 신청서 확인
    - **API** : `/api/seller/searchExchangeRefundLog/{userId}?Params`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 판매자 확인
        ```
        ```jsonc
        {Params} 동적
        {
        "userId" : 상품 이름,
        "status" : 교환/환불 종류,
        "logStatus" : 신청 상태,
        "timeGoe" : 등록 날짜 이상,
        "timeLoe" : 등록 날짜 이하,
        "size" : 페이지 최대 표시 수,
        "page" : 페이지 번호
        }
        ```

    - **Response**
        - 200 OK
        ```
        Page<SearchExchangeRefundLogDto>
        SearchExchangeRefundLogDto
        {
            "createdDate": 신청 날짜,
            "logId": 신청서 Id,
            "userId": 신청자 Id,
            "orderItemId": 신청한 주문 상품 Id,
            "reason": 신청 사유,
            "status": 신청 종류,
            "logStatus" : 신청 상태,
            "processingTime" : 처리 시간
        }
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        판매자가 아닙니다. 먼저 판매자 신청을 해주세요.
        ```
<br/>