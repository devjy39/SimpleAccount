# SimpleAccount
RestAPI 기반의 Simple Account 만들기

사용 기술
- Spring boot
- spring data jpa
- redisson
- embedded redis
- h2 DB

#### H2 DB를 이용해 미리 입력된 사용자 데이터를 바탕으로 동작하는 기능 개발.<br/>
- Account - 계좌 생성, 계좌 삭제, 계좌 조회<br/>
- Transaction - 잔액 사용, 거래 취소, 거래 조회<br/>

#### Exception
- GlobalExceptionHandler로 공통 Exception 처리 후 ErrorCode response

#### Test 
- 다양한 케이스의 유닛 테스트 코드 작성.
- Mockito -> mock을 생성해 의존성 없이 테스트.
- WebMvcTest -> 특정 컨트롤러만 격리시켜서 유닛단위로 테스트.

#### 동시성 이슈로 인한 lost update
- 거래금액을 늘리거나 줄이는 과정에서 여러 쓰레드 혹은 인스턴스에서 같은 계좌에 접근할 경우 발생
- Spring AOP를 이용해서 redis locking & unlocking.
