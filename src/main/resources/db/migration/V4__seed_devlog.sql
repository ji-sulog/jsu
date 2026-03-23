-- ── devlog 전체 데이터 ────────────────────────────────────────────────────────
-- 트러블슈팅 → 플랫폼 정리 → 구조 개선 → 유지보수 → 세션/공지 → 버그수정 순

-- ① 트러블슈팅 기록 ───────────────────────────────────────────────────────────

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'Thymeleaf fragment 파라미터에 = 기호 포함 시 TemplateInputException 발생',
  '문제:
sql.html에서 topbarFull fragment를 호출할 때 guideAction 파라미터로
document.getElementById(''guideModal'').style.display=''flex'' 를 넘겼더니
TemplateInputException: Could not parse as expression 오류 발생.

원인:
Thymeleaf 3.1 파서가 fragment 파라미터 내 = 기호를 named parameter 구분자로
오해하여 파싱 실패.

해결:
파라미터에서 = 가 포함된 인라인 표현식을 제거하고,
대신 openGuideModal() 같은 단순 함수명만 전달한 뒤
해당 페이지 스크립트에 래퍼 함수를 별도로 정의하는 방식으로 우회.',
  'Thymeleaf,Fragment,파싱오류',
  TIMESTAMP '2026-03-01 08:17:00',
  TIMESTAMP '2026-03-01 08:17:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'Thymeleaf fragment 파라미터에 = 기호 포함 시 TemplateInputException 발생');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  '플로팅 채팅 위젯이 th:replace 페이지에서 렌더링 안 되는 문제',
  '문제:
sidebar.html에 채팅 위젯 HTML을 추가했는데 각 도구 페이지에서 위젯이 표시되지 않음.

원인:
th:replace="~{fragments/sidebar :: sidebarFull(...)}" 는 sidebarFull fragment 블록만
가져오는데, 위젯 HTML이 </aside> 바깥에 위치해 있어서 fragment 범위에 포함되지 않았음.

해결:
위젯 HTML 전체(FAB 버튼 + 패널 + script)를 </aside> 닫는 태그 안쪽으로 이동.
position: fixed 이므로 DOM 위치와 무관하게 뷰포트 기준으로 고정 표시됨.',
  'Thymeleaf,Fragment,CSS,플로팅위젯',
  TIMESTAMP '2026-03-01 09:24:00',
  TIMESTAMP '2026-03-01 09:24:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = '플로팅 채팅 위젯이 th:replace 페이지에서 렌더링 안 되는 문제');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'H2 → PostgreSQL 전환 시 data.sql 중복 삽입 문제',
  '문제:
H2에서 PostgreSQL로 DB를 전환한 뒤 앱을 재시작할 때마다
tool, notice 테이블에 데이터가 중복으로 쌓이는 현상 발생.

원인:
data.sql의 ON CONFLICT DO NOTHING은 특정 컬럼의 UNIQUE 제약 충돌 시에만 동작하는데,
id는 BIGSERIAL(auto-increment)이라 재시작마다 새로운 id로 삽입되어
충돌이 발생하지 않았음.

해결:
data.sql 방식을 완전히 제거하고 Flyway 기반 마이그레이션으로 전환.
V2 시드 파일에서 INSERT ... SELECT ... WHERE NOT EXISTS (SELECT 1 FROM tool WHERE sort_order = N)
패턴을 사용해 sort_order 기준으로 중복 방지.',
  'PostgreSQL,data.sql,중복삽입,Flyway',
  TIMESTAMP '2026-03-08 11:48:00',
  TIMESTAMP '2026-03-08 11:48:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'H2 → PostgreSQL 전환 시 data.sql 중복 삽입 문제');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'Spring Boot 4.0 + Flyway 11 auto-configuration 미작동 문제',
  '문제:
application.yaml에 spring.flyway.enabled: true 설정 후 재시작해도
flyway_schema_history 테이블이 생성되지 않고 V1, V2 마이그레이션이 실행되지 않음.
Flyway 관련 로그도 전혀 출력되지 않음 (DEBUG 레벨에서도).

원인:
Spring Boot 4.0.3에서 Flyway auto-configuration의 @ConditionalOn* 조건 중
일부가 충족되지 않아 FlywayAutoConfiguration 자체가 skip됨.
정확한 원인을 확정하진 못했지만,
당시에는 Spring Boot 4.0 초기 릴리즈와 Flyway 11.x 조합의 연동 문제 가능성을 높게 봤음.

해결:
auto-configuration에 의존하지 않고 FlywayConfig.java에서 @Bean으로 직접 등록.

  @Bean(initMethod = "migrate")
  public Flyway flyway(DataSource dataSource, ...) {
      return Flyway.configure()
          .dataSource(dataSource)
          .locations("classpath:db/migration")
          .baselineOnMigrate(true)
          .baselineVersion("0")
          .load();
  }

spring.flyway.enabled: false 로 auto-config는 비활성화하고
명시적 Bean이 Flyway를 전담하도록 구성.',
  'SpringBoot4,Flyway,AutoConfiguration,마이그레이션',
  TIMESTAMP '2026-03-08 15:10:00',
  TIMESTAMP '2026-03-08 15:10:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'Spring Boot 4.0 + Flyway 11 auto-configuration 미작동 문제');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'Flyway baseline-version 설정 오류로 V1, V2 마이그레이션 건너뜀',
  '문제:
baseline-version: 2로 설정했더니 flyway_schema_history에 version 2 베이스라인만
기록되고 V1, V2 마이그레이션이 실행되지 않아 테이블이 생성되지 않음.

원인:
baseline-on-migrate: true + baseline-version: 2 조합은
"현재 DB를 version 2 상태로 간주하겠다"는 의미이므로
V1, V2는 이미 적용된 것으로 처리되어 건너뜀.

해결:
baseline-version: 0 으로 변경.
version 0 = 아무것도 없는 초기 상태로 간주하고
V1부터 순서대로 실행되도록 수정.
flyway_schema_history 테이블과 기존 데이터 정리 후 재시작으로 해결.',
  'Flyway,baseline,마이그레이션설정',
  TIMESTAMP '2026-03-08 16:10:00',
  TIMESTAMP '2026-03-08 16:10:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'Flyway baseline-version 설정 오류로 V1, V2 마이그레이션 건너뜀');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'V3 마이그레이션 파일 중복으로 앱 시작 실패',
  '문제:
앱 재시작 시 "Found more than one migration with version 3" 오류로
ApplicationContext 로드 실패.

원인:
Q&A 게시판으로 설계를 변경하면서 V3__create_qna_tables.sql을 새로 만들었는데
기존 V3__create_chat_tables.sql을 삭제하지 않아 동일 버전 파일이 2개 공존.
build/resources/main/ 하위에도 캐시된 파일이 남아 있어 빌드 디렉토리까지
함께 삭제해야 해결됨.

해결:
src/main/resources/db/migration/V3__create_chat_tables.sql 삭제
build/resources/main/db/migration/V3__create_chat_tables.sql 삭제
이후 정상 재시작 확인.',
  'Flyway,마이그레이션,파일충돌,빌드캐시',
  TIMESTAMP '2026-03-08 17:00:00',
  TIMESTAMP '2026-03-08 17:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'V3 마이그레이션 파일 중복으로 앱 시작 실패');

-- ② 플랫폼 정리 기록 ──────────────────────────────────────────────────────────

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'Spring Boot 4 → 3.3 다운그레이드로 플랫폼 안정화',
  '문제:
MVP 단계에서 Spring Boot 4.0.3 기반으로 올려 둔 상태였는데,
web + security + thymeleaf + jpa + flyway 조합에서
contextLoads 테스트가 반복적으로 실패했다.

대표 증상:
- WebMvcSecurityConfiguration 초기화 중 ClassNotFoundException 발생
- HttpMessageConverters$ServerBuilder 타입을 찾지 못해 앱 시작 실패

원인:
현재 조합에서는 최신 버전을 유지하는 이점보다
실행 안정성과 호환성 확보가 더 중요했다.
특히 web + security + thymeleaf + jpa + flyway 조합은
Spring Boot 3 계열이 더 검증된 선택지라고 판단했다.

해결:
- build.gradle의 Spring Boot 버전을 3.3.10으로 조정
- 실제로 쓰지 않는 의존성(webservices, websocket, springdoc, modulith, restdocs 등) 제거
- 최소 의존성 조합만 남겨 빌드 구성을 단순화
- ./gradlew test 통과 확인

효과:
플랫폼 자체의 불안정성 때문에 막히는 구간이 줄었고,
이후 리팩터링을 더 안전하게 진행할 수 있는 바닥을 만들었다.',
  'SpringBoot3,플랫폼정리,의존성정리,안정화',
  TIMESTAMP '2026-03-15 15:00:00',
  TIMESTAMP '2026-03-15 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'Spring Boot 4 → 3.3 다운그레이드로 플랫폼 안정화');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'Q&A / DevLog 컨트롤러-서비스 경계 정리 및 공통 예외 처리 도입',
  '문제:
Q&A와 DevLog API는 컨트롤러가 너무 많은 일을 하고 있었다.

기존 문제:
- Map<String, Object> / Map<String, String> 형태로 요청과 응답을 직접 다룸
- 컨트롤러에서 관리자 권한 체크, 입력 검증, 에러 응답 생성까지 모두 처리
- 같은 예외 처리 코드가 컨트롤러마다 반복됨

해결:
1. Q&A, DevLog 모두 요청/응답 DTO(record) 도입
2. 입력 검증과 관리자 권한 체크를 서비스로 이동
3. 서비스는 예외를 던지고, 컨트롤러는 요청 전달과 응답 반환만 담당하도록 정리
4. ApiExceptionHandler를 추가해 공통 예외 처리로 통합

효과:
- 컨트롤러는 더 짧고 단순해짐
- 서비스는 비즈니스 규칙을 담당
- 에러 응답 형식이 한 곳에서 통일됨
- 이후 Compare, Scan 같은 다른 기능에도 같은 패턴을 적용하기 쉬워짐

검증:
구조 변경 후에도 ./gradlew test 통과 확인.',
  '리팩터링,Controller,Service,DTO,예외처리',
  TIMESTAMP '2026-03-15 16:00:00',
  TIMESTAMP '2026-03-15 16:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'Q&A / DevLog 컨트롤러-서비스 경계 정리 및 공통 예외 처리 도입');

-- ③ 구조 개선 기록 ────────────────────────────────────────────────────────────

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'ApiExceptionHandler 전역 적용 및 공통 예외 계층 도입',
  '배경:
ApiExceptionHandler가 @ControllerAdvice(assignableTypes = {QnaController.class, DevLogController.class})로
선언되어 있어 ScanController, CompareController, SqlController, NamingController에서 발생하는
예외는 처리되지 않고 500 Internal Server Error가 그대로 클라이언트에 전달되는 문제가 있었다.

추가 문제:
QnaBadRequestException, DevLogBadRequestException, QnaAccessDeniedException,
DevLogAccessDeniedException 4개의 모듈별 예외가 독립적으로 존재했는데,
새 모듈이 추가될 때마다 ApiExceptionHandler에 핸들러를 추가해야 했다.

해결:
1. common 패키지에 BadRequestException, ForbiddenException 공통 부모 클래스 생성
2. 기존 예외 4개를 공통 부모 클래스 상속으로 변경 (서비스 코드 변경 없음)
3. ApiExceptionHandler의 assignableTypes 제거 → 전역 @ControllerAdvice로 변경
4. 핸들러 파라미터를 부모 타입으로 단순화 (4개 → 2개 핸들러)
5. IllegalArgumentException → 404 매핑 핸들러 제거
   (전역화 시 의도치 않은 404 응답 발생 위험 제거)

효과:
- 새 모듈 추가 시 공통 부모 예외를 상속한 예외만 만들면 핸들러 수정 없이 자동 처리
- 예외 처리가 누락된 컨트롤러 없음
- 서비스 코드 전혀 변경하지 않고 핸들러만 교체하는 최소 침습 방식으로 해결',
  '리팩터링,예외처리,ControllerAdvice,공통화',
  TIMESTAMP '2026-03-20 10:00:00',
  TIMESTAMP '2026-03-20 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'ApiExceptionHandler 전역 적용 및 공통 예외 계층 도입');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'FileExtractService를 compare 패키지에서 common 패키지로 이동',
  '배경:
파일 텍스트 추출 기능(txt, md, docx, pptx, pdf 지원)이 compare 패키지 하위에 있었다.
처음에는 요구사항 비교 도구에서만 사용했기 때문에 그 위치가 자연스러웠지만,
이후 scan, sql, naming 도구에서도 파일 업로드 기능이 생기면서 문제가 됐다.

문제:
- FileExtractService가 compare.service 패키지에 있어 다른 모듈에서 쓰기 위해
  compare 패키지를 import해야 하는 부자연스러운 의존 방향이 생김
- 구조만 봐서는 해당 서비스가 공통 유틸인지 compare 전용인지 판단 불가

해결:
- com.jjp.jsu.compare.service.FileExtractService
  → com.jjp.jsu.common.FileExtractService 로 이동
- CompareController의 import 경로 수정
- 기존 파일 삭제

효과:
- 파일이 어느 패키지에 있는지만 봐도 공통 유틸임을 즉시 파악 가능
- 어떤 도구에서도 common.FileExtractService를 주입해 바로 사용 가능',
  '리팩터링,패키지구조,FileExtract,common',
  TIMESTAMP '2026-03-20 11:00:00',
  TIMESTAMP '2026-03-20 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'FileExtractService를 compare 패키지에서 common 패키지로 이동');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  '공통 JS 유틸(common.js) 도입으로 HTML 중복 코드 제거',
  '배경:
scan, compare, sql, naming 4개의 도구 HTML 파일 각각에 동일한 JS 함수가 중복 선언되어 있었다.

중복 내역:
- escHtml(s): HTML 특수문자 이스케이프 함수가 4개 파일 모두에 각각 선언됨
- 알림 모달: scan에는 fileModal+showModal/closeModal, naming에는 alertModal+showAlert/closeAlert,
  sql에는 native alert() 3가지 방식이 혼재
- 중복 선언으로 인해 한 곳의 버그를 수정해도 나머지 파일에 반영이 안 되는 위험 존재

해결:
- /static/js/common.js 신규 생성
  - escHtml(s): 통합 이스케이프 함수
  - setSpinner(show): id=spinner 스피너 제어
  - showAlert(icon, title, msg) / closeAlert(): DOM에 자동 주입되는 공통 알림 모달
- 4개 HTML에서 중복 함수 제거 후 <script src="/js/common.js"> 추가
- sql.html의 native alert() 4곳을 showAlert()로 교체해 UI 일관성 확보

효과:
- 알림 모달 스타일/동작 수정 시 common.js 한 곳만 수정하면 전 페이지 반영
- escHtml 버그 발생 시 단일 지점에서 수정 가능
- 새 도구 페이지 추가 시 common.js 한 줄 include로 유틸 즉시 사용 가능',
  '리팩터링,JavaScript,공통화,중복제거,common.js',
  TIMESTAMP '2026-03-20 13:00:00',
  TIMESTAMP '2026-03-20 13:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = '공통 JS 유틸(common.js) 도입으로 HTML 중복 코드 제거');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'chat 패키지를 qna 패키지로 리네이밍',
  '배경:
채팅 형식의 Q&A 기능을 담당하는 패키지 이름이 chat이었다.
그러나 실제 기능은 "질문-답변 게시판" 성격에 가까워
패키지 이름과 기능 의도 사이에 불일치가 있었다.

문제:
- 새로운 팀원이 코드를 볼 때 chat이라는 이름으로는 실시간 채팅인지,
  LLM 대화창인지, Q&A 게시판인지 구분하기 어려움
- 도메인 언어(ubiquitous language) 관점에서 qna가 훨씬 정확

해결:
- com.jjp.jsu.chat → com.jjp.jsu.qna 패키지 전체 이동 (19개 파일)
- 패키지 선언 및 내부 import 경로 일괄 교체
- 기존 chat/ 디렉터리 삭제

효과:
- 패키지 이름만 보고 기능 파악 가능 (명확한 도메인 언어 사용)
- 향후 chat(실시간 채팅) 기능 추가 시 이름 충돌 없음',
  '리팩터링,패키지구조,리네이밍,qna',
  TIMESTAMP '2026-03-20 14:00:00',
  TIMESTAMP '2026-03-20 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'chat 패키지를 qna 패키지로 리네이밍');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'icon_type 컬럼 도입으로 sidebar·index 아이콘 매핑 안정화',
  '배경:
sidebar.html과 index.html에서 도구 아이콘을 표시할 때
tool.id(PK, 1·2·3·4...)를 직접 비교해 SVG를 선택하는 방식을 사용했다.

문제:
- DB에 새 도구가 INSERT되거나 순서가 바뀌면 id 값이 달라져
  아이콘 매핑이 틀어짐
- naming, sql 도구가 ACTIVE 상태로 전환됐을 때
  ACTIVE 카드 블록에 해당 SVG가 없어 아이콘이 빈칸으로 표시되는 버그 발생
- id는 DB 내부 식별자일 뿐, 도구 종류를 의미하지 않음

해결:
1. tool 테이블에 icon_type VARCHAR(50) 컬럼 추가
   - compare, scan, sql, naming, api-spec, log-analyzer 값 설정
2. portal/Tool.java에 iconType 필드(@Column) 추가
3. sidebar.html: th:if="tool.id == N" → th:if="tool.iconType == ''compare''" 등으로 전환
4. index.html: ACTIVE 카드 블록에 sql, naming SVG 추가 및 iconType 기반으로 통일

효과:
- 도구 순서 변경, 신규 도구 추가 시 아이콘 매핑이 깨지지 않음
- 아이콘 누락 버그 해결 (naming, sql 카드 아이콘 정상 노출)
- iconType 값만 보면 어떤 도구인지 코드 레벨에서도 명확히 파악 가능',
  '버그수정,아이콘,icon_type,Flyway,Thymeleaf,리팩터링',
  TIMESTAMP '2026-03-20 15:00:00',
  TIMESTAMP '2026-03-20 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'icon_type 컬럼 도입으로 sidebar·index 아이콘 매핑 안정화');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'AdminAuthService 공통화 및 관리자 비밀번호 환경변수 오버라이드 패턴 도입',
  '배경:
관리자 인증 로직(비밀번호 검증)이 QnaService, DevLogService 두 곳에
@Value("$${chat.admin-password}") 필드와 isAdmin() 메서드로 각각 중복 구현되어 있었다.

추가 문제:
- 비밀번호가 application.yaml에 평문으로 하드코딩(admin1234)되어
  운영 환경에서 변경하려면 소스 코드를 수정해야 했음
- 프로퍼티 키가 chat.admin-password로 qna 기능에 종속된 이름이었음

해결:
1. common 패키지에 AdminAuthService 생성
   - @Value("$${app.admin-password}")로 비밀번호 주입
   - isAdmin(password): boolean 반환
   - validateAdmin(password): 실패 시 ForbiddenException throw
2. QnaService, DevLogService에서 @Value 필드 제거 → AdminAuthService 주입
3. application.yaml 키 변경:
   chat.admin-password: admin1234
   → app.admin-password: $${APP_ADMIN_PASSWORD:admin1234}
   (환경변수 APP_ADMIN_PASSWORD 설정 시 자동 오버라이드)

효과:
- 관리자 인증 로직 단일 지점 관리 (새 서비스 추가 시 AdminAuthService 주입만 하면 됨)
- 운영 환경에서 소스 수정 없이 환경변수로 비밀번호 교체 가능
- 프로퍼티 키가 app.* 네임스페이스로 통일되어 설정 파악이 쉬워짐',
  '리팩터링,공통화,보안,AdminAuthService,환경변수,Spring',
  TIMESTAMP '2026-03-20 16:00:00',
  TIMESTAMP '2026-03-20 16:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'AdminAuthService 공통화 및 관리자 비밀번호 환경변수 오버라이드 패턴 도입');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  '규칙 엔진 단위 테스트 도입 (ScanService, NamingService, SqlQualityService)',
  '배경:
세 가지 핵심 규칙 엔진(하드코딩 탐지, 네이밍 컨벤션 검사, SQL 품질 검사)에
테스트 코드가 전혀 없었다. 규칙 추가·수정 시 기존 규칙이 망가져도
빌드 단계에서 알 수 없는 상태였다.

해결:
JUnit 5 + AssertJ로 3개 테스트 클래스 작성

1. ScanServiceTest (12개 케이스)
   - HC-001: 템플릿 ID 직접 비교 (탐지 1, 미탐지 1)
   - HC-002: 상태코드 문자열 직접 비교 (탐지 2, 미탐지 1)
   - HC-003: 역할명 직접 비교 (탐지 1, 미탐지 1)
   - HC-005: 매직 넘버 (탐지 1, 미탐지 1)
   - HC-006: URL·경로 하드코딩 (탐지 2, 미탐지 1)
   - HC-008: 승인 단계값 직접 사용 (탐지 1, 미탐지 1)

2. NamingServiceTest (14개 케이스)
   - NM-001: 타입 이름 PascalCase (탐지 2, 미탐지 2)
   - NM-002: 메서드명 camelCase (탐지 1, 미탐지 1)
   - NM-003: 필드/변수명 camelCase (탐지 1, 미탐지 1)
   - NM-004: 상수 UPPER_SNAKE_CASE (탐지 1, 미탐지 1)
   - NM-005: boolean 접두사 (탐지 1, 미탐지 2)
   - NM-006: 의미가 약한 이름 (탐지 2, 미탐지 1)

3. SqlQualityServiceTest (14개 케이스)
   - SQ01: SELECT * (탐지 1, 미탐지 1)
   - SQ02: WHERE 없는 UPDATE (탐지 1, 미탐지 1)
   - SQ03: WHERE 없는 DELETE (탐지 1, 미탐지 1)
   - SQ04: 앞 와일드카드 LIKE (탐지 1, 미탐지 1)
   - SQ05: 페이지네이션 누락 (탐지 1, 미탐지 1)
   - SQ06: 서브쿼리 중첩 (탐지 1, 미탐지 1)
   - SQ07: OR 조건 과다 (탐지 1, 미탐지 1)

테스트 구조:
- @Nested + @DisplayName으로 규칙별 논리 그룹화
- hasRule() 헬퍼로 반복 코드 최소화
- 스프링 컨텍스트 미사용 → 빠른 단위 테스트

효과:
- 규칙 수정 시 기존 탐지·미탐지 케이스 즉시 회귀 검증 가능
- 새 규칙 추가 시 테스트 케이스 함께 작성하는 문화 정착 기반 마련',
  '테스트,JUnit5,AssertJ,단위테스트,규칙엔진,ScanService,NamingService,SqlQualityService',
  TIMESTAMP '2026-03-20 17:00:00',
  TIMESTAMP '2026-03-20 17:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = '규칙 엔진 단위 테스트 도입 (ScanService, NamingService, SqlQualityService)');

-- ④ 유지보수 개선 기록 ────────────────────────────────────────────────────────

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'DB 자격증명을 환경변수 오버라이드 패턴으로 전환',
  '배경:
application.yaml에 DB 접속 정보(url, username, password)가 평문으로 하드코딩되어 있었다.
app.admin-password는 이미 환경변수 오버라이드 패턴을 적용했지만
DB 자격증명은 누락된 상태로 설정 관리 방식이 일관되지 않았다.

문제:
- 운영/개발 환경 전환 시 소스 코드를 직접 수정해야 하는 구조
- Git 히스토리에 DB 비밀번호가 그대로 노출될 위험

해결:
- url:      jdbc:postgresql://localhost:5432/jsudb  →  $${DB_URL:jdbc:postgresql://localhost:5432/jsudb}
- username: jsu                                     →  $${DB_USERNAME:jsu}
- password: jsu1234                                 →  $${DB_PASSWORD:jsu1234}
  (환경변수 미설정 시 기존 로컬 기본값으로 폴백하므로 기존 개발 환경 영향 없음)

효과:
- DB_URL / DB_USERNAME / DB_PASSWORD 환경변수만 주입하면 소스 수정 없이 운영 DB 연결 가능
- app.admin-password와 동일한 오버라이드 패턴으로 설정 관리 방식 통일',
  '설정,보안,환경변수,DB자격증명,application.yaml',
  TIMESTAMP '2026-03-21 10:00:00',
  TIMESTAMP '2026-03-21 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'DB 자격증명을 환경변수 오버라이드 패턴으로 전환');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'ScanController 집계 로직을 ScanResponse 정적 팩토리로 이동',
  '배경:
ScanController.doScan()에서 ScanService로부터 List<ScanItem>을 받은 뒤
severity별 카운트(high/medium/low/info)를 컨트롤러가 직접 집계하고 있었다.

문제:
- 컨트롤러에 비즈니스 집계 로직이 존재 → 컨트롤러는 얇게 유지해야 한다는 원칙 위반
- 집계 로직이 컨트롤러 안에 있어서 ScanService 단위 테스트만으로는 카운트 검증 불가
- 다른 진입점(예: 배치)에서 ScanService를 호출할 때 카운트 로직을 중복 작성해야 하는 위험

해결:
- ScanResponse record에 정적 팩토리 메서드 of(List<ScanItem>) 추가
  → 내부에서 severity별 카운트 집계 후 ScanResponse 생성
- ScanController: 5줄 집계 코드 → ScanResponse.of(scanService.scan(request)) 1줄로 단순화
- ScanService.scan()은 List<ScanItem> 반환을 그대로 유지
  → 기존 단위 테스트 영향 없음

효과:
- 컨트롤러가 순수 라우팅 역할만 담당
- 집계 로직을 ScanResponse 단위로 독립 테스트 가능
- 향후 카운트 로직 변경 시 ScanResponse 한 곳만 수정',
  '리팩터링,ScanController,ScanResponse,정적팩토리,단일책임',
  TIMESTAMP '2026-03-21 11:00:00',
  TIMESTAMP '2026-03-21 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'ScanController 집계 로직을 ScanResponse 정적 팩토리로 이동');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  '모듈별 예외 클래스 제거 및 공통 예외 직접 사용으로 통일',
  '배경:
ApiExceptionHandler를 전역화하면서 공통 부모 예외(BadRequestException, ForbiddenException)를
도입했지만, 기존 모듈별 예외 클래스 4개가 그대로 남아 있었다.
- QnaBadRequestException extends BadRequestException
- QnaAccessDeniedException extends ForbiddenException
- DevLogBadRequestException extends BadRequestException
- DevLogAccessDeniedException extends ForbiddenException

문제:
- 핸들러가 부모 타입으로만 처리하므로 자식 타입이 실질적인 기능 차이를 주지 않음
- scan, sql, naming, compare 모듈에는 모듈별 예외가 없어서
  "새 모듈 추가 시 예외 클래스를 만들어야 하나?" 판단 기준이 불명확
- 파일만 늘어나고 관리 부담만 가중

해결:
- QnaService: QnaBadRequestException → BadRequestException,
              QnaAccessDeniedException → ForbiddenException 직접 사용으로 교체
- DevLogService: DevLogBadRequestException → BadRequestException 직접 사용으로 교체
- 모듈별 예외 클래스 4개 파일 삭제

효과:
- 예외 계층이 common(BadRequestException, ForbiddenException) 두 클래스로 단일화
- 새 모듈 추가 시 모듈별 예외 클래스 불필요 → 공통 예외 직접 throw하면 됨
- 파일 수 감소로 패키지 구조 단순화',
  '리팩터링,예외처리,패키지구조,단순화,BadRequestException,ForbiddenException',
  TIMESTAMP '2026-03-21 12:00:00',
  TIMESTAMP '2026-03-21 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = '모듈별 예외 클래스 제거 및 공통 예외 직접 사용으로 통일');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'NamingController 파일 업로드를 FileExtractService로 통일 및 지원 형식 명시',
  '배경:
compare 도구의 파일 업로드는 common.FileExtractService를 사용하지만
naming 도구는 컨트롤러에서 file.getBytes()로 직접 읽는 독자 처리 방식을 쓰고 있었다.
FileExtractService를 common으로 이동한 이유가 정확히 이 재사용을 위해서였는데 미적용 상태였음.

추가 문제:
- 지원 형식 검사 조건이 인라인 문자열 비교(ext.equals(".java"))로 흩어져 있어
  형식 추가/제거 시 코드 파악이 어려움

해결:
1. NamingController에 FileExtractService 주입
2. ALLOWED_EXTENSIONS = Set.of(".java", ".txt") 상수로 허용 형식 명시적 선언
   - 네이밍 검사는 소스 코드(텍스트) 기반이므로 .java, .txt만 허용
   - docx/pptx/pdf는 코드 텍스트가 보장되지 않아 의도적으로 제외 (주석으로 사유 명시)
3. 파일 텍스트 추출 → fileExtractService.extract(file) 위임
4. java.nio.charset.StandardCharsets import 제거 (불필요해짐)

효과:
- 파일 업로드 처리 방식이 compare/naming 양쪽 모두 FileExtractService로 통일
- 허용 형식이 상수로 선언되어 변경 시 단일 지점만 수정
- 향후 scan 도구 파일 업로드 추가 시에도 동일 패턴 적용 가능',
  '리팩터링,NamingController,FileExtractService,파일업로드,공통화,지원형식',
  TIMESTAMP '2026-03-21 13:00:00',
  TIMESTAMP '2026-03-21 13:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'NamingController 파일 업로드를 FileExtractService로 통일 및 지원 형식 명시');

-- ⑤ 세션 인증 통합 / 공지 IDU ────────────────────────────────────────────────

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  '통합 관리자 세션 인증 구현 (devlog · qna · notice 공유)',
  '기존 X-Admin-Password 헤더 방식(무상태)을 HttpSession 기반 서버 세션 방식으로 전환하였다.

[변경 내용]
- AdminController 신규 생성 (POST /api/admin/login, POST /api/admin/logout, GET /api/admin/status)
- AdminAuthService에 login / logout / isLoggedIn / requireLogin 메서드 추가
- DevLogController, QnaController, NoticeController 모두 동일한 requireLogin(session) 호출로 통일
- 한 번 로그인하면 devlog / qna / notice 어느 페이지에서든 관리자 상태 유지
- 어느 페이지에서 로그아웃해도 서버 세션 전체 무효화 (session.invalidate())

[Front-end 변경]
- devlog.html: _adminPw 변수 제거, /api/admin/login·logout·status 호출로 교체
- fragments/sidebar.html (QnA 위젯): 동일하게 세션 기반 전환, sessionStorage 제거
- index.html: 페이지 로드 시 /api/admin/status 확인 → 관리자 컨트롤 자동 표시

[효과]
- 비밀번호가 HTTP 헤더에 매 요청마다 노출되던 문제 해소
- 로그인 상태가 브라우저 탭/페이지 간에 서버 세션으로 일관되게 공유됨',
  '세션인증,AdminController,HttpSession,로그인통합,Spring',
  TIMESTAMP '2026-03-21 14:00:00',
  TIMESTAMP '2026-03-21 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = '통합 관리자 세션 인증 구현 (devlog · qna · notice 공유)');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  '공지 관리 IDU REST API 구현 (NoticeController · NoticeService)',
  '대시보드 공지를 Flyway SQL로만 관리하던 구조에서 REST API를 통해 런타임에 등록/수정/삭제할 수 있도록 개선하였다.

[추가된 엔드포인트]
- GET    /api/notice        — 전체 공지 목록 (인증 불필요)
- POST   /api/notice        — 공지 등록 (관리자 세션 필요)
- PUT    /api/notice/{id}   — 공지 수정 (관리자 세션 필요)
- DELETE /api/notice/{id}   — 공지 삭제 (관리자 세션 필요)

[도메인 변경]
- Notice 엔티티에 생성자 및 update() 메서드 추가
- NoticeRequest DTO 신규 생성 (type, title, body, displayDate, sortOrder, pinned)
- NoticeService에 create / update / delete 및 validate / nextSortOrder 구현

[index.html 관리 UI]
- 섹션 헤더에 관리자 로그인 버튼 및 공지 추가 버튼 노출 (세션 상태 연동)
- 각 공지 아이템에 수정/삭제 인라인 버튼 추가 (관리자 로그인 시에만 표시)
- 공지 등록/수정 모달 구현 (type, title, body, displayDate, sortOrder, pinned 입력)
- 처리 완료 후 location.reload()로 Thymeleaf 서버 렌더링 갱신',
  '공지관리,NoticeController,NoticeService,IDU,REST,index',
  TIMESTAMP '2026-03-21 15:00:00',
  TIMESTAMP '2026-03-21 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = '공지 관리 IDU REST API 구현 (NoticeController · NoticeService)');

-- ⑥ 버그 수정 ─────────────────────────────────────────────────────────────────

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'QnA 위젯 관리자 세션 미반영 버그 수정 (race condition)',
  '배경:
devlog·공지 페이지는 관리자 로그인 후 새 페이지가 열릴 때 /api/admin/status를
호출하므로 세션 공유가 정상 동작했다.
그러나 QnA 위젯(플로팅 FAB)은 같은 페이지 내에서 JavaScript로 열리는 구조라
별도의 세션 확인 타이밍이 없었다.

문제:
- 페이지 로드 시 /api/admin/status를 비동기(fetch)로 호출해 _isAdmin을 설정
- 그런데 fetch 응답이 도착하기 전에 사용자가 FAB 버튼을 클릭하면
  _isAdmin이 아직 false인 상태로 qnaShowList()가 실행됨
- 결과: 관리자로 로그인된 세션임에도 공개 목록만 표시되고
  관리자 UI(전체 목록, 답변 입력창, 로그아웃 버튼)가 나타나지 않음

원인 분류:
Race condition — 비동기 상태 초기화와 UI 이벤트 처리 순서가 보장되지 않는 문제

해결:
- qnaToggle() 함수(패널 열기) 내부에서 직접 /api/admin/status를 호출
- 응답 완료 후 _isAdmin을 갱신하고 qnaShowList() 실행
- 페이지 로드 시점의 초기 fetch 제거 (패널 열 때마다 확인하므로 불필요)

효과:
- FAB 버튼을 클릭하는 시점에 항상 최신 세션 상태를 서버에서 확인
- devlog·공지·QnA 세 화면 모두 동일한 서버 세션 기준으로 관리자 상태 공유',
  '버그수정,QnA,세션,race-condition,비동기,JavaScript,sidebar',
  TIMESTAMP '2026-03-21 16:00:00',
  TIMESTAMP '2026-03-21 16:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'QnA 위젯 관리자 세션 미반영 버그 수정 (race condition)');

INSERT INTO devlog (title, content, tags, created_at, updated_at)
SELECT
  'DiffService에 PriorityService 직접 주입 (CompareController 의존 제거)',
  '배경:
CompareController가 DiffService와 PriorityService를 모두 주입받아
diff() 호출 시 PriorityService를 직접 인자로 넘겨주고 있었다.

문제:
- 컨트롤러가 두 서비스의 협력 방식(DiffService가 PriorityService를 필요로 한다는 사실)을 알아야 함
- 이는 컨트롤러가 서비스 내부 구현 세부사항에 결합되는 구조
- 컨트롤러는 요청 수신 → 서비스 위임 → 응답 반환만 담당해야 한다는 원칙 위반

해결:
- DiffService 생성자에서 PriorityService를 직접 주입받도록 변경
- diff(oldText, newText, priorityService) → diff(oldText, newText) 시그니처 단순화
- CompareController에서 PriorityService 필드 및 생성자 파라미터 제거

효과:
- CompareController가 PriorityService 존재를 알 필요가 없어짐 (결합도 감소)
- DiffService가 자신이 필요한 의존성을 스스로 관리 (응집도 향상)
- diff() 메서드 시그니처가 단순해져 향후 호출부 추가 시 실수 가능성 감소',
  '리팩터링,CompareController,DiffService,PriorityService,의존성,결합도',
  TIMESTAMP '2026-03-21 17:00:00',
  TIMESTAMP '2026-03-21 17:00:00'
WHERE NOT EXISTS (SELECT 1 FROM devlog WHERE title = 'DiffService에 PriorityService 직접 주입 (CompareController 의존 제거)');
