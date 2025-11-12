package com.hyukmin.foods.controller;

import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.entity.Restaurant;
import com.hyukmin.foods.entity.Board;
import com.hyukmin.foods.service.UserService;
import com.hyukmin.foods.service.RestaurantService;
import com.hyukmin.foods.service.BoardService;
import com.hyukmin.foods.service.CommentService;
import com.hyukmin.foods.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * AdminController - 관리자 페이지 컨트롤러
 *
 * 이 파일이 하는 일:
 * 1. 관리자 대시보드 (통계 정보 보여주기)
 * 2. 회원 관리 (조회, 삭제, 권한 변경)
 * 3. 맛집 관리 (목록 조회)
 * 4. 게시판 관리 (목록 조회)
 *
 * 접근 권한: ROLE_ADMIN만 접근 가능 (SecurityConfig에서 설정)
 *
 * 연결되는 파일:
 * → UserRepository: 회원 데이터 조회/수정/삭제
 * → RestaurantService: 맛집 데이터 조회
 * → BoardService: 게시글 데이터 조회
 * → admin-dashboard.html, admin-users.html 등: 화면 표시
 */

@Controller
@RequestMapping("/admin")// 모든 URL이 /admin으로 시작
public class AdminController {

    private final UserRepository userRepository;
    private final RestaurantService restaurantService;
    private final BoardService boardService;
    private final CommentService commentService;
    // 생성자 주입 (Spring이 자동으로 연결)
    public AdminController(UserRepository userRepository,
                           RestaurantService restaurantService,
                           BoardService boardService,
                           CommentService commentService) {
        this.userRepository = userRepository;
        this.restaurantService = restaurantService;
        this.boardService = boardService;
        this.commentService = commentService;
    }

    /**
     * 관리자 대시보드 메인 페이지
     * URL: GET /admin
     *
     * 보여주는 정보:
     * - 전체 회원 수, 맛집 수, 게시글 수
     * - 최근 가입한 회원 5명
     * - 최근 등록된 맛집 5개
     * - 최근 작성된 게시글 5개
     */
    @GetMapping
    public String dashboard(Model model) {
        // 전체 통계
        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantService.findAll().size();
        long totalBoards = boardService.getTotalCount();

        // 최근 가입 회원 5명 (가입일 기준 내림차순)
        List<User> recentUsers = userRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        // 최근 등록 맛집 5개
        List<Restaurant> recentRestaurants = restaurantService.findAll()
                .stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(5)
                .toList();

        // 최근 게시글 (5개)
        var recentBoards = boardService.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        // Model에 데이터 추가 → HTML에서 사용 가능
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalRestaurants", totalRestaurants);
        model.addAttribute("totalBoards", totalBoards);
        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("recentRestaurants", recentRestaurants);
        model.addAttribute("recentBoards", recentBoards);

        System.out.println("📊 관리자 대시보드 접근");
        return "admin-dashboard";// admin-dashboard.html 렌더링
    }

    /**
     * 회원 관리 페이지
     * URL: GET /admin/users?keyword=검색어
     *
     * 기능:
     * - 전체 회원 목록 조회 (최신순)
     * - 아이디로 검색
     */
    @GetMapping("/users")
    public String users(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            Model model) {

        List<User> users;

        // 검색어가 있으면 검색, 없으면 전체 조회
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCase(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        model.addAttribute("users", users);
        System.out.println("👥 회원 관리 페이지: " + users.size() + "명");
        return "admin-users"; // admin-users.html 렌더링
    }

    /**
     * 회원 삭제
     * URL: POST /admin/users/{id}/delete
     *
     * 처리 흐름:
     * 1. id로 회원 조회
     * 2. 존재하면 삭제
     * 3. 성공/실패 메시지 표시
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
                return "redirect:/admin/users";
            }

            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "회원이 삭제되었습니다.");
            System.out.println("🗑️ 회원 삭제: " + user.getUsername());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "삭제 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/users";// 회원 목록으로 리다이렉트
    }

    /**
     * 회원 권한 변경 (일반 ↔ 관리자)
     * URL: POST /admin/users/{id}/toggle-role
     *
     * 처리 흐름:
     * 1. 현재 권한 확인
     * 2. ROLE_ADMIN → ROLE_USER 또는 ROLE_USER → ROLE_ADMIN
     * 3. 변경 사항 저장
     */
    @PostMapping("/users/{id}/toggle-role")
    public String toggleRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
                return "redirect:/admin/users";
            }

            // 권한 토글(반대로 변경)
            if ("ROLE_ADMIN".equals(user.getRole())) {
                user.setRole("ROLE_USER");
                redirectAttributes.addFlashAttribute("message", "일반 사용자로 변경되었습니다.");
            } else {
                user.setRole("ROLE_ADMIN");
                redirectAttributes.addFlashAttribute("message", "관리자로 변경되었습니다.");
            }

            userRepository.save(user);
            System.out.println("🔄 권한 변경: " + user.getUsername() + " → " + user.getRole());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "권한 변경 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/users";
    }
    /**
     * 맛집 관리 페이지
     * URL: GET /admin/restaurants
     *
     * 기능: 전체 맛집 목록 조회
     */
    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        List<Restaurant> restaurants = restaurantService.findAll();
        model.addAttribute("restaurants", restaurants);
        System.out.println("🍽️ 맛집 관리 페이지: " + restaurants.size() + "개");
        return "admin-restaurants";
    }

    /**
     * 게시판 관리 페이지
     * URL: GET /admin/boards
     *
     * 기능: 전체 게시글 목록 조회 (최신순 100개)
     */
    @GetMapping("/boards")
    public String boards(Model model) {
        var boards = boardService.findAll(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        model.addAttribute("boards", boards);
        System.out.println("📋 게시판 관리 페이지: " + boards.size() + "개");
        return "admin-boards";// admin-boards.html 렌더링
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 관리자가 대시보드 접속
 *   GET /admin
 *   → 회원 수, 맛집 수 등 통계 계산
 *   → 최근 데이터 5개씩 조회
 *   → admin-dashboard.html에 표시
 *
 * 시나리오 2) 회원 검색
 *   GET /admin/users?keyword=hong
 *   → "hong"이 포함된 아이디 검색
 *   → 검색 결과를 admin-users.html에 표시
 *
 * 시나리오 3) 회원 삭제
 *   POST /admin/users/5/delete
 *   → id가 5인 회원 조회
 *   → 존재하면 삭제
 *   → "회원이 삭제되었습니다" 메시지와 함께 목록으로 이동
 *
 * 시나리오 4) 권한 변경
 *   POST /admin/users/5/toggle-role
 *   → 현재 권한이 ROLE_USER면 → ROLE_ADMIN으로 변경
 *   → 현재 권한이 ROLE_ADMIN면 → ROLE_USER로 변경
 *   → 변경 후 목록으로 이동
 *
 * 데이터 흐름:
 *   요청 → AdminController
 *       → Repository/Service에서 데이터 조회
 *       → Model에 데이터 추가
 *       → HTML 템플릿 렌더링
 *       → 사용자에게 화면 표시
 */