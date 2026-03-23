package com.jjp.jsu.common;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자 세션 인증 컨트롤러.
 * <p>
 * 로그인 진입점은 devlog, qna, notice 등 각 페이지에서 별도로 제공하지만,
 * 실제 인증 처리는 이 컨트롤러가 단일 지점에서 담당합니다.
 * 서버 세션에 상태를 저장하므로 한 번 로그인하면 모든 페이지에서 유지됩니다.
 *
 * POST /api/admin/login   — 로그인 (비밀번호 검증 + 세션 저장)
 * POST /api/admin/logout  — 로그아웃 (세션 전체 무효화)
 * GET  /api/admin/status  — 현재 로그인 여부 확인 (페이지 초기화 시 사용)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        String password = body.get("password");
        adminAuthService.login(password, session);          // 실패 시 ForbiddenException → 403
        return ResponseEntity.ok(Map.of("loggedIn", true));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        adminAuthService.logout(session);
        return ResponseEntity.ok(Map.of("loggedIn", false));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(HttpSession session) {
        return ResponseEntity.ok(Map.of("loggedIn", adminAuthService.isLoggedIn(session)));
    }
}
