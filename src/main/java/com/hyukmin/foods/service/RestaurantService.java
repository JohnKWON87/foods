package com.hyukmin.foods.service;
// 비지니스 로직 CRUD 처리를 위함
import com.hyukmin.foods.entity.Restaurant;
import com.hyukmin.foods.entity.RestaurantImage;
import com.hyukmin.foods.repository.RestaurantRepository;
import com.hyukmin.foods.repository.RestaurantImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
/**
 * RestaurantService - 맛집 비즈니스 로직
 *
 * 이 파일 역할: 맛집 등록/수정/삭제 + 사진 관리
 *
 * 데이터 흐름:
 * Controller → Service(여기! 검증/처리) → Repository → DB
 */
@Service
// 어노테이션이 붙은 서비스 계층 클래스
// 비지니스 로직 처리 및 데이터 접근 객체 Repository(저장소)와 컨트롤러 사이를 연결하는 역할
public class RestaurantService {

//    RestaurantRepository는 타입(클래스, 인터페이스 이름)이고,
//    repository는 그 타입을 가진 변수 이름.
//    왜 변수로 선언하냐면?
//    객체를 담는 그릇이 필요하기 때문. int number, String name 과 같은 의미
//    변수명은 소문자로 시작하고 카멜 케이스(camelCase)를 사용하는 게 관례입니다.
//  int userAge;
//  String userName;
//  RestaurantRepository restaurantRepository;

    // 저장소(Repository)를 사용하기 위한 열쇠
    private final RestaurantRepository repository;
    // RestaurantRepository를 받아 사용하기 위한 필드
    // final 키워드가 있어서 한번 할당된 정보는 변하지 않는다.
    private final RestaurantImageRepository imageRepository;
    private final FileUploadService fileUploadService;

    // 생성자: 스프링이 자동으로 창고 열쇠를 줌
    public RestaurantService(RestaurantRepository repository,
                             RestaurantImageRepository imageRepository,
                             FileUploadService fileUploadService) {
        this.repository = repository;
        this.imageRepository = imageRepository;
        this.fileUploadService = fileUploadService;

// this는 같은 이름의 매개변수와 필드를 구분할 때 쓴다.
// 새로운 생성자나 매개변수를 추가할 때 사용하는 게 아니라,
// 같은 이름일 때 ‘내 필드’임을 명확히 하기 위해 쓴다.
// 같은 이름을 가진 필드와 매개변수를 구분하기 위해, 큰 메뉴판에 메뉴들을 추가한다고 보면 된다.
    }

    // 1. 모든 맛집 조회
    public List<Restaurant> findAll() {
        return repository.findAll();
        // "창고에 있는 식당 리스트 전부 가져와줘!"
    }

    // 2. 맛집 1개 조회
    public Restaurant findById(Long id) {
        return repository.findById(id).orElse(null);
        // "ID가 5번인 식당 찾아줘. 없으면 null 줘"
    }

    // 3. 맛집 저장/수정
    public Restaurant save(Restaurant restaurant) {
        return repository.save(restaurant);
        // "이 식당 정보 저장해줘 (새로 추가 or 수정)"
    }

    // 4. 맛집 삭제 (사진도 함께)
    @Transactional
    public void delete(Long id) {
        Restaurant restaurant = findById(id);
        if (restaurant != null) {
            // 사진 파일들 먼저 삭제
            for (RestaurantImage image : restaurant.getImages()) {
                fileUploadService.deleteFile(image.getFilePath());
            }
            // "ID가 3번인 식당 삭제해줘"
        }
        repository.deleteById(id);
    }

    // 5. 사진 추가
    @Transactional
    public RestaurantImage addImage(Long restaurantId, MultipartFile file,
                                    String menuName, boolean isMainImage) throws IOException {
        // 1. 맛집 찾기
        Restaurant restaurant = findById(restaurantId);
        if (restaurant == null) {
            throw new IllegalArgumentException("식당을 찾을 수 없습니다. ID: " + restaurantId);
        }

        // 2. 이미지 파일 검증
        if (!fileUploadService.isImageFile(file)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다: " + file.getOriginalFilename());
        }

        System.out.println("🖼️ 이미지 저장 시작: " + file.getOriginalFilename());
        System.out.println("   - 메뉴명: " + menuName);
        System.out.println("   - 대표 이미지: " + isMainImage);

        // 10월 13일========== 🔥 핵심 개선: 대표 이미지 처리 순서 변경 ==========
        // 대표 이미지로 설정하려는 경우, 먼저 모든 기존 이미지의 대표 플래그 해제
        // 3. 대표 이미지로 설정하려면, 기존 대표 해제
        if (isMainImage) {
            System.out.println("   - 기존 대표 이미지 해제 시작...");
            List<RestaurantImage> existingImages = imageRepository.findByRestaurantId(restaurantId);
            int unsetCount = 0;

            for (RestaurantImage existingImage : existingImages) {
                if (Boolean.TRUE.equals(existingImage.getIsMainImage())) {
                    existingImage.setIsMainImage(false);
                    imageRepository.save(existingImage);
                    unsetCount++;
                    System.out.println("   - 해제: " + existingImage.getMenuName() + " (ID: " + existingImage.getId() + ")");
                }
            }
            System.out.println("   - 총 " + unsetCount + "개 대표 이미지 해제 완료");
        }

        // 4. 파일 저장
        String filePath = fileUploadService.saveFile(file);
        System.out.println("   - 저장 경로: " + filePath);

        // 5. 이미지 엔티티 생성
        RestaurantImage image = new RestaurantImage();
        image.setFileName(file.getOriginalFilename());
        image.setOriginalName(file.getOriginalFilename());
        image.setFilePath(filePath);
        image.setFileSize(file.getSize());
        image.setDescription(menuName != null && !menuName.trim().isEmpty() ? menuName : null);
        image.setIsMainImage(isMainImage);

        // 6. 맛집과 연결
        restaurant.addImage(image);

        // 7. DB 저장
        RestaurantImage savedImage = imageRepository.save(image);

        // 8. 대표 이미지면 Restaurant에도 경로 저장
        if (isMainImage) {
            restaurant.setMainImagePath(filePath);
            repository.save(restaurant);
            System.out.println("   - Restaurant의 mainImagePath 업데이트됨" + filePath);
        }

        System.out.println("✅ 이미지 저장 완료: ID=" + savedImage.getId());

        // ========== 🔥 추가 검증: 저장 후 대표 이미지 개수 확인 ==========
        verifyMainImageCount(restaurantId);

        return savedImage;
    }

    // 6. 🆕 대표 이미지 개수 검증 메서드
    private void verifyMainImageCount(Long restaurantId) {
        List<RestaurantImage> allImages = imageRepository.findByRestaurantId(restaurantId);
        long mainImageCount = allImages.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMainImage()))
                .count();

        System.out.println("🔍 대표 이미지 검증: " + mainImageCount + "개");

        if (mainImageCount > 1) {
            System.err.println("⚠️ 경고: 대표 이미지가 " + mainImageCount + "개 발견됨!");
            // 자동 수정: 첫 번째만 남기고 나머지는 해제
            boolean foundFirst = false;
            for (RestaurantImage img : allImages) {
                if (Boolean.TRUE.equals(img.getIsMainImage())) {
                    if (foundFirst) {
                        img.setIsMainImage(false);
                        imageRepository.save(img);
                        System.out.println("   - 자동 해제: " + img.getMenuName());
                    } else {
                        foundFirst = true;
                        System.out.println("   - 대표 유지: " + img.getMenuName());
                    }
                }
            }
        } else if (mainImageCount == 0 && !allImages.isEmpty()) {
            System.out.println("⚠️ 대표 이미지가 없음. 첫 번째 이미지를 대표로 설정...");
            RestaurantImage firstImage = allImages.get(0);
            firstImage.setIsMainImage(true);
            imageRepository.save(firstImage);

            Restaurant restaurant = findById(restaurantId);
            if (restaurant != null) {
                restaurant.setMainImagePath(firstImage.getFilePath());
                repository.save(restaurant);
            }
            System.out.println("   - 대표 이미지 자동 설정: " + firstImage.getMenuName());
        }
    }

    // 🆕 5-2. 이미지 추가 (설명 포함 버전)
    @Transactional
    public RestaurantImage addImage(Long restaurantId, MultipartFile file,
                                    String menuName, String description, boolean isMainImage) throws IOException {
        Restaurant restaurant = findById(restaurantId);
        if (restaurant == null) {
            throw new IllegalArgumentException("식당을 찾을 수 없습니다. ID: " + restaurantId);
        }

        if (!fileUploadService.isImageFile(file)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
        }

        String filePath = fileUploadService.saveFile(file);

        if (isMainImage) {
            List<RestaurantImage> existingImages = imageRepository.findByRestaurantId(restaurantId);
            for (RestaurantImage existingImage : existingImages) {
                if (existingImage.getIsMainImage()) {
                    existingImage.setIsMainImage(false);
                    imageRepository.save(existingImage);
                }
            }
        }

        RestaurantImage image = new RestaurantImage();
        image.setFileName(file.getOriginalFilename());
        image.setOriginalName(file.getOriginalFilename());
        image.setFilePath(filePath);
        image.setFileSize(file.getSize());
        image.setMenuName(menuName != null && !menuName.trim().isEmpty() ? menuName : "메뉴");
        image.setDescription(description); // 설명 포함
        image.setIsMainImage(isMainImage);

        restaurant.addImage(image);
        RestaurantImage savedImage = imageRepository.save(image);

        if (isMainImage) {
            restaurant.setMainImagePath(filePath);
            repository.save(restaurant);
        }

        return savedImage;
    }

    // 6. 사진 삭제
    @Transactional
    public void deleteImage(Long imageId) {
        RestaurantImage image = imageRepository.findById(imageId).orElse(null);

        if (image == null) {
            System.err.println("❌ 삭제할 이미지를 찾을 수 없습니다. ID: " + imageId);
            return;
        }

        Restaurant restaurant = image.getRestaurant();
        boolean wasMainImage = image.getIsMainImage();

        System.out.println("🗑️ 이미지 삭제 시작: " + image.getMenuName());
        System.out.println("   - 파일 경로: " + image.getFilePath());
        System.out.println("   - 대표 이미지였는가: " + wasMainImage);

        // 파일 삭제
        fileUploadService.deleteFile(image.getFilePath());

        // DB에서 삭제
        imageRepository.deleteById(imageId);

        // 대표 이미지였다면 다른 이미지를 대표로 설정
        if (wasMainImage && restaurant != null) {
            List<RestaurantImage> remainingImages = imageRepository.findByRestaurantId(restaurant.getId());

            if (!remainingImages.isEmpty()) {
                // 첫 번째 남은 이미지를 대표로 설정
                RestaurantImage newMainImage = remainingImages.get(0);
                newMainImage.setIsMainImage(true);
                imageRepository.save(newMainImage);

                restaurant.setMainImagePath(newMainImage.getFilePath());
                repository.save(restaurant);

                System.out.println("   - 새 대표 이미지 설정: " + newMainImage.getMenuName());
            } else {
                // 이미지가 하나도 없으면 mainImagePath null로 설정
                restaurant.setMainImagePath(null);
                repository.save(restaurant);
                System.out.println("   - 모든 이미지 삭제됨, mainImagePath null로 설정");
            }
        }

        System.out.println("✅ 이미지 삭제 완료");

        // 검증
        if (restaurant != null) {
            verifyMainImageCount(restaurant.getId());
        }
    }

    // 7. 대표 이미지 변경
    @Transactional
    public void changeMainImage(Long restaurantId, Long imageId) {
        Restaurant restaurant = findById(restaurantId);

        if (restaurant == null) {
            throw new IllegalArgumentException("식당을 찾을 수 없습니다. ID: " + restaurantId);
        }

        RestaurantImage newMainImage = imageRepository.findById(imageId).orElse(null);

        if (newMainImage == null) {
            throw new IllegalArgumentException("이미지를 찾을 수 없습니다. ID: " + imageId);
        }

        System.out.println("🔄 대표 이미지 변경 시작");
        System.out.println("   - 레스토랑: " + restaurant.getName());
        System.out.println("   - 새 대표 이미지: " + newMainImage.getMenuName());

        // 모든 사진의 대표 플래그 해제 후, 선택된 것만 대표로
        List<RestaurantImage> allImages = imageRepository.findByRestaurantId(restaurantId);

        // 모든 이미지의 대표 플래그 해제 후, 선택된 이미지만 대표로 설정
        for (RestaurantImage img : allImages) {
            boolean shouldBeMain = img.getId().equals(imageId);

            // 상태가 바뀌는 경우만 업데이트
            if (Boolean.TRUE.equals(img.getIsMainImage()) != shouldBeMain) {
                img.setIsMainImage(shouldBeMain);
                imageRepository.save(img);

                if (shouldBeMain) {
                    System.out.println("   - 대표 이미지로 설정: " + img.getMenuName());
                } else {
                    System.out.println("   - 대표 해제: " + img.getMenuName());
                }
            }
        }

        // Restaurant에 대표 이미지 경로 저장
        restaurant.setMainImagePath(newMainImage.getFilePath());
        repository.save(restaurant);

        System.out.println("✅ 대표 이미지 변경 완료");

        // 검증
        verifyMainImageCount(restaurantId);
    }

    // 9. 🆕 특정 레스토랑의 대표 이미지 가져오기
    public RestaurantImage getMainImage(Long restaurantId) {
        List<RestaurantImage> images = imageRepository.findByRestaurantId(restaurantId);

        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMainImage()))
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    // 10. 🆕 레스토랑의 모든 이미지 개수 가져오기
    public int getImageCount(Long restaurantId) {
        return imageRepository.findByRestaurantId(restaurantId).size();
    }
    /**
     * ✅ 기존 이미지의 설명만 업데이트
     */
    /**
     * 기존 이미지의 설명만 업데이트
     */
    public void updateImageDescription(Long imageId, String newDescription) {
        // ✅ orElseThrow 대신 orElse(null) 사용 == 호출 위치 추적
        RestaurantImage image = imageRepository.findById(imageId).orElse(null);

        if (image == null) {
            System.err.println("⚠ 이미지를 찾을 수 없습니다. 건너뜁니다: " + imageId);
            // ✅ 스택 트레이스 출력
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                System.err.println("    at " + element);
            }
            return; // 예외를 던지지 않고 조용히 리턴
        }

        if (newDescription != null && !newDescription.trim().isEmpty()) {
            image.setDescription(newDescription.trim());
        } else {
            image.setDescription(null);
        }

        imageRepository.save(image);
        System.out.println("✓ 이미지 설명 업데이트: ID=" + imageId);
    }

    // ========== 🆕 주소 → 좌표 변환 기능 ==========

    /**
     * 수정하고 찾아 오시는길 지도 표시 구간
     * 주소를 좌표로 변환 (카카오맵 Geocoding API 사용)
     * 프론트엔드에서 변환한 좌표를 받아서 저장
     */
    // 10. 좌표 업데이트 (지도 표시용)
    @Transactional
    public void updateCoordinates(Long restaurantId, Double latitude, Double longitude) {
        Restaurant restaurant = findById(restaurantId);

        if (restaurant == null) {
            System.err.println("❌ 레스토랑을 찾을 수 없습니다: " + restaurantId);
            return;
        }

        restaurant.setLatitude(latitude);
        restaurant.setLongitude(longitude);
        repository.save(restaurant);

        System.out.println("✅ 좌표 업데이트 완료: lat=" + latitude + ", lng=" + longitude);
    }

    // 8. 조회수 증가
    @Transactional
    public void increaseViewCount(Long restaurantId) {
        Restaurant restaurant = findById(restaurantId);
        if (restaurant != null) {
            restaurant.increaseViewCount();
            repository.save(restaurant);
            System.out.println("👁️ 조회수 증가: ID=" + restaurantId + " (" + restaurant.getViewCount() + ")");
        }
    }

    // ========== 🆕 검색 기능 ==========
    /**
     * 맛집 이름 또는 주소로 검색
     */
    /**
     * 🔥 통합 검색: 키워드 + 카테고리
     */
    // 9. 맛집 검색 (키워드 + 카테고리)
    public List<Restaurant> search(String keyword, String category) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        System.out.println("🔍 통합 검색 - 키워드: " + keyword + ", 카테고리: " + category);

        if (hasKeyword && hasCategory) {
            // 키워드 + 카테고리 동시 검색
            return repository.findByNameContainingIgnoreCaseAndCategoryOrAddressContainingIgnoreCaseAndCategory(
                    keyword, category, keyword, category);
        } else if (hasKeyword) {
            // 키워드만
            return repository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                    keyword, keyword);
        } else if (hasCategory) {
            // 카테고리만
            return repository.findByCategory(category);
        } else {
            // 전체
            return findAll();
        }
    }
}

/*
 * 핵심 역할:
 *
 * 1. 맛집 CRUD
 *    - 등록/수정/삭제/조회
 *
 * 2. 사진 관리
 *    - 사진 추가 시 대표 이미지 자동 관리
 *    - 대표 이미지는 항상 1개만 유지
 *    - 사진 삭제 시 파일도 함께 삭제
 *
 * 3. 검색
 *    - 키워드만 / 카테고리만 / 둘 다 / 전체
 *
 * 4. 부가 기능
 *    - 조회수 증가
 *    - 좌표 저장 (지도 표시)
 *
 *
 * 동작 흐름 예시:
 *
 * 맛집 사진 등록:
 * 1. 사진 선택 → addImage()
 * 2. 대표 이미지로 설정하면 → 기존 대표 해제
 * 3. 파일 저장 → UUID로 저장
 * 4. DB에 경로 저장
 * 5. Restaurant에도 대표 이미지 경로 저장
 *
 * 맛집 삭제:
 * 1. delete(id)
 * 2. 사진 파일들 먼저 삭제 (uploads/images/에서)
 * 3. DB에서 맛집 삭제 (사진 데이터도 자동 삭제)
 *
 * 검색:
 * 1. "강남" + "일식" 검색
 * 2. 이름에 "강남" 포함 AND 카테고리 "일식"
 * 3. OR 주소에 "강남" 포함 AND 카테고리 "일식"
 */