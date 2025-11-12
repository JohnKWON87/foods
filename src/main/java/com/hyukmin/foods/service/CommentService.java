package com.hyukmin.foods.service;

import com.hyukmin.foods.entity.Comment;
import com.hyukmin.foods.entity.Board;
import com.hyukmin.foods.repository.CommentRepository;
import com.hyukmin.foods.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
/**
 * CommentService - 댓글 비즈니스 로직
 *
 * 이 파일 역할: 댓글 작성/수정/삭제 전에 검증하고 처리
 *
 * 데이터 흐름:
 * Controller → Service(여기! 검증/처리) → Repository → DB
 */
@Service
public class CommentService {

    private final CommentRepository repository;
    private final BoardRepository boardRepository;

    public CommentService(CommentRepository repository, BoardRepository boardRepository) {
        this.repository = repository;
        this.boardRepository = boardRepository;
    }

    /**
     * 특정 게시글의 댓글 조회
     */
    public List<Comment> findByBoardId(Long boardId) {
        return repository.findByBoardId(boardId);
    }

    /**
     * 특정 댓글 1개 조회
     */
    public Comment findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public Comment save(Comment comment) {
        // 검증: 게시글 있어야 함
        if (comment.getBoard() == null) {
            throw new IllegalArgumentException("게시글 정보가 필요합니다.");
        }
        // 검증: 작성자 있어야 함
        if (comment.getAuthor() == null) {
            throw new IllegalArgumentException("작성자 정보가 필요합니다.");
        }
        // 검증: 내용 필수
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        // 검증: 길이 제한 (500자)
        if (comment.getContent().length() > 500) {
            throw new IllegalArgumentException("댓글은 500자 이하여야 합니다.");
        }

        System.out.println("💬 댓글 저장: " + comment.getContent() + " (작성자: " + comment.getAuthor().getUsername() + ")");
        return repository.save(comment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public Comment update(Long id, Comment updateComment) {
        // 1. 기존 댓글 찾기
        Comment comment = findById(id);
        if (comment == null) {
            throw new IllegalArgumentException("댓글을 찾을 수 없습니다.");
        }

        // 2. 내용 변경 및 검증
        if (updateComment.getContent() != null && !updateComment.getContent().trim().isEmpty()) {
            if (updateComment.getContent().length() > 500) {
                throw new IllegalArgumentException("댓글은 500자 이하여야 합니다.");
            }
            comment.setContent(updateComment.getContent());
        }

        // 3. 수정일 업데이트
        comment.updateModifiedDate();

        System.out.println("✏️ 댓글 수정: ID=" + id);
        return repository.save(comment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void delete(Long id) {
        Comment comment = findById(id);
        if (comment == null) {
            throw new IllegalArgumentException("댓글을 찾을 수 없습니다.");
        }

        System.out.println("🗑️ 댓글 삭제: ID=" + id);
        repository.deleteById(id);
    }

    /**
     * 특정 게시글의 댓글 개수
     */
    public long getCommentCountByBoard(Long boardId) {
        return repository.countByBoardId(boardId);
    }

    /**
     * 내가 쓴 댓글 개수
     */
    public long getCommentCountByAuthor(Long authorId) {
        return repository.countByAuthorId(authorId);
    }
}

/*
 * 핵심 역할:
 *
 * 1. 검증
 *    - 내용 비어있는지 확인
 *    - 길이 제한 (500자)
 *    - 게시글/작성자 존재 확인
 *
 * 2. 비즈니스 로직
 *    - 수정일 업데이트
 *    - 댓글 개수 계산
 *
 * 3. Repository 호출
 *    - 검증 통과하면 DB 작업 실행
 *
 *
 * 동작 흐름 예시:
 *
 * 댓글 작성:
 * Controller → save(comment) → 검증(내용/길이/게시글/작성자) → DB저장 → 화면표시
 *
 * 게시글 열기:
 * Controller → findByBoardId() → 댓글목록 → 화면에 댓글들 표시
 *
 * 댓글 개수:
 * Controller → getCommentCountByBoard() → 12 → "댓글 12개" 표시
 */