package com.jjp.jsu.sql;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 품질 규칙 엔진.
 *
 * SQ01 SELECT *             HIGH
 * SQ02 WHERE 없는 UPDATE    HIGH
 * SQ03 WHERE 없는 DELETE    HIGH
 * SQ04 앞 와일드카드 LIKE    MEDIUM  ('%keyword')
 * SQ05 페이지네이션 누락      MEDIUM  (LIMIT/ROWNUM/FETCH 없는 SELECT)
 * SQ06 서브쿼리 중첩          MEDIUM  (SELECT 안 SELECT)
 * SQ07 OR 조건 과다           LOW     (3개 이상 OR)
 * SQ08 커스텀 금지 패턴       HIGH
 */
@Service
public class SqlQualityService {

    // ── 정규식 사전 컴파일 (성능) ───────────────────────────────────────────
    private static final Pattern P_SELECT_STAR      = Pattern.compile("(?i)SELECT\\s+\\*");
    private static final Pattern P_LEADING_LIKE     = Pattern.compile("(?i)LIKE\\s+['\"]%");
    private static final Pattern P_NESTED_SUBQUERY  = Pattern.compile("(?i)\\(\\s*SELECT\\b");
    private static final Pattern P_OR               = Pattern.compile("(?i)\\bOR\\b");

    // ── 전처리 ─────────────────────────────────────────────────────────────

    private String[] lines(String sql) {
        return sql.split("\\r?\\n", -1);
    }

    /** 주석 제거 후 대문자 정규화된 단일 문자열 */
    private String normalized(String sql) {
        return sql.replaceAll("--[^\n]*", " ")
                  .replaceAll("/\\*[\\s\\S]*?\\*/", " ")
                  .toUpperCase()
                  .replaceAll("\\s+", " ")
                  .trim();
    }

    // ── 메인 진입점 ────────────────────────────────────────────────────────

    public SqlCheckResponse check(String sql, String customPatterns) {
        List<SqlIssue> issues = new ArrayList<>();
        String[] lineArr = lines(sql);
        String norm = normalized(sql);

        checkSelectStar(lineArr, issues);
        checkUpdateWithoutWhere(lineArr, norm, issues);
        checkDeleteWithoutWhere(lineArr, norm, issues);
        checkLeadingWildcardLike(lineArr, issues);
        checkMissingPagination(norm, issues);
        checkNestedSubquery(lineArr, issues);
        checkExcessiveOr(lineArr, issues);
        if (customPatterns != null && !customPatterns.isBlank()) {
            checkCustomPatterns(lineArr, customPatterns, issues);
        }

        long high   = issues.stream().filter(i -> "HIGH".equals(i.severity())).count();
        long medium = issues.stream().filter(i -> "MEDIUM".equals(i.severity())).count();
        long low    = issues.stream().filter(i -> "LOW".equals(i.severity())).count();

        return new SqlCheckResponse(issues, issues.size(), (int) high, (int) medium, (int) low);
    }

    // ── SQ01 SELECT * ──────────────────────────────────────────────────────

    private void checkSelectStar(String[] lines, List<SqlIssue> issues) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && P_SELECT_STAR.matcher(lines[i]).find()) {
                issues.add(new SqlIssue("SQ01", "HIGH", i + 1, trim(lines[i]),
                        "SELECT * 사용",
                        "SELECT *는 불필요한 컬럼까지 조회해 네트워크·메모리 비용을 높입니다. 필요한 컬럼만 명시하세요."));
            }
        }
    }

    // ── SQ02 WHERE 없는 UPDATE ─────────────────────────────────────────────

    private void checkUpdateWithoutWhere(String[] lines, String norm, List<SqlIssue> issues) {
        if (!norm.contains("UPDATE ")) return;

        String[] stmts = norm.split(";");
        int lineOffset = 0;
        for (String stmt : stmts) {
            stmt = stmt.trim();
            if (stmt.startsWith("UPDATE ") && !stmt.contains(" WHERE ")) {
                // fromOffset을 넘겨 현재 구문 이후 라인에서만 탐색
                int lineNo = findLineContaining(lines, "UPDATE", lineOffset);
                String lineText = (lineNo > 0 && lineNo <= lines.length)
                        ? trim(lines[lineNo - 1]) : "";
                issues.add(new SqlIssue("SQ02", "HIGH", lineNo, lineText,
                        "WHERE 절 없는 UPDATE",
                        "WHERE 조건 없이 UPDATE를 실행하면 테이블 전체 행이 변경됩니다. 의도적 전체 갱신이 아니라면 WHERE 조건을 추가하세요."));
            }
            lineOffset += stmt.split("\\n").length;
        }
    }

    // ── SQ03 WHERE 없는 DELETE ─────────────────────────────────────────────

    private void checkDeleteWithoutWhere(String[] lines, String norm, List<SqlIssue> issues) {
        if (!norm.contains("DELETE ")) return;

        String[] stmts = norm.split(";");
        int lineOffset = 0;
        for (String stmt : stmts) {
            stmt = stmt.trim();
            if ((stmt.startsWith("DELETE FROM ") || stmt.startsWith("DELETE "))
                    && !stmt.contains(" WHERE ")) {
                int lineNo = findLineContaining(lines, "DELETE", lineOffset);
                String lineText = (lineNo > 0 && lineNo <= lines.length)
                        ? trim(lines[lineNo - 1]) : "";
                issues.add(new SqlIssue("SQ03", "HIGH", lineNo, lineText,
                        "WHERE 절 없는 DELETE",
                        "WHERE 조건 없이 DELETE를 실행하면 테이블 전체 행이 삭제됩니다. 반드시 WHERE 조건을 추가하세요."));
            }
            lineOffset += stmt.split("\\n").length;
        }
    }

    // ── SQ04 앞 와일드카드 LIKE ────────────────────────────────────────────

    private void checkLeadingWildcardLike(String[] lines, List<SqlIssue> issues) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && P_LEADING_LIKE.matcher(lines[i]).find()) {
                issues.add(new SqlIssue("SQ04", "MEDIUM", i + 1, trim(lines[i]),
                        "앞 와일드카드 LIKE 사용",
                        "LIKE '%값' 형태는 인덱스를 사용할 수 없어 풀스캔이 발생합니다. LIKE '값%' 형태나 전문 검색(Full-Text Search)을 고려하세요."));
            }
        }
    }

    // ── SQ05 페이지네이션 누락 ─────────────────────────────────────────────

    private void checkMissingPagination(String norm, List<SqlIssue> issues) {
        if (!norm.contains("SELECT ")) return;
        boolean hasPaging = norm.contains(" LIMIT ")
                || norm.contains("ROWNUM")
                || norm.contains("FETCH NEXT")
                || norm.contains("FETCH FIRST")
                || norm.contains("TOP ");
        if (!hasPaging) {
            issues.add(new SqlIssue("SQ05", "MEDIUM", 0, "",
                    "페이지네이션 누락",
                    "LIMIT / ROWNUM / FETCH NEXT 없이 대량 데이터를 조회하면 메모리 초과·응답 지연이 발생할 수 있습니다. 페이지네이션을 추가하세요."));
        }
    }

    // ── SQ06 서브쿼리 중첩 ────────────────────────────────────────────────

    private void checkNestedSubquery(String[] lines, List<SqlIssue> issues) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && P_NESTED_SUBQUERY.matcher(lines[i]).find()) {
                issues.add(new SqlIssue("SQ06", "MEDIUM", i + 1, trim(lines[i]),
                        "서브쿼리 중첩",
                        "인라인 서브쿼리는 행마다 반복 실행되어 N+1 문제를 유발할 수 있습니다. JOIN 또는 WITH(CTE)로 리팩터링을 고려하세요."));
            }
        }
    }

    // ── SQ07 OR 조건 과다 ─────────────────────────────────────────────────

    private void checkExcessiveOr(String[] lines, List<SqlIssue> issues) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] == null) continue;
            Matcher m = P_OR.matcher(lines[i]);
            long cnt = 0;
            while (m.find()) cnt++;
            if (cnt >= 3) {
                issues.add(new SqlIssue("SQ07", "LOW", i + 1, trim(lines[i]),
                        "OR 조건 과다 사용 (" + cnt + "개)",
                        "OR 조건이 많으면 인덱스 활용이 어렵고 실행 계획이 복잡해집니다. IN 절이나 UNION으로 분리하는 것을 고려하세요."));
            }
        }
    }

    // ── SQ08 커스텀 금지 패턴 ────────────────────────────────────────────

    private void checkCustomPatterns(String[] lines, String customPatterns, List<SqlIssue> issues) {
        for (String raw : customPatterns.split(",")) {
            String pat = raw.trim();
            if (pat.isBlank()) continue;
            try {
                Pattern p = Pattern.compile(Pattern.quote(pat), Pattern.CASE_INSENSITIVE);
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i] != null && p.matcher(lines[i]).find()) {
                        issues.add(new SqlIssue("SQ08", "HIGH", i + 1, trim(lines[i]),
                                "커스텀 금지 패턴 — " + pat,
                                "팀에서 등록한 금지 패턴 [" + pat + "] 이(가) 발견되었습니다."));
                    }
                }
            } catch (Exception ignored) {
                // 패턴 컴파일 실패 시 해당 패턴 건너뜀
            }
        }
    }

    // ── 유틸 ──────────────────────────────────────────────────────────────

    private String trim(String line) {
        if (line == null) return "";
        String t = line.strip();
        return t.length() > 120 ? t.substring(0, 120) + "…" : t;
    }

    /**
     * fromOffset 라인(0-based)부터 keyword를 포함하는 첫 번째 라인 번호(1-based)를 반환합니다.
     * 찾지 못하면 0을 반환합니다.
     */
    private int findLineContaining(String[] lines, String keyword, int fromOffset) {
        String kw = keyword.toUpperCase();
        int start = Math.max(0, fromOffset);
        for (int i = start; i < lines.length; i++) {
            if (lines[i] != null && lines[i].toUpperCase().contains(kw)) return i + 1;
        }
        return 0;
    }
}
