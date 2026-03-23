package com.jjp.jsu.portal;

import com.jjp.jsu.common.AdminAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 공지 관리 REST API.
 * 조회는 인증 불필요, 등록/수정/삭제는 관리자 세션 필요.
 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final AdminAuthService adminAuthService;

    /** GET /api/notice — 전체 공지 목록 */
    @GetMapping
    public ResponseEntity<List<Notice>> list() {
        return ResponseEntity.ok(noticeService.getAll());
    }

    /** POST /api/notice — 공지 등록 (관리자) */
    @PostMapping
    public ResponseEntity<Notice> create(
            HttpSession session,
            @RequestBody NoticeRequest request) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(noticeService.create(request));
    }

    /** PUT /api/notice/{id} — 공지 수정 (관리자) */
    @PutMapping("/{id}")
    public ResponseEntity<Notice> update(
            @PathVariable Long id,
            HttpSession session,
            @RequestBody NoticeRequest request) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(noticeService.update(id, request));
    }

    /** DELETE /api/notice/{id} — 공지 삭제 (관리자) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id,
            HttpSession session) {
        adminAuthService.requireLogin(session);
        noticeService.delete(id);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
