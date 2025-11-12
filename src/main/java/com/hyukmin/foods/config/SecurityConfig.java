package com.hyukmin.foods.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * SecurityConfig.java
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * [역할]
 * Spring Security 보안 설정을 담당하는 설정 클래스
 * - 인증(Authentication): 누가 접근하는가? (로그인)
 * - 인가(Authorization): 어떤 권한을 가지는가? (접근 제어)
 *
 * [주요 기능]
 * 1. URL별 접근 권한 설정 (누구나 접근 vs 로그인 필요 vs 관리자만)
 * 2. 로그인/로그아웃 처리 흐름 정의
 * 3. 비밀번호 암호화 방식 설정
 * 4. CSRF 보안 설정
 *
 * [연결되는 파일]
 * → CustomUserDetailsService: 로그인 시 사용자 정보 조회
 * → 모든 Controller: Security 필터를 거쳐 요청이 전달됨
 * → login.html: 로그인 폼 페이지
 *
 * [데이터 흐름]
 * 1. 사용자 요청 발생
 * 2. SecurityFilterChain이 요청 가로챔
 * 3. URL 패턴 확인 → 권한 체크
 *    - 권한 없음 → /login으로 리다이렉트
 *    - 권한 있음 → Controller로 요청 전달
 * 4. 로그인 시도 시 CustomUserDetailsService가 사용자 인증
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * [비밀번호 암호화기 Bean 등록]
     *
     * BCrypt 해시 알고리즘 사용
     * - 단방향 암호화 (복호화 불가능)
     * - Salt 자동 생성 (같은 비밀번호도 매번 다른 해시값)
     * - 회원가입 시 비밀번호 저장할 때 사용
     * - 로그인 시 비밀번호 검증할 때 사용
     *
     * 사용 예시:
     * String rawPassword = "1234";
     * String encrypted = passwordEncoder.encode(rawPassword);
     * → "$2a$10$abcd..." 형태로 저장
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//BCrypt : 단방향 해시 알고리즘 (복호화 불가능)
    /**
     * [Spring Security 핵심 설정]
     *
     * 모든 HTTP 요청이 이 필터 체인을 거쳐감
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 누구나 접근 가능한 페이지, 로그인 하지않아도 사용가능
                        .requestMatchers("/", "/signup", "/login").permitAll()
                        // 🔥 정적 리소스 모두 허용 (중요!)
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        // 🔥 업로드된 이미지 경로 허용 (추가!)
                        .requestMatchers("/uploads/**").permitAll()
                        // API 엔드포인트 - 누구나 접근 가능
                        .requestMatchers("/api/**").permitAll()
                        // 맛집 목록/상세 조회는 누구나 가능
                        .requestMatchers("/restaurants", "/restaurants/*").permitAll()
                        // 게시글 목록/상세 조회는 누구나 가능
                        .requestMatchers("/boards", "/boards/*").permitAll()

                        // 맛집 등록/수정/삭제는 로그인 필요★★★
                        .requestMatchers("/restaurants/new", "/restaurants/*/edit", "/restaurants/*/delete").authenticated()
                        // ========== Board (게시판) 권한 설정 ==========
                        // 게시글 등록/수정/삭제는 로그인 필요
                        .requestMatchers("/boards/new", "/boards/*/edit", "/boards/*/delete").authenticated()
                        // 댓글 작성/수정/삭제는 로그인 필요
                        .requestMatchers("/comments", "/comments/*").authenticated()

                        // 관리자 페이지는 ADMIN 권한 필요, 관리자만 접근
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")// GET /login → 로그인 폼 보여줌
                        .loginProcessingUrl("/login")// POST /login → 실제 로그인 처리
                        .defaultSuccessUrl("/restaurants", true) // 로그인 성공 시 맛집 목록으로
                        .failureUrl("/login?error=true")        // 로그인 실패 시
                        .usernameParameter("username")          // 로그인 폼의 username 필드명
                        .passwordParameter("password")          // 로그인 폼의 password 필드명
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")// POST /logout → 로그아웃 처리
                        .logoutSuccessUrl("/")// 로그아웃 성공 → 홈으로
                        .invalidateHttpSession(true)// 세션 완전 삭제
                        .deleteCookies("JSESSIONID")// 쿠키 삭제
                        .permitAll()
                        .logoutRequestMatcher(request ->
                                request.getServletPath().equals("/logout")
                        )
                )

                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // 4. CSRF 보안 설정
                // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                // CSRF: 사이트 간 요청 위조 공격 방지
                // H2 Console은 개발용이므로 CSRF 체크 제외
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )// CSRF Cross-Site Request Forgery : 사이트 간 요청 위조 공격 방지

                // H2 Console 사용을 위한 설정 (개발용)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())

                );

        return http.build();
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 예시 1) 로그인 안 한 상태에서 맛집 목록 보기
 *   /restaurants 접속 → 바로 보임 ✅
 *
 * 예시 2) 로그인 안 한 상태에서 맛집 등록하기
 *   /restaurants/new 접속 → /login으로 자동 이동 🔒
 *   로그인 성공 → 다시 /restaurants/new로 이동
 *
 * 예시 3) 일반 회원이 관리자 페이지 접속
 *   /admin/dashboard 접속 → 403 에러 (권한 없음) ❌
 *
 * 예시 4) 로그인 흐름
 *   1. /login 접속
 *   2. 아이디/비밀번호 입력
 *   3. CustomUserDetailsService가 DB에서 사용자 확인
 *   4. 비밀번호 맞으면 로그인 성공 → /restaurants로 이동
 */