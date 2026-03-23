package com.jjp.jsu.devlog;

import com.jjp.jsu.common.AdminAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class DevLogController {

    private final DevLogService service;
    private final AdminAuthService adminAuthService;

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
    public ResponseEntity<DevLogDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    /** POST /api/devlog — 등록 (관리자) */
    @PostMapping("/api/devlog")
    @ResponseBody
    public ResponseEntity<DevLogIdResponse> create(
            HttpSession session,
            @RequestBody DevLogUpsertRequest request) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(service.create(request));
    }

    /** PUT /api/devlog/{id} — 수정 (관리자) */
    @PutMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<DevLogStatusResponse> update(
            @PathVariable Long id,
            HttpSession session,
            @RequestBody DevLogUpsertRequest request) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(service.update(id, request));
    }

    /** DELETE /api/devlog/{id} — 삭제 (관리자) */
    @DeleteMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<DevLogStatusResponse> delete(
            @PathVariable Long id,
            HttpSession session) {
        adminAuthService.requireLogin(session);
        return ResponseEntity.ok(service.delete(id));
    }
}
