package com.jjp.jsu.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SqlQualityService 규칙 엔진 단위 테스트.
 * 각 규칙(SQ01~SQ07)의 탐지/미탐지 케이스를 검증합니다.
 */
class SqlQualityServiceTest {

    private SqlQualityService service;

    @BeforeEach
    void setUp() {
        service = new SqlQualityService();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private List<SqlIssue> check(String sql) {
        return service.check(sql, null).issues();
    }

    private boolean hasRule(List<SqlIssue> issues, String ruleId) {
        return issues.stream().anyMatch(i -> ruleId.equals(i.ruleId()));
    }

    // ── SQ01: SELECT * ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("SQ01 SELECT *")
    class SQ01 {

        @Test
        @DisplayName("탐지: SELECT * 사용")
        void detect_selectStar() {
            String sql = "SELECT * FROM users WHERE id = 1";
            assertThat(hasRule(check(sql), "SQ01")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 컬럼 명시")
        void notDetect_specificColumns() {
            String sql = "SELECT id, name, email FROM users WHERE id = 1";
            assertThat(hasRule(check(sql), "SQ01")).isFalse();
        }
    }

    // ── SQ02: WHERE 없는 UPDATE ───────────────────────────────────────────────

    @Nested
    @DisplayName("SQ02 WHERE 없는 UPDATE")
    class SQ02 {

        @Test
        @DisplayName("탐지: WHERE 절 없는 UPDATE")
        void detect_updateWithoutWhere() {
            String sql = "UPDATE users SET status = 'INACTIVE'";
            assertThat(hasRule(check(sql), "SQ02")).isTrue();
        }

        @Test
        @DisplayName("미탐지: WHERE 절 있는 UPDATE")
        void notDetect_updateWithWhere() {
            String sql = "UPDATE users SET status = 'INACTIVE' WHERE id = 5";
            assertThat(hasRule(check(sql), "SQ02")).isFalse();
        }
    }

    // ── SQ03: WHERE 없는 DELETE ───────────────────────────────────────────────

    @Nested
    @DisplayName("SQ03 WHERE 없는 DELETE")
    class SQ03 {

        @Test
        @DisplayName("탐지: WHERE 절 없는 DELETE")
        void detect_deleteWithoutWhere() {
            String sql = "DELETE FROM temp_logs";
            assertThat(hasRule(check(sql), "SQ03")).isTrue();
        }

        @Test
        @DisplayName("미탐지: WHERE 절 있는 DELETE")
        void notDetect_deleteWithWhere() {
            String sql = "DELETE FROM temp_logs WHERE created_at < '2024-01-01'";
            assertThat(hasRule(check(sql), "SQ03")).isFalse();
        }
    }

    // ── SQ04: 앞 와일드카드 LIKE ──────────────────────────────────────────────

    @Nested
    @DisplayName("SQ04 앞 와일드카드 LIKE")
    class SQ04 {

        @Test
        @DisplayName("탐지: '%keyword' 형태의 앞 와일드카드")
        void detect_leadingWildcard() {
            String sql = "SELECT id FROM users WHERE name LIKE '%홍길동'";
            assertThat(hasRule(check(sql), "SQ04")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 뒤 와일드카드 'keyword%'")
        void notDetect_trailingWildcard() {
            String sql = "SELECT id FROM users WHERE name LIKE '홍길동%'";
            assertThat(hasRule(check(sql), "SQ04")).isFalse();
        }
    }

    // ── SQ05: 페이지네이션 누락 ───────────────────────────────────────────────

    @Nested
    @DisplayName("SQ05 페이지네이션 누락")
    class SQ05 {

        @Test
        @DisplayName("탐지: LIMIT 없는 SELECT")
        void detect_selectWithoutLimit() {
            String sql = "SELECT id, name FROM users WHERE status = 'ACTIVE'";
            assertThat(hasRule(check(sql), "SQ05")).isTrue();
        }

        @Test
        @DisplayName("미탐지: LIMIT 있는 SELECT")
        void notDetect_selectWithLimit() {
            String sql = "SELECT id, name FROM users WHERE status = 'ACTIVE' LIMIT 20";
            assertThat(hasRule(check(sql), "SQ05")).isFalse();
        }
    }

    // ── SQ06: 서브쿼리 중첩 ──────────────────────────────────────────────────

    @Nested
    @DisplayName("SQ06 서브쿼리 중첩")
    class SQ06 {

        @Test
        @DisplayName("탐지: SELECT 안에 SELECT 중첩")
        void detect_nestedSubquery() {
            String sql = "SELECT * FROM orders WHERE user_id IN (SELECT id FROM users WHERE status = 'ACTIVE')";
            assertThat(hasRule(check(sql), "SQ06")).isTrue();
        }

        @Test
        @DisplayName("미탐지: 단순 SELECT")
        void notDetect_simpleSelect() {
            String sql = "SELECT id, amount FROM orders WHERE user_id = 1 LIMIT 10";
            assertThat(hasRule(check(sql), "SQ06")).isFalse();
        }
    }

    // ── SQ07: OR 조건 과다 ────────────────────────────────────────────────────

    @Nested
    @DisplayName("SQ07 OR 조건 과다")
    class SQ07 {

        @Test
        @DisplayName("탐지: OR 3개 이상")
        void detect_excessiveOr() {
            String sql = "SELECT id FROM codes WHERE code = 'A' OR code = 'B' OR code = 'C' OR code = 'D'";
            assertThat(hasRule(check(sql), "SQ07")).isTrue();
        }

        @Test
        @DisplayName("미탐지: OR 2개 이하")
        void notDetect_fewOr() {
            String sql = "SELECT id FROM codes WHERE code = 'A' OR code = 'B' LIMIT 10";
            assertThat(hasRule(check(sql), "SQ07")).isFalse();
        }
    }
}
