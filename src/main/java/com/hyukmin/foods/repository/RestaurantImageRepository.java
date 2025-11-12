package com.hyukmin.foods.repository;

import com.hyukmin.foods.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * RestaurantImageRepository - 맛집 사진 DB 작업
 *
 * 이 파일 역할: 맛집 사진 조회/저장/삭제
 *
 * 데이터 흐름:
 * 사진업로드/조회 → Controller → Service → Repository(여기!) → DB
 */
public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {

    // 맛집의 모든 사진 가져오기
    // SQL: SELECT * FROM restaurant_image WHERE restaurant_id=?
    List<RestaurantImage> findByRestaurantId(Long restaurantId);

    // 맛집의 대표 사진 1장만 가져오기 (썸네일용)
    // SQL: SELECT * FROM restaurant_image WHERE restaurant_id=? AND is_main_image=true
    RestaurantImage findByRestaurantIdAndIsMainImageTrue(Long restaurantId);
}
/*
 * 핵심 동작 2가지:
 *
 * 1. 맛집 상세페이지
 *    맛집 클릭 → findByRestaurantId() → 모든 사진 슬라이드로 표시
 *
 * 2. 맛집 목록페이지
 *    목록 보기 → findByRestaurantIdAndIsMainImageTrue() → 대표사진만 표시
 */