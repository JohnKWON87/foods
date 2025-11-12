package com.hyukmin.foods.controller;

import com.hyukmin.foods.entity.Board;
import com.hyukmin.foods.entity.Comment;
import com.hyukmin.foods.entity.User;
import com.hyukmin.foods.service.BoardService;
import com.hyukmin.foods.service.CommentService;
import com.hyukmin.foods.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 * BoardController - 게시판 컨트롤러
 *
 * 이 파일이 하는 일:
 * 1. 게시글 목록 조회 (페이징, 검색)
 * 2. 게시글 작성/수정/삭제
 * 3. 게시글 상세 보기 (댓글 포함)
 * 4. 권한 검증 (작성자 본인 또는 관리자만 수정/삭제 가능)
 *
 * 연결되는 파일:
 * → BoardService: 게시글 CRUD 로직
 * → CommentService: 댓글 조회
 * → UserRepository: 작성자 정보 조회
 * → board-list.html, board-form.html, board-detail.html: 화면 표시
 */
@Controller
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    public BoardController(BoardService boardService, CommentService commentService, UserRepository userRepository) {
        this.boardService = boardService;
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    /**
     * 게시글 목록 조회
     * URL: GET /boards?page=0&keyword=검색어
     *
     * 기능:
     * - 한 페이지에 10개씩 표시 (페이징)
     * - 제목+내용으로 검색
     * - 최신순 정렬
     */
    @GetMapping
    public String list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            Model model) {

        // 페이징: 한 페이지에 10개씩, 최신 순
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Board> boards;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있으면 검색
            boards = boardService.search(keyword, pageable);
            model.addAttribute("keyword", keyword);
            System.out.println("🔍 게시글 검색: " + keyword);
        } else {
            // 없으면 전체 조회
            boards = boardService.findAll(pageable);
        }

        // 화면에 전달할 데이터
        model.addAttribute("boards", boards);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", boards.getTotalPages());
        model.addAttribute("totalElements", boards.getTotalElements());

        System.out.println("📋 게시글 목록 조회: " + boards.getTotalElements() + "개 (" + page + "페이지)");
        return "board-list";
    }

    /**
     * 게시글 상세 조회
     * URL: GET /boards/123
     *
     * 기능:
     * - 게시글 내용 표시
     * - 조회수 자동 증가
     * - 댓글 목록 표시
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Board board = boardService.findById(id);

        if (board == null) {
            redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
            return "redirect:/boards";
        }

        // 조회수 +1 증가
        boardService.increaseViewCount(id);

        // 댓글 목록 조회
        java.util.List<Comment> comments = commentService.findByBoardId(id);

        model.addAttribute("board", board);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", comments.size());

        System.out.println("📖 게시글 상세 조회: ID=" + id + " (" + board.getTitle() + ")");
        return "board-detail";
    }

    /**
     * 게시글 작성 폼
     * URL: GET /boards/new
     *
     * 로그인 필요 (SecurityConfig에서 설정)
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("board", new Board());
        System.out.println("✍️ 게시글 작성 폼 오픈");
        return "board-form";
    }

    /**
     * 게시글 작성 처리
     * URL: POST /boards
     *
     * 처리 흐름:
     * 1. 현재 로그인한 사용자를 작성자로 설정
     * 2. 게시글 저장
     * 3. 상세 페이지로 이동
     */
    @PostMapping
    public String save(
            @ModelAttribute Board board,
            Authentication authentication,// Spring Security가 자동으로 주입
            RedirectAttributes redirectAttributes) {

        try {
            // 로그인한 사용자 정보 가져오기
            String username = authentication.getName();
            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 작성자 설정
            board.setAuthor(author);

            // 게시글 저장
            Board savedBoard = boardService.save(board);
            redirectAttributes.addFlashAttribute("message", "게시글이 작성되었습니다! ✍️");

            System.out.println("✅ 게시글 작성 완료: ID=" + savedBoard.getId());
            return "redirect:/boards/" + savedBoard.getId();

        } catch (Exception e) {
            System.err.println("❌ 게시글 작성 실패: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "게시글 작성 실패: " + e.getMessage());
            return "redirect:/boards/new";
        }
    }

    /**
     * 게시글 수정 폼
     * URL: GET /boards/123/edit
     *
     * 권한 체크: 작성자 본인 또는 관리자만 가능
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model,
                           RedirectAttributes redirectAttributes) {

        Board board = boardService.findById(id);
        if (board == null) {
            redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
            return "redirect:/boards";
        }

        // 권한 체크: 작성자 본인 또는 관리자만
        if (!canModify(board, authentication)) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/boards/" + id;
        }

        model.addAttribute("board", board);
        System.out.println("✏️ 게시글 수정 폼 오픈: ID=" + id);
        return "board-form";
    }

    /**
     * 게시글 수정 처리
     * URL: POST /boards/123/edit
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @ModelAttribute Board updateBoard,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Board board = boardService.findById(id);
            if (board == null) {
                redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
                return "redirect:/boards";
            }

            // 권한 체크
            if (!canModify(board, authentication)) {
                redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
                return "redirect:/boards/" + id;
            }

            // 게시글 수정
            boardService.update(id, updateBoard);
            redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다! ✏️");

            System.out.println("✅ 게시글 수정 완료: ID=" + id);
            return "redirect:/boards/" + id;

        } catch (Exception e) {
            System.err.println("❌ 게시글 수정 실패: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "게시글 수정 실패: " + e.getMessage());
            return "redirect:/boards/" + id + "/edit";
        }
    }

    /**
     * 게시글 삭제
     * URL: POST /boards/123/delete , 123번을 삭제한다는 뜻이다.
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Board board = boardService.findById(id);
            if (board == null) {
                redirectAttributes.addFlashAttribute("error", "게시글을 찾을 수 없습니다.");
                return "redirect:/boards";
            }

            // 권한 체크
            if (!canModify(board, authentication)) {
                redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
                return "redirect:/boards/" + id;
            }

            boardService.delete(id);
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");

            System.out.println("✅ 게시글 삭제 완료: ID=" + id);
            return "redirect:/boards";

        } catch (Exception e) {
            System.err.println("❌ 게시글 삭제 실패: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "게시글 삭제 실패: " + e.getMessage());
            return "redirect:/boards/" + id;
        }
    }

    /**
     * 수정/삭제 권한 체크 (private 메서드)
     *
     * 권한이 있는 경우:
     * 1. 작성자 본인
     * 2. 관리자 (ROLE_ADMIN)
     */
    private boolean canModify(Board board, Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        String currentUsername = authentication.getName();

        // 관리자는 모든 권한 있음
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            System.out.println("✅ 관리자 권한으로 접근");
            return true;
        }

        // 작성자 본인 체크
        if (board.getAuthor() != null &&
                board.getAuthor().getUsername().equals(currentUsername)) {
            System.out.println("✅ 작성자 본인으로 접근");
            return true;
        }

        System.out.println("❌ 권한 없음: 현재=" + currentUsername +
                ", 작성자=" + (board.getAuthor() != null ? board.getAuthor().getUsername() : "없음"));
        return false;
    }
}
/*
 * ========== 실제로 어떻게 동작하나요? ==========
 *
 * 시나리오 1) 게시글 목록 보기
 *   GET /boards
 *   → 최신 게시글 10개 조회 (1페이지)
 *   → board-list.html에 표시
 *
 * 시나리오 2) 게시글 검색
 *   GET /boards?keyword=맛집
 *   → "맛집"이 포함된 게시글 검색
 *   → 검색 결과를 board-list.html에 표시
 *
 * 시나리오 3) 게시글 작성
 *   1. GET /boards/new → 작성 폼 표시
 *   2. 제목/내용 입력 후 제출
 *   3. POST /boards → 저장
 *   4. 현재 로그인 사용자를 작성자로 설정
 *   5. 작성 완료 → /boards/123으로 이동
 *
 * 시나리오 4) 다른 사람 게시글 수정 시도
 *   1. 사용자 A가 작성한 게시글
 *   2. 사용자 B가 수정 시도 → GET /boards/123/edit
 *   3. canModify() 체크 → false
 *   4. "수정 권한이 없습니다" 메시지
 *   5. 상세 페이지로 리다이렉트
 *
 * 시나리오 5) 관리자가 게시글 삭제
 *   1. POST /boards/123/delete
 *   2. canModify() 체크 → isAdmin = true
 *   3. 삭제 성공
 *   4. 목록 페이지로 이동
 *
 * 데이터 흐름:
 *   사용자 요청
 *   → BoardController
 *   → BoardService (비즈니스 로직)
 *   → BoardRepository (DB 접근)
 *   → Model에 데이터 추가
 *   → HTML 템플릿 렌더링
 */