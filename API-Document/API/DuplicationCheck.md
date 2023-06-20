## ☑️ DuplicationCheck ( 중복확인 )

- 이메일 중복 체크
    - **API** : `/api/users/check/duplicationEmail/{userEmail}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "userEmail" : Email 을 통하여 중복 체크
        ```

    - **Response**
        - 200 OK
        ```
        {userEmail} 은 사용가능한 이메일입니다.
        이메일 인증을 해주세요.
        ```
        - 409 *CONFLICT*
        ```jsonc
        이미 가입된 이메일입니다.
        ```
<br/>


- 닉네임 중복 체크
    - **API** : `/api/users/check/duplicationNickname/{nickname}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "nickname" : nickname 을 통하여 중복 체크
        ```

    - **Response**
        - 200 OK
        ```
        {nickname} 은 사용가능한 닉네임입니다.
        ```
        - 409 *CONFLICT*
        ```jsonc
        다른 사용자가 사용중인 닉네임입니다.
        ```
<br/>

- 전화번호 중복 체크
    - **API** : `/api/users/check/duplicationPNum/{pNum}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "pNum" : pNum 을 통하여 중복 체크
        ```

    - **Response**
        - 200 OK
        ```
        {pNum} 은 사용가능한 번호입니다.
        전화번호 인증을 해주세요.
        ```
        - 409 *CONFLICT*
        ```jsonc
        이미 등록된 휴대폰 번호입니다.
        ```
<br/>