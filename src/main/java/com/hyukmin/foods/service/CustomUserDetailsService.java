package com.hyukmin.foods.service;

import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
/**
 * CustomUserDetailsService - 로그인 처리
 *
 * 이 파일 역할: Spring Security가 로그인 시 사용자 정보를 가져옴
 *
 * 데이터 흐름:
 * 로그인폼 제출 → Spring Security → loadUserByUsername(여기!) → DB조회 → 비밀번호 검증
 */
/**
 * Spring Security가 로그인 시 사용자 정보를 조회하는 서비스
 * UserDetailsService 인터페이스를 구현해야 함
 */
@Service
@RequiredArgsConstructor
// UserDetailsService: Spring Security가 요구하는 인터페이스
// loadUserByUsername() 메서드를 반드시 구현해야 함
public class CustomUserDetailsService implements UserDetailsService {
// Spring Security가 로그인 시 자동으로 호출하는 인터페이스
// loadUserByUsername() 메서드를 반드시 구현
//사용자가 로그인 폼 제출
//    ↓
//Spring Security가 자동으로 loadUserByUsername() 호출
//    ↓
//DB에서 username으로 사용자 조회
//    ↓
//User 엔티티를 UserDetails로 변환
//    ↓
//Spring Security가 비밀번호 검증
//    ↓
//로그인 성공/실패
    private final UserRepository userRepository;
    /**
     * Spring Security가 로그인 시 자동으로 호출하는 메서드
     *
     * @param username 로그인 폼에서 입력한 ID
     * @return UserDetails (Spring Security가 비밀번호 검증에 사용)
     */

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 2. User 엔티티를 Spring Security의 UserDetails로 변환
        return createUserDetails(user);
    }
//동작:
//
//DB에서 username으로 사용자 조회
//사용자가 없으면 → UsernameNotFoundException 발생 (로그인 실패)
//사용자가 있으면 → UserDetails로 변환하여 반환

    /**
     * User 엔티티를 Spring Security의 UserDetails 객체로 변환
     * @param user DB에서 조회한 User 엔티티
     * @return UserDetails 객체
     */
    /**
     * User 엔티티 → UserDetails 변환
     */
    private UserDetails createUserDetails(User user) {
        // 권한 설정 (ROLE_USER 또는 ROLE_ADMIN)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        // Spring Security의 User 객체 생성 (org.springframework.security.core.userdetails.User)
        return org.springframework.security.core.userdetails.User.builder()// 명확하게 전체 경로로 작성
                .username(user.getUsername())      // 로그인 ID
                .password(user.getPassword())      // 이미 암호화된 비밀번호
                .authorities(authorities)          // 권한 목록, ROLE_USER 또는 ROLE_ADMIN
                .accountExpired(false)             // 계정 만료 여부
                .accountLocked(false)              // 계정 잠김 여부
                .credentialsExpired(false)         // 비밀번호 만료 여부
                .disabled(false)                   // 계정 비활성화 여부
                .build();
//중요 포인트:
//
//password는 이미 암호화된 비밀번호를 그대로 전달
//Spring Security가 자동으로 입력한 비밀번호와 비교
//authorities에 권한 정보 설정 (ROLE_USER, ROLE_ADMIN)
    }
}
// ❌ 잘못된 import
//import com.hyukmin.foods.entity.User;  // 우리 엔티티
// ✅ 올바른 import (return 타입)
//import org.springframework.security.core.userdetails.User;  // Spring Security의 User

/*
 * 핵심 역할:
 *
 * 1. 사용자 조회
 *    - DB에서 username으로 사용자 찾기
 *    - 없으면 UsernameNotFoundException 발생
 *
 * 2. 권한 설정
 *    - ROLE_USER 또는 ROLE_ADMIN 권한 부여
 *
 * 3. UserDetails 변환
 *    - 우리 User 엔티티를 Spring Security가 이해하는 형식으로 변환
 *
 *
 * 로그인 동작 흐름:
 *
 * 1. 사용자: ID/PW 입력 → 로그인 버튼 클릭
 * 2. Spring Security: loadUserByUsername("hyukmin") 자동 호출
 * 3. DB 조회: SELECT * FROM users WHERE username='hyukmin'
 * 4. User 엔티티 → UserDetails 변환
 * 5. Spring Security: 비밀번호 자동 검증
 *    - 입력한 PW vs DB의 암호화된 PW 비교
 * 6. 일치하면 로그인 성공, 다르면 실패
 *
 *
 * 주의사항:
 *
 * User 클래스가 2개!
 * - com.hyukmin.foods.entity.User (우리가 만든 엔티티)
 * - org.springframework.security.core.userdetails.User (Spring Security)
 *
 * 반환할 때는 Spring Security의 User 사용!
 */