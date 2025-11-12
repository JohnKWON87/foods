package com.hyukmin.foods.entity;

import jakarta.persistence.*;
/**
 * RestaurantImage - 맛집 이미지 엔티티
 *
 * 이 파일이 하는 일:
 * 1. 맛집에 업로드된 이미지 정보를 저장
 * 2. DB의 restaurant_image 테이블과 1:1 매핑
 * 3. 파일명, 경로, 크기, 설명, 대표 이미지 여부 등을 관리
 *
 * 왜 필요한가?
 * - 한 맛집에 여러 이미지(메뉴 사진)를 등록할 수 있도록
 * - 대표 이미지를 지정하여 목록에서 표시
 * - 원본 파일명과 저장된 파일명을 분리하여 보안 및 관리 향상
 * - 각 이미지에 대한 설명(메뉴명, 설명 등) 추가 가능
 *
 * 연결되는 파일:
 * → Restaurant 엔티티: 이미지가 속한 맛집 (N:1 관계)
 * → RestaurantImageRepository: 이미지 데이터 조회/저장
 * → FileUploadService: 이미지 파일 업로드/저장 처리
 * → RestaurantService: 맛집 등록/수정 시 이미지 관리
 * → RestaurantController: 이미지 업로드 요청 처리
 */
@Entity
public class RestaurantImage {

    // 이미지 고유 번호 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 저장된 파일명 (UUID + 확장자)
    // 예: "a3b5c7d9-e1f2-4a5b-8c9d-0e1f2a3b4c5d_pizza.jpg"
    // 이유: 파일명 중복 방지, 보안 강화
    private String fileName;      // 저장된 파일명 (예: uuid_pizza.jpg)

    // 원본 파일명 (사용자가 업로드한 파일명)
    // 예: "맛있는짬뽕.jpg"
    // 이유: 사용자에게 표시하거나 다운로드 시 사용
    private String originalName;  // 원본 파일명 (예: 짬뽕.jpg)

    // 파일 경로 (브라우저에서 접근 가능한 URL 경로)
    // 예: "/uploads/images/a3b5c7d9-e1f2-4a5b-8c9d-0e1f2a3b4c5d_pizza.jpg"
    // 이유: HTML에서 <img src="..."> 태그로 표시
    private String filePath;      // 파일 경로 (예: /uploads/images/uuid_pizza.jpg)

    // 파일 크기 (바이트 단위)
    // 예: 1024000 (약 1MB)
    // 이유: 저장 공간 관리, 파일 크기 제한 체크
    private Long fileSize;        // 파일 크기

    // 이미지 설명 (메뉴명, 설명 등)
    // 예: "짬뽕", "매운 짬뽕 8,000원"
    // 최대 500자
    @Column(length = 500)
    private String description;   // 🆕 메뉴 설명

    // 대표 이미지 여부
    // true: 맛집 목록에서 표시할 대표 이미지
    // false: 일반 이미지
    // null: 설정되지 않음
    private Boolean isMainImage;  // 대표 이미지 여부


    // 이미지가 속한 맛집
    // N:1 관계 - 여러 이미지(N) : 한 개의 맛집(1)
    // LAZY: 이미지 조회 시 맛집 정보는 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // ========== Getter / Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    // 🆕 추가
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsMainImage() {
        return isMainImage;
    }

    public void setIsMainImage(Boolean isMainImage) {
        this.isMainImage = isMainImage;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    /**
     * 하위 호환성 메서드
     * 기존 코드에서 getMenuName() 호출하는 곳을 위해 유지
     * description이 있으면 반환, 없으면 originalName 반환
     */
    public String getMenuName() {
        return description != null ? description : originalName;
    }

    public void setMenuName(String menuName) {
        this.description = menuName;
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 맛집 등록 시 이미지 3개 업로드
 *
 *   사용자(hyukmin): "강남 피자집" 등록하면서 이미지 3개 업로드
 *   - 이미지1: "피자1.jpg" (대표 이미지)
 *   - 이미지2: "피자2.jpg"
 *   - 이미지3: "인테리어.jpg"
 *   ↓
 *   POST /restaurants (이미지 파일 포함)
 *   ↓
 *   RestaurantController.create()
 *   ↓
 *   FileUploadService.uploadFile(이미지1)
 *   - UUID 생성: "a3b5c7d9-e1f2-4a5b-8c9d-0e1f2a3b4c5d"
 *   - 파일명: "a3b5c7d9-e1f2-4a5b-8c9d-0e1f2a3b4c5d_pizza1.jpg"
 *   - 실제 저장 경로: "uploads/images/a3b5c7d9-..._pizza1.jpg"
 *   - 반환 경로: "/uploads/images/a3b5c7d9-..._pizza1.jpg"
 *   ↓
 *   RestaurantImage image1 = new RestaurantImage();
 *   image1.setFileName("a3b5c7d9-e1f2-4a5b-8c9d-0e1f2a3b4c5d_pizza1.jpg");
 *   image1.setOriginalName("피자1.jpg");
 *   image1.setFilePath("/uploads/images/a3b5c7d9-..._pizza1.jpg");
 *   image1.setFileSize(1024000L);  // 1MB
 *   image1.setDescription("마르게리타 피자");
 *   image1.setIsMainImage(true);    // 대표 이미지로 설정
 *   image1.setRestaurant(restaurant);
 *   ↓
 *   (이미지2, 이미지3도 동일하게 처리, 단 isMainImage=false)
 *   ↓
 *   RestaurantImageRepository.save(image1);
 *   RestaurantImageRepository.save(image2);
 *   RestaurantImageRepository.save(image3);
 *   ↓
 *   DB restaurant_image 테이블에 저장:
 *   +----+----------------------------------------+---------------+-------------------------------+----------+--------------+---------------+
 *   | id | file_name                              | original_name | file_path                     | file_size| description  | is_main_image |
 *   +----+----------------------------------------+---------------+-------------------------------+----------+--------------+---------------+
 *   | 1  | a3b5c7d9-..._pizza1.jpg                | 피자1.jpg     | /uploads/images/a3b5c7d9-...  | 1024000  | 마르게리타   | 1 (true)      |
 *   | 2  | b4c6d8e0-..._pizza2.jpg                | 피자2.jpg     | /uploads/images/b4c6d8e0-...  | 890000   | 페퍼로니     | 0 (false)     |
 *   | 3  | c5d7e9f1-..._interior.jpg              | 인테리어.jpg  | /uploads/images/c5d7e9f1-...  | 2048000  | 매장 내부    | 0 (false)     |
 *   +----+----------------------------------------+---------------+-------------------------------+----------+--------------+---------------+
 *   ↓
 *   맛집 등록 완료!
 *
 *
 * 시나리오 2) 맛집 상세 페이지에서 이미지 표시
 *
 *   사용자: "강남 피자집" 상세 페이지 접속
 *   ↓
 *   GET /restaurants/1
 *   ↓
 *   RestaurantController.detail(1)
 *   ↓
 *   RestaurantService.findById(1)
 *   ↓
 *   Restaurant restaurant = { ..., images: [image1, image2, image3] }
 *   ↓
 *   restaurant-detail.html:
 *
 *   <div class="image-gallery">
 *       <!-- 대표 이미지 (크게 표시) -->
 *       <img src="/uploads/images/a3b5c7d9-..._pizza1.jpg" class="main-image">
 *       <p>마르게리타 피자</p>
 *
 *       <!-- 나머지 이미지들 -->
 *       <img src="/uploads/images/b4c6d8e0-..._pizza2.jpg" class="thumbnail">
 *       <p>페퍼로니 피자</p>
 *
 *       <img src="/uploads/images/c5d7e9f1-..._interior.jpg" class="thumbnail">
 *       <p>매장 내부</p>
 *   </div>
 *
 *
 * 시나리오 3) 맛집 목록에서 대표 이미지만 표시
 *
 *   사용자: 맛집 목록 페이지 접속
 *   ↓
 *   GET /restaurants
 *   ↓
 *   RestaurantController.list()
 *   ↓
 *   각 맛집의 대표 이미지만 가져오기:
 *   restaurant.getMainImagePath()
 *   ↓
 *   Restaurant 엔티티의 getMainImagePath() 메서드:
 *   1. images 리스트에서 isMainImage=true인 이미지 찾기
 *   2. 찾으면 그 이미지의 filePath 반환
 *   3. 없으면 첫 번째 이미지의 filePath 반환
 *   ↓
 *   restaurant-list.html:
 *
 *   ┌─────────────────────┐
 *   │ [대표 이미지]        │ ← /uploads/images/a3b5c7d9-..._pizza1.jpg
 *   │ 강남 피자집          │
 *   │ 양식 | 15,000원     │
 *   └─────────────────────┘
 *
 *
 * 시나리오 4) 이미지 추가 업로드
 *
 *   관리자: "강남 피자집"에 새 메뉴 사진 추가
 *   ↓
 *   GET /restaurants/1/edit (수정 페이지)
 *   ↓
 *   새 이미지 업로드: "샐러드.jpg"
 *   ↓
 *   POST /restaurants/1/add-image
 *   ↓
 *   FileUploadService.uploadFile(새이미지)
 *   ↓
 *   RestaurantImage newImage = new RestaurantImage();
 *   newImage.setFileName("d6e8f0g2-..._salad.jpg");
 *   newImage.setOriginalName("샐러드.jpg");
 *   newImage.setFilePath("/uploads/images/d6e8f0g2-..._salad.jpg");
 *   newImage.setFileSize(750000L);
 *   newImage.setDescription("시저 샐러드");
 *   newImage.setIsMainImage(false);
 *   newImage.setRestaurant(restaurant);
 *   ↓
 *   RestaurantImageRepository.save(newImage);
 *   ↓
 *   DB에 새 행 추가:
 *   | 4  | d6e8f0g2-..._salad.jpg | 샐러드.jpg | /uploads/images/d6e8f0g2-... | 750000 | 시저 샐러드 | 0 (false) |
 *   ↓
 *   redirect:/restaurants/1 (수정된 맛집 상세페이지로)
 *
 *
 * 시나리오 5) 대표 이미지 변경
 *
 *   관리자: "강남 피자집"의 대표 이미지를 이미지2로 변경
 *   ↓
 *   POST /restaurants/1/change-main-image?imageId=2
 *   ↓
 *   RestaurantImageService.changeMainImage(1, 2)
 *   ↓
 *   1. 기존 대표 이미지 찾기 (image1):
 *      image1.setIsMainImage(false);
 *   ↓
 *   2. 새 대표 이미지로 설정 (image2):
 *      image2.setIsMainImage(true);
 *   ↓
 *   RestaurantImageRepository.save(image1);
 *   RestaurantImageRepository.save(image2);
 *   ↓
 *   DB 업데이트:
 *   | 1  | ... | 마르게리타 | 0 (false) |  ← 기존 대표 이미지
 *   | 2  | ... | 페퍼로니   | 1 (true)  |  ← 새 대표 이미지
 *   ↓
 *   맛집 목록에서 이제 페퍼로니 피자 사진이 대표 이미지로 표시됨
 *
 *
 * 시나리오 6) 이미지 삭제
 *
 *   관리자: "강남 피자집"의 이미지3 삭제
 *   ↓
 *   POST /restaurants/images/3/delete
 *   ↓
 *   RestaurantImageService.deleteImage(3)
 *   ↓
 *   1. DB에서 이미지 정보 조회:
 *      RestaurantImage image = findById(3);
 *      fileName = "c5d7e9f1-..._interior.jpg"
 *   ↓
 *   2. 실제 파일 삭제:
 *      File file = new File("uploads/images/c5d7e9f1-..._interior.jpg");
 *      file.delete();
 *   ↓
 *   3. DB에서 이미지 정보 삭제:
 *      RestaurantImageRepository.deleteById(3);
 *   ↓
 *   DB에서 id=3인 행 삭제:
 *   DELETE FROM restaurant_image WHERE id = 3;
 *   ↓
 *   이미지 삭제 완료!
 *
 *
 * ========== DB 테이블 구조 ==========
 *
 * restaurant_image 테이블:
 *
 * +---------------+--------------+------+-----+---------+----------------+
 * | Field         | Type         | Null | Key | Default | Extra          |
 * +---------------+--------------+------+-----+---------+----------------+
 * | id            | bigint       | NO   | PRI | NULL    | auto_increment |
 * | file_name     | varchar(255) | YES  |     | NULL    |                |
 * | original_name | varchar(255) | YES  |     | NULL    |                |
 * | file_path     | varchar(255) | YES  |     | NULL    |                |
 * | file_size     | bigint       | YES  |     | NULL    |                |
 * | description   | varchar(500) | YES  |     | NULL    |                |
 * | is_main_image | tinyint(1)   | YES  |     | NULL    |                |
 * | restaurant_id | bigint       | YES  | FK  | NULL    |                |
 * +---------------+--------------+------+-----+---------+----------------+
 *
 * 실제 데이터 예시:
 * +----+----------------------------------------+---------------+-------------------------------+----------+----------------+---------------+---------------+
 * | id | file_name                              | original_name | file_path                     | file_size| description    | is_main_image | restaurant_id |
 * +----+----------------------------------------+---------------+-------------------------------+----------+----------------+---------------+---------------+
 * | 1  | a3b5c7d9-..._pizza1.jpg                | 피자1.jpg     | /uploads/images/a3b5c7d9-...  | 1024000  | 마르게리타     | 1             | 1             |
 * | 2  | b4c6d8e0-..._pizza2.jpg                | 피자2.jpg     | /uploads/images/b4c6d8e0-...  | 890000   | 페퍼로니       | 0             | 1             |
 * | 3  | c5d7e9f1-..._interior.jpg              | 인테리어.jpg  | /uploads/images/c5d7e9f1-...  | 2048000  | 매장 내부      | 0             | 1             |
 * | 4  | d6e8f0g2-..._tteokbokki.jpg            | 떡볶이.jpg    | /uploads/images/d6e8f0g2-...  | 650000   | 매운 떡볶이    | 1             | 2             |
 * | 5  | e7f9g1h3-..._sushi1.jpg                | 스시1.jpg     | /uploads/images/e7f9g1h3-...  | 1500000  | 모둠 스시      | 1             | 3             |
 * +----+----------------------------------------+---------------+-------------------------------+----------+----------------+---------------+---------------+
 *
 * 외래키 관계:
 * - restaurant_id → restaurant 테이블의 id (이미지가 속한 맛집)
 *
 *
 * ========== 엔티티 관계도 ==========
 *
 * Restaurant (맛집)
 *   ↓ 1
 *   | (한 개의 맛집에)
 *   ↓ N
 * RestaurantImage (이미지)
 *
 * 의미:
 * - 한 개의 맛집(Restaurant)에 여러 이미지(RestaurantImage)가 등록될 수 있음
 * - 이미지는 반드시 하나의 맛집에만 속함
 *
 * 예시:
 * "강남 피자집" (restaurant_id=1):
 *   - 이미지1: 마르게리타 피자 (대표 이미지)
 *   - 이미지2: 페퍼로니 피자
 *   - 이미지3: 매장 내부
 *   - 이미지4: 시저 샐러드
 *
 *
 * ========== 파일명 생성 전략 (UUID 사용 이유) ==========
 *
 * 문제점 (UUID 사용 안 할 경우):
 * 1. 파일명 중복
 *    - 사용자A가 "피자.jpg" 업로드
 *    - 사용자B도 "피자.jpg" 업로드
 *    → 두 번째 파일이 첫 번째 파일을 덮어씀!
 *
 * 2. 보안 문제
 *    - 원본 파일명 노출: "회사기밀문서.jpg"
 *    - 파일명 예측 가능: "image1.jpg", "image2.jpg"
 *    → 다른 사람의 파일을 추측하여 접근 가능
 *
 * 3. 특수문자 문제
 *    - 한글 파일명: "맛있는 짬뽕.jpg"
 *    - 공백, 특수문자: "pizza&pasta.jpg"
 *    → 일부 시스템에서 오류 발생
 *
 * 해결책 (UUID 사용):
 * - UUID 생성: "a3b5c7d9-e1f2-4a5b-8c9d-0e1f2a3b4c5d"
 * - 저장 파일명: "a3b5c7d9-..._pizza.jpg"
 * - 장점:
 *   1. 중복 불가능 (UUID는 고유값)
 *   2. 예측 불가능 (보안 강화)
 *   3. 특수문자 없음 (안정성)
 *   4. 원본 파일명은 originalName 필드에 별도 보관
 *
 *
 * ========== 대표 이미지 설정 로직 ==========
 *
 * isMainImage 필드:
 * - true: 대표 이미지 (맛집당 1개만 가능)
 * - false 또는 null: 일반 이미지
 *
 * 규칙:
 * 1. 첫 번째 이미지 업로드 시: 자동으로 대표 이미지로 설정
 * 2. 대표 이미지 변경 시:
 *    - 기존 대표 이미지: isMainImage=false로 변경
 *    - 새 대표 이미지: isMainImage=true로 설정
 * 3. 대표 이미지 삭제 시:
 *    - 남은 이미지 중 첫 번째를 대표 이미지로 자동 설정
 *
 *
 * ========== 파일 크기 제한 ==========
 *
 * fileSize 필드 활용:
 * - 이미지당 최대 5MB 제한 가능
 * - 전체 저장 공간 관리
 * - 큰 파일 업로드 방지
 *
 * 예시 (FileUploadService):
 * if (file.getSize() > 5 * 1024 * 1024) {  // 5MB
 *     throw new RuntimeException("파일 크기는 5MB를 초과할 수 없습니다");
 * }
 *
 *
 * ========== 하위 호환성 메서드 ==========
 *
 * getMenuName() / setMenuName():
 * - 기존 코드에서 menuName 필드를 사용했던 곳을 위해 유지
 * - 실제로는 description 필드를 사용
 * - 점진적 마이그레이션 가능
 *
 * 예시:
 * // 기존 코드
 * image.setMenuName("짬뽕");
 * String name = image.getMenuName();
 *
 * // 실제로는 description에 저장됨
 * image.description = "짬뽕";
 */