package com.hyukmin.foods.repository;
// 인터페이스는 많이 쓰이는 기능(메서드)을 미리 **“설계도”**처럼 정의해 놓고,
// 필요할 때 언제든 구현해서 쓸 수 있게 만드는 도구.
// 객체지향 설계에서 유연성, 일관성, 다형성을 지원하는 핵심 기능
import com.hyukmin.foods.entity.Restaurant;
//레스토랑 객체와 DB 연결을 위한 선언
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository를 가져오는 것이다.
// JPA에서 제공하는 기본적인 DB 작업(저장, 삭제, 조회 등) 메서드가 미리 구현된 인터페이스입니다.
/**
 * RestaurantRepository - 맛집 DB 작업
 *
 * 이 파일 역할: 맛집 검색/조회/저장/삭제
 *
 * 데이터 흐름:
 * 맛집검색/등록 → Controller → Service → Repository(여기!) → DB
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // JpaRepository<Restaurant, Long>: 맛집 데이터를 다루는 저장소
    // - Restaurant: 다룰 엔티티
    // - Long: 맛집 ID 타입

    // 기본 제공 메서드 (JpaRepository가 자동으로 만들어줌):
    // save(restaurant) - 맛집 저장
    // findById(id) - ID로 맛집 찾기
    // findAll() - 모든 맛집 조회
    // delete(restaurant) - 맛집 삭제
// Restaurant 엔티티를, id 타입(Long)으로 CRUD 작업하는 저장소 정의

// Repository : 저장소 역할
// enum은 고정된 값들의 “집합” (ex. 상영관 종류, 가격표, 요일 등) 을 미리 정의해 둠.
// 특별히 메서드 넣을 수도 있지만, 보통은 상수화된 고유명사(값) 개념으로 사용

    // ✅ 검색 메서드 추가
    // 이름으로 검색 (예: "피자" 검색)
    // SQL: SELECT * FROM restaurant WHERE name LIKE '%피자%'
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    // 카테고리로 검색 (예: "한식" 검색)
    // SQL: SELECT * FROM restaurant WHERE category='한식'
    List<Restaurant> findByCategory(String category);

    // 이름 또는 주소로 검색 (예: "강남" 검색)
    // SQL: SELECT * FROM restaurant WHERE name LIKE '%강남%' OR address LIKE '%강남%'
    List<Restaurant> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name, String address);

    // 🔥 이 메서드 추가!
    // 키워드 + 카테고리 동시 검색 (예: "피자" + "양식" 검색)
    // SQL: SELECT * FROM restaurant
    //      WHERE (name LIKE '%피자%' AND category='양식')
    //         OR (address LIKE '%피자%' AND category='양식')
    List<Restaurant> findByNameContainingIgnoreCaseAndCategoryOrAddressContainingIgnoreCaseAndCategory(
            String name, String category1, String address, String category2);
}

/*
 * 핵심 동작 4가지:
 *
 * 1. 맛집 등록
 *    등록폼 작성 → save() → DB에 저장 → 목록에 표시
 *
 * 2. 간단 검색 (키워드만)
 *    "피자" 입력 → findByNameContaining() → 피자 맛집 목록
 *
 * 3. 카테고리 필터
 *    "한식" 선택 → findByCategory() → 한식 맛집 목록
 *
 * 4. 상세 검색 (키워드 + 카테고리)
 *    "강남" + "일식" 선택 → findByNameContaining...AndCategory() → 결과 표시
 */




