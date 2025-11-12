package com.hyukmin.foods.repository;

import com.hyukmin.foods.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * UserRepository - 회원 DB 작업
 *
 * 이 파일 역할: 회원가입/로그인/중복체크
 *
 * 데이터 흐름:
 * 회원가입/로그인 → Controller → Service → Repository(여기!) → DB
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 // 기본 CRUD 메서드 자동 제공 (save(), findById(), findAll(), delete() 등)
 //User: 엔티티 타입
 //Long: Primary Key 타입
    // JpaRepository<User, Long>: 회원 데이터를 다루는 저장소
    // - User: 다룰 엔티티
    // - Long: 회원 ID 타입

    // 기본 제공 메서드:
    // save(user) - 회원 저장
    // findById(id) - ID로 회원 찾기
    // findAll() - 모든 회원 조회

    // 로그인 시 아이디로 회원 찾기
    // SQL: SELECT * FROM users WHERE username=?
    // 반환: Optional<User> (있을 수도, 없을 수도)
    Optional<User> findByUsername(String username);

    // 아이디 중복 체크 (회원가입 시)
    // SQL: SELECT EXISTS(SELECT 1 FROM users WHERE username=?)
    // 반환: true(중복) / false(사용가능)
    boolean existsByUsername(String username);

    // 이메일 중복 체크 (회원가입 시)
    // SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email=?)
    // 반환: true(중복) / false(사용가능)
    boolean existsByEmail(String email);

    // ✅ 관리자용 검색 메서드 추가
    // 관리자 - 회원 검색
    // SQL: SELECT * FROM users WHERE username LIKE '%검색어%'
    List<User> findByUsernameContainingIgnoreCase(String username);
}
/*
 * 핵심 동작 3가지:
 *
 * 1. 회원가입
 *    정보입력 → existsByUsername(중복체크) → save(저장) → 가입완료
 *
 * 2. 로그인
 *    ID/PW입력 → findByUsername(회원찾기) → 비밀번호확인 → 로그인성공
 *
 * 3. 중복체크 (실시간)
 *    아이디입력중 → existsByUsername() → "사용가능" or "중복" 표시
 */