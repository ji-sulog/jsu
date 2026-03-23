package com.jjp.jsu.naming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NamingService 규칙 엔진 단위 테스트.
 * 각 규칙(NM-001~NM-008)의 탐지/미탐지 케이스를 검증합니다.
 */
class NamingServiceTest {

    private NamingService service;

    @BeforeEach
    void setUp() {
        service = new NamingService();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private List<NamingIssue> check(String code) {
        NamingCheckRequest req = new NamingCheckRequest(code, null, "java", false, true, true);
        return service.check(req).items();
    }

    private boolean hasRule(List<NamingIssue> issues, String ruleId) {
        return issues.stream().anyMatch(i -> ruleId.equals(i.ruleId()));
    }

    // ── NM-001: 타입 이름 PascalCase ──────────────────────────────────────────

    @Nested
    @DisplayName("NM-001 타입 이름 PascalCase")
    class NM001 {

        @Test
        @DisplayName("탐지: 소문자로 시작하는 클래스명")
        void detect_lowercaseClass() {
            String code = "public class myService { }";
            assertThat(hasRule(check(code), "NM-001")).isTrue();
        }

        @Test
        @DisplayName("탐지: 언더스코어 포함 클래스명")
        void detect_underscoreClass() {
            String code = "public class My_Service { }";
            assertThat(hasRule(check(code), "NM-001")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 올바른 PascalCase 클래스명")
        void notDetect_validPascalCase() {
            String code = "public class MyService { }";
            assertThat(hasRule(check(code), "NM-001")).isFalse();
        }

        @Test
        @DisplayName("미탐지: 올바른 interface 이름")
        void notDetect_validInterface() {
            String code = "public interface UserRepository { }";
            assertThat(hasRule(check(code), "NM-001")).isFalse();
        }
    }

    // ── NM-002: 메서드명 camelCase ────────────────────────────────────────────

    @Nested
    @DisplayName("NM-002 메서드명 camelCase")
    class NM002 {

        @Test
        @DisplayName("탐지: 대문자로 시작하는 메서드명")
        void detect_uppercaseMethod() {
            String code = "public void GetUser() {}";
            assertThat(hasRule(check(code), "NM-002")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 올바른 camelCase 메서드명")
        void notDetect_validCamelCase() {
            String code = "public void getUserById() {}";
            assertThat(hasRule(check(code), "NM-002")).isFalse();
        }
    }

    // ── NM-003: 필드/변수명 camelCase ────────────────────────────────────────

    @Nested
    @DisplayName("NM-003 필드/변수명 camelCase")
    class NM003 {

        @Test
        @DisplayName("탐지: 언더스코어 포함 필드명")
        void detect_underscoreField() {
            String code = "private String user_name;";
            assertThat(hasRule(check(code), "NM-003")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 올바른 camelCase 필드명")
        void notDetect_validField() {
            String code = "private String userName;";
            assertThat(hasRule(check(code), "NM-003")).isFalse();
        }
    }

    // ── NM-004: 상수 UPPER_SNAKE_CASE ────────────────────────────────────────

    @Nested
    @DisplayName("NM-004 상수 UPPER_SNAKE_CASE")
    class NM004 {

        @Test
        @DisplayName("탐지: camelCase 상수명")
        void detect_camelCaseConstant() {
            String code = "private static final int maxRetryCount = 3;";
            assertThat(hasRule(check(code), "NM-004")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 올바른 UPPER_SNAKE_CASE 상수")
        void notDetect_validConstant() {
            String code = "private static final int MAX_RETRY_COUNT = 3;";
            assertThat(hasRule(check(code), "NM-004")).isFalse();
        }
    }

    // ── NM-005: boolean 이름 접두사 ──────────────────────────────────────────

    @Nested
    @DisplayName("NM-005 boolean 이름 is/has/can 접두사")
    class NM005 {

        @Test
        @DisplayName("탐지: 접두사 없는 boolean 변수")
        void detect_booleanWithoutPrefix() {
            String code = "boolean active;";
            assertThat(hasRule(check(code), "NM-005")).isTrue();
        }

        @Test
        @DisplayName("미탐지: is 접두사 boolean 변수")
        void notDetect_isPrefix() {
            String code = "boolean isActive;";
            assertThat(hasRule(check(code), "NM-005")).isFalse();
        }

        @Test
        @DisplayName("미탐지: has 접두사 boolean 변수")
        void notDetect_hasPrefix() {
            String code = "boolean hasPermission;";
            assertThat(hasRule(check(code), "NM-005")).isFalse();
        }
    }

    // ── NM-006: 의미가 약한 이름 ─────────────────────────────────────────────

    @Nested
    @DisplayName("NM-006 의미가 약한 이름")
    class NM006 {

        @Test
        @DisplayName("탐지: 블랙리스트 이름 'data'")
        void detect_weakNameData() {
            String code = "private String data;";
            assertThat(hasRule(check(code), "NM-006")).isTrue();
        }

        @Test
        @DisplayName("탐지: 블랙리스트 이름 'tmp'")
        void detect_weakNameTmp() {
            String code = "private String tmp;";
            assertThat(hasRule(check(code), "NM-006")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 의미 있는 이름")
        void notDetect_meaningfulName() {
            String code = "private String userName;";
            assertThat(hasRule(check(code), "NM-006")).isFalse();
        }
    }
}
