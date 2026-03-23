package com.jjp.jsu.qna;

import com.jjp.jsu.common.AdminAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final AdminAuthService adminAuthService;

    /** GET /api/qna/posts — 공개 글 목록 */
    @GetMapping("/posts")
    public ResponseEntity<List<QnaPostSummaryResponse>> getPublicPosts() {
        return ResponseEntity.ok(qnaService.getPublicList());
    }

    /** GET /api/qna/posts/all — 전체 글 목록 (관리자) */
    @GetMapping("/posts/all")
    public ResponseEntity<List<QnaPostSummaryResponse>> getAllPosts(HttpSession session) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(qnaService.getAllList());
    }

    /** POST /api/qna/posts — 글쓰기 */
    @PostMapping("/posts")
    public ResponseEntity<QnaIdResponse> createPost(@RequestBody QnaCreatePostRequest request) {
        return ResponseEntity.ok(qnaService.createPost(request));
    }

    /** GET /api/qna/posts/{id} — 상세 조회 (비공개 글: X-Post-Password 헤더) */
    @GetMapping("/posts/{id}")
    public ResponseEntity<QnaPostDetailResponse> getPost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password", required = false) String postPw,
            HttpSession session) {
        boolean isAdmin = adminAuthService.isLoggedIn(session);
        return ResponseEntity.ok(qnaService.getDetail(id, postPw, isAdmin));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<QnaStatusResponse> updatePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password", required = false) String postPw,
            HttpSession session,
            @RequestBody QnaUpdatePostRequest request) {
        boolean isAdmin = adminAuthService.isLoggedIn(session);
        return ResponseEntity.ok(qnaService.updatePost(id, postPw, isAdmin, request));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<QnaStatusResponse> deletePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password", required = false) String postPw,
            HttpSession session) {
        boolean isAdmin = adminAuthService.isLoggedIn(session);
        return ResponseEntity.ok(qnaService.deletePost(id, postPw, isAdmin));
    }

    /** POST /api/qna/posts/{id}/replies — 답변 등록 (관리자 전용) */
    @PostMapping("/posts/{id}/replies")
    public ResponseEntity<QnaIdResponse> addReply(
            @PathVariable Long id,
            HttpSession session,
            @RequestBody QnaCreateReplyRequest request) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(qnaService.addReply(id, request));
    }
}
