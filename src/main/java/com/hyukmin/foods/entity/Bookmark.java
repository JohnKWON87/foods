package com.hyukmin.foods.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
/**
 * Bookmark - 북마크(즐겨찾기) 엔티티
 *
 * 이 파일이 하는 일:
 * 1. 사용자가 맛집을 즐겨찾기한 정보를 저장
 * 2. 어떤 사용자(User)가 어떤 맛집(Restaurant)을 즐겨찾기했는지 연결
 * 3. DB의 bookmarks 테이블과 1:1 매핑
 *
 * 왜 필요한가?
 * - 사용자가 나중에 다시 보고 싶은 맛집을 저장하기 위해
 * - 한 사용자가 여러 맛집을 즐겨찾기 가능
 * - 한 맛집이 여러 사용자에게 즐겨찾기될 수 있음
 * - 중복 방지: 같은 사용자가 같은 맛집을 두 번 즐겨찾기할 수 없음
 *
 * 연결되는 파일:
 * → User 엔티티: 즐겨찾기한 사용자
 * → Restaurant 엔티티: 즐겨찾기된 맛집
 * → BookmarkRepository: 즐겨찾기 데이터 조회/저장
 * → BookmarkService: 즐겨찾기 추가/삭제 로직
 * → BookmarkController: 즐겨찾기 요청 처리
 */
@Entity
@Table(name = "bookmarks",
        // 중복 방지: 같은 사용자가 같은 맛집을 두 번 즐겨찾기할 수 없음
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "restaurant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    // 북마크 고유 번호 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 즐겨찾기한 사용자
    // N:1 관계 - 여러 북마크(N) : 한 명의 사용자(1)
    // LAZY: 북마크 조회 시 사용자 정보는 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 즐겨찾기된 맛집
    // N:1 관계 - 여러 북마크(N) : 한 개의 맛집(1)
    // LAZY: 북마크 조회 시 맛집 정보는 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // 즐겨찾기한 날짜 (자동으로 현재 시간 저장)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 편의 메서드: 사용자 설정
     * 양방향 관계를 자동으로 설정해줌
     *
     * 동작:
     * 1. 이 북마크의 user를 설정
     * 2. user의 bookmarks 리스트에도 이 북마크를 추가
     */
    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getBookmarks().contains(this)) {
            user.getBookmarks().add(this);
        }
    }
    /**
     * 편의 메서드: 맛집 설정
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 맛집 즐겨찾기 추가
 *
 *   사용자(hyukmin): "강남 맛집" 즐겨찾기 버튼 클릭
 *   ↓
 *   restaurant-detail.html에서 즐겨찾기 버튼 클릭
 *   ↓
 *   POST /bookmarks?restaurantId=123
 *   ↓
 *   BookmarkController.add(123)
 *   ↓
 *   BookmarkService.addBookmark(user, restaurant)
 *   - 이미 즐겨찾기했는지 확인
 *   - Bookmark 객체 생성
 *   ↓
 *   Bookmark bookmark = Bookmark.builder()
 *       .user(hyukmin)           // 사용자: hyukmin
 *       .restaurant(강남맛집)     // 맛집: id=123
 *       .createdAt(현재시간)      // 자동 설정
 *       .build();
 *   ↓
 *   BookmarkRepository.save(bookmark)
 *   ↓
 *   DB bookmarks 테이블에 저장:
 *   +----+---------+---------------+---------------------+
 *   | id | user_id | restaurant_id | created_at          |
 *   +----+---------+---------------+---------------------+
 *   | 1  | 5       | 123           | 2025-01-15 10:30:00 |
 *   +----+---------+---------------+---------------------+
 *   ↓
 *   "즐겨찾기에 추가되었습니다" 메시지
 *
 *
 * 시나리오 2) 중복 즐겨찾기 시도
 *
 *   사용자(hyukmin): 이미 즐겨찾기한 "강남 맛집"을 또 즐겨찾기 시도
 *   ↓
 *   POST /bookmarks?restaurantId=123
 *   ↓
 *   BookmarkService.addBookmark(user, restaurant)
 *   - BookmarkRepository에서 이미 존재하는지 확인
 *   - bookmarkRepository.existsByUserAndRestaurant(hyukmin, 강남맛집)
 *   - 결과: true (이미 존재함)
 *   ↓
 *   예외 발생: "이미 즐겨찾기한 맛집입니다"
 *   ↓
 *   에러 메시지 표시
 *
 * 참고: DB 레벨에서도 중복 방지
 *   - uniqueConstraints로 (user_id, restaurant_id) 조합이 유일해야 함
 *   - 만약 같은 조합으로 INSERT 시도하면 DB 에러 발생
 *
 *
 * 시나리오 3) 내 즐겨찾기 목록 조회
 *
 *   사용자(hyukmin): "내 즐겨찾기" 페이지 접속
 *   ↓
 *   GET /bookmarks
 *   ↓
 *   BookmarkController.myBookmarks()
 *   ↓
 *   BookmarkService.findByUser(hyukmin)
 *   ↓
 *   BookmarkRepository.findByUser(hyukmin)
 *   ↓
 *   DB 조회:
 *   SELECT * FROM bookmarks WHERE user_id = 5;
 *   ↓
 *   결과를 Bookmark 객체 리스트로 변환:
 *   List<Bookmark> bookmarks = [
 *       Bookmark { id=1, user=hyukmin, restaurant=강남맛집, createdAt=2025-01-15 },
 *       Bookmark { id=2, user=hyukmin, restaurant=홍대맛집, createdAt=2025-01-16 },
 *       Bookmark { id=3, user=hyukmin, restaurant=이태원맛집, createdAt=2025-01-17 }
 *   ]
 *   ↓
 *   bookmark-list.html에 즐겨찾기 목록 표시:
 *   - 강남 맛집 (2025-01-15 추가)
 *   - 홍대 맛집 (2025-01-16 추가)
 *   - 이태원 맛집 (2025-01-17 추가)
 *
 *
 * 시나리오 4) 즐겨찾기 삭제
 *
 *   사용자(hyukmin): "강남 맛집" 즐겨찾기 취소 버튼 클릭
 *   ↓
 *   POST /bookmarks/delete?restaurantId=123
 *   ↓
 *   BookmarkController.delete(123)
 *   ↓
 *   BookmarkService.deleteBookmark(hyukmin, 강남맛집)
 *   ↓
 *   BookmarkRepository.findByUserAndRestaurant(hyukmin, 강남맛집)
 *   - DB에서 해당 북마크 찾기
 *   ↓
 *   Bookmark bookmark = { id=1, user_id=5, restaurant_id=123 }
 *   ↓
 *   BookmarkRepository.delete(bookmark)
 *   ↓
 *   DB에서 삭제:
 *   DELETE FROM bookmarks WHERE id = 1;
 *   ↓
 *   "즐겨찾기가 취소되었습니다" 메시지
 *
 *
 * 시나리오 5) 특정 맛집을 즐겨찾기한 사용자 수 조회
 *
 *   관리자: "강남 맛집"이 몇 명에게 즐겨찾기되었는지 확인
 *   ↓
 *   BookmarkService.countByRestaurant(강남맛집)
 *   ↓
 *   BookmarkRepository.countByRestaurant(강남맛집)
 *   ↓
 *   DB 조회:
 *   SELECT COUNT(*) FROM bookmarks WHERE restaurant_id = 123;
 *   ↓
 *   결과: 25명이 즐겨찾기함
 *
 *
 * ========== DB 테이블 구조 ==========
 *
 * bookmarks 테이블 (이 Bookmark 클래스가 만드는 테이블):
 *
 * +---------------+----------+------+-----+---------+----------------+
 * | Field         | Type     | Null | Key | Default | Extra          |
 * +---------------+----------+------+-----+---------+----------------+
 * | id            | bigint   | NO   | PRI | NULL    | auto_increment |
 * | user_id       | bigint   | NO   | FK  | NULL    |                |
 * | restaurant_id | bigint   | NO   | FK  | NULL    |                |
 * | created_at    | datetime | NO   |     | NULL    |                |
 * +---------------+----------+------+-----+---------+----------------+
 *
 * 제약 조건:
 * - UNIQUE(user_id, restaurant_id): 같은 사용자가 같은 맛집을 두 번 즐겨찾기 불가
 *
 * 실제 데이터 예시:
 * +----+---------+---------------+---------------------+
 * | id | user_id | restaurant_id | created_at          |
 * +----+---------+---------------+---------------------+
 * | 1  | 5       | 123           | 2025-01-15 10:30:00 |  (hyukmin → 강남맛집)
 * | 2  | 5       | 456           | 2025-01-16 14:20:00 |  (hyukmin → 홍대맛집)
 * | 3  | 7       | 123           | 2025-01-17 09:15:00 |  (minsu → 강남맛집)
 * | 4  | 8       | 123           | 2025-01-17 11:40:00 |  (jimin → 강남맛집)
 * +----+---------+---------------+---------------------+
 *
 * 외래키 관계:
 * - user_id → users 테이블의 id (즐겨찾기한 사용자)
 * - restaurant_id → restaurants 테이블의 id (즐겨찾기된 맛집)
 *
 *
 * ========== 엔티티 관계도 ==========
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Bookmark (즐겨찾기)
 *   ↓ N
 *   | (여러 맛집을)
 *   ↓ 1
 * Restaurant (맛집)
 *
 * 의미:
 * - 한 명의 사용자(User)가 여러 맛집을 즐겨찾기(Bookmark) 가능
 * - 한 개의 맛집(Restaurant)이 여러 사용자에게 즐겨찾기(Bookmark)될 수 있음
 * - Bookmark는 User와 Restaurant를 연결하는 중간 테이블 역할
 *
 * 예시:
 * hyukmin 사용자 (id=5):
 *   - 강남 맛집 (id=123) 즐겨찾기
 *   - 홍대 맛집 (id=456) 즐겨찾기
 *   - 이태원 맛집 (id=789) 즐겨찾기
 *
 * 강남 맛집 (id=123):
 *   - hyukmin (id=5)이 즐겨찾기
 *   - minsu (id=7)이 즐겨찾기
 *   - jimin (id=8)이 즐겨찾기
 *
 *
 * ========== 중복 방지 메커니즘 ==========
 *
 * 1. DB 레벨 (uniqueConstraints):
 *    - (user_id, restaurant_id) 조합이 유일해야 함
 *    - 같은 조합으로 INSERT 시도 시 DB 에러 발생
 *
 *    시도:
 *    INSERT INTO bookmarks (user_id, restaurant_id) VALUES (5, 123);  // 성공
 *    INSERT INTO bookmarks (user_id, restaurant_id) VALUES (5, 123);  // 에러!
 *
 * 2. 애플리케이션 레벨 (BookmarkService):
 *    - 저장하기 전에 이미 존재하는지 확인
 *    - bookmarkRepository.existsByUserAndRestaurant()로 체크
 *    - 이미 존재하면 예외 발생
 *
 *
 * ========== 편의 메서드 설명 ==========
 *
 * setUser(User user) 메서드:
 *
 * 일반적인 방법:
 *   Bookmark bookmark = new Bookmark();
 *   bookmark.setUser(hyukmin);
 *   hyukmin.getBookmarks().add(bookmark);  // 수동으로 추가해야 함
 *
 * 편의 메서드 사용:
 *   Bookmark bookmark = new Bookmark();
 *   bookmark.setUser(hyukmin);  // 자동으로 hyukmin.bookmarks에도 추가됨!
 *
 * 장점:
 * - 양방향 관계를 한 번에 설정
 * - 실수로 한쪽만 설정하는 것을 방지
 * - 코드가 간결해짐
 *
 *
 * ========== Lombok 어노테이션 설명 ==========
 *
 * @Getter: 모든 필드의 getter 자동 생성
 *   - getId(), getUser(), getRestaurant(), getCreatedAt()
 *
 * @Setter: 모든 필드의 setter 자동 생성
 *   - setId(), setUser(), setRestaurant(), setCreatedAt()
 *
 * @NoArgsConstructor: 기본 생성자 자동 생성
 *   - new Bookmark()
 *
 * @AllArgsConstructor: 모든 필드를 받는 생성자 자동 생성
 *   - new Bookmark(id, user, restaurant, createdAt)
 *
 * @Builder: 빌더 패턴 사용 가능
 *   - Bookmark.builder()
 *       .user(hyukmin)
 *       .restaurant(강남맛집)
 *       .build();
 *
 *
 * ========== 실제 사용 예시 코드 ==========
 *
 * // 1. 즐겨찾기 추가
 * Bookmark bookmark = Bookmark.builder()
 *     .user(currentUser)
 *     .restaurant(restaurant)
 *     .build();
 * bookmarkRepository.save(bookmark);
 *
 * // 2. 즐겨찾기 존재 여부 확인
 * boolean exists = bookmarkRepository.existsByUserAndRestaurant(user, restaurant);
 * if (exists) {
 *     throw new RuntimeException("이미 즐겨찾기한 맛집입니다");
 * }
 *
 * // 3. 내 즐겨찾기 목록 조회
 * List<Bookmark> myBookmarks = bookmarkRepository.findByUser(currentUser);
 * for (Bookmark bookmark : myBookmarks) {
 *     Restaurant restaurant = bookmark.getRestaurant();
 *     System.out.println(restaurant.getName());
 * }
 *
 * // 4. 즐겨찾기 삭제
 * Bookmark bookmark = bookmarkRepository.findByUserAndRestaurant(user, restaurant);
 * bookmarkRepository.delete(bookmark);
 */