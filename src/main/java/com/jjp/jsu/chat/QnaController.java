package com.jjp.jsu.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    /** GET /api/qna/posts — 공개 글 목록 */
    @GetMapping("/posts")
    public ResponseEntity<List<QnaPostSummaryResponse>> getPublicPosts() {
        return ResponseEntity.ok(qnaService.getPublicList());
    }

    /** GET /api/qna/posts/all — 전체 글 목록 (관리자) */
    @GetMapping("/posts/all")
    public ResponseEntity<List<QnaPostSummaryResponse>> getAllPosts(
            @RequestHeader(value = "X-Admin-Password", required = false) String pw) {
        qnaService.validateAdmin(pw);
        return ResponseEntity.ok(qnaService.getAllList());
    }

    /** POST /api/qna/posts — 글쓰기
     * body: { title, content, isPublic, password? }
     */
    @PostMapping("/posts")
    public ResponseEntity<QnaIdResponse> createPost(@RequestBody QnaCreatePostRequest request) {
        return ResponseEntity.ok(qnaService.createPost(request));
    }

    /** GET /api/qna/posts/{id} — 상세 조회
     * 비공개 글: X-Post-Password 헤더 또는 관리자 비번
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<QnaPostDetailResponse> getPost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password",  required = false) String postPw,
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPw) {
        return ResponseEntity.ok(qnaService.getDetail(id, postPw, qnaService.isAdmin(adminPw)));
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<QnaStatusResponse> updatePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password", required = false) String postPw,
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPw,
            @RequestBody QnaUpdatePostRequest request) {
        return ResponseEntity.ok(qnaService.updatePost(id, postPw, adminPw, request));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<QnaStatusResponse> deletePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password", required = false) String postPw,
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPw) {
        return ResponseEntity.ok(qnaService.deletePost(id, postPw, adminPw));
    }

    /** POST /api/qna/posts/{id}/replies — 답변 등록 (관리자 전용)
     * body: { content }
     */
    @PostMapping("/posts/{id}/replies")
    public ResponseEntity<QnaIdResponse> addReply(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPw,
            @RequestBody QnaCreateReplyRequest request) {
        return ResponseEntity.ok(qnaService.addReply(id, adminPw, request));
    }
}
