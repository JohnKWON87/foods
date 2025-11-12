package com.hyukmin.foods.service;

import com.hyukmin.foods.entity.Bookmark;
import com.hyukmin.foods.entity.Restaurant;
import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.repository.BookmarkRepository;
import com.hyukmin.foods.repository.RestaurantRepository;
import com.hyukmin.foods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BookmarkService - 즐겨찾기 비즈니스 로직
 *
 * 이 파일 역할: 즐겨찾기 추가/삭제 전에 검증하고 처리
 *
 * 데이터 흐름:
 * Controller → Service(여기! 검증/처리) → Repository → DB
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)// 기본은 조회만 (성능 최적화)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    /**
     * 북마크 추가, 즐겨찾기 추가
     */
    @Transactional // DB 변경 작업이라 readOnly 해제
    public boolean addBookmark(String username, Long restaurantId) {
        // 1. 사용자 찾기
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 2. 맛집 찾기
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("맛집을 찾을 수 없습니다."));

        // 3. 이미 즐겨찾기 했는지 확인
        if (bookmarkRepository.existsByUserAndRestaurant(user, restaurant)) {
            return false;
        }
        // 4. 새 즐겨찾기 생성 및 저장
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .restaurant(restaurant)
                .build();

        bookmarkRepository.save(bookmark);
        return true;// 추가 성공
    }

    /**
     * 북마크 제거, 즐겨찾기 삭제
     */
    @Transactional
    public boolean removeBookmark(String username, Long restaurantId) {
        // 1. 사용자/맛집 찾기
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("맛집을 찾을 수 없습니다."));
        // 2. 즐겨찾기 찾기
        Bookmark bookmark = bookmarkRepository.findByUserAndRestaurant(user, restaurant)
                .orElse(null);

        if (bookmark == null) {
            return false;// 즐겨찾기 안했음
        }
        // 3. 삭제
        bookmarkRepository.delete(bookmark);
        return true;// 삭제 성공
    }

    /**
     * 북마크 토글, 즐겨찾기 토글 (있으면 제거, 없으면 추가)
     */
    @Transactional
    public boolean toggleBookmark(String username, Long restaurantId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("맛집을 찾을 수 없습니다."));
        // 이미 즐겨찾기 했으면 삭제
        if (bookmarkRepository.existsByUserAndRestaurant(user, restaurant)) {
            bookmarkRepository.deleteByUserAndRestaurant(user, restaurant);
            return false; // 제거됨(🤍)
            // 없으면 추가
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .build();
            bookmarkRepository.save(bookmark);
            return true; // 추가됨 (❤️)
        }
    }

    /**
     * 사용자의 북마크 목록 조회
     */
    public List<Bookmark> getUserBookmarks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return bookmarkRepository.findByUserWithRestaurant(user);
    }

    /**
     * 북마크 여부 확인
     */
    public boolean isBookmarked(String username, Long restaurantId) {
        User user = userRepository.findByUsername(username).orElse(null);
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);

        if (user == null || restaurant == null) {
            return false;
        }

        return bookmarkRepository.existsByUserAndRestaurant(user, restaurant);
    }

    /**
     * 맛집의 북마크 수 조회
     */
    public long getBookmarkCount(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("맛집을 찾을 수 없습니다."));

        return bookmarkRepository.countByRestaurant(restaurant);
    }
}

/*
 * 핵심 역할:
 *
 * 1. 검증
 *    - 사용자 존재하는지 확인
 *    - 맛집 존재하는지 확인
 *    - 이미 즐겨찾기 했는지 확인
 *
 * 2. 비즈니스 로직
 *    - 토글 기능 (있으면 삭제, 없으면 추가)
 *    - 중복 방지 (이미 즐겨찾기면 추가 안함)
 *
 * 3. Repository 호출
 *    - 검증 통과하면 DB 작업 실행
 *
 *
 * 동작 흐름 예시:
 *
 * 즐겨찾기 버튼 클릭:
 * Controller → toggleBookmark() →
 *   있으면? → 삭제 → false반환 → 🤍 표시
 *   없으면? → 추가 → true반환 → ❤️ 표시
 *
 * 맛집 상세페이지 열기:
 * Controller → isBookmarked() → true/false → 버튼 색깔 결정
 * Controller → getBookmarkCount() → 125 → "❤️ 125명" 표시
 */