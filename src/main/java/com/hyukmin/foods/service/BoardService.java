package com.hyukmin.foods.service;

import com.hyukmin.foods.entity.Board;
import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.repository.BoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * BoardService - 게시판 비즈니스 로직
 *
 * 이 파일 역할: 게시글 작성/수정/삭제 전에 검증하고 처리
 *
 * 데이터 흐름:
 * Controller → Service(여기! 검증/처리) → Repository → DB
 */
@Service
public class BoardService {

    private final BoardRepository repository;

    public BoardService(BoardRepository repository) {
        this.repository = repository;
    }

    /**
     * 모든 게시글 조회 (페이징)
     */
    public Page<Board> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * 특정 ID의 게시글 조회
     */
    public Board findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * 게시글 작성
     */
    @Transactional// DB 작업 실패시 자동 롤백
    public Board save(Board board) {
        // 검증: 작성자 있어야 함
        if (board.getAuthor() == null) {
            throw new IllegalArgumentException("작성자 정보가 필요합니다.");
        }
        // 검증: 제목 필수
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        // 검증: 내용 필수
        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }

        System.out.println("✅ 게시글 저장: " + board.getTitle() + " (작성자: " + board.getAuthor().getUsername() + ")");
        return repository.save(board);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public Board update(Long id, Board updateBoard) {
        // 1. 기존 게시글 찾기
        Board board = findById(id);
        if (board == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        // 2. 제목/내용 변경
        if (updateBoard.getTitle() != null && !updateBoard.getTitle().trim().isEmpty()) {
            board.setTitle(updateBoard.getTitle());
        }
        if (updateBoard.getContent() != null && !updateBoard.getContent().trim().isEmpty()) {
            board.setContent(updateBoard.getContent());
        }

        // 3. 수정일 업데이트
        board.updateModifiedDate();

        System.out.println("✅ 게시글 수정: ID=" + id);
        return repository.save(board);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void delete(Long id) {
        Board board = findById(id);
        if (board == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        System.out.println("🗑️ 게시글 삭제: ID=" + id + " (" + board.getTitle() + ")");
        repository.deleteById(id);
    }

    /**
     * 제목 또는 내용으로 검색 (페이징)
     */
    public Page<Board> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(pageable);
        }

        System.out.println("🔍 게시글 검색: " + keyword);
        return repository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    /**
     * 특정 사용자의 게시글 조회 (페이징)
     */
    public Page<Board> findByAuthor(Long authorId, Pageable pageable) {
        return repository.findByAuthorId(authorId, pageable);
    }

    /**
     * 조회수 +1 증가
     */
    @Transactional
    public void increaseViewCount(Long id) {
        Board board = findById(id);
        if (board != null) {
            board.increaseViewCount(); // 조회수 ++
            repository.save(board);
            System.out.println("👁️ 조회수 증가: ID=" + id + " (" + board.getViewCount() + ")");
        }
    }

    /**
     * 전체 게시글 개수
     */
    public long getTotalCount() {
        return repository.count();
    }
}
/*
 * 핵심 역할:
 *
 * 1. 검증 (Validation)
 *    - 제목/내용 비어있는지 확인
 *    - 작성자 있는지 확인
 *    - 게시글 존재하는지 확인
 *
 * 2. 비즈니스 로직
 *    - 조회수 증가 처리
 *    - 수정일 업데이트
 *    - 검색어 없을 때 전체 조회
 *
 * 3. Repository 호출
 *    - 검증 통과하면 DB 작업 실행
 *
 *
 * 동작 흐름 예시:
 *
 * 게시글 작성:
 * Controller → save(board) → 검증(제목/내용/작성자) → repository.save() → DB저장
 *
 * 게시글 수정:
 * Controller → update(id, board) → 게시글찾기 → 내용변경 → 수정일갱신 → DB저장
 *
 * 게시글 조회:
 * Controller → findById(id) → increaseViewCount(id) → 조회수+1 → 화면표시
 */