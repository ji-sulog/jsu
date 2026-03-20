package com.jjp.jsu.devlog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<DevLogDetailResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    /** GET /api/devlog/admin-check — 관리자 비밀번호 확인 */
    @GetMapping("/api/devlog/admin-check")
    @ResponseBody
    public ResponseEntity<DevLogStatusResponse> adminCheck(
            @RequestHeader(value = "X-Admin-Password", required = false) String pw) {
        service.validateAdmin(pw);
        return ResponseEntity.ok(new DevLogStatusResponse("OK"));
    }

    /** POST /api/devlog — 등록 (관리자) */
    @PostMapping("/api/devlog")
    @ResponseBody
    public ResponseEntity<DevLogIdResponse> create(
            @RequestHeader(value = "X-Admin-Password", required = false) String pw,
            @RequestBody DevLogUpsertRequest request) {
        return ResponseEntity.ok(service.create(pw, request));
    }

    /** PUT /api/devlog/{id} — 수정 (관리자) */
    @PutMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<DevLogStatusResponse> update(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Password", required = false) String pw,
            @RequestBody DevLogUpsertRequest request) {
        return ResponseEntity.ok(service.update(id, pw, request));
    }

    /** DELETE /api/devlog/{id} — 삭제 (관리자) */
    @DeleteMapping("/api/devlog/{id}")
    @ResponseBody
    public ResponseEntity<DevLogStatusResponse> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Password", required = false) String pw) {
        return ResponseEntity.ok(service.delete(id, pw));
    }
}
