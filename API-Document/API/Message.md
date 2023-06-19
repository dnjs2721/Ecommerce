## ✉️ message ( 메시지 )

- 메시지 전송
    - **API** : `/api/mail/sendMessage/{pNum}`
    - **Method : GET**

    - **Request**

        ```jsonc
        "pNum" : pNum 에게 랜덤 인증코드 발송
        ```

    - **Response**
        - 200 OK
        ![message](../IMG/message.jpeg)
        ```
        인증코드가 발송 되었습니다.
        ```
<br/>

- 전화번호 인증
    - **API** : `/api/message/validateMessage`
    - **Method : POST**
    - **Body : raw(json)**

    - **Request**

        ```jsonc
        {
            "pnum" : 사용자의 전화번호,
            "authCode" : 인증코드
        }
        ```

    - **Response**
        - 200 OK
        ```
        {pnum} 인증 성공
        ```
        - 203 *NON_AUTHORITATIVE_INFORMATION*
        ```jsonc
        잘못된 인증코드 입니다.
        ``` 
        - 404 *NOT_FOUND*
        ```
        만료된 인증코드 혹은 잘못된 키 입니다.
        ```
<br/>