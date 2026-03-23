# JSU

사내 개발 보조 도구를 한 곳에 모아 둔 Spring Boot 웹 프로젝트입니다.

지금은 아래 도구들을 제공합니다.

- 요구사항 비교
- 하드코딩 탐지
- SQL 품질 검사
- 네이밍 컨벤션 검사
- Dev Log
- 사이드바 Q&A

화면은 Thymeleaf로 렌더링하고, 데이터는 PostgreSQL + JPA + Flyway로 관리합니다.

설계 의도와 내부 동작 원리를 더 자세히 보고 싶다면 아래 문서를 같이 보면 됩니다.

- [`logic-story.md`](./logic-story.md)

## 한눈에 보기

- 백엔드: Spring Boot 3.3.10, Spring MVC, Spring Data JPA, Security
- 화면: Thymeleaf
- DB: PostgreSQL
- 마이그레이션: Flyway
- 파일 처리: Apache POI, PDFBox
- Java: 21

## 제공 도구

### 1. 요구사항 비교

- 경로: `/compare`
- 이전 문서와 최신 문서를 비교해서 변경 항목을 찾아줍니다.
- 중요도 규칙을 적용해서 어떤 변경을 먼저 봐야 할지 보여줍니다.
- 텍스트 직접 입력과 파일 업로드를 모두 지원합니다.

지원 파일:

- `.txt`
- `.md`
- `.docx`
- `.pptx`
- `.pdf`

### 2. 하드코딩 탐지

- 경로: `/scan`
- 코드 안에 들어간 하드코딩 값, 고객사 분기, 설정화 후보를 찾아줍니다.
- 사용자 정의 키워드도 같이 넣어서 탐지할 수 있습니다.

### 3. SQL 품질 검사

- 경로: `/sql`
- SQL을 규칙 기반으로 검사해서 품질 이슈를 보여줍니다.
- 예: `SELECT *`, 인덱스 없는 LIKE, 페이지네이션 누락 등
- `.sql` 파일 업로드도 지원합니다.

### 4. 네이밍 컨벤션 검사

- 경로: `/naming`
- 클래스·메서드·변수명이 PascalCase / camelCase / UPPER_SNAKE_CASE 규칙을 따르는지 검사합니다.
- boolean 접두사, 의미 없는 이름, 축약어 과다 같은 의미 품질 규칙도 함께 점검합니다.
- 팀 자체 접두사·접미사 규칙을 직접 등록해 적용할 수 있습니다.
- `.java`, `.txt` 파일 업로드를 지원합니다.

### 5. Dev Log

- 경로: `/devlog`
- 프로젝트 작업 기록을 남기는 화면입니다.
- 목록/상세 조회는 공개입니다.
- 등록/수정/삭제는 관리자 세션이 있어야 가능합니다.

### 6. Q&A

- API 경로: `/api/qna`
- 사이드바에서 질문과 답변을 주고받는 간단한 게시판입니다.
- 공개 글 / 비공개 글을 나눠서 운영할 수 있습니다.
- 답변 등록은 관리자 전용입니다.

## 프로젝트 구조

```text
src/main/java/com/jjp/jsu
├── common      # 공통 유틸 · 예외 처리 (FileExtractService, AdminAuthService, ...)
├── compare     # 요구사항 비교 도구
├── config      # Flyway 등 설정
├── devlog      # 개발 로그 화면/API
├── naming      # 네이밍 컨벤션 검사 도구
├── portal      # 메인 화면, 공지, 도구 목록
├── qna         # Q&A API, 엔티티, 서비스
├── scan        # 하드코딩 탐지 도구
└── sql         # SQL 품질 검사 도구
```

```text
src/main/resources
├── application.yaml
├── db/migration    # Flyway 마이그레이션 (V1~V4)
├── static/js       # 공통 JS (common.js)
└── templates       # Thymeleaf 화면
```

## 주요 화면 파일

- `templates/index.html`: 메인 대시보드 (공지 목록 + 관리자 공지 관리 UI)
- `templates/compare.html`: 요구사항 비교
- `templates/scan.html`: 하드코딩 탐지
- `templates/sql.html`: SQL 품질 검사
- `templates/naming.html`: 네이밍 컨벤션 검사
- `templates/devlog.html`: Dev Log
- `templates/fragments/sidebar.html`: 공통 사이드바 + Q&A UI

## 주요 API

### 관리자 인증

관리자 작업은 세션 기반으로 인증합니다. 모든 관리자 API(`POST /api/devlog`, `PUT /api/notice/{id}` 등)는 유효한 세션이 필요합니다.

- `POST /api/admin/login` — 로그인 (body: `{ "password": "..." }`)
- `POST /api/admin/logout` — 로그아웃
- `GET  /api/admin/status` — 현재 세션 로그인 여부 확인

### 요구사항 비교

- `POST /api/compare`
- `POST /api/upload`

### 하드코딩 탐지

- `POST /api/scan`

### SQL 품질 검사

- `POST /api/sql/check`
- `POST /api/sql/upload`

### 네이밍 컨벤션 검사

- `POST /api/naming/check`
- `POST /api/naming/upload`

### 공지

- `GET    /api/notice` — 목록 조회 (인증 불필요)
- `POST   /api/notice` — 등록 (관리자 세션 필요)
- `PUT    /api/notice/{id}` — 수정 (관리자 세션 필요)
- `DELETE /api/notice/{id}` — 삭제 (관리자 세션 필요)

### Dev Log

- `GET    /api/devlog`
- `GET    /api/devlog/{id}`
- `POST   /api/devlog` — 관리자 세션 필요
- `PUT    /api/devlog/{id}` — 관리자 세션 필요
- `DELETE /api/devlog/{id}` — 관리자 세션 필요

### Q&A

- `GET  /api/qna/posts` — 공개 목록
- `GET  /api/qna/posts/all` — 전체 목록 (관리자 세션 필요)
- `POST /api/qna/posts`
- `GET  /api/qna/posts/{id}`
- `POST /api/qna/posts/{id}/replies` — 관리자 세션 필요

비공개 글 조회/수정/삭제 시:

- `X-Post-Password` 헤더로 작성자 비밀번호 전달



## TODO

### 결과 카드 규칙 ID 인라인 표시
- [ ] 각 결과 카드의 이유 태그 옆에 규칙 ID 뱃지 추가 (예: `R07`)
- [ ] 규칙 ID 클릭 시 해당 규칙 설명으로 스크롤 또는 툴팁 표시
- [ ] 적용 대상: 요구사항 비교 / 하드코딩 탐지 / SQL 품질 검사 / 네이밍 컨벤션 검사

### 결과 공유하기
- [ ] 결과 목록 상단에 "결과 복사" 버튼 추가
- [ ] 복사 형식 1 — 메신저용 요약 텍스트
- [ ] 복사 형식 2 — PR 코멘트용 마크다운
- [ ] 복사 완료 시 "복사됨 ✓" 토스트 메시지 표시
- [ ] 적용 대상: 요구사항 비교 / 하드코딩 탐지 / SQL 품질 검사 / 네이밍 컨벤션 검사

 
## 실행 방법

### 1. PostgreSQL 준비

기본 설정은 아래 값을 사용합니다.

- DB: `jsudb`
- 사용자: `jsu`
- 비밀번호: `jsu1234`

운영 환경에서는 환경변수로 오버라이드할 수 있습니다:

```bash
export DB_URL=jdbc:postgresql://prod-host:5432/jsudb
export DB_USERNAME=prod_user
export DB_PASSWORD=prod_password
export APP_ADMIN_PASSWORD=your_admin_pw
```

### 2. DB 초기화 (최초 실행 또는 스키마 리셋 시)

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

이후 앱을 재시작하면 Flyway가 V1 → V4 순서로 자동 적용합니다.

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. 테스트 실행

```bash
./gradlew test
```

## 설정 파일

주요 설정은 `src/main/resources/application.yaml`에 있습니다.

대표 항목:

- `spring.datasource.*` — DB 접속 정보 (환경변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 오버라이드 가능)
- `app.admin-password` — 관리자 비밀번호 (환경변수 `APP_ADMIN_PASSWORD`로 오버라이드 가능)
- `spring.flyway.*`

## Flyway 메모

이 프로젝트는 Flyway를 자동 설정 대신 `FlywayConfig.java`에서 직접 등록해 사용합니다.

마이그레이션 파일 구조 (`db/migration`):

| 파일 | 내용 |
|---|---|
| `V1__ddl_create_tables.sql` | 전체 테이블 DDL (tool, notice, qna_post, qna_reply, devlog 등) |
| `V2__seed_tool.sql` | tool 초기 데이터 6건 (최종 상태 기준 단일 INSERT) |
| `V3__seed_notice.sql` | notice 초기 데이터 8건 |
| `V4__seed_devlog.sql` | devlog 전체 이력 23건 (트러블슈팅 → 플랫폼 → 구조개선 → 유지보수 → 세션/공지 → 버그수정) |

주의할 점:

- 한 번 DB에 적용된 마이그레이션 파일은 수정하지 않습니다 (checksum mismatch 발생).
- devlog 내용에 `${...}` 형태의 텍스트가 있으면 Flyway 플레이스홀더로 오해하므로 `$${...}` 형태로 이스케이프합니다.
- DB를 완전히 초기화하고 재적용할 때는 `flyway_schema_history` 포함 schema 전체 drop 후 재시작합니다.

## 최근 정리 사항

- Spring Boot 3.3.10 기준으로 정리
- Q&A / Dev Log / 공지는 컨트롤러-서비스 경계를 나눠서 정리
- 예외 처리는 `ApiExceptionHandler` 전역 핸들러 + `BadRequestException` / `ForbiddenException` 공통 예외로 통합
- 관리자 인증을 `X-Admin-Password` 헤더 방식에서 `HttpSession` 세션 기반으로 전환 (로그인 상태가 모든 페이지에서 공유됨)
- `AdminAuthService`를 `common` 패키지에서 중앙 관리, `QnaService` / `DevLogService` / `NoticeController` 모두 동일 서비스 주입
- `FileExtractService`를 `compare` → `common` 패키지로 이동하여 모든 도구에서 재사용
- `chat` 패키지를 `qna`로 리네이밍
- `DiffService`가 `PriorityService`를 생성자 주입으로 직접 관리 (CompareController에서 협력 방식 제거)
- `ScanResponse`에 집계 로직 정적 팩토리로 이동 (ScanController 역할 축소)
- 모듈별 예외 클래스 4개 제거, 공통 예외 직접 사용으로 단일화
- Flyway 마이그레이션을 V1~V12에서 V1~V4로 통합 정리
- `common.js`로 HTML 중복 JS 함수 제거 (escHtml, showAlert, setSpinner)
- 네이밍 컨벤션 검사 도구 추가 (`/naming`)

## 현재 테스트 상태

`ScanService`, `NamingService`, `SqlQualityService` 규칙 엔진에 대한 단위 테스트를 도입했습니다.

- `ScanServiceTest`: 12개 케이스 (HC-001, HC-002, HC-003, HC-005, HC-006, HC-008)
- `NamingServiceTest`: 14개 케이스 (NM-001 ~ NM-006)
- `SqlQualityServiceTest`: 14개 케이스 (SQ01 ~ SQ07)
- `JsuApplicationTests`: 애플리케이션 컨텍스트 로드 확인

테스트는 `@Nested` + `@DisplayName`으로 규칙별 논리 그룹화, Spring 컨텍스트 미사용 빠른 단위 테스트 방식으로 작성되어 있습니다.
