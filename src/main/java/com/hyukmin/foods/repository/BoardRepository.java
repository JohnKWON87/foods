package com.hyukmin.foods.repository;

import com.hyukmin.foods.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * BoardRepository - 게시판 데이터 접근 인터페이스
 *
 * 이 파일이 하는 일:
 * 1. DB의 board 테이블에서 데이터를 조회/저장/수정/삭제
 * 2. 게시글 검색 기능 제공 (제목, 내용으로 검색)
 * 3. 특정 사용자가 쓴 글 조회
 * 4. 페이징 처리 (한 페이지에 10개씩 보기)
 *
 * 왜 필요한가?
 * - Service 계층에서 DB에 직접 접근하지 않고 Repository를 통해 접근
 * - 복잡한 SQL 쿼리를 메서드 이름만으로 자동 생성
 * - 페이징, 정렬 기능을 쉽게 구현
 *
 * 연결되는 파일:
 * → Board 엔티티: 게시글 데이터 구조 정의
 * → BoardService: 이 Repository를 사용해서 비즈니스 로직 처리
 * → BoardController: Service를 통해 간접적으로 사용
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
// JpaRepository<Board, Long>의 의미:
    // - Board: 다룰 엔티티 타입
    // - Long: Board 엔티티의 ID(Primary Key) 타입

    // JpaRepository가 기본으로 제공하는 메서드들:
    // - save(board): 게시글 저장/수정
    // - findById(id): ID로 게시글 조회
    // - findAll(): 모든 게시글 조회
    // - deleteById(id): ID로 게시글 삭제
    // - count(): 전체 게시글 수

    /**
     * 제목 또는 내용으로 검색 (페이징 지원)
     *
     * 메서드 이름 분석:
     * findBy - "~로 찾기" (검색)
     * TitleContaining - 제목(Title)에 포함(Containing)
     * IgnoreCase - 대소문자 구분 안 함
     * Or - 또는
     * ContentContaining - 내용(Content)에 포함
     * IgnoreCase - 대소문자 구분 안 함
     *
     * 실제 실행되는 SQL:
     * SELECT * FROM board
     * WHERE LOWER(title) LIKE LOWER('%검색어%')
     *    OR LOWER(content) LIKE LOWER('%검색어%')
     * ORDER BY created_at DESC
     * LIMIT 10 OFFSET 0;
     *
     * @param title 제목 검색어
     * @param content 내용 검색어 (보통 title과 같은 값)
     * @param pageable 페이징 정보 (페이지 번호, 한 페이지 크기, 정렬 방식)
     * @return 검색된 게시글 목록 (페이징 포함)
     */
    Page<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);
    /**
     * 특정 사용자의 게시글 조회 (페이징 지원)
     *
     * 메서드 이름 분석:
     * findBy - "~로 찾기"
     * AuthorId - 작성자(Author)의 ID
     *
     * 실제 실행되는 SQL:
     * SELECT * FROM board
     * WHERE author_id = 1
     * ORDER BY created_at DESC
     * LIMIT 10 OFFSET 0;
     *
     * @param authorId 작성자 ID (User의 ID)
     * @param pageable 페이징 정보
     * @return 해당 사용자가 작성한 게시글 목록
     */
    Page<Board> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * 전체 게시글 개수
     *
     * JpaRepository가 기본 제공하는 count() 메서드를 오버라이드
     * (사실 안 써도 되지만, 명시적으로 선언)
     *
     * 실제 실행되는 SQL:
     * SELECT COUNT(*) FROM board;
     *
     * @return 전체 게시글 수
     */
    long count();
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 게시글 검색 - "맛집" 키워드로 검색
 *
 *   사용자: 검색창에 "맛집" 입력 후 검색 버튼 클릭
 *   ↓
 *   GET /boards/search?keyword=맛집&page=0
 *   ↓
 *   BoardController.search(keyword="맛집", page=0)
 *   ↓
 *   BoardService.searchBoards("맛집", 0)
 *   ↓
 *   Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
 *   // 0페이지, 한 페이지에 10개, 작성일 기준 내림차순 정렬
 *   ↓
 *   boardRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
 *       "맛집", "맛집", pageable
 *   )
 *   ↓
 *   Spring Data JPA가 자동으로 SQL 생성:
 *   SELECT * FROM board
 *   WHERE LOWER(title) LIKE LOWER('%맛집%')
 *      OR LOWER(content) LIKE LOWER('%맛집%')
 *   ORDER BY created_at DESC
 *   LIMIT 10 OFFSET 0;
 *   ↓
 *   검색 결과:
 *   Page<Board> {
 *       content: [
 *           Board{id=5, title="강남 맛집 추천", content="..."},
 *           Board{id=3, title="홍대 근처 맛집", content="..."},
 *           Board{id=1, title="서울 맛집 베스트 10", content="..."}
 *       ],
 *       totalElements: 15,  // 전체 검색 결과 수
 *       totalPages: 2,      // 전체 페이지 수 (15개 ÷ 10개 = 2페이지)
 *       number: 0,          // 현재 페이지 번호
 *       size: 10            // 한 페이지 크기
 *   }
 *   ↓
 *   board-list.html에 검색 결과 표시
 *   - 1페이지: 게시글 1~10
 *   - 2페이지: 게시글 11~15
 *
 *
 * 시나리오 2) 내가 쓴 글 보기
 *
 *   사용자(hyukmin, id=1): "내가 쓴 글" 메뉴 클릭
 *   ↓
 *   GET /boards/my-posts?page=0
 *   ↓
 *   BoardController.myPosts(Authentication auth, page=0)
 *   ↓
 *   현재 로그인한 사용자 정보:
 *   String username = auth.getName();  // "hyukmin"
 *   User user = userRepository.findByUsername("hyukmin");
 *   Long userId = user.getId();  // 1
 *   ↓
 *   BoardService.findByAuthorId(userId=1, page=0)
 *   ↓
 *   Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
 *   ↓
 *   boardRepository.findByAuthorId(1L, pageable)
 *   ↓
 *   실행되는 SQL:
 *   SELECT * FROM board
 *   WHERE author_id = 1
 *   ORDER BY created_at DESC
 *   LIMIT 10 OFFSET 0;
 *   ↓
 *   결과:
 *   Page<Board> {
 *       content: [
 *           Board{id=10, title="오늘의 맛집", author_id=1},
 *           Board{id=8, title="강남 피자집 후기", author_id=1},
 *           Board{id=5, title="홍대 카페 추천", author_id=1}
 *       ],
 *       totalElements: 3,
 *       totalPages: 1,
 *       number: 0
 *   }
 *   ↓
 *   my-posts.html에 내가 쓴 글 목록 표시
 *
 *
 * 시나리오 3) 게시글 저장 (JpaRepository 기본 메서드)
 *
 *   사용자: 새 게시글 작성
 *   - 제목: "강남 맛집 추천"
 *   - 내용: "강남역 근처 피자집이 정말 맛있어요!"
 *   ↓
 *   POST /boards
 *   ↓
 *   BoardController.create(boardDTO, Authentication auth)
 *   ↓
 *   BoardService.create(boardDTO, username)
 *   ↓
 *   User author = userRepository.findByUsername("hyukmin");
 *   ↓
 *   Board board = Board.builder()
 *       .title("강남 맛집 추천")
 *       .content("강남역 근처 피자집이 정말 맛있어요!")
 *       .author(author)
 *       .build();
 *   ↓
 *   boardRepository.save(board);
 *   ↓
 *   실행되는 SQL:
 *   INSERT INTO board (title, content, author_id, created_at, updated_at, view_count)
 *   VALUES ('강남 맛집 추천', '강남역 근처 피자집이 정말 맛있어요!', 1, NOW(), NOW(), 0);
 *   ↓
 *   DB board 테이블:
 *   +----+------------------+------------------------------+-----------+---------------------+---------------------+------------+
 *   | id | title            | content                      | author_id | created_at          | updated_at          | view_count |
 *   +----+------------------+------------------------------+-----------+---------------------+---------------------+------------+
 *   | 1  | 강남 맛집 추천   | 강남역 근처 피자집이...       | 1         | 2025-10-17 10:30:00 | 2025-10-17 10:30:00 | 0          |
 *   +----+------------------+------------------------------+-----------+---------------------+---------------------+------------+
 *   ↓
 *   redirect:/boards/1 (작성된 게시글 상세 페이지로)
 *
 *
 * 시나리오 4) 게시글 조회 (JpaRepository 기본 메서드)
 *
 *   사용자: 게시글 클릭
 *   ↓
 *   GET /boards/1
 *   ↓
 *   BoardController.detail(id=1)
 *   ↓
 *   BoardService.findById(1L)
 *   ↓
 *   boardRepository.findById(1L)
 *   ↓
 *   실행되는 SQL:
 *   SELECT * FROM board WHERE id = 1;
 *   ↓
 *   결과:
 *   Optional<Board> {
 *       Board {
 *           id: 1,
 *           title: "강남 맛집 추천",
 *           content: "강남역 근처 피자집이 정말 맛있어요!",
 *           author: User{id=1, username="hyukmin"},
 *           createdAt: 2025-10-17 10:30:00,
 *           viewCount: 0
 *       }
 *   }
 *   ↓
 *   조회수 증가:
 *   board.setViewCount(board.getViewCount() + 1);
 *   boardRepository.save(board);  // 조회수 업데이트
 *   ↓
 *   board-detail.html에 게시글 내용 표시
 *
 *
 * 시나리오 5) 게시글 목록 조회 - 페이징
 *
 *   사용자: 게시판 메뉴 클릭
 *   ↓
 *   GET /boards?page=0
 *   ↓
 *   BoardController.list(page=0)
 *   ↓
 *   BoardService.findAll(page=0)
 *   ↓
 *   Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
 *   ↓
 *   boardRepository.findAll(pageable)
 *   ↓
 *   실행되는 SQL:
 *   SELECT * FROM board
 *   ORDER BY created_at DESC
 *   LIMIT 10 OFFSET 0;
 *   ↓
 *   결과:
 *   Page<Board> {
 *       content: [최신 게시글 10개],
 *       totalElements: 156,  // 전체 게시글 수
 *       totalPages: 16,      // 156 ÷ 10 = 16페이지
 *       number: 0,           // 현재 0페이지
 *       size: 10
 *   }
 *   ↓
 *   board-list.html에 게시글 목록 표시
 *   페이징: [1] [2] [3] ... [16]
 *
 *
 * 시나리오 6) 전체 게시글 수 조회
 *
 *   관리자: 대시보드에서 통계 확인
 *   ↓
 *   GET /admin/dashboard
 *   ↓
 *   AdminController.dashboard()
 *   ↓
 *   long totalBoards = boardRepository.count();
 *   ↓
 *   실행되는 SQL:
 *   SELECT COUNT(*) FROM board;
 *   ↓
 *   결과: 156
 *   ↓
 *   대시보드에 "전체 게시글: 156개" 표시
 *
 *
 * ========== Spring Data JPA 메서드 이름 규칙 ==========
 *
 * 키워드              SQL               예시
 * ───────────────────────────────────────────────────────────
 * findBy            SELECT            findByTitle
 * And               AND               findByTitleAndContent
 * Or                OR                findByTitleOrContent
 * Containing        LIKE %값%         findByTitleContaining
 * StartingWith      LIKE 값%          findByTitleStartingWith
 * EndingWith        LIKE %값          findByTitleEndingWith
 * IgnoreCase        LOWER()           findByTitleIgnoreCase
 * OrderBy           ORDER BY          findByTitleOrderByCreatedAtDesc
 * Between           BETWEEN           findByCreatedAtBetween
 * LessThan          <                 findByViewCountLessThan
 * GreaterThan       >                 findByViewCountGreaterThan
 * IsNull            IS NULL           findByDeletedAtIsNull
 * IsNotNull         IS NOT NULL       findByDeletedAtIsNotNull
 *
 * 예시:
 * findByTitleContainingAndAuthorId(String title, Long authorId)
 * → SELECT * FROM board WHERE title LIKE '%값%' AND author_id = ?
 *
 *
 * ========== 페이징 처리 설명 ==========
 *
 * Pageable 객체 생성:
 * Pageable pageable = PageRequest.of(
 *     0,                              // 페이지 번호 (0부터 시작)
 *     10,                             // 한 페이지 크기
 *     Sort.by("createdAt").descending() // 정렬 방식
 * );
 *
 * Page 객체 구조:
 * Page<Board> page = boardRepository.findAll(pageable);
 *
 * page.getContent()        → List<Board>: 현재 페이지의 게시글 목록
 * page.getTotalElements()  → long: 전체 게시글 수
 * page.getTotalPages()     → int: 전체 페이지 수
 * page.getNumber()         → int: 현재 페이지 번호 (0부터)
 * page.getSize()           → int: 한 페이지 크기
 * page.hasNext()           → boolean: 다음 페이지 존재 여부
 * page.hasPrevious()       → boolean: 이전 페이지 존재 여부
 *
 * 실제 SQL 변환:
 * PageRequest.of(0, 10)    → LIMIT 10 OFFSET 0
 * PageRequest.of(1, 10)    → LIMIT 10 OFFSET 10
 * PageRequest.of(2, 10)    → LIMIT 10 OFFSET 20
 *
 *
 * ========== Repository 사용 흐름 ==========
 *
 * Controller
 *    ↓ (요청 받음)
 *    ↓
 * Service
 *    ↓ (비즈니스 로직)
 *    ↓
 * Repository (여기!)
 *    ↓ (SQL 실행)
 *    ↓
 * Database
 *    ↓ (결과 반환)
 *    ↓
 * Repository
 *    ↓
 * Service
 *    ↓
 * Controller
 *    ↓ (응답)
 *    ↓
 * View (HTML)
 *
 *
 * ========== JpaRepository가 제공하는 기본 메서드 ==========
 *
 * 저장/수정:
 * - save(entity): 엔티티 저장 또는 수정
 * - saveAll(entities): 여러 엔티티 한번에 저장
 *
 * 조회:
 * - findById(id): ID로 조회 → Optional<Board>
 * - findAll(): 전체 조회 → List<Board>
 * - findAll(pageable): 페이징 조회 → Page<Board>
 * - findAllById(ids): 여러 ID로 조회 → List<Board>
 * - existsById(id): ID 존재 여부 → boolean
 * - count(): 전체 개수 → long
 *
 * 삭제:
 * - deleteById(id): ID로 삭제
 * - delete(entity): 엔티티 삭제
 * - deleteAll(): 전체 삭제
 * - deleteAll(entities): 여러 엔티티 삭제
 *
 *
 * ========== 이 파일의 핵심 정리 ==========
 *
 * 1. BoardRepository는 인터페이스만 선언하면 됨
 *    → Spring Data JPA가 자동으로 구현 클래스 생성
 *
 * 2. 메서드 이름만으로 SQL 쿼리 자동 생성
 *    → findByTitleContaining → WHERE title LIKE '%값%'
 *
 * 3. 페이징 처리가 매우 쉬움
 *    → Pageable 파라미터만 추가하면 자동으로 LIMIT, OFFSET 처리
 *
 * 4. 기본 CRUD 메서드는 JpaRepository가 제공
 *    → save(), findById(), findAll(), delete() 등
 *
 * 5. 복잡한 쿼리는 커스텀 메서드로 추가 가능
 *    → findByTitleContainingOrContentContaining...
 */
