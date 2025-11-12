package com.hyukmin.foods.controller;

import com.hyukmin.foods.service.UserService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
/**
 * ========================================
 * UserController - 회원 관련 요청 처리
 * ========================================
 *
 * [역할]
 * - 회원가입, 로그인 페이지 표시 및 처리
 * - 아이디/이메일 중복 체크 API 제공
 * - 사용자 요청을 받아 UserService로 전달하고 결과를 화면에 표시
 *
 * [데이터 흐름]
 * 1. 브라우저 → Controller (HTTP 요청)
 * 2. Controller → UserService (비즈니스 로직 처리 요청)
 * 3. UserService → UserRepository → DB (데이터 저장/조회)
 * 4. DB → UserRepository → UserService → Controller (처리 결과)
 * 5. Controller → 브라우저 (HTML 페이지 또는 JSON 응답)
 *
 * [연결된 파일들]
 * - UserService: 실제 회원가입/중복체크 로직 수행
 * - SecurityConfig: 로그인 처리 및 인증 담당
 * - templates/*.html: 화면 렌더링 (index.html, signup.html, login.html)
 * - User 엔티티: 회원 정보 저장 구조
 */
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * [GET /] 홈페이지
     * 요청: 사용자가 메인 페이지 접속
     * 처리: index.html 렌더링
     * 응답: 홈페이지 화면
     */

    @GetMapping("/")
    public String index() {
        return "index";  // index.html 렌더링
    }

    /**
     * [GET /signup] 회원가입 페이지
     * 요청: 사용자가 회원가입 페이지 접속
     * 처리: signup.html 렌더링
     * 응답: 회원가입 폼 화면
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";  // signup.html 렌더링
    }

    /**
     * [POST /signup] 회원가입 처리
     *
     * 요청 데이터:
     * - username: 사용자 아이디
     * - password: 비밀번호
     * - passwordConfirm: 비밀번호 확인
     * - email: 이메일
     *
     * 처리 흐름:
     * 1. 비밀번호 일치 확인
     * 2. UserService.registerUser() 호출
     *    → 아이디 중복 체크
     *    → 이메일 중복 체크
     *    → 비밀번호 암호화 (BCrypt)
     *    → DB 저장
     * 3. 성공 시 로그인 페이지로 리다이렉트
     * 4. 실패 시 에러 메시지와 함께 회원가입 페이지 재표시
     *
     * 응답:
     * - 성공: redirect:/login?success=true
     * - 실패: signup.html + 에러 메시지
     */
    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam String email,
            org.springframework.ui.Model model
    ) {
//🔹 프로세스
//1️⃣ 비밀번호 확인 체크
//   ↓
//2️⃣ UserService.registerUser() 호출
//   - 아이디 중복 체크
//   - 이메일 중복 체크
//   - 비밀번호 암호화
//   - DB 저장
//   ↓
//3️⃣ 회원가입 성공 → /login?success=true로 리다이렉트
//   ↓
//4️⃣ 예외 발생 → signup.html에 에러 메시지 표시
        try {
            // 1. 비밀번호 확인 체크, 정상 프로세스
            if (!password.equals(passwordConfirm)) {
                model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "signup";
            }

            // 2. 회원가입 (UserService에서 중복 체크 + 암호화)
            userService.registerUser(username, password, email);

            // 3. 회원가입 성공 → 로그인 페이지로 리다이렉트
            return "redirect:/login?success=true";

        } catch (IllegalArgumentException e) {
            // 4. 아이디 또는 이메일 중복 에러
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "signup";

        } catch (Exception e) {
            // 5. 예상치 못한 에러
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "signup";
        }
    }

    /**
     * [GET /login] 로그인 페이지
     *
     * 요청 파라미터:
     * - error: 로그인 실패 시 "true"
     * - success: 회원가입 성공 시 "true"
     *
     * 처리:
     * - 파라미터에 따라 성공/실패 메시지 설정
     * - login.html 렌더링
     *
     * 응답: 로그인 폼 화면 + 메시지
     *
     * 참고: 실제 로그인 처리는 SecurityConfig에서 수행
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String success,
            org.springframework.ui.Model model
    ) {
        // 로그인 실패 메시지
        if ("true".equals(error)) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 회원가입 성공 메시지
        if ("true".equals(success)) {
            model.addAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
        }

        return "login";  // login.html 렌더링
    }

    /**
     * [GET /api/check-username] 아이디 중복 체크 API
     *
     * 요청: /api/check-username?username=testuser
     *
     * 처리 흐름:
     * 1. 입력값 검증 (빈 문자열 체크)
     * 2. UserService.isUsernameDuplicated() 호출
     *    → UserRepository에서 DB 조회
     * 3. 중복 여부 판단
     *
     * 응답: JSON
     * {
     *   "success": true,
     *   "duplicated": false,
     *   "message": "사용 가능한 아이디입니다."
     * }
     *
     * 사용처: signup.html의 JavaScript에서 실시간 중복 체크
     */
    @GetMapping("/api/check-username")
    @ResponseBody
    public CheckUsernameResponse checkUsername(@RequestParam String username) {
        // 빈 문자열 체크
        if (username == null || username.trim().isEmpty()) {
            return new CheckUsernameResponse(false, false, "아이디를 입력해주세요.");
        }

        // 중복 체크
        boolean isDuplicated = userService.isUsernameDuplicated(username);
        String message = isDuplicated ? "이미 사용 중인 아이디입니다." : "사용 가능한 아이디입니다.";

        return new CheckUsernameResponse(true, isDuplicated, message);
    }

    /**
     * [GET /api/check-email] 이메일 중복 체크 API
     *
     * 요청: /api/check-email?email=test@example.com
     *
     * 처리 흐름:
     * 1. 입력값 검증 (빈 문자열 체크)
     * 2. 이메일 형식 검증 (@ 포함 여부)
     * 3. UserService.isEmailDuplicated() 호출
     *    → UserRepository에서 DB 조회
     * 4. 중복 여부 판단
     *
     * 응답: JSON
     * {
     *   "success": true,
     *   "duplicated": false,
     *   "message": "사용 가능한 이메일입니다."
     * }
     *
     * 사용처: signup.html의 JavaScript에서 실시간 중복 체크
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public CheckEmailResponse checkEmail(@RequestParam String email) {
        // 빈 문자열 체크
        if (email == null || email.trim().isEmpty()) {
            return new CheckEmailResponse(false, false, "이메일을 입력해주세요.");
        }

        // 이메일 형식 체크 (간단한 검증)
        if (!email.contains("@")) {
            return new CheckEmailResponse(false, false, "올바른 이메일 형식이 아닙니다.");
        }

        // 중복 체크
        boolean isDuplicated = userService.isEmailDuplicated(email);
        String message = isDuplicated ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.";

        return new CheckEmailResponse(true, isDuplicated, message);
    }

    /**
     * 아이디 중복 체크 응답 DTO
     * JavaScript로 전달될 JSON 구조
     */
    @Getter
    @AllArgsConstructor
    public static class CheckUsernameResponse {
        public boolean success;     // API 호출 성공 여부
        public boolean duplicated;  // 중복 여부
        public String message;      // 사용자에게 표시할 메시지

    }

    /**
     * 이메일 중복 체크 응답 DTO
     * JavaScript로 전달될 JSON 구조
     */
    @Getter
    @AllArgsConstructor
    public static class CheckEmailResponse {
        public boolean success;         // API 호출 성공 여부
        public boolean duplicated;      // 중복 여부
        public String message;          // 사용자에게 표시할 메시지

    }
}
//🔄 전체 흐름도
//┌─────────────────────┐
//│   사용자 접근         │
//└──────────┬──────────┘
//           │
//     ┌─────▼─────┐
//     │  GET /    │
//     └─────┬─────┘
//           │
//    ┌──────▼──────┐
//    │ 홈페이지      │
//    │ (index.html)│
//    └──────┬──────┘
//           │
//    ┌──────┴──────────────┐
//    │                     │
//┌───▼──────┐      ┌──────▼──────┐
//│ [회원가입] │      │ [로그인]     │
//└───┬──────┘      └──────┬──────┘
//    │                     │
//┌───▼──────────┐   ┌──────▼──────────┐
//│ POST /signup │   │ GET /login      │
//└───┬──────────┘   └──────┬──────────┘
//    │                     │
//    │ (UserService)       │ (Spring Security)
//    │ - 중복 체크           │ - loadUserByUsername()
//    │ - 암호화              │ - 비밀번호 검증
//    │ - DB 저장            │
//    │                     │
//    └──┬─────────────────┬┘
//       │                 │
//   성공 │             성공 │
//   로그인│            로그인│
//   페이지│          맛집목록│

/**
 * ========================================
 * 전체 데이터 흐름도
 * ========================================
 *
 * [회원가입 흐름]
 * 1. 사용자가 /signup 접속 → signup.html 표시
 * 2. 아이디 입력 → JavaScript가 /api/check-username 호출 → 중복 체크
 * 3. 이메일 입력 → JavaScript가 /api/check-email 호출 → 중복 체크
 * 4. 폼 제출 → POST /signup
 * 5. Controller → UserService.registerUser()
 * 6. UserService → 중복 체크 → 비밀번호 암호화 → DB 저장
 * 7. 성공 → /login?success=true로 리다이렉트
 * 8. 실패 → signup.html에 에러 메시지 표시
 *
 * [로그인 흐름]
 * 1. 사용자가 /login 접속 → login.html 표시
 * 2. 로그인 폼 제출 → POST /login (SecurityConfig가 처리)
 * 3. SecurityConfig → CustomUserDetailsService.loadUserByUsername()
 * 4. DB에서 사용자 조회 → 비밀번호 검증
 * 5. 성공 → 메인 페이지로 이동
 * 6. 실패 → /login?error=true로 리다이렉트
 *
 * [연결 관계]
 * UserController
 *     ↓ 의존
 * UserService
 *     ↓ 의존
 * UserRepository
 *     ↓ 의존
 * User 엔티티
 *     ↓ 매핑
 * users 테이블 (DB)
 */