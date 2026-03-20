# JSU

사내 개발 보조 도구를 한 곳에 모아 둔 Spring Boot 웹 프로젝트입니다.

지금은 아래 도구들을 제공합니다.

- 요구사항 비교
- 하드코딩 탐지
- SQL 품질 검사
- Dev Log
- 사이드바 Q&A

화면은 Thymeleaf로 렌더링하고, 데이터는 PostgreSQL + JPA + Flyway로 관리합니다.

설계 의도와 내부 동작 원리를 더 자세히 보고 싶다면 아래 문서를 같이 보면 됩니다.

- [`logic-story.md`](/home/jskim/code/jsu/logic-story.md)

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

### 4. Dev Log

- 경로: `/devlog`
- 프로젝트 작업 기록을 남기는 화면입니다.
- 목록/상세 조회는 공개입니다.
- 등록/수정/삭제는 관리자 비밀번호가 있어야 가능합니다.

### 5. Q&A

- API 경로: `/api/qna`
- 사이드바에서 질문과 답변을 주고받는 간단한 게시판입니다.
- 공개 글 / 비공개 글을 나눠서 운영할 수 있습니다.
- 답변 등록은 관리자 전용입니다.

## 프로젝트 구조

```text
src/main/java/com/jjp/jsu
├── chat        # Q&A API, 엔티티, 서비스
├── common      # 공통 예외 처리
├── compare     # 요구사항 비교 도구
├── config      # Flyway 등 설정
├── devlog      # 개발 로그 화면/API
├── portal      # 메인 화면, 공지, 도구 목록
├── scan        # 하드코딩 탐지 도구
└── sql         # SQL 품질 검사 도구
```

```text
src/main/resources
├── application.yaml
├── db/migration    # Flyway 마이그레이션
└── templates       # Thymeleaf 화면
```

## 주요 화면 파일

- `templates/index.html`: 메인 대시보드
- `templates/compare.html`: 요구사항 비교
- `templates/scan.html`: 하드코딩 탐지
- `templates/sql.html`: SQL 품질 검사
- `templates/devlog.html`: Dev Log
- `templates/fragments/sidebar.html`: 공통 사이드바 + Q&A UI

## 주요 API

### 요구사항 비교

- `POST /api/compare`
- `POST /api/upload`

### 하드코딩 탐지

- `POST /api/scan`

### SQL 품질 검사

- `POST /api/sql/check`
- `POST /api/sql/upload`

### Dev Log

- `GET /api/devlog`
- `GET /api/devlog/{id}`
- `POST /api/devlog`
- `PUT /api/devlog/{id}`
- `DELETE /api/devlog/{id}`

관리자 작업 시 헤더:

- `X-Admin-Password`

### Q&A

- `GET /api/qna/posts`
- `GET /api/qna/posts/all`
- `POST /api/qna/posts`
- `GET /api/qna/posts/{id}`
- `POST /api/qna/posts/{id}/replies`

사용 헤더:

- `X-Admin-Password`
- `X-Post-Password`

## 실행 방법

### 1. PostgreSQL 준비

기본 설정은 아래 값을 사용합니다.

- DB: `jsudb`
- 사용자: `jsu`
- 비밀번호: `jsu1234`

설정 위치:

- [`application.yaml`](/home/jskim/code/jsu/src/main/resources/application.yaml)

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. 테스트 실행

```bash
./gradlew test
```

## 설정 파일

주요 설정은 [`application.yaml`](/home/jskim/code/jsu/src/main/resources/application.yaml)에 있습니다.

대표 항목:

- `spring.datasource.*`
- `spring.flyway.*`
- `chat.admin-password`

## Flyway 메모

이 프로젝트는 Flyway를 자동 설정 대신 [`FlywayConfig.java`](/home/jskim/code/jsu/src/main/java/com/jjp/jsu/config/FlywayConfig.java)에서 직접 등록해 사용합니다.

주의할 점:

- 한 번 DB에 적용된 마이그레이션 파일은 가능하면 수정하지 않는 것이 좋습니다.
- 이미 적용된 파일을 수정하면 checksum mismatch가 발생할 수 있습니다.
- 변경이 필요하면 새 버전 파일을 추가하는 방식이 안전합니다.

관련 문서:

- [`logic-story.md`](/home/jskim/code/jsu/logic-story.md)
- [`docs/flyway-reset-local-db.md`](/home/jskim/code/jsu/docs/flyway-reset-local-db.md)

## 최근 정리 사항

- Spring Boot 3.3.10 기준으로 정리
- Q&A / Dev Log는 컨트롤러-서비스 경계를 나눠서 정리
- Q&A / Dev Log 예외 처리는 공통 핸들러로 통합

## 현재 테스트 상태

현재 기본 테스트는 애플리케이션 컨텍스트가 정상적으로 뜨는지 확인하는 수준입니다.

- `src/test/java/com/jjp/jsu/JsuApplicationTests.java`

앞으로 API 테스트나 서비스 단위 테스트를 더 늘릴 여지가 있습니다.
