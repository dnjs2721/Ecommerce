# ECommerce
>### 개발환경
- Language : JAVA
- Framework : SprigBoot
- SpringBoot Version : 3.0.6
- DBMS : Spring-Data-Jpa, Redis, querydsl, MySQL
- Build Version : Java 17
- DevTool : Intelli J
- TestTool : PostMan, DBeaver
- JDK : open-jdk:19
- External API : CoolSMS(Message), PortOne(Payment)
---
>### 구현
### 관리자
1. 회원가입 ✅
   - 관리자 회원가입
2. 사용자 검색 ✅
   - 관리자만 확인 가능
   - 페이지
   - userStatus 조건 검색 가능 // 동적쿼리(querydsl)
3. 일반-판매자 변경 요청 로그 확인 ✅
   - 관리자만 확인 가능
   - 페이지
   - 사용자 id, 요청 날짜, 요청 상태, 요청 처리한 관리자 id, 출력 개수, 페이지 수 설정 가능 // 동적쿼리(querydsl)
4. 일반 사용자에서 판매자로 변경, 판매자에서 일반 사용자로 변경 승인 ✅
   - 요청 로그를 보고 관리자가 승인
5. 카테고리 생성 ✅
   - 부모 카테고리 설정 가능
6. 카테고리 내 상품 조회 ✅
   - 자신과 자식 카테고리에 등록된 상품 모두 조회
7. 카테고리 삭제 ✅
   - 삭제할 카테고리와 자식 카테고리에 등록된 상품의 판매자에게 상품 카테고리 변경 권고 메일 발송 ✅
   - 삭제할 카테고리와 자식 카테고리 상품을 기타 카테고리로 일괄 변경 후 변경 안내 메일 발송 ✅
   - 카테고리 삭제 ✅
      - 외래키 제약조건 해결

### 사용자
1. 중복체크 ✅
   - 이메일 중복 체크
   - 닉네임 중복 체크
   - 전화번호 중복 체크
2. 인증 ✅
   - 이메일 인증 (제한 5분)
     - MailSender, Redis
   - 전화번호 인증 (제한 5분)
     - CoolSms API, Redis
3. 회원가입 ✅
    - 일반 사용자 회원가입
    - 장바구니 생성
4. 로그인 ✅
5. 아이디(이메일) 찾기 ✅
   - 전화번호 인증 
6. 비밀번호 변경 ✅
   - 기존 비밀번호 인증
7. 정보 수정 ✅
   - 비밀번호 인증
   - 닉네임, 주소 변경 가능 (동적 쿼리)
   - 닉네임 변경시 admin(대소문자 상관없이)이 포함되어있으면 예외발생
   - 주소 형태가 올바르지 않을시 에외 발생 
8. 회원 탈퇴 ✅
   - 패스워드 인증
   - 장바구니 상품 삭제 , 장바구니 삭제
   - 회원 정보 저장
9. 일반 사용자에서 판매자로 변경, 판매자에서 일반 사용자로 변경 신청 ✅
   - 요청 로그 작성 (사용자가 탈퇴하여도 로그는 남아있음)
10. 상품 조회 ✅
    - 페이지
    - 판매자 닉네임, 상품 이름, 상품 가격 범위, 카테고리 검색가능 (동적쿼리)
    - 검색 내역 정렬 가능(상품이름, 가격 - 오름, 내림 정렬 가능 - 동적쿼리) 3개까지 다중 정렬 가능
11. 장바구니 담기 ✅
12. 장바구니 상품 수량 변경 ✅
13. 장바구니 상품 삭제(단건, 복수) ✅
14. 장바구니 비우기 ✅
15. 장바구니 전체 조회 ✅
16. 장바구니 상품 전체 주문 ✅
    - 주문 상태 : 결제 대기
17. 장바구니 선택 상품 주문(단건, 복수) ✅
    - 주문 상태 : 결제 대기
18. 상품 단건주문 ✅
19. 주문 조회 ✅
    - 검색 조건 설정 : 주문 날짜 ✅
    - 주문 상세 조회 ✅
20. 주문 취소 ✅
    - 주문 상품 상태 취소로 변경 ✅
    - 배송단계, 배송완료 상품은 취소 불가 ✅
    - 주문 취소시 상품 재고 수정 ✅
    - 주문 취소시 "구매자에 의한 취소" 코멘트 남김 ✅
21. 결제 ✅
22. 결제취소 ✅
    - 결제 상품 기준으로 취소 
23. 배송완료 상태일때 교환/환불 신청 ✅
    - 배송완료 상태일때 만 시청 가능
    - 교환/환불 신청 로그 생성
    - 교환/환불 중복 신청 불가능
    - 교환/환불 신청 취소 가능

### 판매자
1. 상품등록 ✅
2. 판매자 본인 판매상품 조회 ✅
   - 페이지
   - 상품이름, 가격 범위, 재고 범위, 카테고리, 출력 개수, 페이지 수 설정 가능 // 동적쿼리(querydsl)
3. 상품 정보 변경 ✅  
   - 가격 변경, 재고 변경, 카테고리 변경 가능
   - 가격 변경시 장바구니에 있는 상품들도 변경 (쿼리 최적화)
4. 상품 삭제 (단건, 복수) ✅
   - 장바구니에 담긴 상품들 삭제
   - 상품 삭제시 판매자 정보와 상품 정보 저장
5. 재고 관리 ✅
6. 주문 조회 ✅
   - 검색 조건 설정 : 주문 날짜✅
   - 주문 상세 조회 ✅
7. 주문 상태 관리 ✅
   - 주문 상품 상태 변경 ✅
   - 주문취소시 "판매자에 의한 취소" + "사유" 코멘트 남김 ✅
   - 배송대기중, 배송중, 배송완료 상태로 변경 가능 ✅
   - 결제 대기중 상태로 변경 불가 ✅
8. 교환/환불
    - 배송완료 상태일때 교환/환불 ❌

### 상품
1. 카테고리 설정 가능 ✅
   - 상품당 카테고리 1개
2. 재고 부족 사용자 예외 ✅

### 카테고리
1. 부모 카테고리 설정 가능 ✅

### 장바구니
1. 장바구니 등록, 수정, 삭제 가능 ✅
2. 가격 변경 쿼리 최적화 ✅
3. 장바구니에서 주문 가능 ✅

### 주문
1. 주문 상품 ✅
   - 주문시 재고 체크, 재고 부족시 예외 발생 ✅
2. 구매자용 주문서 ✅
3. 판매자용 주문서 ✅
4. 회원 탈퇴 혹은 판매자가 상품 삭제를 하여도 주문 상품 데이터는 보존 ✅
5. 장바구니 단건, 여러건 주문 ✅
6. 단건주문 ✅

### 결제 가상결제 서비스 PortOne
1. 생성자를 통해 토큰 발급 ✅
2. 결제, 취소 테스트를 위한 페이지 paymentHome ✅
3. 결제 페이지 payment ✅
    - 카카오페이 간편결제를 이용
    - 결제 후 PortOne 서버의 결제 금액과 DB의 금액을 비교 후 검증
        - 예외 발생시 VerifyIamportException 사용자 정의 예외 발생
    - 검증 통과 후 DB 수정
4. 주문 취소 페이지 cancelOrderHome ✅
    - 전달 받은 주문상품 Id의 결제 상태가 결제 대기중이라면 주문 취소
    - 전달 받은 주문상품 Id의 결제 상태가 결제 완료라면 결제 취소
      - 환불 후 PortOne 서버의 환불 금액과 DB의 금액을 비교 후 검증
        - 예외 발생시 VerifyIamportException 사용자 정의 예외 발생 
    - 이 외에는 예외처리

### 보관 데이터
1. 사용자 회원 탈퇴시 ✅
   - id, 이름, 닉네임, 이메일, 비밀번호, 전화번호, 생일, 주소, 권한 저장
2. 상품 삭제시 ✅
   - 판매자 id, 판매자 이름, 판매자 닉네임, 판매자 이메일, 판매자 전화번호, 판매자 생일, 판매자 주소, 상품 id, 상품 이름, 상품 가격 저장
3. 모든 보관 데이터는 7일간 저장하며 매일 자정(0시 0분)에 보관 기간이 지난 데이터를 삭제 ✅