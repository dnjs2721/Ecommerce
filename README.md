# ECommerce
>### 개발환경
- Language : JAVA
- Framework : SprigBoot
- SpringBoot Version : 3.0.6
- DBMS : Spring-Data-Jpa, Redis, querydsl, MySQL
- Build Version : Java 17
- DevTool : Intelli J
- TestTool : PostMan
- JDK : open-jdk:19
---
>### 구현
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
9. 일반 사용자에서 판매자로 변경, 판매자에서 일반 사용자로 변경 신청 ✅
   - 요청 로그 작성 (사용자가 탈퇴하여도 로그는 남아있음)

### 관리자 기능
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

### 판매자
1. 상품등록 ✅
2. 판매자 본인 판매상품 조회 ✅
   - 페이지
   - 상품이름, 가격 범위, 재고 범위, 카테고리, 출력 개수, 페이지 수 설정 가능 // 동적쿼리(querydsl)
3. 상품 정보 변경 ✅  
   - 가격 변경, 재고 변경, 카테고리 변경 가능
4. 재고 관리
5. 상품 삭제
6. 주문 조회

### 상품
1. 카테고리 설정 가능 ✅
   - 상품당 카테고리 1개

### 카테고리
1. 부모 카테고리 설정 가능 ✅ 

### 검색
1. 상품 이름 검색
2. 상품 카테고리 검색
3. 판매자 검색
4. 가격순 정렬
5. 동적 쿼리를 이용

## 장바구니
1. 장바구니 등록, 수정, 삭제 가능
2. 장바구니에서 주문 가능

## 주문
1. 장바구니를 통하지 않고 주문시 단건 주문
2. 장바구니를 통해 주문시 여러 상품 주문
3. 결제 완료시 주문완료 상태로 변경
4. 취소 가능
5. 주문 조회 가능
    - 날짜, 상태 조건으로 검색
    - 동적 쿼리 작성

## 결제
- 가상 결제 아임포트 사용