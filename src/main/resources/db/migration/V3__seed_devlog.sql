-- ── 개발자 로그 트러블슈팅 기록 ────────────────────────────────────────

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

-- ── 개발자 로그: 플랫폼 정리 / 구조 정리 기록 ─────────────────────────────

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
