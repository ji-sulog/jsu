package com.jjp.jsu.devlog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DevLogController {

    private final DevLogService service;

    /** 페이지 렌더링 */
    @GetMapping("/devlog")
    public String page() {
        return "devlog";
    }

    /* ── REST API ───────────────────────────────────────────────── */

    /** GET /api/devlog — 목록 */
    @GetMapping("/api/devlog")
    @ResponseBody
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.getList());
    }

    /** GET /api/devlog/{id} — 상세 */
    @GetMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<?> detail(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.getDetail(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** POST /api/devlog — 등록 (관리자) */
    @PostMapping("/api/devlog")
    @ResponseBody
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Admin-Password", required = false) String pw,
            @RequestBody Map<String, String> body) {
        if (!service.isAdmin(pw))
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 없습니다."));

        String title   = body.get("title");
        String content = body.get("content");
        String tags    = body.getOrDefault("tags", "");

        if (title == null || title.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "제목을 입력하세요."));
        if (content == null || content.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "내용을 입력하세요."));

        DevLog log = service.create(title, content, tags);
        return ResponseEntity.ok(Map.of("id", log.getId()));
    }

    /** PUT /api/devlog/{id} — 수정 (관리자) */
    @PutMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Password", required = false) String pw,
            @RequestBody Map<String, String> body) {
        if (!service.isAdmin(pw))
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 없습니다."));

        String title   = body.get("title");
        String content = body.get("content");
        String tags    = body.getOrDefault("tags", "");

        if (title == null || title.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "제목을 입력하세요."));
        if (content == null || content.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "내용을 입력하세요."));

        try {
            service.update(id, title, content, tags);
            return ResponseEntity.ok(Map.of("status", "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/devlog/{id} — 삭제 (관리자) */
    @DeleteMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Password", required = false) String pw) {
        if (!service.isAdmin(pw))
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 없습니다."));
        service.delete(id);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
