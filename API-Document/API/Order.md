## 💳 Order ( 주문 )

- 장바구니 전체 상품 주문
    - **API** : `/api/order/allItemAtShoppingCart/{userId}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통해 사용자, 장바구니 체크
        ```

    - **Response**
        - 200 OK
        ```
        상품 {상품들 이름} 이 주문되었습니다. 상태(결제대기)
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        장바구니에 담긴 상품이 없습니다.
        ```
        - 409 *CONFLICT*
        ```jsonc
        {상품 이름} 의 재고가 부족합니다.
        ```
<br/>

- 장바구니 상품 선택 주문
    - **API** : `/api/order/selectItemAtShoppingCart/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**
 
        ```jsonc
        "userId" : userId 를 통해 사용자, 장바구니 체크
        ```
        ```jsonc
        {
            "shoppingCartItemIds" : 쇼핑카드 상품 Id 들
        }
        ```

    - **Response**
        - 200 OK
        ```
        상품 {상품들 이름} 이 주문되었습니다. 상태(결제대기)
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        장바구니에 담긴 상품이 없습니다.
        ```
        - 409 *CONFLICT*
        ```
        잘못된 장바구니 상품 정보입니다.
        ```
        ```
        {상품 이름} 의 재고가 부족합니다.
        ```
<br/>

- 단건 주문
    - **API** : `/api/order/singleItem/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통해 사용자 체크
        ```
        ```jsonc
        {
            "itemId" : 상품 Id,
            "itemCount" : 상품 수량
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품 이름} 이 주문되었습니다. 상태(결제대기)
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        존재하지 않는 상품입니다.
        ```
        - 409 *CONFLICT*
        ```
        {상품 이름} 의 재고가 부족합니다.
        ```
<br/>