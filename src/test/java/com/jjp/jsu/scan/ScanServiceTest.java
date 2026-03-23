package com.jjp.jsu.scan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScanService 규칙 엔진 단위 테스트.
 * 각 규칙(HC/CL)의 탐지/미탐지 케이스를 검증합니다.
 */
class ScanServiceTest {

    private ScanService service;

    @BeforeEach
    void setUp() {
        service = new ScanService();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private List<ScanItem> scan(String code) {
        ScanRequest req = new ScanRequest(code, "java", null, List.of(), List.of());
        return service.scan(req);
    }

    private boolean hasRule(List<ScanItem> issues, String ruleId) {
        return issues.stream().anyMatch(i -> ruleId.equals(i.ruleId()));
    }

    // ── HC-001: 템플릿 ID 직접 비교 ───────────────────────────────────────────

    @Nested
    @DisplayName("HC-001 템플릿 ID 직접 비교")
    class HC001 {

        @Test
        @DisplayName("탐지: 문자열 템플릿 ID 리터럴")
        void detect_templateId_literal() {
            String code = "if (tmplId.equals(\"TMPL_NOTICE_001\")) { doSomething(); }";
            assertThat(hasRule(scan(code), "HC-001")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 일반 문자열 변수")
        void notDetect_normalString() {
            String code = "String name = \"hello world\";";
            assertThat(hasRule(scan(code), "HC-001")).isFalse();
        }
    }

    // ── HC-002: 상태코드 문자열 직접 비교 ─────────────────────────────────────

    @Nested
    @DisplayName("HC-002 상태코드 문자열 직접 비교")
    class HC002 {

        @Test
        @DisplayName("탐지: APPROVED 문자열 직접 비교")
        void detect_statusApproved() {
            String code = "if (status.equals(\"APPROVED\")) { process(); }";
            assertThat(hasRule(scan(code), "HC-002")).isTrue();
        }

        @Test
        @DisplayName("탐지: useYn == 'Y' 비교")
        void detect_ynFlag() {
            String code = "if (useYn == 'Y') { return true; }";
            assertThat(hasRule(scan(code), "HC-002")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 상수 열거형 비교")
        void notDetect_enumConstant() {
            String code = "if (status == Status.APPROVED) { process(); }";
            assertThat(hasRule(scan(code), "HC-002")).isFalse();
        }
    }

    // ── HC-003: 역할명 직접 비교 ──────────────────────────────────────────────

    @Nested
    @DisplayName("HC-003 역할명 직접 비교")
    class HC003 {

        @Test
        @DisplayName("탐지: ROLE_ADMIN 문자열 비교")
        void detect_roleAdmin() {
            String code = "if (userRole.equals(\"ROLE_ADMIN\")) { grantAccess(); }";
            assertThat(hasRule(scan(code), "HC-003")).isTrue();
        }

        @Test
        @DisplayName("미탐지: enum Role 사용")
        void notDetect_enumRole() {
            String code = "if (role == Role.ADMIN) { grantAccess(); }";
            assertThat(hasRule(scan(code), "HC-003")).isFalse();
        }
    }

    // ── HC-005: 매직 넘버 ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("HC-005 매직 넘버")
    class HC005 {

        @Test
        @DisplayName("탐지: if 조건 내 비표준 매직 넘버")
        void detect_magicNumber() {
            String code = "if (retryCount > 99) { throw new Exception(); }";
            assertThat(hasRule(scan(code), "HC-005")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 화이트리스트 숫자 200")
        void notDetect_whitelistedHttpStatus() {
            String code = "if (statusCode == 200) { return ok(); }";
            assertThat(hasRule(scan(code), "HC-005")).isFalse();
        }
    }

    // ── HC-006: URL / 경로 하드코딩 ──────────────────────────────────────────

    @Nested
    @DisplayName("HC-006 URL·경로 하드코딩")
    class HC006 {

        @Test
        @DisplayName("탐지: https URL 문자열")
        void detect_httpsUrl() {
            String code = "String endpoint = \"https://api.example.com/v1/data\";";
            assertThat(hasRule(scan(code), "HC-006")).isTrue();
        }

        @Test
        @DisplayName("탐지: 절대 파일 경로")
        void detect_absolutePath() {
            String code = "String path = \"/usr/local/app/config\";";
            assertThat(hasRule(scan(code), "HC-006")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 4자 이하 짧은 경로")
        void notDetect_shortPath() {
            String code = "String p = \"/api\";";
            assertThat(hasRule(scan(code), "HC-006")).isFalse();
        }
    }

    // ── HC-008: 승인 단계값 직접 사용 ────────────────────────────────────────

    @Nested
    @DisplayName("HC-008 승인 단계값 직접 사용")
    class HC008 {

        @Test
        @DisplayName("탐지: apprStep 숫자 직접 비교")
        void detect_approvalStep() {
            String code = "if (apprStep == 2) { notifyApprover(); }";
            assertThat(hasRule(scan(code), "HC-008")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 일반 카운터 변수")
        void notDetect_genericCounter() {
            String code = "if (count == 2) { retry(); }";
            assertThat(hasRule(scan(code), "HC-008")).isFalse();
        }
    }
}
