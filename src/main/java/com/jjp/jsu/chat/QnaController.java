package com.jjp.jsu.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    /** GET /api/qna/posts — 공개 글 목록 */
    @GetMapping("/posts")
    public ResponseEntity<List<Map<String, Object>>> getPublicPosts() {
        return ResponseEntity.ok(qnaService.getPublicList());
    }

    /** GET /api/qna/posts/all — 전체 글 목록 (관리자) */
    @GetMapping("/posts/all")
    public ResponseEntity<?> getAllPosts(@RequestHeader(value = "X-Admin-Password", required = false) String pw) {
        if (!qnaService.isAdmin(pw)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 없습니다."));
        }
        return ResponseEntity.ok(qnaService.getAllList());
    }

    /** POST /api/qna/posts — 글쓰기
     * body: { title, content, isPublic, password? }
     */
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> body) {
        String title    = (String) body.get("title");
        String content  = (String) body.get("content");
        boolean pub     = Boolean.parseBoolean(String.valueOf(body.getOrDefault("isPublic", "true")));
        String password = (String) body.get("password");

        if (title == null || title.isBlank())   return ResponseEntity.badRequest().body(Map.of("error", "제목을 입력하세요."));
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "내용을 입력하세요."));
        if (!pub && (password == null || password.isBlank()))
            return ResponseEntity.badRequest().body(Map.of("error", "비공개 글에는 비밀번호가 필요합니다."));

        QnaPost post = qnaService.createPost(title, content, pub, password);
        return ResponseEntity.ok(Map.of("id", post.getId(), "status", "OK"));
    }

    /** GET /api/qna/posts/{id} — 상세 조회
     * 비공개 글: X-Post-Password 헤더 또는 관리자 비번
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Post-Password",  required = false) String postPw,
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPw) {

        boolean admin = qnaService.isAdmin(adminPw);
        Map<String, Object> detail = qnaService.getDetail(id, postPw, admin);
        if (detail.containsKey("error")) {
            return ResponseEntity.status(403).body(detail);
        }
        return ResponseEntity.ok(detail);
    }

    /** POST /api/qna/posts/{id}/replies — 답변 등록 (관리자 전용)
     * body: { content }
     */
    @PostMapping("/posts/{id}/replies")
    public ResponseEntity<?> addReply(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Password", required = false) String adminPw,
            @RequestBody Map<String, String> body) {

        if (!qnaService.isAdmin(adminPw)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 없습니다."));
        }
        String content = body.get("content");
        if (content == null || content.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "답변 내용을 입력하세요."));

        QnaReply reply = qnaService.addReply(id, content);
        return ResponseEntity.ok(Map.of("id", reply.getId(), "status", "OK"));
    }
}
