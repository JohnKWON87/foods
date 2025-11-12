package com.hyukmin.foods.controller;

import com.hyukmin.foods.entity.Bookmark;
import com.hyukmin.foods.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * BookmarkController - 찜하기(북마크) 컨트롤러
 *
 * 이 파일이 하는 일:
 * 1. 찜한 맛집 목록 보기
 * 2. 찜하기 추가/취소 (토글)
 * 3. 찜하기 상태 확인
 *
 * 특징:
 * - AJAX 방식으로 페이지 새로고침 없이 작동
 * - JSON 응답 반환 (@ResponseBody)
 * - 로그인 필수
 *
 * 연결되는 파일:
 * → BookmarkService: 찜하기 비즈니스 로직
 * → bookmark-list.html: 찜 목록 페이지
 * → restaurant-detail.js: 하트 버튼 클릭 처리
 */
@Controller
@RequiredArgsConstructor// final 필드 자동 주입
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 내가 찜한 맛집 목록 페이지
     * URL: GET /bookmarks
     *
     * 기능:
     * - 로그인한 사용자의 찜 목록 조회
     * - 찜한 맛집 개수 표시
     */
    @GetMapping("/bookmarks")
    public String bookmarkList(Authentication authentication, Model model) {
        // 로그인 안 했으면 로그인 페이지로
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(username);

        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("totalCount", bookmarks.size());

        return "bookmark-list";// bookmark-list.html 렌더링
    }

    /**
     * 찜하기 토글 (AJAX)
     * URL: POST /api/bookmarks/toggle?restaurantId=123
     *
     * 기능:
     * - 찜하기 상태면 → 취소
     * - 찜하기 안 한 상태면 → 추가
     *
     * 응답 형식 (JSON):
     * {
     *   "success": true,
     *   "isBookmarked": true,      // 현재 찜 상태
     *   "bookmarkCount": 5,         // 이 맛집의 총 찜 개수
     *   "message": "북마크에 추가되었습니다."
     * }
     */
    @PostMapping("/api/bookmarks/toggle")
    @ResponseBody// JSON으로 응답
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @RequestParam Long restaurantId, // 맛집 ID (예: 123)
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        // 로그인 체크
        if (authentication == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String username = authentication.getName();
            // 찜하기 토글 (있으면 삭제, 없으면 추가)
            boolean isBookmarked = bookmarkService.toggleBookmark(username, restaurantId);
            // 이 맛집의 총 찜 개수
            long bookmarkCount = bookmarkService.getBookmarkCount(restaurantId);

            // 성공 응답
            response.put("success", true);
            response.put("isBookmarked", isBookmarked);
            response.put("bookmarkCount", bookmarkCount);
            response.put("message", isBookmarked ? "좋아요 에 추가되었습니다." : "좋아요 가 제거되었습니다.");

            return ResponseEntity.ok(response); // 200 OK
            /*
             * ========== 우리 코드에서 사용하는 상태 코드 정리 ==========
             *
             * ResponseEntity.ok(response)
             * = 200 OK
             * = "성공했어요! 데이터 받으세요!"
             *
             * ResponseEntity.status(401).body(response)
             * = 401 Unauthorized
             * = "로그인이 필요해요!"
             *
             * ResponseEntity.status(500).body(response)
             * = 500 Internal Server Error
             * = "서버에서 에러가 났어요!"
             *
             * 200번대 - 성공 ✅
             * ├─ 200 OK: 요청 성공! 문제없음
             * ├─ 201 Created: 새로운 데이터 생성 성공
             * └─ 204 No Content: 성공했지만 보낼 데이터 없음
             *
             * 300번대 - 리다이렉션 🔄
             * └─ 301 Moved Permanently: 페이지가 영구적으로 이동됨
             *
             * 400번대 - 클라이언트 오류 ❌
             * ├─ 400 Bad Request: 잘못된 요청
             * ├─ 401 Unauthorized: 로그인 필요
             * ├─ 403 Forbidden: 권한 없음
             * └─ 404 Not Found: 페이지를 찾을 수 없음
             *
             * 500번대 - 서버 오류 💥
             * └─ 500 Internal Server Error: 서버에서 에러 발생
             *
             * ========== 왜 필요한가요? ==========
             *
             * JavaScript가 응답을 받았을 때 "성공인지 실패인지" 쉽게 구분하기 위해!
             *
             * if (response.ok) {           // 200번대
             *     하트를 빨갛게 바꾸기
             * } else if (status === 401) { // 로그인 필요
             *     로그인 페이지로 이동
             * } else {                     // 기타 에러
             *     에러 메시지 표시
             * }
             */


        }
        catch (Exception e) {
            // 에러 응답
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 찜하기 여부 확인 (AJAX)
     * URL: GET /api/bookmarks/check?restaurantId=123
     *
     * 기능:
     * - 현재 사용자가 이 맛집을 찜했는지 확인
     * - 페이지 로드 시 하트 아이콘 상태 설정용
     *
     * 응답 형식 (JSON):
     * {
     *   "isBookmarked": true,
     *   "bookmarkCount": 5
     * }
     */
    @GetMapping("/api/bookmarks/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkBookmark(
            @RequestParam Long restaurantId,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        // 비로그인 상태면 찜 안 한 것으로 처리
        if (authentication == null) {
            response.put("isBookmarked", false);
            return ResponseEntity.ok(response);
        }

        String username = authentication.getName();
        boolean isBookmarked = bookmarkService.isBookmarked(username, restaurantId);
        long bookmarkCount = bookmarkService.getBookmarkCount(restaurantId);

        response.put("isBookmarked", isBookmarked);
        response.put("bookmarkCount", bookmarkCount);

        return ResponseEntity.ok(response);
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 맛집 상세 페이지에서 하트 버튼 클릭
 *
 *   사용자: ❤️ 버튼 클릭
 *   ↓
 *   JavaScript (restaurant-detail.js):
 *   fetch('/api/bookmarks/toggle?restaurantId=123', {
 *       method: 'POST'
 *   })
 *   ↓
 *   BookmarkController.toggleBookmark()
 *   - 찜 상태 확인 → 없으면 추가, 있으면 삭제
 *   - JSON 응답: {"success": true, "isBookmarked": true}
 *   ↓
 *   JavaScript:
 *   - 하트 아이콘 색상 변경 (빈 하트 → 꽉찬 하트)
 *   - 찜 개수 업데이트
 *
 *
 * 시나리오 2) 페이지 로드 시 찜 상태 확인
 *
 *   맛집 상세 페이지 접속
 *   ↓
 *   JavaScript:
 *   fetch('/api/bookmarks/check?restaurantId=123')
 *   ↓
 *   BookmarkController.checkBookmark()
 *   - 찜 여부 확인
 *   - JSON 응답: {"isBookmarked": true, "bookmarkCount": 5}
 *   ↓
 *   JavaScript:
 *   - 이미 찜한 상태면 빨간 하트 표시
 *   - 안 찜했으면 빈 하트 표시
 *
 *
 * 시나리오 3) 내 찜 목록 보기
 *
 *   GET /bookmarks
 *   ↓
 *   BookmarkController.bookmarkList()
 *   - 내가 찜한 맛집 목록 조회
 *   - bookmark-list.html에 표시
 *   ↓
 *   화면에 찜한 맛집들 카드 형태로 보여줌
 *
 *
 * ========== 데이터 흐름 ==========
 *
 * 하트 버튼 클릭
 *   ↓
 * JavaScript (AJAX 요청)
 *   ↓
 * BookmarkController
 *   ↓
 * BookmarkService (비즈니스 로직)
 *   ↓
 * BookmarkRepository (DB 저장/삭제)
 *   ↓
 * JSON 응답 반환
 *   ↓
 * JavaScript가 받아서 화면 업데이트
 *
 *
 * ========== 왜 AJAX를 쓰나요? ==========
 *
 * 일반 방식:
 *   하트 클릭 → 페이지 전체 새로고침 → 다시 스크롤 내려야 함 (불편)
 *
 * AJAX 방식:
 *   하트 클릭 → 페이지 그대로 → 하트 색만 바뀜 (편리!)
 */