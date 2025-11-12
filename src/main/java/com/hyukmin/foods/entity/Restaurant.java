package com.hyukmin.foods.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

/**
 * Restaurant - 맛집 엔티티
 *
 * 이 파일이 하는 일:
 * 1. 맛집 정보 데이터를 표현 (이름, 카테고리, 주소, 전화번호 등)
 * 2. DB의 restaurant 테이블과 1:1 매핑
 * 3. 맛집 이미지, 작성자와의 연관 관계 관리
 *
 * 왜 필요한가?
 * - 사용자들이 맛집 정보를 등록하고 공유하기 위해
 * - 한 맛집에 여러 이미지를 등록할 수 있음
 * - 한 사용자가 여러 맛집을 등록할 수 있음
 * - 맛집 조회수, 별점 등의 정보 관리
 *
 * 연결되는 파일:
 * → User 엔티티: 맛집 등록자(작성자) 정보
 * → RestaurantImage 엔티티: 맛집에 등록된 이미지들 (1:N 관계)
 * → Bookmark 엔티티: 사용자들의 즐겨찾기 정보
 * → RestaurantRepository: 맛집 데이터 조회/저장
 * → RestaurantService: 맛집 비즈니스 로직
 * → RestaurantController: 맛집 요청 처리
 */
@Entity // JAP에서 사용하는 엔티티이다. Restaurant 클래스는 DB 테이블과 연결,
        // 즉, Restaurant 라는 이름과 테이블로 매핑이 된다. 서로 연결이 된다는 뜻
public class Restaurant {

    @Id  // 이 필드가 기본키 Primary Key라는 의미
    // PK는 중복이 될수없고 각 엔티티를 유일하게 식별하는 값
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ID값을 DB가 자동으로 증가시키는 방식
    // 즉 ID와 GeneratedBalue 는 기본키를 DB에서 자동으로 1,2,3 순서대로 증가시키는 것
    // 상품을 등록할수록 1,2 순으로 번호를 자동으로 매겨주는 방식
    private Long id;
    // 대문자를 쓰는 경우는 null값을 가지기 위함, 소문자는 0부터 시작하기에 null값을 가질수없다
    // 그래서 1번 2번 3번 이런경우를 대비해서 Long 대문자를 쓰는게 원칙으로 가진다.


    private String name;        // 식당 이름
    private String category;    // 한식, 중식, 양식 등
    private String address;     // 주소
    private String phone;       // 전화번호
    private String description; // 설명

    //TODO  ========== 🆕 새로 추가된 필드들 ========== 10월 10일
//    @Deprecated // 더 이상 사용하지 않음 (하위 호환성 유지용) 삭제함 10월10일 오후12시26분
//    private String imagePath;   // 이미지 경로 (예: /images/pizza.jpg)
    private Integer price;      // 가격 (예: 15000)
    private Integer rating;     // 별점 (1~5)
    private Integer viewCount = 0;  // 조회수 추가
    // =========================================


    // 대표 이미지 경로 캐싱용
    // 매번 이미지 리스트를 조회하지 않고 빠르게 대표 이미지를 가져오기 위함
    // ========== 🆕 대표 이미지 경로 필드 추가 ==========
    private String mainImagePath;  // 대표 이미지 경로 캐싱용
    // =============================================
    // 맛집 등록 날짜 (자동으로 현재 시간 저장)
    private LocalDateTime createdAt = LocalDateTime.now();
    //"이 객체가 언제 만들어졌는지" 를 기록하기 위한 용도
    // getter, setter

    // 위도, 경도 (지도 표시용)
    private Double latitude;   // 위도
    private Double longitude;  // 경도
    // ========== 🆕 작성자 필드 추가 ==========
    // 맛집 작성자 (등록한 사용자)
    // N:1 관계 - 여러 맛집(N) : 한 명의 사용자(1)
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = true)  // 기존 데이터 때문에 nullable
    private User author;  // 작성자


    // 맛집 이미지 목록
    // 1:N 관계 - 한 개의 맛집(1) : 여러 이미지(N)
    // cascade: 맛집 삭제 시 이미지도 함께 삭제
    // orphanRemoval: 이미지 리스트에서 제거되면 DB에서도 삭제
    // ========== 다중 이미지 관계 추가 ==========
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantImage> images = new ArrayList<>();

    /**
     * 대표 이미지 경로 가져오기
     *
     * 우선순위:
     * 1. mainImagePath 필드 값이 있으면 반환
     * 2. 없으면 isMainImage=true인 이미지 찾기
     * 3. 없으면 첫 번째 이미지
     * 4. 이미지가 하나도 없으면 null
     */
    public String getMainImagePath() {
        // 캐싱된 값이 있으면 반환
        if (mainImagePath != null && !mainImagePath.isEmpty()) {
            return mainImagePath;
        }

        // 이미지가 없으면 null
        if (images == null || images.isEmpty()) {
            return null;
        }

        // 대표 이미지로 설정된 것 찾기
        for (RestaurantImage image : images) {
            if (image.getIsMainImage() != null && image.getIsMainImage()) {
                return image.getFilePath();
            }
        }

        // 대표 이미지가 없으면 첫 번째 이미지 반환
        return images.get(0).getFilePath();
    }

    /**
     * 대표 이미지 경로 설정
     */
    public void setMainImagePath(String mainImagePath) {
        this.mainImagePath = mainImagePath;
    }

    /**
     * 이미지 추가
     * 양방향 관계 설정: restaurant ↔ image
     */
    public void addImage(RestaurantImage image) {
        images.add(image);
        image.setRestaurant(this);
    }

    /**
     * 이미지 제거
     * 양방향 관계 해제
     */
    public void removeImage(RestaurantImage image) {
        images.remove(image);
        image.setRestaurant(null);
    }

    /**
     * 조회수 증가
     * 언제 사용? RestaurantService에서 맛집 상세보기 할 때
     */
    public void increaseViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    // ========== Getter / Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    // ✅ 여기에 추가!
    public Integer getViewCount() { return viewCount;    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RestaurantImage> getImages() {
        return images;
    }

    public void setImages(List<RestaurantImage> images) {
        this.images = images;
    }

//위도 경도 추가 getter, setter

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // ========== 작성자 Getter/Setter ==========
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }



}

/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 맛집 등록
 *
 *   사용자(hyukmin): 새 맛집 "강남 피자집" 등록
 *   ↓
 *   restaurant-form.html에서 정보 입력:
 *   - 이름: 강남 피자집
 *   - 카테고리: 양식
 *   - 주소: 서울 강남구 ...
 *   - 전화번호: 02-1234-5678
 *   - 가격: 15000원
 *   - 별점: 5점
 *   - 이미지: 3개 업로드
 *   ↓
 *   POST /restaurants
 *   ↓
 *   RestaurantController.create()
 *   ↓
 *   RestaurantService.save()
 *   - Restaurant 객체 생성
 *   ↓
 *   Restaurant restaurant = new Restaurant();
 *   restaurant.setName("강남 피자집");
 *   restaurant.setCategory("양식");
 *   restaurant.setAddress("서울 강남구...");
 *   restaurant.setPhone("02-1234-5678");
 *   restaurant.setPrice(15000);
 *   restaurant.setRating(5);
 *   restaurant.setViewCount(0);
 *   restaurant.setAuthor(hyukmin);
 *   restaurant.setCreatedAt(현재시간);
 *   ↓
 *   이미지 파일 저장 및 연결:
 *   - RestaurantImage image1 = 이미지1 저장
 *   - restaurant.addImage(image1);
 *   - RestaurantImage image2 = 이미지2 저장
 *   - restaurant.addImage(image2);
 *   - RestaurantImage image3 = 이미지3 저장
 *   - restaurant.addImage(image3);
 *   ↓
 *   RestaurantRepository.save(restaurant)
 *   ↓
 *   DB restaurant 테이블에 저장:
 *   +----+----------------+----------+-------------------+----------------+-------+--------+------------+---------------------+
 *   | id | name           | category | address           | phone          | price | rating | view_count | created_at          |
 *   +----+----------------+----------+-------------------+----------------+-------+--------+------------+---------------------+
 *   | 1  | 강남 피자집     | 양식     | 서울 강남구...     | 02-1234-5678   | 15000 | 5      | 0          | 2025-01-15 10:30:00 |
 *   +----+----------------+----------+-------------------+----------------+-------+--------+------------+---------------------+
 *   ↓
 *   redirect:/restaurants (맛집 목록 페이지로)
 *
 *
 * 시나리오 2) 맛집 목록 조회
 *
 *   사용자: 맛집 목록 페이지 접속
 *   ↓
 *   GET /restaurants
 *   ↓
 *   RestaurantController.list()
 *   ↓
 *   RestaurantService.findAll()
 *   ↓
 *   RestaurantRepository.findAll()
 *   ↓
 *   DB에서 restaurant 테이블 전체 조회
 *   ↓
 *   각 행이 Restaurant 객체로 변환:
 *   List<Restaurant> restaurants = [
 *       Restaurant {
 *           id=1,
 *           name="강남 피자집",
 *           category="양식",
 *           price=15000,
 *           rating=5,
 *           viewCount=23,
 *           mainImagePath="/uploads/images/pizza1.jpg"
 *       },
 *       Restaurant {
 *           id=2,
 *           name="홍대 떡볶이",
 *           category="한식",
 *           price=8000,
 *           rating=4,
 *           viewCount=45,
 *           mainImagePath="/uploads/images/tteokbokki1.jpg"
 *       },
 *       Restaurant {
 *           id=3,
 *           name="이태원 스시",
 *           category="일식",
 *           price=25000,
 *           rating=5,
 *           viewCount=67,
 *           mainImagePath="/uploads/images/sushi1.jpg"
 *       }
 *   ]
 *   ↓
 *   restaurant-list.html에 맛집 목록 표시:
 *
 *   ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
 *   │ [이미지]       │  │ [이미지]       │  │ [이미지]       │
 *   │ 강남 피자집    │  │ 홍대 떡볶이    │  │ 이태원 스시    │
 *   │ 양식           │  │ 한식           │  │ 일식           │
 *   │ 15,000원       │  │ 8,000원        │  │ 25,000원       │
 *   │ ★★★★★ 5.0  │  │ ★★★★☆ 4.0  │  │ ★★★★★ 5.0  │
 *   │ 조회수: 23     │  │ 조회수: 45     │  │ 조회수: 67     │
 *   └────────────────┘  └────────────────┘  └────────────────┘
 *
 *
 * 시나리오 3) 맛집 상세보기
 *
 *   사용자: "강남 피자집" 클릭
 *   ↓
 *   GET /restaurants/1
 *   ↓
 *   RestaurantController.detail(1)
 *   ↓
 *   RestaurantService.findById(1)
 *   ↓
 *   RestaurantRepository.findById(1)
 *   ↓
 *   DB에서 id=1인 맛집 조회:
 *   Restaurant restaurant = {
 *       id: 1,
 *       name: "강남 피자집",
 *       category: "양식",
 *       address: "서울 강남구...",
 *       phone: "02-1234-5678",
 *       price: 15000,
 *       rating: 5,
 *       viewCount: 23,
 *       images: [image1, image2, image3],
 *       author: User객체 { username: "hyukmin" },
 *       latitude: 37.5012,
 *       longitude: 127.0396
 *   }
 *   ↓
 *   조회수 증가:
 *   restaurant.increaseViewCount();  // viewCount: 23 → 24
 *   RestaurantRepository.save(restaurant);
 *   ↓
 *   restaurant-detail.html에 맛집 상세 정보 표시:
 *
 *   ┌───────────────────────────────────────┐
 *   │ 강남 피자집                            │
 *   │ 작성자: hyukmin | 조회수: 24           │
 *   ├───────────────────────────────────────┤
 *   │ [이미지1] [이미지2] [이미지3]          │
 *   ├───────────────────────────────────────┤
 *   │ 카테고리: 양식                         │
 *   │ 가격: 15,000원                         │
 *   │ 별점: ★★★★★ 5.0                   │
 *   │ 주소: 서울 강남구...                   │
 *   │ 전화번호: 02-1234-5678                 │
 *   │ 설명: 정통 이탈리아 피자...            │
 *   ├───────────────────────────────────────┤
 *   │ [지도]                                 │
 *   │ 위치: 위도 37.5012, 경도 127.0396      │
 *   └───────────────────────────────────────┘
 *
 *   [즐겨찾기] [수정] [삭제]
 *
 *
 * 시나리오 4) 맛집 수정
 *
 *   사용자(hyukmin): "강남 피자집" 정보 수정
 *   - 가격: 15,000원 → 18,000원
 *   - 설명 수정
 *   ↓
 *   GET /restaurants/1/edit (수정 폼 표시)
 *   ↓
 *   정보 수정 후 제출
 *   ↓
 *   POST /restaurants/1/edit
 *   ↓
 *   RestaurantController.update(1)
 *   ↓
 *   RestaurantService.update(1, updatedRestaurant)
 *   - 기존 맛집 조회
 *   - 정보 수정
 *   ↓
 *   Restaurant restaurant = restaurantRepository.findById(1);
 *   restaurant.setPrice(18000);
 *   restaurant.setDescription("정통 이탈리아 피자 전문점...");
 *   ↓
 *   RestaurantRepository.save(restaurant)
 *   ↓
 *   DB 업데이트:
 *   UPDATE restaurant SET price = 18000, description = '...' WHERE id = 1;
 *   ↓
 *   redirect:/restaurants/1 (수정된 맛집 상세페이지로)
 *
 *
 * 시나리오 5) 카테고리별 맛집 조회
 *
 *   사용자: "양식" 카테고리 클릭
 *   ↓
 *   GET /restaurants?category=양식
 *   ↓
 *   RestaurantController.list("양식")
 *   ↓
 *   RestaurantService.findByCategory("양식")
 *   ↓
 *   RestaurantRepository.findByCategory("양식")
 *   ↓
 *   DB 조회:
 *   SELECT * FROM restaurant WHERE category = '양식';
 *   ↓
 *   결과:
 *   List<Restaurant> restaurants = [
 *       Restaurant { id=1, name="강남 피자집", category="양식" },
 *       Restaurant { id=5, name="이태원 파스타", category="양식" },
 *       Restaurant { id=8, name="홍대 스테이크", category="양식" }
 *   ]
 *   ↓
 *   restaurant-list.html에 양식 맛집만 표시
 *
 *
 * 시나리오 6) 대표 이미지 가져오기
 *
 *   맛집 목록 페이지에서 각 맛집의 대표 이미지를 표시해야 함
 *   ↓
 *   HTML: <img src="${restaurant.mainImagePath}">
 *   ↓
 *   restaurant.getMainImagePath() 호출
 *   ↓
 *   1. mainImagePath 필드에 값이 있나?
 *      → 있으면 바로 반환 (빠름)
 *   ↓
 *   2. 없으면 images 리스트 확인
 *      → isMainImage=true인 이미지 찾기
 *   ↓
 *   3. 대표 이미지가 없으면?
 *      → 첫 번째 이미지 반환
 *   ↓
 *   4. 이미지가 하나도 없으면?
 *      → null 반환 (기본 이미지 표시)
 *
 *
 * 시나리오 7) 맛집에 이미지 추가
 *
 *   관리자: "강남 피자집"에 새 이미지 추가
 *   ↓
 *   RestaurantImage newImage = new RestaurantImage();
 *   newImage.setFilePath("/uploads/images/pizza4.jpg");
 *   ↓
 *   restaurant.addImage(newImage);
 *   ↓
 *   addImage() 메서드 동작:
 *   1. images 리스트에 newImage 추가
 *   2. newImage.setRestaurant(this) 호출 (양방향 연결)
 *   ↓
 *   RestaurantRepository.save(restaurant)
 *   ↓
 *   DB restaurant_image 테이블에 새 행 추가:
 *   INSERT INTO restaurant_image (restaurant_id, file_path, is_main_image)
 *   VALUES (1, '/uploads/images/pizza4.jpg', false);
 *
 *
 * ========== DB 테이블 구조 ==========
 *
 * restaurant 테이블:
 *
 * +------------------+--------------+------+-----+---------+----------------+
 * | Field            | Type         | Null | Key | Default | Extra          |
 * +------------------+--------------+------+-----+---------+----------------+
 * | id               | bigint       | NO   | PRI | NULL    | auto_increment |
 * | name             | varchar(255) | YES  |     | NULL    |                |
 * | category         | varchar(255) | YES  |     | NULL    |                |
 * | address          | varchar(255) | YES  |     | NULL    |                |
 * | phone            | varchar(255) | YES  |     | NULL    |                |
 * | description      | text         | YES  |     | NULL    |                |
 * | price            | int          | YES  |     | NULL    |                |
 * | rating           | int          | YES  |     | NULL    |                |
 * | view_count       | int          | YES  |     | 0       |                |
 * | main_image_path  | varchar(255) | YES  |     | NULL    |                |
 * | created_at       | datetime     | YES  |     | NULL    |                |
 * | latitude         | double       | YES  |     | NULL    |                |
 * | longitude        | double       | YES  |     | NULL    |                |
 * | author_id        | bigint       | YES  | FK  | NULL    |                |
 * +------------------+--------------+------+-----+---------+----------------+
 *
 * 실제 데이터 예시:
 * +----+----------------+----------+-------------------+----------------+-------+--------+------------+---------------------+-----------+
 * | id | name           | category | address           | phone          | price | rating | view_count | author_id           | latitude  |
 * +----+----------------+----------+-------------------+----------------+-------+--------+------------+---------------------+-----------+
 * | 1  | 강남 피자집     | 양식     | 서울 강남구...     | 02-1234-5678   | 15000 | 5      | 24         | 5                   | 37.5012   |
 * | 2  | 홍대 떡볶이     | 한식     | 서울 마포구...     | 02-2345-6789   | 8000  | 4      | 45         | 7                   | 37.5563   |
 * | 3  | 이태원 스시     | 일식     | 서울 용산구...     | 02-3456-7890   | 25000 | 5      | 67         | 5                   | 37.5345   |
 * +----+----------------+----------+-------------------+----------------+-------+--------+------------+---------------------+-----------+
 *
 * 외래키 관계:
 * - author_id → users 테이블의 id (맛집 등록자)
 *
 *
 * ========== 엔티티 관계도 ==========
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Restaurant (맛집)
 *   ↓ 1
 *   | (한 개의 맛집에)
 *   ↓ N
 * RestaurantImage (맛집 이미지)
 *
 * Restaurant (맛집)
 *   ↓ 1
 *   | (한 개의 맛집이)
 *   ↓ N
 * Bookmark (즐겨찾기)
 *   ↓ N
 *   | (여러 사용자에게)
 *   ↓ 1
 * User (사용자)
 *
 * 의미:
 * - 한 명의 사용자(User)가 여러 맛집(Restaurant)을 등록할 수 있음
 * - 한 개의 맛집(Restaurant)에 여러 이미지(RestaurantImage)를 등록할 수 있음
 * - 한 개의 맛집(Restaurant)이 여러 사용자에게 즐겨찾기(Bookmark)될 수 있음
 *
 *
 * ========== 카테고리 분류 ==========
 *
 * category 필드 값:
 * - "한식": 김치찌개, 떡볶이, 불고기 등
 * - "중식": 짜장면, 짬뽕, 탕수육 등
 * - "일식": 스시, 라멘, 돈까스 등
 * - "양식": 피자, 파스타, 스테이크 등
 * - "분식": 떡볶이, 순대, 튀김 등
 * - "카페": 커피, 디저트 등
 * - "기타": 분류되지 않는 음식점
 *
 *
 * ========== 조회수 관리 ==========
 *
 * increaseViewCount() 메서드:
 * - 맛집 상세 페이지 조회 시 자동으로 호출
 * - null 체크 후 1씩 증가
 * - 조회수가 높은 순으로 인기 맛집 정렬 가능
 *
 * 예시:
 * SELECT * FROM restaurant ORDER BY view_count DESC LIMIT 10;
 * → 조회수 상위 10개 맛집 조회
 */
