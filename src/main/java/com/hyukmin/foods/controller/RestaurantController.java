package com.hyukmin.foods.controller;

import com.hyukmin.foods.entity.Restaurant;
import com.hyukmin.foods.service.RestaurantService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.repository.UserRepository;

/**
 * RestaurantController - 맛집 컨트롤러
 *
 * 이 파일이 하는 일:
 * 1. 맛집 목록 조회 (검색, 카테고리 필터)
 * 2. 맛집 등록 (이미지 업로드 포함)
 * 3. 맛집 상세 보기
 * 4. 맛집 수정/삭제
 * 5. 이미지 관리 (추가, 삭제, 대표 이미지 변경)
 * 6. 좌표 저장 (Kakao Map API 연동)
 *
 * 연결되는 파일:
 * → RestaurantService: 맛집 CRUD 로직
 * → UserRepository: 작성자 정보 조회
 * → restaurant-list.html: 목록 페이지
 * → restaurant-form.html: 등록/수정 폼
 * → restaurant-detail.html: 상세 페이지
 */
@Controller
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService service;
    private final UserRepository userRepository;

    public RestaurantController(RestaurantService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    /**
     * 맛집 목록 조회
     * URL: GET /restaurants?keyword=검색어&category=카테고리
     *
     * 기능:
     * - 키워드 검색 (이름, 주소)
     * - 카테고리 필터 (한식, 중식, 일식 등)
     * - 둘 다 사용 가능 (AND 조건)
     */
    @GetMapping
    public String list(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "category", defaultValue = "") String category,
            Model model) {

        // 🔥 통합 검색 (키워드 + 카테고리 동시 지원)
        List<Restaurant> restaurants = service.search(keyword, category);

        model.addAttribute("restaurants", restaurants);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);

        System.out.println("🔍 검색 결과: " + restaurants.size() + "개 (키워드: " + keyword + ", 카테고리: " + category + ")");

        return "restaurant-list";

    }

    /**
     * 맛집 등록 폼
     * URL: GET /restaurants/new
     *
     * 로그인 필요 (SecurityConfig에서 설정)
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "restaurant-form";
    }

    /**
     * 맛집 등록 처리
     * URL: POST /restaurants
     *
     * 처리 흐름:
     * 1. 로그인한 사용자를 작성자로 설정
     * 2. 맛집 기본 정보 저장
     * 3. 대표 이미지 저장 (필수)
     * 4. 추가 이미지들 저장 (선택)
     *
     * 파라미터 설명:
     * - mainImage: 대표 이미지 파일 (필수)
     * - mainImageDesc: 대표 이미지 설명
     * - additionalImages[]: 추가 이미지 파일들 (여러 개 가능)
     * - additionalImageDescs[]: 추가 이미지 설명들
     */
    @PostMapping
    public String save(@ModelAttribute Restaurant restaurant, Authentication authentication,
                       // 🔥 대표 이미지 (1개, 필수)
                       @RequestParam(value = "mainImage", required = true) MultipartFile mainImage,
                       @RequestParam(value = "mainImageDesc", required = false) String mainImageDesc,
                       // 🔥 추가 이미지들 (여러 개, 선택)
                       @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
                       @RequestParam(value = "additionalImageDescs", required = false) List<String> additionalImageDescs,
                       RedirectAttributes redirectAttributes) {

        System.out.println("============ 맛집 등록 디버깅 ============");
        System.out.println("맛집 이름: " + restaurant.getName());
        System.out.println("대표 이미지: " + (mainImage != null && !mainImage.isEmpty() ? mainImage.getOriginalFilename() : "없음"));
        System.out.println("추가 이미지 개수: " + (additionalImages != null ? additionalImages.length : 0));

        try {
            // 1. 현재 로그인한 사용자를 작성자로 설정
            String username = authentication.getName();
            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            restaurant.setAuthor(author);
            System.out.println("✅ 작성자 설정: " + username);

            // 2. 맛집 기본 정보 저장 (이름, 주소, 전화번호 등)
            Restaurant savedRestaurant = service.save(restaurant);
            System.out.println("✓ 레스토랑 기본 정보 저장 완료: ID=" + savedRestaurant.getId());

            // 3. 대표 이미지 저장 (필수)
            if (mainImage != null && !mainImage.isEmpty()) {
                try {
                    String desc = (mainImageDesc != null && !mainImageDesc.trim().isEmpty())
                            ? mainImageDesc.trim()
                            : "대표 이미지";

                    System.out.println("🌟 대표 이미지 저장: " + mainImage.getOriginalFilename() + " (" + desc + ")");
                    service.addImage(savedRestaurant.getId(), mainImage, null, desc, true);
                    System.out.println("✓ 대표 이미지 저장 성공!");

                } catch (IOException e) {
                    System.err.println("✗ 대표 이미지 저장 실패");
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "대표 이미지 저장 실패");
                    return "redirect:/restaurants/new";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "대표 이미지는 필수입니다");
                return "redirect:/restaurants/new";
            }

            // 4. 추가 이미지들 저장 (선택)
            if (additionalImages != null && additionalImages.length > 0) {
                int successCount = 0;
                int validFileIndex = 0;

                for (int i = 0; i < additionalImages.length; i++) {
                    MultipartFile file = additionalImages[i];

                    // 빈 파일은 건너뛰기
                    if (file.isEmpty()) {
                        System.out.println("빈 파일 건너뜀: " + i);
                        continue;
                    }
                    // 설명 가져오기
                    try {
                        String desc = null;
                        if (additionalImageDescs != null && validFileIndex < additionalImageDescs.size()
                                && additionalImageDescs.get(validFileIndex) != null
                                && !additionalImageDescs.get(validFileIndex).trim().isEmpty()) {
                            desc = additionalImageDescs.get(validFileIndex).trim();
                        }

                        System.out.println("📸 추가 이미지 저장 [" + validFileIndex + "]: "
                                + file.getOriginalFilename()
                                + " (설명: " + desc + ")");

                        // 이미지 저장
                        service.addImage(savedRestaurant.getId(), file, null, desc, false);
                        successCount++;
                        validFileIndex++;

                    } catch (IOException e) {
                        System.err.println("✗ 추가 이미지 저장 실패: " + file.getOriginalFilename());
                        e.printStackTrace();
                    }
                }

                System.out.println("총 " + successCount + "개 추가 이미지 저장 완료");
            }

            redirectAttributes.addFlashAttribute("message", "맛집이 성공적으로 등록되었습니다! 🎉");
            return "redirect:/restaurants/" + savedRestaurant.getId();

        } catch (Exception e) {
            System.err.println("✗ 맛집 등록 중 오류 발생");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "맛집 등록 실패: " + e.getMessage());
            return "redirect:/restaurants/new";
        }
    }

    /**
     * 맛집 상세 조회
     * URL: GET /restaurants/123
     *
     * 기능:
     * - 맛집 정보 표시
     * - 조회수 자동 증가
     * - 이미지 갤러리
     * - 지도 표시
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Restaurant restaurant = service.findById(id);
        if (restaurant == null) {
            return "redirect:/restaurants";
        }
        // ✅ 조회수 +1 증가
        service.increaseViewCount(id);

        model.addAttribute("restaurant", restaurant);
        return "restaurant-detail";
    }

    /**
     * 맛집 삭제
     * URL: POST /restaurants/123/delete
     *
     * 권한: 작성자 본인 또는 관리자만
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication authentication,  // 🆕 추가
                         RedirectAttributes redirectAttributes) {
        try {
            Restaurant restaurant = service.findById(id);
            if (restaurant == null) {
                redirectAttributes.addFlashAttribute("error", "맛집을 찾을 수 없습니다.");
                return "redirect:/restaurants";
            }

            // 🆕 권한 체크: 작성자 본인 또는 관리자만
            if (!canModify(restaurant, authentication)) {
                redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
                return "redirect:/restaurants/" + id;
            }

            service.delete(id);
            redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
            return "redirect:/restaurants";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "삭제 실패: " + e.getMessage());
            return "redirect:/restaurants";
        }
    }

    /**
     * 맛집 수정 폼
     * URL: GET /restaurants/123/edit
     *
     * 권한: 작성자 본인 또는 관리자만
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication,
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            Restaurant restaurant = service.findById(id);
            if (restaurant == null) {
                redirectAttributes.addFlashAttribute("error", "맛집을 찾을 수 없습니다.");
                return "redirect:/restaurants";
            }

            // 🆕 권한 체크: 작성자 본인 또는 관리자만
            if (!canModify(restaurant, authentication)) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/restaurants/" + id;
            }

            // ✅ 이미지 경로 검증 추가
            System.out.println("========== 수정 폼 디버깅 ==========");
            System.out.println("레스토랑 ID: " + id);
            System.out.println("레스토랑 이름: " + restaurant.getName());
            System.out.println("이미지 개수: " + (restaurant.getImages() != null ? restaurant.getImages().size() : 0));

            if (restaurant.getImages() != null) {
                for (int i = 0; i < restaurant.getImages().size(); i++) {
                    var img = restaurant.getImages().get(i);
                    System.out.println("이미지 " + (i+1) + ": ID=" + img.getId() +
                            ", 경로=" + img.getFilePath() +
                            ", 대표=" + img.getIsMainImage());
                }
            }
            System.out.println("===================================");

            model.addAttribute("restaurant", restaurant);
            return "restaurant-form";

        } catch (Exception e) {
            System.err.println("❌ 수정 폼 로딩 중 오류 발생");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "수정 폼 로딩 실패: " + e.getMessage());
            return "redirect:/restaurants";
        }
    }

    /**
     * 맛집 수정 처리
     * URL: POST /restaurants/123/edit
     *
     * 처리 흐름:
     * 1. DB에서 기존 맛집 정보 불러오기 (기존 이미지 포함)
     * 2. 기본 정보만 업데이트 (이름, 주소, 전화번호 등)
     * 3. 좌표 정보 업데이트 (있으면)
     * 4. 기존 이미지 설명 수정
     * 5. 새 대표 이미지 업로드 시 교체
     * 6. 추가 이미지 업로드
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, Authentication authentication,
                         @ModelAttribute Restaurant restaurant,
                         @RequestParam(value = "latitude", required = false) Double latitude,   // ✅ 추가!
                         @RequestParam(value = "longitude", required = false) Double longitude, // ✅ 추가!
                         @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                         @RequestParam(value = "mainImageDesc", required = false) String mainImageDesc,
                         @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
                         @RequestParam(value = "additionalImageDescs", required = false) List<String> additionalImageDescs,
                         @RequestParam Map<String, String> allParams,  // ✅ 추가
                         RedirectAttributes redirectAttributes) {

        System.out.println("============ 맛집 수정 디버깅 ============");
        System.out.println("맛집 ID: " + id);
        System.out.println("받은 좌표: lat=" + latitude + ", lng=" + longitude); // ✅ 추가!


        try {
            // ✅ 핵심 수정: 폼에서 받은 restaurant 객체가 images를 비워놓지 않도록 처리
            // 1단계: DB에서 기존 맛집 정보 불러오기 (이미지 포함)
            Restaurant dbRestaurant = service.findById(id);
            if (dbRestaurant == null) {
                redirectAttributes.addFlashAttribute("error", "레스토랑을 찾을 수 없습니다.");
                return "redirect:/restaurants";
            }

            // 🆕 권한 체크: 작성자 본인 또는 관리자만
            if (!canModify(dbRestaurant, authentication)) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/restaurants/" + id;
            }

            // 2단계: 폼에서 받은 기본 정보만 업데이트 (이미지는 건드리지 않음)
            dbRestaurant.setName(restaurant.getName());
            dbRestaurant.setCategory(restaurant.getCategory());
            dbRestaurant.setAddress(restaurant.getAddress());
            dbRestaurant.setPhone(restaurant.getPhone());
            dbRestaurant.setPrice(restaurant.getPrice());
            dbRestaurant.setRating(restaurant.getRating());
            dbRestaurant.setDescription(restaurant.getDescription());

            // 🔥 좌표 값 유지 (폼에서 받지 않으므로 기존 DB 값 보존)
            // latitude, longitude는 set하지 않음 → 기존 값 자동 유지 ✅
            // ✅ 좌표 업데이트 (새 값이 있으면 업데이트, 없으면 유지)
            if (latitude != null && longitude != null) {
                dbRestaurant.setLatitude(latitude);
                dbRestaurant.setLongitude(longitude);
                System.out.println("✅ 새 좌표 저장: lat=" + latitude + ", lng=" + longitude);
            } else {
                System.out.println("ℹ️ 기존 좌표 유지: lat=" + dbRestaurant.getLatitude() + ", lng=" + dbRestaurant.getLongitude());
            }

            // 기본 정보만 저장 (이미지는 유지됨)
            service.save(dbRestaurant);
            System.out.println("✓ 레스토랑 기본 정보 업데이트 완료");

            // 4단계: 기존 이미지 설명 업데이트
            Set<Long> processedImageIds = new HashSet<>();

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();

                if (key.startsWith("existingImageDesc_")) {
                    try {
                        Long imageId = Long.parseLong(key.substring("existingImageDesc_".length()));

                        if (processedImageIds.contains(imageId)) {
                            continue;
                        }

                        String newDescription = entry.getValue();

                        // 이미지 존재 여부 확인
                        if (dbRestaurant.getImages() != null) {
                            boolean imageExists = dbRestaurant.getImages().stream()
                                    .anyMatch(img -> img.getId().equals(imageId));

                            if (imageExists) {
                                service.updateImageDescription(imageId, newDescription);
                                processedImageIds.add(imageId);
                                System.out.println("✓ 이미지 설명 업데이트: ID=" + imageId + ", 설명=" + newDescription);
                            } else {
                                System.out.println("⚠ 해당 레스토랑에 없는 이미지 ID: " + imageId + " (건너뜀)");
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("⚠ 잘못된 이미지 ID 형식: " + key);
                    } catch (Exception e) {
                        System.err.println("⚠ 이미지 설명 업데이트 실패: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            // 5단계: 새 대표 이미지가 있으면 기존 대표 이미지 교체
            if (mainImage != null && !mainImage.isEmpty()) {
                try {
                    String desc = (mainImageDesc != null && !mainImageDesc.trim().isEmpty())
                            ? mainImageDesc.trim()
                            : "대표 이미지";

                    System.out.println("🌟 새 대표 이미지로 교체: " + mainImage.getOriginalFilename());
                    service.addImage(id, mainImage, null, desc, true);
                    System.out.println("✓ 대표 이미지 교체 성공!");

                } catch (IOException e) {
                    System.err.println("✗ 대표 이미지 업로드 실패");
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "대표 이미지 업로드 실패");
                }
            }

            // 6단계: 새 추가 이미지들 추가
            if (additionalImages != null && additionalImages.length > 0) {
                int successCount = 0;
                int validFileIndex = 0;

                for (int i = 0; i < additionalImages.length; i++) {
                    MultipartFile file = additionalImages[i];

                    if (file.isEmpty()) {
                        continue;
                    }

                    try {
                        String desc = null;
                        if (additionalImageDescs != null && validFileIndex < additionalImageDescs.size()
                                && additionalImageDescs.get(validFileIndex) != null
                                && !additionalImageDescs.get(validFileIndex).trim().isEmpty()) {
                            desc = additionalImageDescs.get(validFileIndex).trim();
                        }

                        System.out.println("🖼 새 추가 이미지 [" + validFileIndex + "]: "
                                + file.getOriginalFilename()
                                + " (설명: " + desc + ")");

                        service.addImage(id, file, null, desc, false);
                        successCount++;
                        validFileIndex++;

                    } catch (IOException e) {
                        System.err.println("✗ 이미지 저장 실패: " + file.getOriginalFilename());
                        e.printStackTrace();
                    }
                }

                System.out.println("이 " + successCount + "개 새 추가 이미지 저장 완료");
            }

            redirectAttributes.addFlashAttribute("message", "맛집 정보가 수정되었습니다! ✓");
            return "redirect:/restaurants/" + id;

        } catch (Exception e) {
            System.err.println("✗ 맛집 수정 중 오류 발생");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "수정 실패: " + e.getMessage());
            return "redirect:/restaurants/" + id + "/edit";
        }
    }

    /**
     * 이미지 삭제
     * URL: POST /restaurants/123/images/456/delete
     *
     * 123 = 맛집 번호
     * 456 = 이미지 번호
     */
    @PostMapping("/{restaurantId}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long restaurantId,
                              @PathVariable Long imageId,
                              RedirectAttributes redirectAttributes) {
        try {
            service.deleteImage(imageId);
            redirectAttributes.addFlashAttribute("message", "이미지가 삭제되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "이미지 삭제 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/restaurants/" + restaurantId + "/edit";
    }

    /**
     * 대표 이미지 변경
     * URL: POST /restaurants/123/images/456/setMain
     *
     * 이미지 456번을 새로운 대표 이미지로 설정
     */
    @PostMapping("/{restaurantId}/images/{imageId}/setMain")
    public String setMainImage(@PathVariable Long restaurantId,
                               @PathVariable Long imageId,
                               RedirectAttributes redirectAttributes) {
        try {
            service.changeMainImage(restaurantId, imageId);
            redirectAttributes.addFlashAttribute("message", "대표 이미지가 변경되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "대표 이미지 변경 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/restaurants/" + restaurantId + "/edit";
    }

    // ========== 🆕 좌표 저장 API ==========
    /**
     * 좌표 저장 API (AJAX)
     * URL: POST /restaurants/123/coordinates?latitude=37.123&longitude=127.456
     *
     * 사용처:
     * - restaurant-form.js에서 Kakao Map API로 주소 검색 후 좌표 계산
     * - 계산된 좌표를 AJAX로 서버에 전송
     */
    /**
     * 프론트엔드에서 계산한 좌표를 받아서 저장
     */
    @PostMapping("/{id}/coordinates")
    @ResponseBody
    public String saveCoordinates(@PathVariable Long id,
                                  @RequestParam Double latitude,
                                  @RequestParam Double longitude) {
        try {
            service.updateCoordinates(id, latitude, longitude);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    /**
     * 수정/삭제 권한 체크 (private 메서드)
     *
     * 권한이 있는 경우:
     * 1. 작성자 본인
     * 2. 관리자 (ROLE_ADMIN)
     */
    private boolean canModify(Restaurant restaurant, Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String currentUsername = authentication.getName();

        // 1. 관리자는 모든 권한 있음
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            System.out.println("✅ 관리자 권한으로 접근");
            return true;
        }

        // 2. 작성자 본인 체크
        if (restaurant.getAuthor() != null &&
                restaurant.getAuthor().getUsername().equals(currentUsername)) {
            System.out.println("✅ 작성자 본인 접근");
            return true;
        }

        System.out.println("❌ 권한 없음: 현재=" + currentUsername +
                ", 작성자=" + (restaurant.getAuthor() != null ?
                restaurant.getAuthor().getUsername() : "없음"));
        return false;
    }

}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 맛집 검색
 *   GET /restaurants?keyword=강남&category=한식
 *   → "강남"이 포함되고 카테고리가 "한식"인 맛집 검색
 *   → restaurant-list.html에 표시
 *
 * 시나리오 2) 맛집 등록
 *   1. GET /restaurants/new → 등록 폼 표시
 *   2. 사용자: 이름, 주소, 대표이미지, 추가이미지 입력
 *   3. POST /restaurants
 *   4. Controller:
 *      - 맛집 기본 정보 저장
 *      - 대표 이미지 저장 (필수)
 *      - 추가 이미지 3개 저장 (선택)
 *   5. redirect:/restaurants/123 (상세 페이지)
 *
 * 시나리오 3) 이미지 관리
 *   수정 페이지에서:
 *   - [삭제] 버튼 → POST /restaurants/123/images/456/delete
 *   - [대표로 설정] 버튼 → POST /restaurants/123/images/456/setMain
 *   - 새 이미지 업로드 → 기존 이미지 유지하면서 추가
 *
 * 시나리오 4) 좌표 저장 (JavaScript 연동)
 *   restaurant-form.js:
 *   1. 주소 입력 → Kakao Map API 호출
 *   2. 좌표 계산 (위도, 경도)
 *   3. fetch POST /restaurants/123/coordinates
 *   4. Controller: DB에 좌표 저장
 *
 * 데이터 흐름:
 *   사용자 입력
 *   → RestaurantController
 *   → RestaurantService (비즈니스 로직)
 *   → FileUploadService (파일 저장)
 *   → RestaurantRepository (DB 저장)
 *   → Model에 데이터 추가
 *   → HTML 템플릿 렌더링
 */