package com.hyukmin.foods.service;

import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * UserService - 회원 비즈니스 로직
 *
 * 이 파일 역할: 회원가입 시 중복 체크 + 비밀번호 암호화
 *
 * 데이터 흐름:
 * Controller → Service(여기! 검증/암호화) → Repository → DB
 */
/**
 * 회원 관련 비즈니스 로직 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 조회만 (성능 최적화)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구
//왜 이렇게?
//
//대부분의 메서드는 조회만 하므로 readOnly = true (성능 최적화)
//회원가입(registerUser)만 DB에 저장하므로 @Transactional 명시
    /**
     * 회원가입
     * @param username 로그인 ID
     * @param password 평문 비밀번호
     * @param email 이메일
     * @return 저장된 User 엔티티
     * @throws IllegalArgumentException 아이디 또는 이메일 중복 시
     */
    // 회원가입
    @Transactional // DB 변경 작업이라 readOnly 해제
    public User registerUser(String username, String password, String email) {
        // 1. 아이디 중복 체크
//🔹 회원가입 프로세스
//1. 아이디 중복 체크
//   ↓
//2. 이메일 중복 체크
//   ↓
//3. 비밀번호 암호화 (BCrypt)
//   ↓
//4. User 엔티티 생성
//   ↓
//5. DB에 저장
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 4. User 엔티티 생성
        User user = User.builder()
                .username(username)
                .password(encodedPassword)  // 암호화된 비밀번호 저장
                .email(email)
                .role("ROLE_USER")  // 기본값: 일반 회원
                .build();

        // 5. DB에 저장
        return userRepository.save(user);
    }

    /**
     * 아이디 중복 체크
     * @param username 체크할 아이디
     * @return 중복이면 true, 사용 가능하면 false
     */
    // 아이디 중복 체크 (실시간 검증용)
    public boolean isUsernameDuplicated(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 이메일 중복 체크
     * @param email 체크할 이메일
     * @return 중복이면 true, 사용 가능하면 false
     */
    // 이메일 중복 체크
    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자 정보 조회 (username으로)
     * @param username 로그인 ID
     * @return User 엔티티 (없으면 null)
     */
    // 사용자 찾기 (username으로)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 사용자 정보 조회 (ID로)
     * @param id 사용자 ID
     * @return User 엔티티 (없으면 null)
     */
    // 사용자 찾기 (ID로)
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
/*
 * 핵심 역할:
 *
 * 1. 회원가입 검증
 *    - 아이디 중복 체크
 *    - 이메일 중복 체크
 *
 * 2. 비밀번호 암호화
 *    - "password123" → "$2a$10$abcd..."
 *    - BCrypt 알고리즘 사용 (단방향 암호화)
 *
 * 3. 기본 권한 설정
 *    - 회원가입 시 자동으로 "ROLE_USER" 부여
 *
 *
 * 동작 흐름:
 *
 * 회원가입:
 * 1. 폼 제출 → registerUser()
 * 2. 아이디 중복? → 에러
 * 3. 이메일 중복? → 에러
 * 4. 비밀번호 암호화
 * 5. DB 저장
 * 6. 로그인 페이지로
 *
 * 실시간 중복 체크:
 * 1. 아이디 입력중 (JavaScript)
 * 2. AJAX → isUsernameDuplicated()
 * 3. true면 "중복" 표시, false면 "사용가능" 표시
 *
 *
 * 비밀번호 암호화 원리:
 *
 * 평문: "password123"
 *   ↓ encode()
 * 암호문: "$2a$10$abcd1234efgh..."
 *   ↓ DB 저장
 *
 * 로그인 시:
 * 입력: "password123"
 *   ↓ matches()
 * DB: "$2a$10$abcd1234efgh..."
 *   ↓ 비교
 * 일치하면 로그인 성공!
 */