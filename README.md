# 🍴 맛집 추천 커뮤니티

> Spring Boot, Thymeleaf 기반 맛집 사이트

## 📌 프로젝트 소개

사용자들이 맛집 정보를 등록하고 공유할 수 있는 커뮤니티 웹사이트입니다.
지역별, 카테고리별로 레스토랑을 검색할 수 있으며, 다른 사용자의 리뷰를 참고하여 맛집을 찾을 수 있습니다.

- **개발 기간**: 2024.10 ~ 2024.11
- **개발 인원**: 1인 (개인 프로젝트)

## 🛠 기술 스택

### Backend
- Java 23
- Spring Boot 3.1.x
- Spring Data JPA
- Spring Security
- Thymeleaf

### Database
- MySQL 8.0

### Frontend
- HTML5 / CSS3
- JavaScript
- Bootstrap 5

### Tools
- IntelliJ IDEA
- Git / GitHub
- Postman

## 🎯 주요 기능

### 1. 회원 관리
- Spring Security를 활용한 회원가입/로그인
- 비밀번호 암호화 (BCrypt)
- 회원 정보 수정

### 2. 맛집 관리
- 맛집 정보 등록 (이름, 주소, 카테고리, 설명)
- 맛집 정보 수정 및 삭제
- 이미지 업로드

### 3. 리뷰 시스템
- 별점 기반 리뷰 작성 (1~5점)
- 리뷰 수정 및 삭제
- 평균 평점 자동 계산

### 4. 검색 및 필터링
- 지역별 맛집 검색
- 카테고리별 필터링
- 키워드 검색

### 5. 페이징 처리
- Spring Data JPA Pageable 활용
- 한 페이지당 10개 항목 표시

## 📂 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/example/restaurant/
│   │       ├── controller/      # 컨트롤러
│   │       ├── service/          # 서비스 계층
│   │       ├── repository/       # JPA Repository
│   │       ├── entity/           # 엔티티 클래스
│   │       ├── dto/              # DTO 클래스
│   │       └── config/           # 설정 파일
│   └── resources/
│       ├── templates/            # Thymeleaf 템플릿
│       ├── static/               # CSS, JS, 이미지
│       └── application.properties
```

## 💾 데이터베이스 설계

### 주요 테이블
- **users**: 회원 정보
- **restaurants**: 맛집 정보
- **reviews**: 리뷰 정보
- **categories**: 카테고리 정보

## 🚀 실행 방법

### 1. 저장소 클론
```bash
git clone https://github.com/본인아이디/restaurant-community.git
cd restaurant-community
```

### 2. 데이터베이스 설정
MySQL에 데이터베이스를 생성합니다.
```sql
CREATE DATABASE restaurant_db;
```

### 3. application.properties 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_db
spring.datasource.username=root
spring.datasource.password=본인비밀번호
```

### 4. 프로젝트 실행
```bash
./mvnw spring-boot:run
```

### 5. 브라우저에서 접속
```
http://localhost:8080
```

## 💡 트러블슈팅

### 1. N+1 문제 해결
**문제**: 맛집 목록 조회 시 각 맛집의 리뷰를 개별적으로 조회하여 쿼리가 과도하게 발생

**해결**: 
- `@EntityGraph`를 사용하여 연관된 리뷰 데이터를 한 번에 Fetch Join으로 조회
- 쿼리 실행 횟수 90% 감소 (100회 → 10회)

```java
@EntityGraph(attributePaths = {"reviews"})
List<Restaurant> findAllWithReviews();
```

### 2. 이미지 업로드 최적화
**문제**: 대용량 이미지 업로드로 인한 서버 용량 부족 및 로딩 속도 저하

**해결**:
- 파일 크기 검증 로직 추가 (5MB 제한)
- 이미지 리사이징 처리
- 평균 이미지 크기 70% 감소

### 3. 카카오 로드맵 API 공유 에러
**문제**: 카카오맵 API 활용 실시간 로드맵 에러

**해결**: : 카카오 개발자 API를 공유하여 실시간 로직처리

결과: 맛집을 등록할때마다 실시간 주소 위치 공유

## 📈 개선 계획

- [ ] 댓글 기능 추가
- [ ] 좋아요/북마크 기능
- [ ] AWS 배포
- [ ] 카카오맵 API 연동
- [ ] 소셜 로그인 (구글, 카카오)

## 📧 문의

<a href="https://github.com/johnkwon87/foods" class="btn btn-primary">GitHub 보기</a>
```

**연락처 부분:**
```
📧 johnkwon33@gmail.com
💻 github.com/johnkwon87

---

© 2025 JohnKwon87. All rights reserved.
