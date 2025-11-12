package com.hyukmin.foods.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * User - 사용자(회원) 엔티티
 *
 * 이 파일이 하는 일:
 * 1. 회원 정보 데이터를 표현 (아이디, 비밀번호, 이메일, 권한 등)
 * 2. DB의 users 테이블과 1:1 매핑
 * 3. 회원가입, 로그인, 권한 관리에 사용
 *
 * 왜 필요한가?
 * - 사용자 인증 및 권한 관리
 * - 맛집 등록자, 게시글 작성자, 댓글 작성자 식별
 * - 즐겨찾기 기능 제공
 * - 일반 사용자와 관리자 구분
 *
 * 연결되는 파일:
 * → Restaurant 엔티티: 사용자가 등록한 맛집들
 * → Board 엔티티: 사용자가 작성한 게시글들
 * → Comment 엔티티: 사용자가 작성한 댓글들
 * → Bookmark 엔티티: 사용자의 즐겨찾기 목록 (1:N 관계)
 * → UserRepository: 사용자 데이터 조회/저장
 * → UserService: 회원가입, 중복 체크 등 비즈니스 로직
 * → UserController: 회원가입, 로그인 페이지 요청 처리
 * → SecurityConfig: Spring Security 인증 설정
 * → CustomUserDetailsService: 로그인 처리
 */
@Entity
@Table(name = "users") //MySQL에서 user는 예역어라 users 변경
@Data // Lombok: getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 받는 생성자 자동 생성
@Builder // Lombok: 빌더 패턴 사용 가능

public class User {

    // 회원 고유 번호 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key 자동증가

    // 로그인 아이디 (중복 불가, 필수, 최대 50자)
    @Column(nullable = false, unique = true, length = 50)
    private String username;  // 로그인 ID

    // 비밀번호 (암호화되어 저장, 필수)
    // 실제로는 BCrypt로 암호화된 문자열 저장
    // 예: "$2a$10$abcd1234..."
    @Column(nullable = false)
    private String password;  // 암호화된 비밀번호

    // 이메일 (중복 불가, 필수, 최대 100자)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 권한 (필수, 최대 20자)
    // "ROLE_USER": 일반 회원
    // "ROLE_ADMIN": 관리자
    @Column(nullable = false, length = 20)
    private String role;  // "ROLE_USER" 또는 "ROLE_ADMIN"

    // 가입일 (자동으로 현재 시간 저장, 수정 불가)
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 가입일

    /**
     * 회원가입 시 기본값 설정
     * DB에 저장되기 직전에 자동 실행
     * role이 null이면 "ROLE_USER"로 자동 설정
     */
    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = "ROLE_USER";  // 기본값: 일반 회원
        }
    }

    // 사용자의 즐겨찾기 목록
    // 1:N 관계 - 한 명의 사용자(1) : 여러 즐겨찾기(N)
    // cascade: 사용자 삭제 시 즐겨찾기도 함께 삭제
    // orphanRemoval: 즐겨찾기 리스트에서 제거되면 DB에서도 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    /**
     * 즐겨찾기 추가 편의 메서드
     * 양방향 관계를 자동으로 설정
     */
    public void addBookmark(Bookmark bookmark) {
        bookmarks.add(bookmark);
        bookmark.setUser(this);
    }

    /**
     * 즐겨찾기 제거 편의 메서드
     * 양방향 관계를 자동으로 해제
     */
    public void removeBookmark(Bookmark bookmark) {
        bookmarks.remove(bookmark);
        bookmark.setUser(null);
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 회원가입
 *
 *   사용자: 회원가입 페이지에서 정보 입력
 *   - 아이디: hyukmin
 *   - 비밀번호: password123
 *   - 비밀번호 확인: password123
 *   - 이메일: hyukmin@example.com
 *   ↓
 *   POST /signup
 *   ↓
 *   UserController.signup()
 *   ↓
 *   1. 비밀번호 일치 확인
 *      password == passwordConfirm? → OK
 *   ↓
 *   2. UserService.registerUser(username, password, email)
 *   ↓
 *   3. 아이디 중복 체크
 *      userRepository.existsByUsername("hyukmin")
 *      → false (중복 아님)
 *   ↓
 *   4. 이메일 중복 체크
 *      userRepository.existsByEmail("hyukmin@example.com")
 *      → false (중복 아님)
 *   ↓
 *   5. 비밀번호 암호화
 *      BCrypt.encode("password123")
 *      → "$2a$10$abcd1234efgh5678ijkl..."
 *   ↓
 *   6. User 객체 생성
 *      User user = User.builder()
 *          .username("hyukmin")
 *          .password("$2a$10$abcd1234...")  // 암호화된 비밀번호
 *          .email("hyukmin@example.com")
 *          .build();
 *   ↓
 *   7. @PrePersist 실행
 *      user.prePersist()
 *      → user.role = "ROLE_USER" (자동 설정)
 *   ↓
 *   8. DB 저장
 *      UserRepository.save(user)
 *   ↓
 *   DB users 테이블에 저장:
 *   +----+----------+--------------------------------+----------------------+-----------+---------------------+
 *   | id | username | password                       | email                | role      | created_at          |
 *   +----+----------+--------------------------------+----------------------+-----------+---------------------+
 *   | 1  | hyukmin  | $2a$10$abcd1234efgh5678ijkl... | hyukmin@example.com  | ROLE_USER | 2025-01-15 10:30:00 |
 *   +----+----------+--------------------------------+----------------------+-----------+---------------------+
 *   ↓
 *   redirect:/login?success=true
 *   ↓
 *   로그인 페이지에 "회원가입이 완료되었습니다. 로그인해주세요." 메시지 표시
 *
 *
 * 시나리오 2) 아이디 중복 체크 (실시간)
 *
 *   사용자: 회원가입 폼에서 아이디 입력 중
 *   - 아이디 필드에 "hyukmin" 입력
 *   ↓
 *   JavaScript가 자동으로 AJAX 요청:
 *   GET /api/check-username?username=hyukmin
 *   ↓
 *   UserController.checkUsername("hyukmin")
 *   ↓
 *   UserService.isUsernameDuplicated("hyukmin")
 *   ↓
 *   UserRepository.existsByUsername("hyukmin")
 *   ↓
 *   DB 조회:
 *   SELECT COUNT(*) FROM users WHERE username = 'hyukmin';
 *   ↓
 *   결과: 1 (이미 존재함)
 *   ↓
 *   JSON 응답:
 *   {
 *     "success": true,
 *     "duplicated": true,
 *     "message": "이미 사용 중인 아이디입니다."
 *   }
 *   ↓
 *   화면에 빨간색으로 "이미 사용 중인 아이디입니다." 표시
 *
 *
 * 시나리오 3) 로그인
 *
 *   사용자: 로그인 페이지에서 정보 입력
 *   - 아이디: hyukmin
 *   - 비밀번호: password123
 *   ↓
 *   POST /login (Spring Security가 자동 처리)
 *   ↓
 *   SecurityConfig 설정에 따라:
 *   CustomUserDetailsService.loadUserByUsername("hyukmin") 호출
 *   ↓
 *   UserRepository.findByUsername("hyukmin")
 *   ↓
 *   DB 조회:
 *   SELECT * FROM users WHERE username = 'hyukmin';
 *   ↓
 *   User 객체 반환:
 *   User {
 *       id: 1,
 *       username: "hyukmin",
 *       password: "$2a$10$abcd1234...",
 *       email: "hyukmin@example.com",
 *       role: "ROLE_USER",
 *       createdAt: 2025-01-15 10:30:00
 *   }
 *   ↓
 *   Spring Security가 비밀번호 검증:
 *   BCrypt.matches("password123", "$2a$10$abcd1234...")
 *   → true (일치함)
 *   ↓
 *   로그인 성공!
 *   - 세션에 사용자 정보 저장
 *   - 권한 설정: ROLE_USER
 *   ↓
 *   redirect:/ (메인 페이지로)
 *
 *
 * 시나리오 4) 비밀번호 불일치 (로그인 실패)
 *
 *   사용자: 잘못된 비밀번호 입력
 *   - 아이디: hyukmin
 *   - 비밀번호: wrongpassword
 *   ↓
 *   POST /login
 *   ↓
 *   CustomUserDetailsService.loadUserByUsername("hyukmin")
 *   → User 객체 찾음
 *   ↓
 *   Spring Security가 비밀번호 검증:
 *   BCrypt.matches("wrongpassword", "$2a$10$abcd1234...")
 *   → false (일치하지 않음)
 *   ↓
 *   로그인 실패!
 *   ↓
 *   redirect:/login?error=true
 *   ↓
 *   로그인 페이지에 "아이디 또는 비밀번호가 일치하지 않습니다." 메시지 표시
 *
 *
 * 시나리오 5) 맛집 등록 (로그인한 사용자)
 *
 *   사용자(hyukmin): 새 맛집 등록
 *   ↓
 *   POST /restaurants
 *   ↓
 *   RestaurantController.create(Authentication auth)
 *   ↓
 *   현재 로그인한 사용자 정보 가져오기:
 *   String username = auth.getName();  // "hyukmin"
 *   User currentUser = userRepository.findByUsername("hyukmin");
 *   ↓
 *   Restaurant restaurant = new Restaurant();
 *   restaurant.setName("강남 피자집");
 *   restaurant.setAuthor(currentUser);  // 작성자 설정
 *   ↓
 *   RestaurantRepository.save(restaurant);
 *   ↓
 *   DB restaurant 테이블:
 *   | id | name        | author_id |
 *   | 1  | 강남 피자집  | 1         |
 *
 *
 * 시나리오 6) 즐겨찾기 추가
 *
 *   사용자(hyukmin): "강남 피자집" 즐겨찾기
 *   ↓
 *   POST /bookmarks?restaurantId=1
 *   ↓
 *   BookmarkService.addBookmark(user, restaurant)
 *   ↓
 *   방법 1 (일반):
 *   Bookmark bookmark = new Bookmark();
 *   bookmark.setUser(user);
 *   bookmark.setRestaurant(restaurant);
 *   user.getBookmarks().add(bookmark);  // 수동으로 추가
 *   bookmarkRepository.save(bookmark);
 *
 *   방법 2 (편의 메서드 사용):
 *   Bookmark bookmark = new Bookmark();
 *   bookmark.setRestaurant(restaurant);
 *   user.addBookmark(bookmark);  // 자동으로 양방향 설정!
 *   bookmarkRepository.save(bookmark);
 *   ↓
 *   DB bookmarks 테이블:
 *   | id | user_id | restaurant_id |
 *   | 1  | 1       | 1             |
 *
 *
 * 시나리오 7) 내 즐겨찾기 목록 조회
 *
 *   사용자(hyukmin): 내 즐겨찾기 페이지 접속
 *   ↓
 *   GET /bookmarks
 *   ↓
 *   BookmarkController.myBookmarks(Authentication auth)
 *   ↓
 *   User currentUser = userRepository.findByUsername("hyukmin");
 *   List<Bookmark> bookmarks = currentUser.getBookmarks();
 *   ↓
 *   JPA가 자동으로 즐겨찾기 조회:
 *   SELECT * FROM bookmarks WHERE user_id = 1;
 *   ↓
 *   결과:
 *   List<Bookmark> bookmarks = [
 *       Bookmark { id=1, user=hyukmin, restaurant=강남피자집 },
 *       Bookmark { id=2, user=hyukmin, restaurant=홍대떡볶이 },
 *       Bookmark { id=3, user=hyukmin, restaurant=이태원스시 }
 *   ]
 *   ↓
 *   bookmark-list.html에 즐겨찾기 목록 표시
 *
 *
 * 시나리오 8) 관리자 권한 체크
 *
 *   관리자(admin): 다른 사람이 쓴 게시글 삭제 시도
 *   ↓
 *   POST /boards/123/delete
 *   ↓
 *   BoardController.delete(123, Authentication auth)
 *   ↓
 *   권한 체크:
 *   boolean isAdmin = auth.getAuthorities().stream()
 *       .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
 *   ↓
 *   결과: true (관리자임)
 *   ↓
 *   게시글 삭제 허용
 *   ↓
 *   일반 사용자였다면?
 *   → "삭제 권한이 없습니다" 에러
 *
 *
 * ========== DB 테이블 구조 ==========
 *
 * users 테이블:
 *
 * +------------+--------------+------+-----+---------+----------------+
 * | Field      | Type         | Null | Key | Default | Extra          |
 * +------------+--------------+------+-----+---------+----------------+
 * | id         | bigint       | NO   | PRI | NULL    | auto_increment |
 * | username   | varchar(50)  | NO   | UNI | NULL    |                |
 * | password   | varchar(255) | NO   |     | NULL    |                |
 * | email      | varchar(100) | NO   | UNI | NULL    |                |
 * | role       | varchar(20)  | NO   |     | NULL    |                |
 * | created_at | datetime     | NO   |     | NULL    |                |
 * +------------+--------------+------+-----+---------+----------------+
 *
 * 제약 조건:
 * - username: UNIQUE (중복 불가)
 * - email: UNIQUE (중복 불가)
 *
 * 실제 데이터 예시:
 * +----+----------+--------------------------------+----------------------+-----------+---------------------+
 * | id | username | password                       | email                | role      | created_at          |
 * +----+----------+--------------------------------+----------------------+-----------+---------------------+
 * | 1  | hyukmin  | $2a$10$abcd1234efgh5678ijkl... | hyukmin@example.com  | ROLE_USER | 2025-01-15 10:30:00 |
 * | 2  | admin    | $2a$10$wxyz9876uvst5432opqr... | admin@example.com    | ROLE_ADMIN| 2025-01-10 09:00:00 |
 * | 3  | minsu    | $2a$10$lmno3456pqrs7890tuvw... | minsu@example.com    | ROLE_USER | 2025-01-16 14:20:00 |
 * +----+----------+--------------------------------+----------------------+-----------+---------------------+
 *
 *
 * ========== 엔티티 관계도 ==========
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Restaurant (맛집)
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Board (게시글)
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Comment (댓글)
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Bookmark (즐겨찾기)
 *
 * 의미:
 * - 한 명의 사용자(User)가 여러 맛집(Restaurant)을 등록할 수 있음
 * - 한 명의 사용자(User)가 여러 게시글(Board)을 작성할 수 있음
 * - 한 명의 사용자(User)가 여러 댓글(Comment)을 작성할 수 있음
 * - 한 명의 사용자(User)가 여러 맛집을 즐겨찾기(Bookmark)할 수 있음
 *
 *
 * ========== 비밀번호 암호화 (BCrypt) ==========
 *
 * 평문 비밀번호 저장의 문제점:
 * - DB가 해킹되면 모든 비밀번호 노출
 * - 관리자도 사용자 비밀번호를 알 수 있음
 *
 * BCrypt 암호화:
 * - 단방향 암호화: 암호화 → OK, 복호화 → 불가능
 * - Salt 자동 생성: 같은 비밀번호도 매번 다르게 암호화
 *
 * 예시:
 * 평문: "password123"
 *
 * 1차 암호화: "$2a$10$abcd1234efgh5678ijkl..."
 * 2차 암호화: "$2a$10$wxyz9876uvst5432opqr..."  (다름!)
 *
 * 로그인 시 검증:
 * - 입력한 평문: "password123"
 * - DB의 암호문: "$2a$10$abcd1234..."
 * - BCrypt.matches("password123", "$2a$10$abcd1234...")
 * → true (일치함)
 *
 *
 * ========== 권한 관리 (role 필드) ==========
 *
 * ROLE_USER (일반 회원):
 * - 맛집 등록, 수정, 삭제 (본인 것만)
 * - 게시글 작성, 수정, 삭제 (본인 것만)
 * - 댓글 작성, 수정, 삭제 (본인 것만)
 * - 즐겨찾기 추가/삭제
 *
 * ROLE_ADMIN (관리자):
 * - 모든 맛집 수정/삭제 가능
 * - 모든 게시글 수정/삭제 가능
 * - 모든 댓글 수정/삭제 가능
 * - 사용자 관리
 *
 * 권한 체크 코드:
 * boolean isAdmin = authentication.getAuthorities().stream()
 *     .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
 *
 *
 * ========== Lombok 어노테이션 설명 ==========
 *
 * @Data:
 * - getter, setter 자동 생성
 * - toString() 자동 생성
 * - equals(), hashCode() 자동 생성
 *
 * @NoArgsConstructor:
 * - 기본 생성자 자동 생성
 * - new User()
 *
 * @AllArgsConstructor:
 * - 모든 필드를 받는 생성자 자동 생성
 * - new User(id, username, password, email, role, createdAt, bookmarks)
 *
 * @Builder:
 * - 빌더 패턴 사용 가능
 * - User user = User.builder()
 *       .username("hyukmin")
 *       .password("encoded_password")
 *       .email("hyukmin@example.com")
 *       .build();
 *
 *
 * ========== @PrePersist 설명 ==========
 *
 * @PrePersist:
 * - DB에 저장되기 직전에 자동 실행되는 메서드
 * - 기본값 설정, 유효성 검사 등에 사용
 *
 * 예시:
 * User user = User.builder()
 *     .username("hyukmin")
 *     .password("encoded_password")
 *     .email("hyukmin@example.com")
 *     .build();
 *
 * // role은 설정하지 않음 (null)
 *
 * userRepository.save(user);
 *
 * // 저장 직전에 prePersist() 자동 호출
 * // → user.role = "ROLE_USER" 자동 설정
 *
 * // DB에는 role="ROLE_USER"로 저장됨
 *
 *
 * ========== 편의 메서드 활용 ==========
 *
 * addBookmark() 메서드 없이:
 * Bookmark bookmark = new Bookmark();
 * bookmark.setUser(user);
 * bookmark.setRestaurant(restaurant);
 * user.getBookmarks().add(bookmark);  // 수동으로 추가
 *
 * addBookmark() 메서드 사용:
 * Bookmark bookmark = new Bookmark();
 * bookmark.setRestaurant(restaurant);
 * user.addBookmark(bookmark);  // 자동으로 양방향 설정!
 *
 * 장점:
 * - 코드 간결
 * - 양방향 관계 설정 자동화
 * - 실수 방지
 */