package com.hyukmin.foods.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
/**
 * Comment - 댓글 엔티티
 *
 * 이 파일이 하는 일:
 * 1. 게시글에 달린 댓글 데이터를 표현
 * 2. DB의 comment 테이블과 1:1 매핑
 * 3. 댓글 내용, 작성자, 어느 게시글에 달렸는지 등의 정보 저장
 *
 * 왜 필요한가?
 * - 사용자들이 게시글에 댓글을 달아 소통하기 위해
 * - 한 게시글에 여러 댓글이 달릴 수 있음
 * - 한 사용자가 여러 댓글을 작성할 수 있음
 *
 * 연결되는 파일:
 * → User 엔티티: 댓글 작성자 정보
 * → Board 엔티티: 댓글이 달린 게시글 정보
 * → CommentRepository: 댓글 데이터 조회/저장
 * → CommentService: 댓글 CRUD 로직
 * → CommentController: 댓글 요청 처리
 */
@Entity
@Table(name = "comment")
public class Comment {

    // 댓글 고유 번호 (자동 증가)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글 내용 (최대 500자, 필수)
    @Column(nullable = false, length = 500)
    private String content;

    // 댓글 작성자
    // N:1 관계 - 여러 댓글(N) : 한 명의 사용자(1)
    // LAZY: 댓글 조회 시 작성자 정보는 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 댓글이 달린 게시글
    // N:1 관계 - 여러 댓글(N) : 한 개의 게시글(1)
    // LAZY: 댓글 조회 시 게시글 정보는 필요할 때만 조회
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    // 댓글 작성일 (한 번 저장되면 수정 불가)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 댓글 수정일 (수정했을 때만 값이 들어감)
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    // ========== Getter / Setter ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
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
     * 수정일 업데이트
     * 언제 사용? CommentService에서 댓글 수정할 때
     */
    public void updateModifiedDate() {
        this.updatedAt = LocalDateTime.now();
    }
}

/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 게시글에 댓글 작성
 *
 *   사용자(hyukmin): 게시글 123번에 "좋은 글이네요!" 댓글 작성
 *   ↓
 *   board-detail.html (게시글 상세 페이지)
 *   - 댓글 폼에 내용 입력 후 제출
 *   ↓
 *   POST /comments?boardId=123&content=좋은 글이네요!
 *   ↓
 *   CommentController.save()
 *   ↓
 *   CommentService.save()
 *   - Comment 객체 생성
 *   ↓
 *   Comment comment = new Comment();
 *   comment.setBoard(게시글123번);
 *   comment.setAuthor(hyukmin);
 *   comment.setContent("좋은 글이네요!");
 *   comment.setCreatedAt(현재시간);
 *   ↓
 *   CommentRepository.save(comment)
 *   ↓
 *   DB comment 테이블에 저장:
 *   +----+------------------+-----------+----------+---------------------+
 *   | id | content          | author_id | board_id | created_at          |
 *   +----+------------------+-----------+----------+---------------------+
 *   | 1  | 좋은 글이네요!    | 5         | 123      | 2025-01-15 10:30:00 |
 *   +----+------------------+-----------+----------+---------------------+
 *   ↓
 *   redirect:/boards/123 (게시글 상세 페이지로 돌아감)
 *   ↓
 *   board-detail.html에 새 댓글 표시:
 *   게시글 제목: "오늘의 맛집"
 *   게시글 내용: ...
 *
 *   댓글:
 *   [hyukmin] 좋은 글이네요! (2025-01-15 10:30)
 *
 *
 * 시나리오 2) 게시글 댓글 목록 조회
 *
 *   사용자: 게시글 123번 접속
 *   ↓
 *   GET /boards/123
 *   ↓
 *   BoardController.detail(123)
 *   ↓
 *   CommentService.findByBoardId(123)
 *   ↓
 *   CommentRepository.findByBoardId(123)
 *   ↓
 *   DB에서 board_id=123인 댓글 모두 조회:
 *   SELECT * FROM comment WHERE board_id = 123 ORDER BY created_at ASC;
 *   ↓
 *   각 행이 Comment 객체로 변환:
 *   List<Comment> comments = [
 *       Comment {
 *           id=1,
 *           content="좋은 글이네요!",
 *           author=hyukmin,
 *           board=게시글123번,
 *           createdAt=2025-01-15 10:30
 *       },
 *       Comment {
 *           id=2,
 *           content="저도 가봤는데 정말 맛있어요",
 *           author=minsu,
 *           board=게시글123번,
 *           createdAt=2025-01-15 11:20
 *       },
 *       Comment {
 *           id=3,
 *           content="다음에 꼭 가봐야겠네요",
 *           author=jimin,
 *           board=게시글123번,
 *           createdAt=2025-01-15 14:45
 *       }
 *   ]
 *   ↓
 *   board-detail.html에 댓글 목록 표시:
 *
 *   댓글 3개:
 *   [hyukmin] 좋은 글이네요! (2025-01-15 10:30)
 *   [minsu] 저도 가봤는데 정말 맛있어요 (2025-01-15 11:20)
 *   [jimin] 다음에 꼭 가봐야겠네요 (2025-01-15 14:45)
 *
 *
 * 시나리오 3) 댓글 수정
 *
 *   사용자(hyukmin): 내가 쓴 댓글 1번 수정
 *   "좋은 글이네요!" → "정말 좋은 글이네요!"
 *   ↓
 *   board-detail.html에서 댓글 수정 버튼 클릭
 *   ↓
 *   POST /comments/1/edit?content=정말 좋은 글이네요!
 *   ↓
 *   CommentController.update(1)
 *   ↓
 *   CommentService.update(1, updateComment)
 *   - 권한 체크 (작성자 본인 또는 관리자만)
 *   - 기존 댓글 조회
 *   ↓
 *   Comment comment = commentRepository.findById(1);
 *   comment.setContent("정말 좋은 글이네요!");
 *   comment.updateModifiedDate();  // updatedAt = 현재시간
 *   ↓
 *   CommentRepository.save(comment)
 *   ↓
 *   DB 업데이트:
 *   +----+------------------------+-----------+----------+---------------------+---------------------+
 *   | id | content                | author_id | board_id | created_at          | updated_at          |
 *   +----+------------------------+-----------+----------+---------------------+---------------------+
 *   | 1  | 정말 좋은 글이네요!     | 5         | 123      | 2025-01-15 10:30:00 | 2025-01-15 15:20:00 |
 *   +----+------------------------+-----------+----------+---------------------+---------------------+
 *   ↓
 *   redirect:/boards/123
 *   ↓
 *   board-detail.html에 수정된 댓글 표시:
 *   [hyukmin] 정말 좋은 글이네요! (2025-01-15 10:30, 수정됨: 2025-01-15 15:20)
 *
 *
 * 시나리오 4) 다른 사람 댓글 수정 시도 (권한 없음)
 *
 *   사용자(minsu): hyukmin이 쓴 댓글 1번을 수정하려고 시도
 *   ↓
 *   POST /comments/1/edit
 *   ↓
 *   CommentController.update(1)
 *   ↓
 *   canModify() 체크
 *   - 현재 사용자: minsu
 *   - 댓글 작성자: hyukmin
 *   - 관리자 권한: 없음
 *   → 결과: false (권한 없음)
 *   ↓
 *   "수정 권한이 없습니다" 메시지
 *   ↓
 *   redirect:/boards/123
 *
 *
 * 시나리오 5) 댓글 삭제
 *
 *   사용자(hyukmin): 내가 쓴 댓글 1번 삭제
 *   ↓
 *   POST /comments/1/delete
 *   ↓
 *   CommentController.delete(1)
 *   ↓
 *   CommentService.delete(1)
 *   - 권한 체크 (작성자 본인 또는 관리자만)
 *   - 게시글 ID 저장 (나중에 리다이렉트용)
 *   ↓
 *   Comment comment = commentRepository.findById(1);
 *   Long boardId = comment.getBoard().getId();  // 123 저장
 *   ↓
 *   CommentRepository.deleteById(1)
 *   ↓
 *   DB에서 삭제:
 *   DELETE FROM comment WHERE id = 1;
 *   ↓
 *   redirect:/boards/123 (원래 게시글로)
 *   ↓
 *   board-detail.html에 댓글이 사라진 채로 표시:
 *
 *   댓글 2개:
 *   [minsu] 저도 가봤는데 정말 맛있어요 (2025-01-15 11:20)
 *   [jimin] 다음에 꼭 가봐야겠네요 (2025-01-15 14:45)
 *
 *
 * 시나리오 6) 관리자가 신고된 댓글 삭제
 *
 *   관리자: 부적절한 댓글 5번 삭제
 *   ↓
 *   POST /comments/5/delete
 *   ↓
 *   CommentController.delete(5)
 *   ↓
 *   canModify() 체크
 *   - 현재 사용자: admin
 *   - 관리자 권한: ROLE_ADMIN 있음
 *   → 결과: true (관리자는 모든 댓글 삭제 가능)
 *   ↓
 *   CommentService.delete(5)
 *   ↓
 *   댓글 삭제 성공
 *   ↓
 *   redirect:/boards/123
 *
 *
 * ========== DB 테이블 구조 ==========
 *
 * comment 테이블 (이 Comment 클래스가 만드는 테이블):
 *
 * +------------+--------------+------+-----+---------+----------------+
 * | Field      | Type         | Null | Key | Default | Extra          |
 * +------------+--------------+------+-----+---------+----------------+
 * | id         | bigint       | NO   | PRI | NULL    | auto_increment |
 * | content    | varchar(500) | NO   |     | NULL    |                |
 * | author_id  | bigint       | NO   | FK  | NULL    |                |
 * | board_id   | bigint       | NO   | FK  | NULL    |                |
 * | created_at | datetime     | NO   |     | NULL    |                |
 * | updated_at | datetime     | YES  |     | NULL    |                |
 * +------------+--------------+------+-----+---------+----------------+
 *
 * 실제 데이터 예시:
 * +----+--------------------------------+-----------+----------+---------------------+---------------------+
 * | id | content                        | author_id | board_id | created_at          | updated_at          |
 * +----+--------------------------------+-----------+----------+---------------------+---------------------+
 * | 1  | 좋은 글이네요!                  | 5         | 123      | 2025-01-15 10:30:00 | NULL                |
 * | 2  | 저도 가봤는데 정말 맛있어요      | 7         | 123      | 2025-01-15 11:20:00 | NULL                |
 * | 3  | 다음에 꼭 가봐야겠네요          | 8         | 123      | 2025-01-15 14:45:00 | NULL                |
 * | 4  | 감사합니다!                     | 5         | 456      | 2025-01-15 16:00:00 | NULL                |
 * | 5  | 정보 감사합니다                 | 7         | 456      | 2025-01-15 17:30:00 | 2025-01-15 18:00:00 |
 * +----+--------------------------------+-----------+----------+---------------------+---------------------+
 *
 * 외래키 관계:
 * - author_id → users 테이블의 id (댓글 작성자)
 * - board_id → board 테이블의 id (댓글이 달린 게시글)
 *
 *
 * ========== 엔티티 관계도 ==========
 *
 * User (사용자)
 *   ↓ 1
 *   | (한 명의 사용자가)
 *   ↓ N
 * Comment (댓글)
 *   ↓ N
 *   | (여러 댓글이)
 *   ↓ 1
 * Board (게시글)
 *
 * 의미:
 * - 한 명의 사용자(User)가 여러 댓글(Comment)을 작성할 수 있음
 * - 한 개의 게시글(Board)에 여러 댓글(Comment)이 달릴 수 있음
 *
 * 예시:
 * hyukmin 사용자 (id=5):
 *   - 게시글 123번에 댓글 1번 작성
 *   - 게시글 456번에 댓글 4번 작성
 *   - 게시글 789번에 댓글 7번 작성
 *
 * 게시글 123번:
 *   - hyukmin의 댓글 1번
 *   - minsu의 댓글 2번
 *   - jimin의 댓글 3번
 *
 *
 * ========== 댓글 표시 화면 예시 ==========
 *
 * board-detail.html:
 *
 * ┌──────────────────────────────────────┐
 * │ 게시글 제목: 오늘의 맛집             │
 * │ 작성자: hyukmin | 조회수: 45          │
 * │──────────────────────────────────────│
 * │ 게시글 내용:                          │
 * │ 오늘 강남에 다녀온 맛집 소개합니다... │
 * │                                      │
 * └──────────────────────────────────────┘
 *
 * ┌──────────────────────────────────────┐
 * │ 댓글 3개                              │
 * │──────────────────────────────────────│
 * │ [hyukmin] 좋은 글이네요!              │
 * │ 2025-01-15 10:30 | [수정] [삭제]     │
 * │──────────────────────────────────────│
 * │ [minsu] 저도 가봤는데 정말 맛있어요   │
 * │ 2025-01-15 11:20                     │
 * │──────────────────────────────────────│
 * │ [jimin] 다음에 꼭 가봐야겠네요        │
 * │ 2025-01-15 14:45                     │
 * └──────────────────────────────────────┘
 *
 * ┌──────────────────────────────────────┐
 * │ 댓글 작성                             │
 * │ [                                  ] │
 * │ [             작성하기              ] │
 * └──────────────────────────────────────┘
 *
 * 참고:
 * - 본인이 쓴 댓글에만 [수정] [삭제] 버튼 표시
 * - 관리자는 모든 댓글에 [삭제] 버튼 표시
 * - 댓글이 수정된 경우 수정 시간도 표시
 */