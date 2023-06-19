## 🛒 ShoppingCart ( 장바구니 )

- 장바구니 상품 추가
    - **API** : `/api/users/addShoppingCartItem/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```
        ```jsonc
        {
        "itemId" : 상품 Id,
        "itemCount" : 선택 수량
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품이름} 이(가) 장바구니에 추가 되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        존재하지 않는 상품입니다.
        ```
<br/>

- 장바구니 상품 수량 변경
    - **API** : `/api/users/changeShoppingCartItemCount/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```
        ```jsonc
        {
        "shoppingCartItemId" : 쇼핑 카트 상품 Id,
        "changCount" : 변경할 수량
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품이름} 수량 변경 완료.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        존재하지 않는 상품입니다.
        ```
        ```
        잘못된 장바구니 상품입니다.
        ```
        - 406 *NOT_ACCEPTABLE*
        ```
        사용자의 장바구니 상품이 아닙니다.
        ```
<br/>

- 장바구니 선택 상품 삭제
    - **API** : `/api/users/deleteShoppingCartItem/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```
        ```jsonc
        {
        "shoppingCartItemsIds" : 쇼핑 카트 상품 Id 들
        }
        ```

    - **Response**
        - 200 OK
        ```
        {상품이름들} 이 장바구니에서 삭제되었습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        - 409 *CONFLICT*
        ```
        잘못된 장바구니 상품 정보입니다.
        ```
<br/>

- 장바구니 비우기
    - **API** : `/api/users/deleteAllShoppingCartItem/{userId}`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```

    - **Response**
        - 200 OK
        ```
        {사용자 이름} 님의 장바구니가 비워졌습니다.
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
        ```
        장바구니에 담긴 상품이 없습니다.
        ```
<br/>

- 장바구니 전체 가격 조회
    - **API** : `/api/users/getShoppingCartTotalPrice/{userId}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```

    - **Response**
        - 200 OK
        ```
        {전체가격} 원
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
<br/>

- 장바구니 전체 상품 조회
    - **API** : `/api/users/getShoppingCartItems/{userId}?Params`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userId" : userId 를 통하여 사용자 확인
        ```
        ```jsonc
        {Params} 동적
        {
        "size" : 페이지 최대 표시 수,
        "page" : 페이지 번호
        }
        ```

    - **Response**
        - 200 OK
        ```
        Page<SearchShoppingCartDto>
        SearchShoppingCartDto
        {
            "shoppingCartItemId": 쇼핑카트 상품 Id,
            "itemName": 상품 Id,
            "itemSellerNickName": 판매자 닉네임,
            "itemCount": 쇼핑 카트 상품 수량,
            "itemPrice": 쇼핑 카트 상품 단품 가격,
            "totalItemPrice": 쇼핑 카트 상품 가격
        }
        ```
        - 404 *NOT_FOUND*
        ```
        가입되지 않은 회원입니다.
        ```
<br/>