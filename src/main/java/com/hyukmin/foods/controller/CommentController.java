package com.hyukmin.foods.controller;

import com.hyukmin.foods.entity.Comment;
import com.hyukmin.foods.entity.Board;
import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.service.CommentService;
import com.hyukmin.foods.service.BoardService;
import com.hyukmin.foods.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 * CommentController - 댓글 컨트롤러
 *
 * 이 파일이 하는 일:
 * 1. 게시글에 댓글 작성
 * 2. 댓글 수정
 * 3. 댓글 삭제
 * 4. 권한 검증 (작성자 본인 또는 관리자만 수정/삭제 가능)
 *
 * 연결되는 파일:
 * → CommentService: 댓글 CRUD 로직
 * → BoardService: 게시글 조회
 * → UserRepository: 작성자 정보 조회
 * → board-detail.html: 댓글 폼과 목록 표시
 */
@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final BoardService boardService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, BoardService boardService, UserRepository userRepository) {
        this.commentService = commentService;
        this.boardService = boardService;
        this.userRepository = userRepository;
    }

    /**
     * 댓글 작성
     * URL: POST /comments?boardId=123&content=댓글내용
     *
     * 처리 흐름:
     * 1. 게시글 존재 확인
     * 2. 로그인한 사용자를 작성자로 설정
     * 3. 댓글 저장
     * 4. 원래 게시글로 리다이렉트
     */
    @PostMapping
    public String save(
            @RequestParam Long boardId,// 어느 게시글에 달 댓글인지
            @RequestParam String content,// 댓글 내용
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // 게시글 존재 확인
            Board board = boardService.findById(boardId);
            if (board == null) {
                redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
                return "redirect:/boards";
            }

            // 로그인한 사용자 정보 가져오기
            String username = authentication.getName();
            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 댓글 생성
            Comment comment = new Comment();
            comment.setBoard(board);        // 어떤 게시글에 달린 댓글인지
            comment.setAuthor(author);      // 누가 작성했는지
            comment.setContent(content);    // 댓글 내용

            // 댓글 저장
            commentService.save(comment);
            redirectAttributes.addFlashAttribute("message", "댓글이 작성되었습니다!");

            // 원래 게시글로 돌아가기
            System.out.println("✅ 댓글 작성 완료: 게시글 ID=" + boardId);
            return "redirect:/boards/" + boardId;

        } catch (Exception e) {
            System.err.println("❌ 댓글 작성 실패: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "댓글 작성 실패: " + e.getMessage());
            return "redirect:/boards/" + boardId;
        }
    }

    /**
     * 댓글 수정
     * URL: POST /comments/456/edit?content=수정된내용
     *
     * 456 = 댓글 번호 (ID)
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,          // 댓글 번호
            @RequestParam String content,   // 수정할 내용
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Comment comment = commentService.findById(id);
            if (comment == null) {
                redirectAttributes.addFlashAttribute("error", "댓글을 찾을 수 없습니다.");
                return "redirect:/boards";
            }

            // 권한 체크: 작성자 본인 또는 관리자만
            if (!canModify(comment, authentication)) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/boards/" + comment.getBoard().getId();
            }

            // 댓글 수정
            Comment updateComment = new Comment();
            updateComment.setContent(content);
            commentService.update(id, updateComment);

            redirectAttributes.addFlashAttribute("message", "댓글이 수정되었습니다!");

            System.out.println("✅ 댓글 수정 완료: ID=" + id);
            // 원래 게시글로 돌아가기
            return "redirect:/boards/" + comment.getBoard().getId();

        } catch (Exception e) {
            System.err.println("❌ 댓글 수정 실패: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "댓글 수정 실패: " + e.getMessage());
            return "redirect:/boards";
        }
    }

    /**
     * 댓글 삭제
     * URL: POST /comments/456/delete
     *
     * 456 = 삭제할 댓글 번호
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Comment comment = commentService.findById(id);
            if (comment == null) {
                redirectAttributes.addFlashAttribute("error", "댓글을 찾을 수 없습니다.");
                return "redirect:/boards";
            }

            // 나중에 리다이렉트할 게시글 번호 저장
            Long boardId = comment.getBoard().getId();

            // 권한 체크
            if (!canModify(comment, authentication)) {
                redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
                return "redirect:/boards/" + boardId;
            }
            // 댓글 삭제
            commentService.delete(id);
            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");

            System.out.println("✅ 댓글 삭제 완료: ID=" + id);

            // 원래 게시글로 돌아가기
            return "redirect:/boards/" + boardId;

        } catch (Exception e) {
            System.err.println("❌ 댓글 삭제 실패: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "댓글 삭제 실패: " + e.getMessage());
            return "redirect:/boards";
        }
    }

    /**
     * 댓글 수정/삭제 권한 체크 (private 메서드)
     *
     * 권한이 있는 경우:
     * 1. 댓글 작성자 본인
     * 2. 관리자 (ROLE_ADMIN)
     */
    private boolean canModify(Comment comment, Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String currentUsername = authentication.getName();

        // 1. 관리자는 모든 댓글 수정/삭제 가능
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            System.out.println("✅ 관리자 권한으로 댓글 접근");
            return true;
        }

        // 2. 작성자 본인 체크
        if (comment.getAuthor() != null &&
                comment.getAuthor().getUsername().equals(currentUsername)) {
            System.out.println("✅ 작성자 본인으로 댓글 접근");
            return true;
        }

        System.out.println("❌ 댓글 권한 없음: 현재=" + currentUsername +
                ", 작성자=" + (comment.getAuthor() != null ? comment.getAuthor().getUsername() : "없음"));
        return false;
    }
}

/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 게시글에 댓글 작성
 *
 *   사용자: 게시글 123번 보는 중
 *   ↓
 *   댓글 폼에 "좋은 글이네요!" 입력 후 제출
 *   ↓
 *   POST /comments?boardId=123&content=좋은 글이네요!
 *   ↓
 *   CommentController.save()
 *   - 게시글 123번 조회
 *   - 현재 로그인 사용자를 작성자로 설정
 *   - 댓글 저장
 *   ↓
 *   redirect:/boards/123 (원래 게시글로 돌아감)
 *   ↓
 *   화면: 새로 작성된 댓글이 보임
 *
 *
 * 시나리오 2) 내 댓글 수정
 *
 *   사용자: 내가 쓴 댓글 456번 수정 버튼 클릭
 *   ↓
 *   POST /comments/456/edit?content=수정된 내용
 *   ↓
 *   CommentController.update()
 *   - 댓글 456번 조회
 *   - canModify() 체크 → 작성자 본인이면 true
 *   - 댓글 내용 수정
 *   ↓
 *   redirect:/boards/123 (원래 게시글로)
 *   ↓
 *   화면: 수정된 댓글이 보임
 *
 *
 * 시나리오 3) 다른 사람 댓글 삭제 시도
 *
 *   사용자 A: 사용자 B가 쓴 댓글 삭제 시도
 *   ↓
 *   POST /comments/456/delete
 *   ↓
 *   CommentController.delete()
 *   - 댓글 456번 조회
 *   - canModify() 체크 → 작성자 아니고 관리자도 아니면 false
 *   ↓
 *   "삭제 권한이 없습니다" 메시지
 *   ↓
 *   redirect:/boards/123 (원래 게시글로)
 *
 *
 * 시나리오 4) 관리자가 댓글 삭제
 *
 *   관리자: 신고된 댓글 삭제
 *   ↓
 *   POST /comments/456/delete
 *   ↓
 *   canModify() 체크 → isAdmin = true
 *   ↓
 *   댓글 삭제 성공
 *   ↓
 *   redirect:/boards/123
 *
 *
 * ========== 데이터 흐름 ==========
 *
 * 게시글 상세 페이지 (board-detail.html)
 *   ↓
 * [댓글 폼] 내용 입력 후 제출
 *   ↓
 * CommentController.save()
 *   ↓
 * CommentService.save()
 *   ↓
 * CommentRepository (DB 저장)
 *   ↓
 * redirect:/boards/123
 *   ↓
 * BoardController.detail()
 *   ↓
 * CommentService.findByBoardId(123) (댓글 목록 조회)
 *   ↓
 * board-detail.html (댓글 목록 표시)
 *
 *
 * ========== HTML 폼 예시 ==========
 *
 * <!-- 댓글 작성 폼 -->
 * <form method="post" action="/comments">
 *     <input type="hidden" name="boardId" value="123">
 *     <textarea name="content" placeholder="댓글을 입력하세요"></textarea>
 *     <button type="submit">작성</button>
 * </form>
 *
 * <!-- 댓글 삭제 버튼 -->
 * <form method="post" action="/comments/456/delete">
 *     <button type="submit">삭제</button>
 * </form>
 */