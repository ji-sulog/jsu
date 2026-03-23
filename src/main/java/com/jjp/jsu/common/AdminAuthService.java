package com.jjp.jsu.common;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 관리자 인증 공통 서비스.
 * <p>
 * 비밀번호는 application.yaml의 app.admin-password 값으로 설정하며,
 * 환경변수 APP_ADMIN_PASSWORD로 오버라이드 가능합니다.
 * <pre>
 *   APP_ADMIN_PASSWORD=my-secure-pw ./gradlew bootRun
 * </pre>
 * 로그인 성공 시 HttpSession에 상태를 저장합니다.
 * 어느 페이지에서 로그인하든 동일한 서버 세션을 공유하므로,
 * 한 번 로그인하면 모든 관리자 기능이 활성화됩니다.
 */
@Service
public class AdminAuthService {

    private static final String SESSION_KEY = "ADMIN_LOGGED_IN";

    @Value("${app.admin-password}")
    private String adminPassword;

    // ── 비밀번호 검증 ────────────────────────────────────────────

    public boolean isAdmin(String password) {
        return adminPassword != null && adminPassword.equals(password);
    }

    public void validateAdmin(String password) {
        if (!isAdmin(password)) {
            throw new ForbiddenException("관리자 권한이 없습니다.");
        }
    }

    // ── 세션 기반 인증 ────────────────────────────────────────────

    /**
     * 비밀번호를 검증하고 세션에 관리자 상태를 저장합니다.
     *
     * @throws ForbiddenException 비밀번호 불일치 시
     */
    public void login(String password, HttpSession session) {
        validateAdmin(password);
        session.setAttribute(SESSION_KEY, Boolean.TRUE);
    }

    /**
     * 세션을 무효화합니다. 어느 페이지에서 호출해도 전체 로그아웃됩니다.
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }

    /** 현재 세션에서 관리자 로그인 여부를 확인합니다. */
    public boolean isLoggedIn(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(SESSION_KEY));
    }

    /**
     * 관리자 로그인 상태를 요구합니다.
     *
     * @throws ForbiddenException 로그인 상태가 아닐 시
     */
    public void requireLogin(HttpSession session) {
        if (!isLoggedIn(session)) {
            throw new ForbiddenException("관리자 로그인이 필요합니다.");
        }
    }
}
