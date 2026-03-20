package com.jjp.jsu.compare.service;

import com.jjp.jsu.compare.ScoringResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 규칙 기반 중요도 평가 서비스 (R01 ~ R10).
 *
 * <pre>
 * 점수 기준
 *   7점 이상  → HIGH   (높음)
 *   4 ~ 6점  → MEDIUM (확인 필요)
 *   1 ~ 3점  → LOW    (낮음)
 *   0점      → GENERAL (일반 문구 수정)
 *
 * [R07] 삭제 가중치 — 키워드 유형별 차등 적용
 *   주체/권한·예외/조건 관련 삭제 : +3
 *   상태/절차·필수여부·허용여부 관련 삭제 : +2
 *
 * [R08] 부정어 반전 — 의미 반전 감지 + 복합 반전 보너스
 *   반전 쌍 기본 : +5
 *   반전 × (권한 or 예외/조건) : 추가 +2
 *   반전 × 필수여부 : 추가 +1
 * </pre>
 *
 * 시스템은 탐지하고, 사람이 판단한다.
 */
@Service
public class PriorityService {

    // ── 키워드 정의 ──────────────────────────────────────────────

    /** R01: 주체/권한 키워드 */
    private static final List<String> SUBJECT_KEYWORDS = List.of(
            "사용자", "관리자", "담당자", "승인자", "요청자", "운영자", "권한", "역할", "대상"
    );

    /** R02: 허용/금지 키워드 */
    private static final List<String> PERMISSION_KEYWORDS = List.of(
            "가능", "불가", "허용", "금지", "포함", "제외", "사용", "미사용", "노출", "비노출"
    );

    /** R03: 필수/선택 키워드 */
    private static final List<String> MANDATORY_KEYWORDS = List.of(
            "필수", "선택", "반드시", "필요", "생략 가능", "불필요"
    );

    /** R04: 수치/기간 감지용 패턴 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    /** R05: 상태/절차 키워드 */
    private static final List<String> STATUS_KEYWORDS = List.of(
            "요청", "승인", "반려", "검토", "완료", "대기", "종료", "상태", "단계", "순서"
    );

    /** R06: 예외/조건 키워드 */
    private static final List<String> EXCEPTION_KEYWORDS = List.of(
            "예외", "조건", "경우", "단,", "if", "when", "제한", "검증"
    );

    /**
     * R08: 부정어 반전 쌍.
     * <p>
     * 확장: 실무 문서에서 자주 나타나는 정책 방향 반전 표현 추가.
     * pair[0] ↔ pair[1] 어느 방향으로든 감지.
     */
    private static final List<String[]> REVERSAL_PAIRS = List.of(
            new String[]{"가능",      "불가"},
            new String[]{"가능하다",  "불가하다"},
            new String[]{"허용",      "금지"},
            new String[]{"포함",      "제외"},
            new String[]{"포함한다",  "제외한다"},
            new String[]{"노출",      "비노출"},
            new String[]{"사용",      "미사용"},
            new String[]{"필수",      "선택"},
            new String[]{"반드시",    "생략 가능"},
            new String[]{"필요",      "불필요"},
            new String[]{"적용",      "미적용"},
            new String[]{"있다",      "없다"},
            new String[]{"있음",      "없음"},
            new String[]{"처리한다",  "처리하지 않는다"},
            new String[]{"처리 가능", "처리 불가"}
    );

    /** R09: 중요 섹션 식별 키워드 */
    private static final List<String> SECTION_KEYWORDS = List.of(
            "권한", "승인 절차", "반려 처리", "예외 처리", "상태 정의", "입력 검증", "제한 사항"
    );

    /** R10: 단순 표현 동의어 쌍 */
    private static final List<String[]> SYNONYM_PAIRS = List.of(
            new String[]{"진행합니다", "수행합니다"},
            new String[]{"진행한다",   "수행한다"},
            new String[]{"화면",       "페이지"},
            new String[]{"클릭합니다", "선택합니다"},
            new String[]{"클릭한다",   "선택한다"},
            new String[]{"이용",       "사용"},
            new String[]{"표시",       "노출"}
    );

    // ── 공개 API ────────────────────────────────────────────────

    /**
     * 변경 항목 하나에 대해 R01~R10 규칙을 적용하여 중요도를 평가합니다.
     *
     * @param changeType  ADDED / REMOVED / MODIFIED
     * @param oldContent  이전 내용
     * @param newContent  변경 내용
     */
    public ScoringResult evaluate(String changeType, String oldContent, String newContent) {
        String oldL = normalize(oldContent);
        String newL = normalize(newContent);

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // R01: 주체/권한 변경 (+5)
        if (matchesKeywords(changeType, oldL, newL, SUBJECT_KEYWORDS)) {
            score += 5;
            reasons.add("주체 또는 권한 관련 변경");
        }

        // R02: 허용 여부 변경 (+5)
        if (matchesKeywords(changeType, oldL, newL, PERMISSION_KEYWORDS)) {
            score += 5;
            reasons.add("허용 여부 변경");
        }

        // R03: 필수 여부 변경 (+4)
        if (matchesKeywords(changeType, oldL, newL, MANDATORY_KEYWORDS)) {
            score += 4;
            reasons.add("필수 여부 변경");
        }

        // R04: 숫자/기간 변경 (+4)
        if (checkNumberChange(changeType, oldContent, newContent)) {
            score += 4;
            reasons.add("숫자 또는 제한값 변경");
        }

        // R05: 상태/절차 변경 (+5)
        if (matchesKeywords(changeType, oldL, newL, STATUS_KEYWORDS)) {
            score += 5;
            reasons.add("상태 또는 절차 변경");
        }

        // R06: 예외/조건 변경 (+4)
        if (matchesKeywords(changeType, oldL, newL, EXCEPTION_KEYWORDS)) {
            score += 4;
            reasons.add("예외 또는 조건 변경");
        }

        // R07: 삭제 가중치 — REMOVED 전용, 키워드 유형별 차등
        // R01(주체)·R06(예외/조건)이 이미 해당 키워드를 감지했다면 HIGH 카테고리는 중복 가산 방지
        if ("REMOVED".equals(changeType)) {
            boolean r01Fired = matchesKeywords("REMOVED", oldL, "", SUBJECT_KEYWORDS);
            boolean r06Fired = matchesKeywords("REMOVED", oldL, "", EXCEPTION_KEYWORDS);
            int deletionBonus = calcDeletionWeight(oldL, r01Fired || r06Fired);
            if (deletionBonus > 0) {
                score += deletionBonus;
                reasons.add("중요 문장 삭제 (삭제 가중치 +" + deletionBonus + "점)");
            }
        }

        // R08: 부정어 반전 — MODIFIED 전용, 복합 반전 시 추가 점수
        if ("MODIFIED".equals(changeType)) {
            String[] reversalPair = findReversalPair(oldL, newL);
            if (reversalPair != null) {
                score += 5;
                reasons.add("의미 반전: " + reversalPair[0] + " ↔ " + reversalPair[1]);

                // 복합 반전 보너스: 반전 × (권한 or 예외/조건) → +2
                boolean highRiskContext =
                        SUBJECT_KEYWORDS.stream().anyMatch(kw -> oldL.contains(kw) || newL.contains(kw))
                        || EXCEPTION_KEYWORDS.stream().anyMatch(kw -> oldL.contains(kw) || newL.contains(kw));
                if (highRiskContext) {
                    score += 2;
                    reasons.add("권한·조건 반전 복합 감지");
                // 반전 × 필수여부 → +1 (권한/조건과 중복 적용 안 함)
                } else if (MANDATORY_KEYWORDS.stream().anyMatch(kw -> oldL.contains(kw) || newL.contains(kw))) {
                    score += 1;
                    reasons.add("필수 여부 반전 복합 감지");
                }
            }
        }

        // R09: 중요 섹션 가중치 (+2)
        if (checkSection(oldL + " " + newL)) {
            score += 2;
            reasons.add("중요 섹션 내 변경");
        }

        // R10: 단순 표현 수정 패널티 (-2) — 점수가 낮을 때만 적용
        if (score <= 2 && checkSynonymOnly(changeType, oldContent, newContent)) {
            score = Math.max(0, score - 2);
            reasons.add("단순 표현 수정 가능성");
        }

        if (reasons.isEmpty()) {
            reasons.add("일반 문구 수정");
        }

        return new ScoringResult(score, toPriority(score), List.copyOf(reasons));
    }

    // ── 규칙별 판정 메서드 ───────────────────────────────────────

    /**
     * R01~R06 공통: 키워드가 변경에 관련되어 있는지 확인합니다.
     * - MODIFIED: old 또는 new 어느 한쪽에 키워드 존재
     * - REMOVED : old 에 키워드 존재
     * - ADDED   : new 에 키워드 존재
     */
    private boolean matchesKeywords(String type, String oldL, String newL, List<String> keywords) {
        return switch (type) {
            case "MODIFIED" -> keywords.stream().anyMatch(kw -> oldL.contains(kw) || newL.contains(kw));
            case "REMOVED"  -> keywords.stream().anyMatch(oldL::contains);
            case "ADDED"    -> keywords.stream().anyMatch(newL::contains);
            default         -> false;
        };
    }

    /** R04: 숫자가 old와 new 사이에 달라졌는지 확인 */
    private boolean checkNumberChange(String type, String old, String newC) {
        if ("MODIFIED".equals(type)) {
            Set<String> oldNums = extractNumbers(old);
            Set<String> newNums = extractNumbers(newC);
            return !oldNums.isEmpty() && !oldNums.equals(newNums);
        }
        // ADDED/REMOVED: 숫자가 포함되어 있으면 플래그
        String target = "REMOVED".equals(type) ? old : newC;
        return !extractNumbers(target).isEmpty();
    }

    private Set<String> extractNumbers(String text) {
        Set<String> nums = new HashSet<>();
        if (text == null) return nums;
        Matcher m = NUMBER_PATTERN.matcher(text);
        while (m.find()) nums.add(m.group());
        return nums;
    }

    /**
     * R07: 삭제 가중치 계산.
     * <p>
     * 키워드 카테고리별 위험도에 따라 차등 점수를 반환합니다.
     * 가장 위험한 카테고리 기준으로 단일 점수 적용 (중복 가산 방지).
     *
     * <pre>
     *   주체/권한 or 예외/조건 관련 삭제  → +3
     *   상태/절차 or 필수여부 or 허용여부 → +2
     *   중요 키워드 없음                  →  0
     * </pre>
     */
    /**
     * R07 삭제 가중치 계산.
     *
     * @param skipHighCategory R01(주체)·R06(예외) 이 이미 해당 키워드를 탐지한 경우 true.
     *                         true 이면 HIGH 카테고리(+3) 중복 적용을 건너뛰고
     *                         MID 카테고리(+2) 만 확인합니다.
     */
    private int calcDeletionWeight(String text, boolean skipHighCategory) {
        // 최고 위험: 권한 주체 또는 예외 조건 삭제 (R01·R06 미감지 시에만 적용)
        if (!skipHighCategory) {
            if (SUBJECT_KEYWORDS.stream().anyMatch(text::contains)
                    || EXCEPTION_KEYWORDS.stream().anyMatch(text::contains)) {
                return 3;
            }
        }
        // 중간 위험: 상태/절차·필수여부·허용여부 관련 삭제
        if (STATUS_KEYWORDS.stream().anyMatch(text::contains)
                || MANDATORY_KEYWORDS.stream().anyMatch(text::contains)
                || PERMISSION_KEYWORDS.stream().anyMatch(text::contains)) {
            return 2;
        }
        return 0;
    }

    /**
     * R08: 부정어 반전 쌍 탐지.
     * <p>
     * 매치된 반전 쌍 배열을 반환합니다 (이유 태그에 활용).
     * 반전이 없으면 null 반환.
     */
    private String[] findReversalPair(String oldL, String newL) {
        for (String[] pair : REVERSAL_PAIRS) {
            boolean forward  = oldL.contains(pair[0]) && newL.contains(pair[1]);
            boolean backward = oldL.contains(pair[1]) && newL.contains(pair[0]);
            if (forward)  return pair;
            if (backward) return new String[]{pair[1], pair[0]};  // 역방향이면 순서 반전하여 반환
        }
        return null;
    }

    /** R09: 중요 섹션 키워드가 포함되어 있으면 true */
    private boolean checkSection(String combined) {
        return SECTION_KEYWORDS.stream().anyMatch(combined::contains);
    }

    /**
     * R10: 변경이 동의어 치환 수준인지 확인.
     * old와 new를 단어 단위로 비교했을 때, 다른 단어들이 모두 동의어 쌍 내에 있으면 true.
     */
    private boolean checkSynonymOnly(String type, String old, String newC) {
        if (!"MODIFIED".equals(type)) return false;
        String oldTrimmed = old == null ? "" : old.trim();
        String newTrimmed = newC == null ? "" : newC.trim();
        if (oldTrimmed.isEmpty() || newTrimmed.isEmpty()) return false;

        String[] oldWords = oldTrimmed.split("\\s+");
        String[] newWords = newTrimmed.split("\\s+");

        Set<String> oldSet = Set.of(oldWords);
        Set<String> newSet = Set.of(newWords);

        Set<String> onlyInOld = new HashSet<>(oldSet);
        onlyInOld.removeAll(newSet);
        Set<String> onlyInNew = new HashSet<>(newSet);
        onlyInNew.removeAll(oldSet);

        if (onlyInOld.isEmpty() && onlyInNew.isEmpty()) return true;

        for (String removed : onlyInOld) {
            boolean matched = false;
            for (String[] pair : SYNONYM_PAIRS) {
                if ((removed.contains(pair[0]) && onlyInNew.stream().anyMatch(w -> w.contains(pair[1])))
                 || (removed.contains(pair[1]) && onlyInNew.stream().anyMatch(w -> w.contains(pair[0])))) {
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return true;
    }

    // ── 유틸 ────────────────────────────────────────────────────

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase();
    }

    private String toPriority(int score) {
        if (score >= 7) return "HIGH";
        if (score >= 4) return "MEDIUM";
        if (score >= 1) return "LOW";
        return "GENERAL";
    }
}
