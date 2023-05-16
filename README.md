# 기능

### 공통
1. 생성 시간 ✅
2. 수정 시간 ✅

### 사용자
1. 회원가입 ✅
    - 이메일 중복 체크✅ -> 이메일 인증(제한시간 5분)✅ -> 닉네임 중복 체크✅
      - mailSender, Redis ✅
2. 로그인 ✅
3. 아이디(이메일) 찾기 ✅
   - 이름, 전화번호를 통해 등록된 메일 출력 ✅
      - querydsl ✅
4. 비밀번호 찾기
   - 이메일 인증 -> 비밀번호 변경
5. 정보 수정
    - 비밀번호 인증 -> 아이디, 비밀번호 제외 변경가능
6. 일반 사용자에서 판매자로 변경
    - 관리자 승인 필요
7. 회원 탈퇴
    - 패스워드 인증

## 판매자
1. 상품등록, 삭제, 변경
2. 재고 관리
3. 주문 조회

## 상품
1. 카테고리 설정 가능

## 검색
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