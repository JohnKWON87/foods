package com.hyukmin.foods.repository;

import com.hyukmin.foods.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CommentRepository - 댓글 DB 작업
 *
 * 이 파일 역할: 댓글 조회/저장/삭제
 *
 * 데이터 흐름:
 * 댓글작성/삭제 → Controller → Service → Repository(여기!) → DB
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 댓글 목록 가져오기
    // SQL: SELECT * FROM comment WHERE board_id=? ORDER BY created_at
    List<Comment> findByBoardId(Long boardId);

    // 내가 쓴 댓글 개수 (예: 25개)
    // SQL: SELECT COUNT(*) FROM comment WHERE author_id=?
    long countByAuthorId(Long authorId);

    // 이 게시글의 댓글 개수 (예: 댓글 12개)
    // SQL: SELECT COUNT(*) FROM comment WHERE board_id=?
    long countByBoardId(Long boardId);
}

/*
 * 핵심 동작 3가지:
 *
 * 1. 댓글 작성
 *    게시글에서 댓글입력 → save() → DB에 저장 → 화면에 표시
 *
 * 2. 댓글 목록 보기
 *    게시글 열기 → findByBoardId() → 댓글 목록 표시
 *
 * 3. 댓글 개수 표시
 *    게시글 목록 → countByBoardId() → "댓글 12개" 표시
 */