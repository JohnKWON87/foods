package com.hyukmin.foods.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Null;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Board - 게시글 엔티티
 *
 * 이 파일이 하는 일:
 * 1. 게시판 게시글 데이터를 표현
 * 2. DB의 board 테이블과 1:1 매핑
 * 3. 게시글 관련 데이터(제목, 내용, 작성자, 조회수 등)를 저장
 *
 * 왜 필요한가?
 * - 사용자가 작성한 게시글을 DB에 저장하고 관리하기 위해
 * - JPA가 이 클래스를 보고 자동으로 board 테이블을 생성하고 관리함
 *
 * 연결되는 파일:
 * → User 엔티티: 게시글 작성자 정보 (한 명의 유저가 여러 게시글 작성)
 * → Comment 엔티티: 게시글에 달린 댓글들 (한 게시글에 여러 댓글)
 * → BoardRepository: DB에서 게시글 조회/저장
 * → BoardService: 게시글 비즈니스 로직
 * → BoardController: 게시글 요청 처리
 */
@Entity
@Table(name = "board")
public class Board {

    // 게시글 고유 번호 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 제목 (최대 100자, 필수)
    @Column(nullable = false, length = 100)
    private String title;

    // 게시글 내용 (긴 텍스트, 필수)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 작성자 정보
    // N:1 관계 - 여러 게시글(N) : 한 명의 유저(1)
    // LAZY: 게시글 조회 시 작성자 정보는 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 조회수 (기본값 0)
    @Column(nullable = false)
    private Integer viewCount = 0;

    // 작성일 (한 번 저장되면 수정 불가)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 수정일 (수정했을 때만 값이 들어감)
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // ========== Getter / Setter ==========
    // getter (읽기)
    public Long getId() {
        return id;
    }
    // setter (쓰기)
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== 비즈니스 로직 ==========

    /**
     * 조회수 증가
     * 언제 사용? BoardService에서 게시글 상세보기 할 때
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 수정일 업데이트
     * 언제 사용? BoardService에서 게시글 수정할 때
     */
    public void updateModifiedDate() {
        this.updatedAt = LocalDateTime.now();
    }
}
/*
        * ========== 실제로 어떻게 동작하나요? ==========
        *
        * 시나리오 1) 게시글 작성
 *
         *   사용자: "오늘의 맛집" 제목으로 게시글 작성
 *   ↓
         *   board-form.html에서 제목, 내용 입력 후 제출
 *   ↓
         *   POST /boards
 *   ↓
         *   BoardController.create()
 *   ↓
         *   BoardService.save()
 *   - Board 객체 생성
 *   - board.setTitle("오늘의 맛집")
 *   - board.setContent("정말 맛있었어요...")
 *   - board.setAuthor(로그인한 유저)
 *   - board.setViewCount(0)
 *   - board.setCreatedAt(현재시간)
 *   ↓
         *   BoardRepository.save(board)
 *   ↓
         *   DB board 테이블에 저장:
        *   +----+-------------+-------------------+-----------+------------+---------------------+
        *   | id | title       | content           | author_id | view_count | created_at          |
        *   +----+-------------+-------------------+-----------+------------+---------------------+
        *   | 1  | 오늘의 맛집 | 정말 맛있었어요... | 5         | 0          | 2025-01-15 10:30:00 |
        *   +----+-------------+-------------------+-----------+------------+---------------------+
        *
        *
        * 시나리오 2) 게시글 목록 조회
 *
         *   사용자: 게시판 접속
 *   ↓
         *   GET /boards
 *   ↓
         *   BoardController.list()
 *   ↓
         *   BoardService.findAll()
 *   ↓
         *   BoardRepository.findAll()
 *   ↓
         *   DB에서 board 테이블 전체 조회
 *   ↓
         *   각 행이 Board 객체로 변환됨:
        *   List<Board> boards = [
        *       Board { id=1, title="오늘의 맛집", viewCount=5, ... },
        *       Board { id=2, title="강남 맛집", viewCount=12, ... },
        *       Board { id=3, title="분위기 좋은 곳", viewCount=8, ... }
 *   ]
         *   ↓
         *   board-list.html에 게시글 목록 표시
 *
         *
         * 시나리오 3) 게시글 상세보기
 *
         *   사용자: 게시글 1번 클릭
 *   ↓
         *   GET /boards/1
        *   ↓
        *   BoardController.detail(1)
 *   ↓
         *   BoardService.findById(1)
 *   - BoardRepository.findById(1) 호출
 *   - DB에서 id=1인 게시글 조회
 *   - Board 객체로 변환
 *   ↓
         *   Board board = {
        *       id: 1,
        *       title: "오늘의 맛집",
        *       content: "정말 맛있었어요...",
        *       author: User객체 { username: "hyukmin", ... },
        *       viewCount: 5
        *   }
        *   ↓
        *   조회수 증가:
        *   board.increaseViewCount();  // viewCount: 5 → 6
 *   BoardRepository.save(board); // DB 업데이트
 *   ↓
         *   board-detail.html에 게시글 내용 표시
 *
         *
         * 시나리오 4) 게시글 수정
 *
         *   사용자: 게시글 1번 수정 버튼 클릭
 *   ↓
         *   GET /boards/1/edit (수정 폼 표시)
 *   ↓
         *   제목 "오늘의 맛집" → "강력 추천 맛집"으로 변경 후 제출
 *   ↓
         *   POST /boards/1/edit
 *   ↓
         *   BoardController.update(1)
 *   ↓
         *   BoardService.update(1, updatedBoard)
 *   - 기존 게시글 조회: Board board = findById(1)
 *   - 제목, 내용 변경:
        *     board.setTitle("강력 추천 맛집")
 *     board.setContent("수정된 내용...")
 *   - 수정일 업데이트:
        *     board.updateModifiedDate()  // updatedAt = 현재시간
 *   ↓
         *   BoardRepository.save(board)
 *   ↓
         *   DB 업데이트:
        *   +----+-----------------+---------------------+
        *   | id | title           | updated_at          |
        *   +----+-----------------+---------------------+
        *   | 1  | 강력 추천 맛집   | 2025-01-15 14:20:00 |
        *   +----+-----------------+---------------------+
        *   ↓
        *   redirect:/boards/1 (수정된 게시글 상세페이지로)
        *
        *
        * 시나리오 5) 게시글 삭제
 *
         *   사용자: 게시글 1번 삭제 버튼 클릭
 *   ↓
         *   POST /boards/1/delete
 *   ↓
         *   BoardController.delete(1)
 *   ↓
         *   BoardService.delete(1)
 *   ↓
         *   BoardRepository.deleteById(1)
 *   ↓
         *   DB에서 id=1인 게시글 삭제:
        *   DELETE FROM board WHERE id = 1;
        *   ↓
        *   redirect:/boards (게시글 목록으로)
 *
         *
         * ========== DB 테이블 구조 ==========
        *
        * board 테이블 (이 Board 클래스가 만드는 테이블):
        *
        * +------------+---------------+------+-----+---------+----------------+
        * | Field      | Type          | Null | Key | Default | Extra          |
        * +------------+---------------+------+-----+---------+----------------+
        * | id         | bigint        | NO   | PRI | NULL    | auto_increment |
        * | title      | varchar(100)  | NO   |     | NULL    |                |
        * | content    | text          | NO   |     | NULL    |                |
        * | author_id  | bigint        | NO   | FK  | NULL    |                |
        * | view_count | int           | NO   |     | 0       |                |
        * | created_at | datetime      | NO   |     | NULL    |                |
        * | updated_at | datetime      | YES  |     | NULL    |                |
        * +------------+---------------+------+-----+---------+----------------+
        *
        * 실제 데이터 예시:
        * +----+-------------+-------------------+-----------+------------+---------------------+---------------------+
        * | id | title       | content           | author_id | view_count | created_at          | updated_at          |
        * +----+-------------+-------------------+-----------+------------+---------------------+---------------------+
        * | 1  | 오늘의 맛집 | 정말 맛있었어요... | 5         | 23         | 2025-01-15 10:30:00 | 2025-01-15 14:20:00 |
        * | 2  | 강남 맛집   | 강남역 근처...     | 7         | 45         | 2025-01-15 11:00:00 | NULL                |
        * | 3  | 분위기 좋은 | 데이트 하기...     | 5         | 12         | 2025-01-15 15:20:00 | NULL                |
        * +----+-------------+-------------------+-----------+------------+---------------------+---------------------+
        *
        * 외래키 관계:
        * - author_id → users 테이블의 id (게시글 작성자)
 *
         *
         * ========== 다른 엔티티와의 관계 ==========
        *
        * Board (게시글)
 *   ↓
         *   @ManyToOne - author (작성자)
 *   ↓
         * User (회원)
 *
         * 의미: 한 명의 회원(User)이 여러 게시글(Board)을 작성할 수 있음
 *
         * 예시:
        * - hyukmin 회원이 "오늘의 맛집", "강남 맛집", "분위기 좋은" 게시글 3개 작성
 * - DB에서 보면:
        *   board 테이블:
        *   | id | title       | author_id |
        *   | 1  | 오늘의 맛집 | 5         |
        *   | 2  | 강남 맛집   | 5         |
        *   | 3  | 분위기 좋은 | 5         |
        *
        *   users 테이블:
        *   | id | username |
        *   | 5  | hyukmin  |
        *
        *
        * ========== JPA가 자동으로 해주는 것들 ==========
        *
        * 1. 테이블 자동 생성
 *    - Spring Boot 실행 시 board 테이블이 없으면 자동 생성
 *
         * 2. 데이터 저장
 *    - boardRepository.save(board) 하면
 *    - JPA가 자동으로 INSERT SQL 생성하고 실행:
        *      INSERT INTO board (title, content, author_id, view_count, created_at)
 *      VALUES ('오늘의 맛집', '정말 맛있었어요...', 5, 0, '2025-01-15 10:30:00');
 *
         * 3. 데이터 조회
 *    - boardRepository.findById(1) 하면
 *    - JPA가 자동으로 SELECT SQL 생성하고 실행:
        *      SELECT * FROM board WHERE id = 1;
 *    - 조회된 데이터를 Board 객체로 변환
 *
         * 4. 데이터 수정
 *    - board.setTitle("새 제목") 후 save() 하면
 *    - JPA가 자동으로 UPDATE SQL 생성하고 실행:
        *      UPDATE board SET title = '새 제목' WHERE id = 1;
 *
         * 5. 데이터 삭제
 *    - boardRepository.deleteById(1) 하면
 *    - JPA가 자동으로 DELETE SQL 생성하고 실행:
        *      DELETE FROM board WHERE id = 1;
        */