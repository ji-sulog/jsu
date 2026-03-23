package com.jjp.jsu.naming;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * 네이밍 컨벤션 검사 서비스.
 *
 * <pre>
 * 규칙 분류
 *   형식 규칙
 *     NM-001 (TYPE)   : 타입 이름은 PascalCase      — HIGH
 *     NM-002 (METHOD) : 메서드명은 camelCase         — MEDIUM
 *     NM-003 (FIELD)  : 필드/변수명은 camelCase      — MEDIUM
 *     NM-004 (FIELD)  : 상수는 UPPER_SNAKE_CASE     — HIGH
 *
 *   의미 품질 규칙
 *     NM-005 (MEANING): boolean 이름에 의도가 드러나야 함  — LOW
 *     NM-006 (MEANING): 의미가 약한 이름 경고              — LOW
 *     NM-007 (MEANING): 컬렉션 이름 품질 경고              — LOW
 *     NM-008 (MEANING): 축약어 과다 경고                    — LOW
 * </pre>
 */
@Service
public class NamingService {

    // ── 형식 규칙 패턴 ─────────────────────────────────────────────────────────

    /** NM-001: class / interface / enum / record 선언부 */
    private static final Pattern NM001_TYPE = Pattern.compile(
            "(?:^|\\s)(?:class|interface|enum|record)\\s+([A-Za-z_$][\\w$]*)(?:\\s|<|\\{|extends|implements|$)");

    /** NM-002: 메서드 선언부 (public/private/protected/package-private + 반환타입 + 이름 + '(') */
    private static final Pattern NM002_METHOD = Pattern.compile(
            "(?:public|private|protected)\\s+(?:static\\s+)?(?:final\\s+)?(?:synchronized\\s+)?" +
            "(?:<[^>]+>\\s+)?[\\w$<>\\[\\]?,\\s]+\\s+([a-zA-Z_$][\\w$]*)\\s*\\(");

    /** NM-003: 필드 선언부 (접근제어자 있는 경우) */
    private static final Pattern NM003_FIELD = Pattern.compile(
            "(?:private|protected|public)\\s+(?!static\\s+final\\b)(?:final\\s+)?" +
            "(?:static\\s+)?[A-Z][\\w<>\\[\\]?,\\s]*\\s+([a-zA-Z_$][\\w$]*)\\s*[=;,)]");

    /** NM-004: static final 상수 선언부 */
    private static final Pattern NM004_CONST = Pattern.compile(
            "(?:private|protected|public)\\s+static\\s+final\\s+[\\w<>\\[\\]?,\\s]+\\s+([A-Za-z_$][\\w$]*)\\s*=");

    // ── 의미 품질 규칙 패턴 ────────────────────────────────────────────────────

    /** NM-005: boolean / Boolean 선언 */
    private static final Pattern NM005_BOOL = Pattern.compile(
            "(?:boolean|Boolean)\\s+([a-zA-Z_$][\\w$]*)\\s*[=;,)]");

    /** NM-007: 컬렉션 선언 */
    private static final Pattern NM007_COLLECTION = Pattern.compile(
            "(?:List|ArrayList|LinkedList|Set|HashSet|TreeSet|Queue|Deque)" +
            "<[^>]*>\\s+([a-zA-Z_$][\\w$]*)\\s*[=;,)]|" +
            "Map(?:<[^>]*>)?\\s+([a-zA-Z_$][\\w$]*)\\s*[=;,)]|" +
            "([a-zA-Z_$][\\w$]*)\\s*\\[\\s*\\]\\s+([a-zA-Z_$][\\w$]*)\\s*[=;,)]");

    // ── NM-006: 의미가 약한 이름 목록 ──────────────────────────────────────────
    private static final Set<String> WEAK_NAMES = Set.of(
            "data", "value", "temp", "tmp", "obj", "info", "result", "val",
            "item", "thing", "stuff", "entity", "object", "res", "ret",
            "num", "cnt", "flag", "str", "buf", "msg", "err"
    );

    // ── NM-008: 축약어 과다 탐지 (연속 소문자 3자 이내 단어가 3개 이상인 식별자) ──
    private static final Pattern NM008_ABBR = Pattern.compile(
            "(?:[a-z]{1,3}(?:[A-Z]|_)){3,}[A-Za-z0-9]*");

    // ── boolean 접두사 화이트리스트 ─────────────────────────────────────────────
    private static final Set<String> BOOL_PREFIXES = Set.of(
            "is", "has", "can", "should", "use", "enable", "enabled",
            "show", "hide", "allow", "include", "exclude", "need", "will"
    );

    // ── 무시할 짧은 이름 (루프 인덱스, 좌표 등) ──────────────────────────────
    private static final Set<String> IGNORE_NAMES = Set.of(
            "i", "j", "k", "x", "y", "z", "n", "m", "t", "e", "ex",
            "id", "ok", "no", "op", "fn", "db", "sb", "io", "in", "os",
            "url", "uri", "api", "dto", "vo", "pk", "fk", "ui"
    );

    // ── 메서드명에서 제외할 이름 (Object/특수 메서드) ────────────────────────
    private static final Set<String> IGNORE_METHOD_NAMES = Set.of(
            "main", "run", "call", "get", "set", "is", "has", "add", "put",
            "remove", "clear", "size", "length", "close", "open", "read",
            "write", "init", "start", "stop", "reset", "build", "create",
            "delete", "update", "find", "load", "save", "send", "post",
            "equals", "hashCode", "toString", "clone", "compareTo"
    );

    // ── NM-002 반환타입으로 잘못 잡히는 키워드 차단 ─────────────────────────
    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "if", "else", "for", "while", "do", "switch", "case", "break",
            "continue", "return", "new", "try", "catch", "finally", "throw",
            "throws", "import", "package", "extends", "implements", "super",
            "this", "instanceof", "null", "true", "false", "abstract",
            "synchronized", "volatile", "transient", "native", "strictfp",
            "class", "interface", "enum", "record", "var"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // 공개 API
    // ─────────────────────────────────────────────────────────────────────────

    public NamingCheckResponse check(NamingCheckRequest request) {
        if (request.code() == null || request.code().isBlank()) {
            return empty();
        }

        String[] lines = request.code().split("\n");
        List<NamingIssue> issues = new ArrayList<>();
        boolean meaningQuality = request.checkMeaningQuality();
        boolean boolNaming     = request.checkBooleanNaming();

        for (int i = 0; i < lines.length; i++) {
            String raw  = lines[i];
            String line = raw.trim();
            int    ln   = i + 1;

            if (line.isEmpty()) continue;
            // 주석 라인은 선언부 규칙 적용 안 함
            boolean isComment = line.startsWith("//") || line.startsWith("*") || line.startsWith("/*");
            if (isComment) continue;
            // 어노테이션 라인 제외
            if (line.startsWith("@")) continue;

            // NM-001: 타입 이름
            applyNM001(line, ln, raw).forEach(issues::add);
            // NM-002: 메서드 이름
            applyNM002(line, ln, raw).forEach(issues::add);
            // NM-003: 필드/변수 이름
            applyNM003(line, ln, raw).forEach(issues::add);
            // NM-004: 상수 이름
            applyNM004(line, ln, raw).forEach(issues::add);

            if (meaningQuality) {
                // NM-005: boolean 이름 (별도 옵션)
                if (boolNaming) {
                    applyNM005(line, ln, raw).forEach(issues::add);
                }
                // NM-006: 의미가 약한 이름
                applyNM006(line, ln, raw).forEach(issues::add);
                // NM-007: 컬렉션 이름
                applyNM007(line, ln, raw).forEach(issues::add);
                // NM-008: 축약어 과다
                applyNM008(line, ln, raw).forEach(issues::add);
            }
        }

        // 중복 제거 (같은 라인+규칙+이름)
        issues = deduplicate(issues);

        // 정렬: HIGH → MEDIUM → LOW, 같은 심각도는 라인 번호 오름차순
        issues.sort(Comparator.comparingInt(NamingService::sevOrder).reversed()
                .thenComparingInt(NamingIssue::lineNumber));

        // 집계
        int high   = (int) issues.stream().filter(x -> "HIGH".equals(x.severity())).count();
        int medium = (int) issues.stream().filter(x -> "MEDIUM".equals(x.severity())).count();
        int low    = (int) issues.stream().filter(x -> "LOW".equals(x.severity())).count();
        int type   = (int) issues.stream().filter(x -> "TYPE".equals(x.category())).count();
        int method = (int) issues.stream().filter(x -> "METHOD".equals(x.category())).count();
        int field  = (int) issues.stream().filter(x -> "FIELD".equals(x.category())).count();
        int mean   = (int) issues.stream().filter(x -> "MEANING".equals(x.category())).count();

        return new NamingCheckResponse(issues, issues.size(), high, medium, low, type, method, field, mean);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-001: 타입 이름은 PascalCase
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM001(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        Matcher m = NM001_TYPE.matcher(line);
        while (m.find()) {
            String name = m.group(1);
            if (name == null || name.isBlank()) continue;
            if (!isPascalCase(name)) {
                String suggested = toPascalCase(name);
                result.add(new NamingIssue(
                        "NM-001", "타입 이름은 PascalCase", "TYPE", "HIGH",
                        ln, raw.stripTrailing(), name,
                        "타입 이름 '" + name + "'이(가) PascalCase 규칙을 따르지 않습니다. " +
                        "클래스·인터페이스·열거형·레코드 이름은 대문자로 시작하고 단어 경계마다 대문자를 사용해야 합니다.",
                        "대문자로 시작하고 단어 구분은 밑줄 없이 대문자로 이어서 작성하세요. 예: UserService, OrderStatus",
                        suggested.equals(name) ? null : suggested
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-002: 메서드명은 camelCase
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM002(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        Matcher m = NM002_METHOD.matcher(line);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String name = m.group(1);
            if (name == null || name.isBlank()) continue;
            if (IGNORE_NAMES.contains(name.toLowerCase())) continue;
            if (IGNORE_METHOD_NAMES.contains(name)) continue;
            if (JAVA_KEYWORDS.contains(name)) continue;
            if (seen.contains(name)) continue;
            seen.add(name);

            if (!isCamelCase(name)) {
                String suggested = toCamelCase(name);
                result.add(new NamingIssue(
                        "NM-002", "메서드명은 camelCase", "METHOD", "MEDIUM",
                        ln, raw.stripTrailing(), name,
                        "메서드명 '" + name + "'이(가) camelCase 규칙을 따르지 않습니다. " +
                        "메서드 이름은 소문자로 시작하고 단어 경계마다 대문자를 사용해야 합니다.",
                        "소문자로 시작하고 밑줄 없이 단어 경계에 대문자를 사용하세요. 예: saveUser, getUserById",
                        suggested.equals(name) ? null : suggested
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-003: 필드/변수명은 camelCase
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM003(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        Matcher m = NM003_FIELD.matcher(line);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String name = m.group(1);
            if (name == null || name.isBlank()) continue;
            if (IGNORE_NAMES.contains(name.toLowerCase())) continue;
            if (JAVA_KEYWORDS.contains(name)) continue;
            if (seen.contains(name)) continue;
            seen.add(name);

            if (!isCamelCase(name)) {
                String suggested = toCamelCase(name);
                result.add(new NamingIssue(
                        "NM-003", "필드/변수명은 camelCase", "FIELD", "MEDIUM",
                        ln, raw.stripTrailing(), name,
                        "필드/변수명 '" + name + "'이(가) camelCase 규칙을 따르지 않습니다. " +
                        "필드와 변수는 소문자로 시작하고 밑줄 없이 단어 경계에 대문자를 사용해야 합니다.",
                        "소문자로 시작하고 밑줄 없이 camelCase로 작성하세요. 예: userName, orderCount",
                        suggested.equals(name) ? null : suggested
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-004: 상수는 UPPER_SNAKE_CASE
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM004(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        Matcher m = NM004_CONST.matcher(line);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String name = m.group(1);
            if (name == null || name.isBlank()) continue;
            if (seen.contains(name)) continue;
            seen.add(name);

            if (!isUpperSnakeCase(name)) {
                String suggested = toUpperSnakeCase(name);
                result.add(new NamingIssue(
                        "NM-004", "상수는 UPPER_SNAKE_CASE", "FIELD", "HIGH",
                        ln, raw.stripTrailing(), name,
                        "상수 '" + name + "'이(가) UPPER_SNAKE_CASE 규칙을 따르지 않습니다. " +
                        "static final 상수는 모두 대문자이고 단어 사이에 밑줄(_)을 사용해야 합니다.",
                        "모두 대문자로 작성하고 단어 사이에 밑줄을 사용하세요. 예: MAX_RETRY_COUNT, DEFAULT_TIMEOUT",
                        suggested.equals(name) ? null : suggested
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-005: boolean 이름은 의도가 드러나야 함
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM005(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        Matcher m = NM005_BOOL.matcher(line);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String name = m.group(1);
            if (name == null || name.isBlank()) continue;
            if (IGNORE_NAMES.contains(name.toLowerCase())) continue;
            if (seen.contains(name)) continue;
            seen.add(name);

            if (!hasBooleanPrefix(name)) {
                result.add(new NamingIssue(
                        "NM-005", "boolean 이름에 의도가 드러나야 함", "MEANING", "LOW",
                        ln, raw.stripTrailing(), name,
                        "boolean 변수 '" + name + "'이(가) 의도를 드러내는 접두사(is/has/can/should 등)를 사용하지 않았습니다. " +
                        "boolean 이름만으로 참/거짓의 의미를 파악하기 어렵습니다.",
                        "is, has, can, should, use, enable 등의 접두사를 붙여 의도를 명확히 하세요. 예: isVisible → canLogin, hasError",
                        suggestBoolName(name)
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-006: 의미가 약한 이름 경고
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM006(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        // 필드/변수 선언에서 이름 추출 (NM-003과 동일 패턴 활용)
        Matcher m3 = NM003_FIELD.matcher(line);
        Set<String> seen = new HashSet<>();
        while (m3.find()) {
            String name = m3.group(1);
            if (name == null) continue;
            String lower = name.toLowerCase();
            if (seen.contains(lower)) continue;
            seen.add(lower);
            if (WEAK_NAMES.contains(lower)) {
                result.add(new NamingIssue(
                        "NM-006", "의미가 약한 이름 경고", "MEANING", "LOW",
                        ln, raw.stripTrailing(), name,
                        "'" + name + "'은(는) 역할과 의미를 알기 어려운 이름입니다. " +
                        "이름만 보고 해당 변수의 타입이나 용도를 파악하기 어려워 리뷰와 유지보수 비용을 높입니다.",
                        "변수가 담는 값이나 역할을 드러내는 구체적인 이름으로 바꾸세요. 예: data → userList, result → validationResult",
                        null
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-007: 컬렉션 이름 품질 경고
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM007(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        Matcher m = NM007_COLLECTION.matcher(line);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            // 컬렉션 이름은 그룹 1 또는 2(Map) 또는 4(배열)
            String name = m.group(1) != null ? m.group(1)
                    : (m.group(2) != null ? m.group(2) : m.group(4));
            if (name == null || name.isBlank()) continue;
            if (IGNORE_NAMES.contains(name.toLowerCase())) continue;
            if (seen.contains(name)) continue;
            seen.add(name);

            boolean isMap = line.contains("Map") && (m.group(2) != null || line.contains("Map<"));
            if (!hasGoodCollectionName(name, isMap)) {
                String hint = isMap
                        ? "Map의 경우 역할을 나타내는 접미사(Map)를 붙이는 것을 권장합니다. 예: userMap, roleMap"
                        : "컬렉션은 복수형 이름을 사용하는 것을 권장합니다. 예: users, orderList, productIds";
                result.add(new NamingIssue(
                        "NM-007", "컬렉션 이름 품질 경고", "MEANING", "LOW",
                        ln, raw.stripTrailing(), name,
                        "컬렉션 변수 '" + name + "'의 이름이 컬렉션임을 명확히 드러내지 않습니다. " +
                        "복수 또는 용도를 담지 않은 이름은 코드 가독성을 낮춥니다.",
                        hint,
                        null
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NM-008: 축약어 과다 경고
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> applyNM008(String line, int ln, String raw) {
        List<NamingIssue> result = new ArrayList<>();
        // 식별자 후보 추출 (선언부에서 나오는 이름들)
        Pattern identPat = Pattern.compile("\\b([a-zA-Z_$][\\w$]{3,})\\b");
        Matcher m = identPat.matcher(line);
        Set<String> seen = new HashSet<>();

        while (m.find()) {
            String name = m.group(1);
            if (seen.contains(name)) continue;
            seen.add(name);
            if (IGNORE_NAMES.contains(name.toLowerCase())) continue;
            if (JAVA_KEYWORDS.contains(name)) continue;

            if (isOverAbbreviated(name)) {
                result.add(new NamingIssue(
                        "NM-008", "축약어 과다 경고", "MEANING", "LOW",
                        ln, raw.stripTrailing(), name,
                        "식별자 '" + name + "'이(가) 지나치게 많은 축약어를 포함합니다. " +
                        "축약어 과다는 코드 가독성을 낮추고 팀 외부 사람이 이해하기 어렵게 만듭니다.",
                        "각 단어를 완전히 풀어서 쓰거나 팀에서 정한 약어 사전 내에서만 축약어를 사용하세요.",
                        null
                ));
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 이름 검사 유틸
    // ─────────────────────────────────────────────────────────────────────────

    /** PascalCase: 대문자로 시작, 밑줄 없음 */
    private boolean isPascalCase(String name) {
        if (name == null || name.isEmpty()) return true;
        char first = name.charAt(0);
        if (!Character.isUpperCase(first)) return false;
        return !name.contains("_");
    }

    /** camelCase: 소문자로 시작, 밑줄 없음 */
    private boolean isCamelCase(String name) {
        if (name == null || name.isEmpty()) return true;
        char first = name.charAt(0);
        if (Character.isUpperCase(first)) return false;
        return !name.contains("_");
    }

    /** UPPER_SNAKE_CASE: 모두 대문자, 숫자, 밑줄만 */
    private boolean isUpperSnakeCase(String name) {
        if (name == null || name.isEmpty()) return true;
        return name.matches("[A-Z][A-Z0-9_]*");
    }

    /** boolean 이름 접두사 검사 */
    private boolean hasBooleanPrefix(String name) {
        String lower = name.toLowerCase();
        return BOOL_PREFIXES.stream().anyMatch(lower::startsWith);
    }

    /**
     * 컬렉션 이름 품질:
     * - List/Set/Queue/Deque: 복수형('s' 끝) 또는 List/Set/Collection 접미사 권장
     * - Map: Map 접미사 권장
     */
    private boolean hasGoodCollectionName(String name, boolean isMap) {
        String lower = name.toLowerCase();
        if (isMap) {
            return lower.endsWith("map") || lower.endsWith("cache") || lower.endsWith("registry")
                    || lower.endsWith("table") || lower.endsWith("index");
        }
        // List/Set 계열: 복수형 또는 접미사
        return lower.endsWith("s") || lower.endsWith("list") || lower.endsWith("set")
                || lower.endsWith("queue") || lower.endsWith("array")
                || lower.endsWith("collection") || lower.endsWith("ids")
                || lower.endsWith("codes") || lower.endsWith("types");
    }

    /**
     * 과도한 축약어 탐지.
     * camelCase에서 1~3글자짜리 단어 토큰이 전체의 50% 이상이고 토큰이 4개 이상이면 과다.
     */
    private boolean isOverAbbreviated(String name) {
        // 단어 토큰 분리 (camelCase 기준)
        String[] tokens = name.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|_");
        if (tokens.length < 4) return false;
        long shortTokens = Arrays.stream(tokens).filter(t -> t.length() <= 3).count();
        return shortTokens >= tokens.length * 0.6;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 이름 변환 유틸 (suggestedName용)
    // ─────────────────────────────────────────────────────────────────────────

    /** snake_case / UPPER_SNAKE_CASE → PascalCase */
    private String toPascalCase(String name) {
        if (name == null || name.isEmpty()) return name;
        String[] parts = name.split("[_\\s]+");
        return Arrays.stream(parts)
                .filter(p -> !p.isEmpty())
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1).toLowerCase())
                .collect(Collectors.joining());
    }

    /** snake_case / PascalCase → camelCase */
    private String toCamelCase(String name) {
        String pascal = toPascalCase(name);
        if (pascal.isEmpty()) return pascal;
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    /** camelCase / PascalCase → UPPER_SNAKE_CASE */
    private String toUpperSnakeCase(String name) {
        if (name == null || name.isEmpty()) return name;
        // camelCase → UPPER_SNAKE
        return name.replaceAll("([a-z])([A-Z])", "$1_$2")
                   .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                   .replace("-", "_")
                   .toUpperCase();
    }

    /** boolean용 접두사 제안 */
    private String suggestBoolName(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("visible") || lower.contains("shown") || lower.contains("display"))
            return "is" + capitalize(name);
        if (lower.contains("error") || lower.contains("fail") || lower.contains("invalid"))
            return "has" + capitalize(name);
        if (lower.contains("login") || lower.contains("access") || lower.contains("edit"))
            return "can" + capitalize(name);
        return "is" + capitalize(name);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 기타 유틸
    // ─────────────────────────────────────────────────────────────────────────

    private List<NamingIssue> deduplicate(List<NamingIssue> issues) {
        Set<String> keys = new LinkedHashSet<>();
        List<NamingIssue> result = new ArrayList<>();
        for (NamingIssue issue : issues) {
            String key = issue.ruleId() + ":" + issue.lineNumber() + ":" + issue.matchedText();
            if (keys.add(key)) result.add(issue);
        }
        return result;
    }

    private static int sevOrder(NamingIssue issue) {
        return switch (issue.severity()) {
            case "HIGH"   -> 3;
            case "MEDIUM" -> 2;
            case "LOW"    -> 1;
            default       -> 0;
        };
    }

    private NamingCheckResponse empty() {
        return new NamingCheckResponse(List.of(), 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
