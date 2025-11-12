package com.hyukmin.foods.repository;

import com.hyukmin.foods.entity.Bookmark;
import com.hyukmin.foods.entity.Restaurant;
import com.hyukmin.foods.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * BookmarkRepository - 즐겨찾기 DB 작업
 *
 * 이 파일 역할: 즐겨찾기 추가/삭제/조회
 *
 * 데이터 흐름:
 * 사용자 버튼클릭 → Controller → Service → Repository(여기!) → DB
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 즐겨찾기 찾기 (삭제할 때 사용)
    // SQL: SELECT * FROM bookmark WHERE user_id=? AND restaurant_id=?
    Optional<Bookmark> findByUserAndRestaurant(User user, Restaurant restaurant);

    // 즐겨찾기 했는지 확인 (버튼 색깔 바꿀 때)
    // SQL: SELECT EXISTS(...) → true/false 반환
    boolean existsByUserAndRestaurant(User user, Restaurant restaurant);

    // 내 즐겨찾기 목록 (맛집정보+이미지 한번에 가져옴)
    // JOIN FETCH: 쿼리 1번으로 모든 데이터 조회 (성능 최적화)
    @Query("SELECT b FROM Bookmark b " +
            "JOIN FETCH b.restaurant r " +
            "LEFT JOIN FETCH r.images " +
            "WHERE b.user = :user " +
            "ORDER BY b.createdAt DESC")
    List<Bookmark> findByUserWithRestaurant(@Param("user") User user);

    // 이 맛집 즐겨찾기 수 (예: 125명)
    // SQL: SELECT COUNT(*) FROM bookmark WHERE restaurant_id=?
    long countByRestaurant(Restaurant restaurant);

    // 내 즐겨찾기 수 (예: 15개)
    // SQL: SELECT COUNT(*) FROM bookmark WHERE user_id=?
    long countByUser(User user);

    // 즐겨찾기 삭제
    // SQL: DELETE FROM bookmark WHERE user_id=? AND restaurant_id=?
    void deleteByUserAndRestaurant(User user, Restaurant restaurant);
}
/*
 * 핵심 동작 3가지:
 *
 * 1. 즐겨찾기 추가
 *    🤍 버튼클릭 → save() → DB에 저장 → ❤️로 변경
 *
 * 2. 즐겨찾기 삭제
 *    ❤️ 버튼클릭 → deleteByUserAndRestaurant() → DB에서 삭제 → 🤍로 변경
 *
 * 3. 내 즐겨찾기 보기
 *    메뉴클릭 → findByUserWithRestaurant() → 맛집 목록 표시
 */