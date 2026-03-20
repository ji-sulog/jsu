package com.jjp.jsu.scan;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * 하드코딩 / 고객사 특화 비즈니스 로직 탐지 서비스.
 *
 * <pre>
 * 규칙 분류
 *   HC (Hardcoding)    : HC-001 ~ HC-008 — 코드에 직접 박힌 값 탐지
 *   CL (Customization) : CL-001 ~ CL-008 — 고객사/조직 특화 로직 탐지
 *   WT (Weight)        : WT-001 ~ WT-005 — 점수 가중치 조정
 *
 * 점수 등급
 *   HIGH   : 8점 이상
 *   MEDIUM : 5 ~ 7점
 *   LOW    : 2 ~ 4점
 *   INFO   : 0 ~ 1점
 * </pre>
 */
@Service
public class ScanService {

    // ── HC-001: 템플릿 ID 직접 비교 ─────────────────────────────────────────
    private static final Pattern HC001_STRING = Pattern.compile(
            "(?i)[\"'`](TMPL|TPL|TEMPLATE|FORM|FRM|NOTICE_T|REPORT_T|MAIL_T|MSG_T)[_-][A-Z0-9_]{2,}[\"'`]");
    private static final Pattern HC001_VAR = Pattern.compile(
            "(?i)\\b(tmplId|templateId|tmplCd|templateCode|formId|formCd|tmplKey)\\b.*?\\.equals\\s*\\(");

    // ── HC-002: 상태코드 문자열 직접 비교 ───────────────────────────────────
    private static final Pattern HC002_STATUS = Pattern.compile(
            "(?i)\\.equals\\s*\\(\\s*[\"'](APPR(OVE(D)?)?|REJT|REJECT(ED)?|WAIT(ING)?|DONE"
            + "|PROC(ESSING)?|COMPL(ETE(D)?)?|CANCEL(LED)?|HOLD|READY|PENDING"
            + "|ACTV?|INACTV?|CLOSE(D)?|OPEN|SUBMIT(TED)?)[A-Z_]*[\"']\\s*\\)");
    private static final Pattern HC002_YN = Pattern.compile(
            "(?i)\\b(useYn|delYn|activeYn|apprYn|useFlag|statusFlag|displayYn"
            + "|activatedYn|enabledYn)\\s*(==|!=|equals)\\s*[\"'][YN1][\"']");

    // ── HC-003: 역할명 직접 비교 ─────────────────────────────────────────────
    private static final Pattern HC003_ROLE_EQUALS = Pattern.compile(
            "(?i)\\.equals\\s*\\(\\s*[\"'](ROLE_|ADMIN|SUPER_?ADMIN|MANAGER|OPERATOR"
            + "|VIEWER|GUEST|STAFF|APPROVER)[\\w_]*[\"']\\s*\\)");
    private static final Pattern HC003_ROLE_VAR = Pattern.compile(
            "(?i)\\b(roleCode|roleCd|roleName|userRole|roleId|authCd)\\s*(==|!=)\\s*[\"'][\\w_]{2,}[\"']");

    // ── HC-004: 고객사/조직/사이트 코드 직접 비교 ───────────────────────────
    private static final Pattern HC004_VAR = Pattern.compile(
            "(?i)\\b(custCd|custCode|customerCode|cmpCd|companyCd|compCode"
            + "|orgCd|orgCode|siteCode|siteCd|clientCode|clientCd|tenantId|tenantCode)\\b");
    private static final Pattern HC004_CUST_LITERAL = Pattern.compile(
            "(?i)[\"'](CUST[_-]|CLIENT[_-]|CORP[_-]|ORG[_-]|SITE[_-]|COMP[_-]|TENANT[_-])[A-Z0-9_]{2,}[\"']");

    // ── HC-005: 매직 넘버 ────────────────────────────────────────────────────
    private static final Pattern HC005_IN_CONDITION = Pattern.compile(
            "(?i)(?:if|while|switch|case|return)\\b.{0,60}?(?<![\\w.])([3-9]\\d|[1-9]\\d{2,3})(?![\\d.])");
    private static final Set<Integer> MAGIC_WHITELIST = Set.of(
            10, 11, 12, 13, 14, 15, 16, 20, 24, 30, 32, 50, 60, 64,
            80, 90, 100, 128, 200, 201, 204, 256, 400, 401, 403, 404,
            409, 500, 503, 1000, 1024, 2048, 3000, 5000, 8080, 9090);

    // ── HC-006: URL / 경로 하드코딩 ─────────────────────────────────────────
    private static final Pattern HC006_URL = Pattern.compile(
            "(https?://[\\w./?=%&#-]{6,}|ftp://[\\w./-]{4,})");
    private static final Pattern HC006_PATH = Pattern.compile(
            "[\"'`](/[\\w/.-]{6,}|[A-Za-z]:[/\\\\][\\w/\\\\.-]{4,})[\"'`]");

    // ── HC-007: SQL 내 고정 코드값 ──────────────────────────────────────────
    private static final Pattern HC007_SQL_KW = Pattern.compile(
            "(?i)\\b(SELECT|FROM|WHERE|AND|OR|SET|INSERT|UPDATE|DELETE)\\b");
    private static final Pattern HC007_SQL_HARDCODE = Pattern.compile(
            "(?i)\\b(WHERE|AND|OR)\\s+[\\w.]+\\s*=\\s*[\"'][\\w]{2,}[\"']");

    // ── HC-008: 승인 단계값 직접 사용 ───────────────────────────────────────
    private static final Pattern HC008_STEP = Pattern.compile(
            "(?i)\\b(apprStep|approvalStep|stepLevel|aprvStep|aprvLevel"
            + "|stepNo|stepCnt|approvalLevel|aprvStepCd)\\b\\s*(==|!=|>=|<=|>|<)\\s*\\d+");
    private static final Pattern HC008_STEP_EQ = Pattern.compile(
            "(?i)\\b(apprStep|approvalStep|stepLevel|stepNo)\\b.*?\\.equals\\s*\\(\\s*[\"']?\\d+[\"']?\\s*\\)");

    // ── CL-001: 고객사 코드 기준 조건문 ─────────────────────────────────────
    private static final Pattern CL001_IF_CUST = Pattern.compile(
            "(?i)\\bif\\s*\\(.*?\\b(custCd|custCode|customerCode|clientCode|cmpCd|tenantId|tenantCode)\\b");

    // ── CL-002: 조직/사업장별 예외 분기 ─────────────────────────────────────
    private static final Pattern CL002_IF_ORG = Pattern.compile(
            "(?i)\\bif\\s*\\(.*?\\b(orgCd|orgCode|siteCode|siteCd|deptCd|deptCode|bizCd|bizCode)\\b");

    // ── CL-003: 고객사 특화 클래스/메서드명 ─────────────────────────────────
    private static final Pattern CL003_CLASS = Pattern.compile(
            "(?i)\\b(class|interface)\\s+\\w*(Cust|Customer|Client|Corp|Tenant|Site|Company)\\w*");
    private static final Pattern CL003_METHOD = Pattern.compile(
            "(?i)\\b(public|private|protected)\\s+[\\w<>\\[\\]]+\\s+"
            + "\\w*(Cust|Customer|Client|Corp|Tenant)\\w*\\s*\\(");

    // ── CL-005: 고객 전용 주석/키워드 ───────────────────────────────────────
    private static final Pattern CL005_KR = Pattern.compile(
            "(?i)(//|/\\*|\\*).*?(고객사?|고객 전용|전용\\s*로직|특화|customized?|customer specific|client only)");
    private static final Pattern CL005_EN = Pattern.compile(
            "(?i)(//|/\\*|\\*).*?\\b(for [A-Za-z]+ (customer|client|corp|only)|[A-Za-z]+ specific)");

    // ── CL-006: 템플릿 전용 하드 분기 ───────────────────────────────────────
    private static final Pattern CL006_IF_TMPL = Pattern.compile(
            "(?i)\\bif\\s*\\(.*?\\b(tmplCd|tmplId|templateCode|templateId|formCd|formId)\\b");

    // ── CL-007: 특정 값 중심 예외 처리 누적 ─────────────────────────────────
    private static final Pattern CL007_IF = Pattern.compile(
            "(?i)\\bif\\s*\\(");
    private static final Pattern CL007_THROW = Pattern.compile(
            "(?i)(throw\\s+new\\s+\\w+Exception\\s*\\(|return\\s+[\"'][A-Z][A-Z0-9_]{3,}[\"'])");

    // ── WT 보조 패턴 ─────────────────────────────────────────────────────────
    private static final Pattern COMMON_AREA = Pattern.compile(
            "(?i)(common|shared|base|core|abstract|default|framework|engine|platform|util(ity)?)");
    private static final Pattern TEST_CODE = Pattern.compile(
            "(?i)(test|spec|mock|stub)");
    private static final Pattern CONSTANT_LINE = Pattern.compile(
            "\\bstatic\\s+final\\b");

    // ─────────────────────────────────────────────────────────────────────────
    // 공개 API
    // ─────────────────────────────────────────────────────────────────────────

    public List<ScanItem> scan(ScanRequest request) {
        if (request.code() == null || request.code().isBlank()) return List.of();

        String[] lines = request.code().split("\n");
        String fileName = request.fileName() != null ? request.fileName() : "";
        List<String> kws = request.customKeywords() != null
                ? request.customKeywords().stream().filter(k -> k != null && !k.isBlank()).toList()
                : List.of();

        boolean isTest   = isTestCode(request.code(), fileName);
        boolean isCommon = isCommonArea(fileName);

        List<ScanItem> items = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String raw  = lines[i];
            String line = raw.trim();
            int    ln   = i + 1;

            if (line.isEmpty()) continue;

            boolean isComment  = line.startsWith("//") || line.startsWith("*") || line.startsWith("/*");
            boolean isConstant = CONSTANT_LINE.matcher(line).find();

            if (!isTest) {
                if (!isComment) {
                    applyHC001(line, ln, raw).ifPresent(items::add);
                    applyHC002(line, ln, raw).ifPresent(items::add);
                    applyHC003(line, ln, raw).ifPresent(items::add);
                    applyHC004(line, ln, raw, kws).ifPresent(items::add);
                    if (!isConstant) applyHC005(line, ln, raw).ifPresent(items::add);
                    applyHC006(line, ln, raw).ifPresent(items::add);
                    applyHC007(line, ln, raw).ifPresent(items::add);
                    applyHC008(line, ln, raw).ifPresent(items::add);
                    applyCL001(line, ln, raw, kws).ifPresent(items::add);
                    applyCL002(line, ln, raw).ifPresent(items::add);
                    applyCL003(line, ln, raw).ifPresent(items::add);
                    applyCL006(line, ln, raw).ifPresent(items::add);
                    applyCL007(line, ln, raw).ifPresent(items::add);
                }
                // CL-005: 주석이 탐지 대상
                applyCL005(line, ln, raw).ifPresent(items::add);
            }
        }

        // CL-008: 반복 분기 패턴 분석 (전체 코드 기반)
        if (!isTest) {
            items.addAll(applyCL008(lines, kws));
        }

        // WT 가중치 적용
        items = applyWeights(items, lines, isCommon);

        // WT-005: 같은 규칙 3회 이상 → 각 항목 +1
        Map<String, Long> ruleCount = items.stream()
                .collect(Collectors.groupingBy(ScanItem::ruleId, Collectors.counting()));
        items = items.stream()
                .map(it -> ruleCount.getOrDefault(it.ruleId(), 0L) >= 3
                        ? withScore(it, it.score() + 1) : it)
                .collect(Collectors.toCollection(ArrayList::new));

        // 점수 내림차순 → 라인 번호 오름차순
        items.sort(Comparator.comparingInt(ScanItem::score).reversed()
                .thenComparingInt(ScanItem::lineNumber));

        // 제외 키워드 필터링 — matchedText 에 제외 키워드가 포함된 항목 제거
        List<String> excludes = request.excludeKeywords() != null
                ? request.excludeKeywords().stream().filter(k -> k != null && !k.isBlank()).toList()
                : List.of();
        if (!excludes.isEmpty()) {
            items = items.stream()
                    .filter(it -> excludes.stream()
                            .noneMatch(ex -> it.matchedText() != null
                                    && it.matchedText().contains(ex)))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return items;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HC 규칙 구현
    // ─────────────────────────────────────────────────────────────────────────

    private Optional<ScanItem> applyHC001(String line, int ln, String raw) {
        Matcher m1 = HC001_STRING.matcher(line);
        Matcher m2 = HC001_VAR.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("HC-001", "템플릿 ID 직접 비교", "HARDCODING", ln, raw, matched,
                "템플릿/폼 ID가 코드에 직접 박혀 있습니다. 템플릿이 추가·변경될 때마다 코드를 수정해야 합니다.",
                "템플릿 ID는 Enum 상수 또는 설정파일(yml/properties)로 분리하고, 코드에서는 상수 참조를 사용하세요.", 3));
    }

    private Optional<ScanItem> applyHC002(String line, int ln, String raw) {
        Matcher m1 = HC002_STATUS.matcher(line);
        Matcher m2 = HC002_YN.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("HC-002", "상태코드 문자열 직접 비교", "HARDCODING", ln, raw, matched,
                "상태 코드 값이 문자열 리터럴로 직접 비교되고 있습니다. 상태 체계 변경 시 모든 비교 지점을 수정해야 합니다.",
                "상태 코드를 Enum으로 정의하고 .equals() 대신 Enum 비교를 사용하세요. 공통 코드 테이블 관리 체계가 있다면 그쪽으로 통합하세요.", 3));
    }

    private Optional<ScanItem> applyHC003(String line, int ln, String raw) {
        Matcher m1 = HC003_ROLE_EQUALS.matcher(line);
        Matcher m2 = HC003_ROLE_VAR.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("HC-003", "역할명 직접 비교", "HARDCODING", ln, raw, matched,
                "역할 코드·이름이 코드에 직접 박혀 있습니다. 역할 체계 변경 시 코드 수정이 필요합니다.",
                "역할 정의를 Enum 또는 Role 상수 클래스로 분리하세요. Spring Security를 사용한다면 @PreAuthorize 같은 선언적 권한 처리를 검토하세요.", 3));
    }

    private Optional<ScanItem> applyHC004(String line, int ln, String raw, List<String> kws) {
        // 변수명 기반 탐지
        Matcher m1 = HC004_VAR.matcher(line);
        boolean varMatch = m1.find() && (line.contains("equals") || line.contains("==") || line.contains("!="));
        // 리터럴 값 기반 탐지
        Matcher m2 = HC004_CUST_LITERAL.matcher(line);
        // 사용자 정의 키워드
        String customMatch = kws.stream().filter(k -> line.contains(k.trim())).findFirst().orElse(null);

        String matched = null;
        if (varMatch)          matched = m1.group();
        else if (m2.find())    matched = m2.group();
        else if (customMatch != null) matched = customMatch;
        if (matched == null) return Optional.empty();

        return Optional.of(item("HC-004", "고객사/조직/사이트 코드 직접 비교", "HARDCODING", ln, raw, matched,
                "고객사·조직·사이트 식별 코드가 코드에 직접 박혀 있습니다. 고객사가 추가·변경될 때마다 코드 수정이 발생합니다.",
                "고객사별 설정은 DB 설정 테이블 또는 외부 설정파일로 관리하고, 전략 패턴(Strategy Pattern)으로 고객사별 처리를 추상화하세요.", 5));
    }

    private Optional<ScanItem> applyHC005(String line, int ln, String raw) {
        Matcher m = HC005_IN_CONDITION.matcher(line);
        while (m.find()) {
            String numStr = m.group(1);
            if (numStr == null) continue;
            try {
                int num = Integer.parseInt(numStr);
                if (!MAGIC_WHITELIST.contains(num)) {
                    return Optional.of(item("HC-005", "매직 넘버 직접 사용", "HARDCODING", ln, raw, numStr,
                            "숫자 " + numStr + "이(가) 의미 없이 코드에 직접 사용되고 있습니다. 이 값이 무엇을 의미하는지 코드만 보고 파악하기 어렵습니다.",
                            "이 값을 의미 있는 이름의 상수(static final)로 추출하거나, 설정파일에서 주입받는 방식으로 외부화하세요.", 2));
                }
            } catch (NumberFormatException ignored) {}
        }
        return Optional.empty();
    }

    private Optional<ScanItem> applyHC006(String line, int ln, String raw) {
        // 주석 내 URL은 일반적 — Javadoc/@see 등 제외
        if (line.startsWith("//") || line.startsWith("*")) return Optional.empty();
        Matcher m1 = HC006_URL.matcher(line);
        Matcher m2 = HC006_PATH.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("HC-006", "URL/경로 하드코딩", "HARDCODING", ln, raw, matched,
                "URL 또는 파일 경로가 코드에 직접 박혀 있습니다. 환경(개발/운영)별로 값이 달라져야 할 경우 배포마다 코드 수정이 필요합니다.",
                "URL과 경로는 application.yml의 환경별 프로파일로 분리하고 @Value 또는 @ConfigurationProperties로 주입받으세요.", 4));
    }

    private Optional<ScanItem> applyHC007(String line, int ln, String raw) {
        if (!HC007_SQL_KW.matcher(line).find()) return Optional.empty();
        Matcher m = HC007_SQL_HARDCODE.matcher(line);
        if (!m.find()) return Optional.empty();
        return Optional.of(item("HC-007", "SQL 내 고정 코드값", "HARDCODING", ln, raw, m.group(),
                "SQL 쿼리 내에 고정 코드값이 직접 박혀 있습니다. 코드값 체계 변경 시 SQL도 함께 수정해야 합니다.",
                "조건값은 파라미터 바인딩(?)으로 처리하거나, 공통 코드 테이블 조인 방식으로 전환하세요. MyBatis라면 동적 SQL로 처리하세요.", 4));
    }

    private Optional<ScanItem> applyHC008(String line, int ln, String raw) {
        Matcher m1 = HC008_STEP.matcher(line);
        Matcher m2 = HC008_STEP_EQ.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("HC-008", "승인 단계값 직접 사용", "HARDCODING", ln, raw, matched,
                "승인 단계 번호가 코드에 직접 박혀 있습니다. 승인 단계 구조 변경 시 모든 비교 지점을 찾아 수정해야 합니다.",
                "승인 단계는 워크플로우 정의 테이블에서 관리하고, 코드에서는 단계 상수(Enum) 또는 설정값을 참조하세요.", 4));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CL 규칙 구현
    // ─────────────────────────────────────────────────────────────────────────

    private Optional<ScanItem> applyCL001(String line, int ln, String raw, List<String> kws) {
        Matcher m = CL001_IF_CUST.matcher(line);
        boolean customHit = !kws.isEmpty() && line.toLowerCase().contains("if")
                && kws.stream().anyMatch(k -> line.contains(k.trim()));
        if (!m.find() && !customHit) return Optional.empty();
        String matched = m.reset().find() ? m.group()
                : kws.stream().filter(k -> line.contains(k.trim())).findFirst().orElse("if(...)");
        return Optional.of(item("CL-001", "고객사 코드 기준 조건문", "CUSTOMIZATION", ln, raw, matched,
                "고객사·클라이언트 코드를 기준으로 분기 처리하고 있습니다. 고객사가 늘어날수록 분기가 누적되어 공통 코드가 오염됩니다.",
                "고객사별 처리는 전략 패턴(Strategy Pattern) 또는 플러그인 확장 포인트로 분리하고, 공통 서비스에서 고객사 코드 기반 if/else를 제거하세요.", 5));
    }

    private Optional<ScanItem> applyCL002(String line, int ln, String raw) {
        Matcher m = CL002_IF_ORG.matcher(line);
        if (!m.find()) return Optional.empty();
        return Optional.of(item("CL-002", "조직/사업장별 예외 분기", "CUSTOMIZATION", ln, raw, m.group(),
                "조직·사업장 코드를 기준으로 분기 처리하고 있습니다. 조직이 늘어날수록 코드 복잡도가 선형으로 증가합니다.",
                "조직별 처리 규칙은 DB 설정 테이블로 외부화하거나 정책 객체(Policy Object) 패턴으로 추상화하세요.", 4));
    }

    private Optional<ScanItem> applyCL003(String line, int ln, String raw) {
        Matcher m1 = CL003_CLASS.matcher(line);
        Matcher m2 = CL003_METHOD.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("CL-003", "고객사 특화 클래스/메서드명", "CUSTOMIZATION", ln, raw, matched,
                "클래스·메서드 이름에 고객사·조직 식별어가 포함되어 있습니다. 공통 코드 안에 고객사 특화 구현이 직접 정의된 신호입니다.",
                "고객사 특화 구현은 별도 패키지(customer.{고객사}.xxx) 또는 확장 모듈로 분리하세요. 공통 인터페이스를 정의하고 고객사별 구현을 주입하는 구조를 검토하세요.", 3));
    }

    private Optional<ScanItem> applyCL005(String line, int ln, String raw) {
        Matcher m1 = CL005_KR.matcher(line);
        Matcher m2 = CL005_EN.matcher(line);
        String matched = null;
        if (m1.find()) matched = m1.group();
        else if (m2.find()) matched = m2.group();
        if (matched == null) return Optional.empty();
        return Optional.of(item("CL-005", "고객사 전용 주석/키워드", "CUSTOMIZATION", ln, raw, matched,
                "주석 또는 코드에 특정 고객사 전용임을 나타내는 표현이 있습니다. 공통 코드에 고객사 특화 흔적이 존재한다는 신호입니다.",
                "고객사 전용 로직은 주석으로만 표시하지 말고 구조적으로 분리하세요. 주석이 생겨나는 이유(분기 로직)를 제거하는 것이 목표입니다.", 3));
    }

    private Optional<ScanItem> applyCL006(String line, int ln, String raw) {
        Matcher m = CL006_IF_TMPL.matcher(line);
        if (!m.find()) return Optional.empty();
        return Optional.of(item("CL-006", "템플릿 전용 하드 분기", "CUSTOMIZATION", ln, raw, m.group(),
                "특정 템플릿 코드를 기준으로 분기 처리하고 있습니다. 템플릿이 추가될 때마다 이 분기 로직도 수정이 필요합니다.",
                "템플릿별 처리 규칙은 핸들러 맵(Map<String, Handler>) 또는 템플릿 정의 테이블로 외부화하세요. 템플릿 추가 시 코드 변경 없이 데이터로 대응할 수 있어야 합니다.", 4));
    }

    private Optional<ScanItem> applyCL007(String line, int ln, String raw) {
        if (!CL007_IF.matcher(line).find()) return Optional.empty();
        Matcher m = CL007_THROW.matcher(line);
        if (!m.find()) return Optional.empty();
        return Optional.of(item("CL-007", "특정 값 중심 예외 처리", "CUSTOMIZATION", ln, raw, m.group(),
                "특정 코드값을 조건으로 예외·에러 반환이 발생합니다. 동일 패턴이 반복되면 예외 처리 로직의 공통화가 필요합니다.",
                "예외 처리 전략을 AOP 또는 공통 ExceptionHandler로 통합하고, 값 기반 분기보다 타입 기반 처리 구조를 검토하세요.", 3));
    }

    /**
     * CL-008: 동일 변수 기준 if 분기가 3회 이상 반복되는 경우 탐지.
     */
    private List<ScanItem> applyCL008(String[] lines, List<String> kws) {
        Pattern ifVarPat = Pattern.compile(
                "(?i)\\bif\\s*\\(.*?\\b(\\w+Cd|\\w+Code|\\w+Id|\\w+Type|\\w+Yn)\\b");
        Map<String, List<Integer>> varLineMap = new LinkedHashMap<>();

        for (int i = 0; i < lines.length; i++) {
            Matcher m = ifVarPat.matcher(lines[i].trim());
            if (m.find()) {
                varLineMap.computeIfAbsent(m.group(1), k -> new ArrayList<>()).add(i + 1);
            }
        }

        List<ScanItem> result = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : varLineMap.entrySet()) {
            if (entry.getValue().size() >= 3) {
                int firstLn = entry.getValue().get(0);
                // 방어적 범위 체크 — 라인 번호가 유효하지 않으면 건너뜀
                if (firstLn < 1 || firstLn > lines.length) continue;
                String lineNums = entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(", "));
                result.add(item("CL-008", "반복 분기 기반 설정화 후보", "REFACTORING",
                        firstLn, lines[firstLn - 1].stripTrailing(), entry.getKey(),
                        "'" + entry.getKey() + "' 변수를 기준으로 " + entry.getValue().size()
                                + "번의 if 분기가 발생합니다 (라인: " + lineNums
                                + "). 이 패턴은 설정화·테이블화의 강력한 후보입니다.",
                        "이 변수를 키로 하는 처리 맵(Map) 또는 설정 테이블로 분기 로직을 외부화하세요."
                                + " 분기가 3개 이상이라면 전략 패턴(Strategy Pattern) 도입을 검토하세요.", 3));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WT 가중치 적용
    // ─────────────────────────────────────────────────────────────────────────

    private List<ScanItem> applyWeights(List<ScanItem> items, String[] lines, boolean isCommon) {
        List<ScanItem> result = new ArrayList<>();
        for (ScanItem item : items) {
            int score = item.score();

            // WT-001: 공통 영역 파일이면 +2
            if (isCommon) score += 2;

            // WT-003: static final 라인이면 -2 (상수 정의 자체는 의도된 것)
            if (item.lineNumber() > 0 && item.lineNumber() <= lines.length) {
                String raw = lines[item.lineNumber() - 1].trim();
                if (CONSTANT_LINE.matcher(raw).find()) score = Math.max(0, score - 2);
            }

            // WT-004: 들여쓰기 깊이 4 이상이면 +1 (중첩 분기 안에 있는 경우)
            if (item.lineNumber() > 0 && item.lineNumber() <= lines.length) {
                if (estimateDepth(lines[item.lineNumber() - 1]) >= 4) score += 1;
            }

            score = Math.max(0, score);
            result.add(withScore(item, score));
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 유틸 메서드
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isTestCode(String code, String fileName) {
        return TEST_CODE.matcher(fileName).find()
                || code.contains("@Test")
                || code.contains("@BeforeEach")
                || code.contains("@ExtendWith")
                || code.contains("Mockito");
    }

    private boolean isCommonArea(String fileName) {
        return COMMON_AREA.matcher(fileName).find();
    }

    private int estimateDepth(String rawLine) {
        int spaces = 0;
        for (char c : rawLine.toCharArray()) {
            if (c == '\t') spaces += 4;
            else if (c == ' ') spaces++;
            else break;
        }
        return spaces / 4;
    }

    private String toSeverity(int score) {
        if (score >= 8) return "HIGH";
        if (score >= 5) return "MEDIUM";
        if (score >= 2) return "LOW";
        return "INFO";
    }

    private ScanItem item(String ruleId, String ruleName, String category,
                          int ln, String raw, String matched,
                          String reason, String recommendation, int baseScore) {
        return new ScanItem(ruleId, ruleName, category,
                ln, raw.stripTrailing(), matched,
                reason, recommendation, baseScore, toSeverity(baseScore));
    }

    private ScanItem withScore(ScanItem s, int newScore) {
        return new ScanItem(s.ruleId(), s.ruleName(), s.category(),
                s.lineNumber(), s.lineContent(), s.matchedText(),
                s.reason(), s.recommendation(), newScore, toSeverity(newScore));
    }
}
