## 🙍🏻‍ User ( 사용자 )

<details>
<summary>메일 전송, 인증 /api/mail</summary>

- 매일 전송
    - **API** : `/api/mail/sendMail/{userEmail}`
    - **Method : GET**
    - **Request**
  ```jsonc
  "userEmail" : userEmail 에게 랜덤 인증코드 발송
  ```
    - **Response**
      - 200 OK
      ```jsonc
      인증코드가 발송 되었습니다.
      ```
      ![mail](../IMG/mail.png)

<br/>

- 매일 인증
    - **API** : `/api/mail/validateEmail`
    - **Method : POST**
    - **Body : raw (json)**
    - **Request**
    ```jsonc
    {
      "email" : 사용자의 이메일,
      "authCode" : 인증코드
    }
    ```
    - **Response**
      - 200 OK
      ```jsonc
      {email} 인증 성공
      ```
      - 203 *NON_AUTHORITATIVE_INFORMATION*
      ```jsonc
      잘못된 인증코드 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      만료된 인증코드 혹은 잘못된 키 입니다.
      ```
</details>

<details>
<summary>메세지 전송, 인증 /api/message</summary>

- 메세지 전송
    - **API** : `/api/mail/sendMessage/{pNum}`
    - **Method : GET**
    - **Request**
    ```jsonc
    "pNum" : pNum 에게 랜덤 인증코드 발송
    ```
    - **Response**
        - 200 OK
      ```jsonc
      인증코드가 발송 되었습니다.
      ```
      ![message](../IMG/message.jpeg)  

<br/>

- 전화번호 인증
    - **API** : `/api/message/validateMessage`
    - **Method : POST**
    - **Body : raw (json)**
    - **Request**
    ```jsonc
    {
      "pnum" : 사용자의 전화번호,
      "authCode" : 인증코드
    }
    ```
    - **Response**
      - 200 OK
      ```jsonc
      {pnum} 인증 성공
      ```
      - 203 *NON_AUTHORITATIVE_INFORMATION*
      ```jsonc
      잘못된 인증코드 입니다.
      ```
      - 404 *NOT_FOUND*
      ```jsonc
      만료된 인증코드 혹은 잘못된 키 입니다.
      ```
      
</details>

<details>
<summary>중복 체크 /api/users/check</summary>

- 이메일 중복 체크
    - **API** : `/api/users/check/duplicationEmail/{userEmail`
    - **Method : GET**
    - **Request**
    ```jsonc
    "userEmail" : Email 을 통하여 중복 체크
    ```
    - **Response**
      - 200 OK
      ```jsonc
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
      ```jsonc
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
      ```jsonc
      {pNum} 은 사용가능한 번호입니다.
      전화번호 인증을 해주세요.
      ```
      - 409 *CONFLICT*
      ```jsonc
      이미 등록된 휴대폰 번호입니다.
      ```
</details>

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