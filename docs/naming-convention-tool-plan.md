# 네이밍 품질 점검 도구 설계안

이 문서는 `JSU`에 새로 추가할 `네이밍 품질 점검` 도구의 1차 설계안입니다.

아직 구현 전 단계이며, 실제 개발에 들어가기 전에 범위, 목표, 규칙 수준을 정리하기 위한 문서입니다.

## 1. 이 도구를 왜 만드는가

지금 `JSU`는 아래 흐름의 도구를 갖고 있습니다.

- 요구사항 비교: 문서 변화 검토
- 하드코딩 탐지: 구조적 부채 점검
- SQL 품질 검사: 쿼리 안티패턴 점검

이 흐름에서 다음으로 잘 맞는 도구는 단순한 스타일 검사기가 아니라,

- 이름이 팀 규칙에 맞는지
- 이름이 읽기 쉬운지
- 리뷰할 가치가 있는 이름인지

를 빠르게 걸러주는 도구입니다.

쉽게 말하면:

- "문법상 맞나?"만 보는 도구가 아니라
- "이 이름이 괜찮은 이름인가?"를 같이 보는 도구

## 2. 도구 성격

이 도구는 `정적 규칙 검사 + 의미 품질 경고` 도구입니다.

즉, 두 층으로 작동합니다.

### 1층: 형식 규칙

- class / interface / enum / record 이름 규칙
- 메서드명 규칙
- 변수명 규칙
- 상수명 규칙

### 2층: 의미 품질 규칙

- 의미가 약한 이름
- boolean 이름 품질
- 컬렉션 이름 품질
- 축약어 과다

이 구조가 중요한 이유는, 형식만 보면 너무 평면적이고 체감이 약하기 때문입니다.

## 3. 1차 목표

1차 목표는 아래 세 가지입니다.

1. 코드 선언부에서 이름 규칙 위반을 자동으로 찾는다.
2. 왜 문제인지와 어떻게 개선하면 좋은지 같이 보여준다.
3. 기존 `scan`, `sql` 도구와 같은 흐름으로 빠르게 사용할 수 있게 만든다.

## 4. 1차 MVP 방향

### 권장 방향

- Java 중심
- 선언부 중심 분석
- 형식 규칙 + 의미 품질 규칙 혼합
- 심각도 필터 + 카테고리 필터 제공

### 왜 이렇게 가는가

너무 가볍게 가면:

- 그냥 lint 같은 느낌이 나고
- JSU의 다른 도구들보다 존재감이 약해집니다.

너무 무겁게 가면:

- AST 풀파싱
- 프로젝트 전체 스캔
- 다국어 정밀 분석

으로 넘어가서 1차 MVP가 지나치게 커집니다.

그래서 1차는

- 선언부는 비교적 정확하게 잡고
- 의미 품질 경고를 실용적으로 주는

중간 지점이 가장 좋습니다.

## 5. 포함 범위

### 포함

- Java 코드 검사
- 텍스트 입력
- 파일 업로드
- 선언부 기반 이름 분석
- 결과 요약 통계
- 심각도 필터
- 카테고리 필터

### 제외

- 자동 수정
- IDE 플러그인 연동
- 프로젝트 전체 폴더 업로드
- Git diff 기반 검사
- JS/TS 정밀 규칙 검사
- AST 기반 풀파싱

## 6. 분석 방식

1차 구현은 `정규식만 100%`도 아니고, `AST 풀파싱`도 아닙니다.

추천 방식은:

- 선언부 중심 약한 구조 인식

즉, 아래 선언은 비교적 안정적으로 잡습니다.

- `class`
- `interface`
- `enum`
- `record`
- 메서드 선언
- 필드 선언
- `static final` 상수 선언
- `boolean` 선언
- 컬렉션 선언

이 방식의 장점:

- 구현 난이도가 적당함
- 기존 `scan`, `sql`과 결이 맞음
- 1차 버전으로 충분히 실용적임

단점:

- 코드 의미를 100% 이해하지는 못함
- 일부 오탐/미탐 가능성은 있음

하지만 1차 도구 목표에는 충분합니다.

## 7. 입력 구조

### 기본 입력

- `code`: 검사할 코드 본문
- `fileName`: 업로드 파일명 또는 수동 입력 파일명
- `language`: `JAVA`
- `strictMode`: 엄격 모드 사용 여부

### 1차 옵션

처음부터 옵션이 너무 많으면 오히려 복잡하므로 최소화합니다.

- `strictMode`
- `checkMeaningQuality`
- `checkBooleanNaming`

## 8. 출력 구조

### 요약

- 전체 위반 수
- `HIGH`
- `MEDIUM`
- `LOW`

### 카테고리 집계

- 타입 이름
- 메서드 이름
- 필드/변수 이름
- 의미 품질 경고

### 항목 구조

각 항목은 아래 정보를 가집니다.

- `ruleId`
- `ruleName`
- `category`
- `severity`
- `lineNumber`
- `matchedText`
- `message`
- `recommendation`
- `suggestedName`

## 9. 1차 규칙 체계

규칙 코드는 `NM-001`부터 시작합니다.

## 10. 형식 규칙

### NM-001 타입 이름은 PascalCase

대상:

- `class`
- `interface`
- `enum`
- `record`

문제 예:

```java
class user_service {
}
```

권장:

```java
class UserService {
}
```

심각도:

- `HIGH`

카테고리:

- `TYPE`

### NM-002 메서드명은 camelCase

대상:

- 일반 메서드

문제 예:

```java
public void Save_user() {
}
```

권장:

```java
public void saveUser() {
}
```

심각도:

- `MEDIUM`

카테고리:

- `METHOD`

### NM-003 필드 / 변수명은 camelCase

대상:

- 필드
- 지역 변수
- 파라미터

문제 예:

```java
String user_name = "kim";
```

심각도:

- `MEDIUM`

카테고리:

- `FIELD`

### NM-004 상수는 UPPER_SNAKE_CASE

대상:

- `static final`

문제 예:

```java
private static final String adminPassword = "1234";
```

권장:

```java
private static final String ADMIN_PASSWORD = "1234";
```

심각도:

- `HIGH`

카테고리:

- `FIELD`

## 11. 의미 품질 규칙

### NM-005 boolean 이름은 의도가 드러나야 함

권장 접두사:

- `is`
- `has`
- `can`
- `should`
- `use`
- `enable`

문제 예:

```java
boolean visible;
boolean login;
boolean flag;
```

더 나은 예:

```java
boolean isVisible;
boolean canLogin;
boolean hasError;
```

심각도:

- `LOW`

카테고리:

- `MEANING`

주의:

- 이 규칙은 강제보다 권장에 가깝게 운영합니다.

### NM-006 의미가 약한 이름 경고

대상 예:

- `data`
- `value`
- `temp`
- `tmp`
- `obj`
- `info`
- `result`

문제 예:

```java
String data = load();
```

심각도:

- `LOW`

카테고리:

- `MEANING`

의도:

- 이름만 보고 역할을 떠올리기 어려운 경우를 미리 잡기 위함

### NM-007 컬렉션 이름 품질 경고

대상:

- `List`
- `Set`
- `Map`
- 배열

문제 예:

```java
List<User> data;
Map<String, Role> item;
```

더 나은 예:

```java
List<User> users;
Map<String, Role> roleMap;
```

심각도:

- `LOW`

카테고리:

- `MEANING`

### NM-008 축약어 과다 경고

문제 예:

- `usrDtlCdVal`
- `mgrAuthYn`

의도:

- 지나치게 압축된 이름은 읽기 어렵고 팀 외부 사람에게 불친절합니다.

심각도:

- `LOW`

카테고리:

- `MEANING`

## 12. 1차에서 보류할 규칙

아래는 유용하지만 1차 MVP에서는 보류하는 편이 낫습니다.

- 파일명과 클래스명 일치 여부
- 패키지명 규칙 검사
- JS/TS 전용 컴포넌트 이름 규칙
- DTO / VO / Entity 접미사 정책 검사
- 자동 수정 제안 고도화

## 13. 심각도 기준

### HIGH

- 타입 이름 규칙 위반
- 상수명 규칙 위반

### MEDIUM

- 메서드명 규칙 위반
- 필드/변수명 규칙 위반

### LOW

- boolean 이름 권장 위반
- 의미 약한 이름
- 컬렉션 이름 품질 경고
- 축약어 과다

## 14. 예외 처리 기본 원칙

아래는 기본 예외 후보로 둡니다.

- `i`, `j`, `k`: 반복문 인덱스
- `id`: 매우 흔한 도메인 필드
- `x`, `y`: 좌표 맥락
- `url`, `api`, `dto`, `vo`: 팀에서 이미 자연스럽게 쓰는 약어

이 예외 목록은 1차에서는 코드 내부 상수로 두고,
나중에 옵션화하는 방향이 좋습니다.

## 15. 화면 구성안

기존 `scan.html`, `sql.html` 패턴을 따라가되,
필터는 이 도구에 맞게 조금 다르게 갑니다.

### 상단

- 페이지 제목
- 짧은 설명
- 가이드 버튼

### 입력 영역

왼쪽:

- 코드 입력 textarea
- 파일 업로드
- 파일명 표시
- 줄 수 표시

오른쪽:

- 언어 선택: 일단 `Java`
- `strictMode`
- 의미 품질 규칙 사용 여부
- boolean 규칙 사용 여부

### 실행 버튼

- `검사 실행`
- `초기화`

### 결과 영역

- 요약 배지
- 결과 카드
- 필터 2종

필터:

- 심각도 필터
- 카테고리 필터

카테고리:

- 전체
- 타입
- 메서드
- 필드/변수
- 의미 품질

## 16. UX 방향

이 도구는 "왜 이게 문제인지"가 중요합니다.

그래서 결과는 아래 순서가 좋습니다.

1. 규칙 이름
2. 실제 문제 이름
3. 왜 문제인지
4. 어떻게 고치면 좋은지
5. 가능하면 추천 이름

즉, 단순히

- `camelCase 아님`

으로 끝나면 아쉽고,

- `의미가 약한 이름이라 리뷰와 유지보수 비용을 높인다`

까지 설명하는 쪽이 좋습니다.

## 17. 백엔드 구조 제안

패키지:

```text
src/main/java/com/jjp/jsu/naming
```

예상 파일:

- `NamingController`
- `NamingService`
- `NamingCheckRequest`
- `NamingCheckResponse`
- `NamingIssue`

### NamingController

역할:

- 페이지 반환
- 검사 API 제공
- 파일 업로드 API 제공

예상 엔드포인트:

- `GET /naming`
- `POST /api/naming/check`
- `POST /api/naming/upload`

### NamingService

역할:

- 코드 라인 분리
- 선언부 후보 추출
- 규칙 적용
- 심각도 집계
- 카테고리 집계
- 결과 정렬

## 18. API 스펙 초안

### POST `/api/naming/check`

요청 예시:

```json
{
  "code": "public class user_service { }",
  "fileName": "UserService.java",
  "language": "JAVA",
  "strictMode": false
}
```

응답 예시:

```json
{
  "items": [
    {
      "ruleId": "NM-001",
      "ruleName": "타입 이름은 PascalCase",
      "category": "TYPE",
      "severity": "HIGH",
      "lineNumber": 1,
      "matchedText": "user_service",
      "message": "타입 이름이 PascalCase 규칙을 따르지 않습니다.",
      "recommendation": "대문자로 시작하고 단어 구분은 대문자로 이어서 작성하세요.",
      "suggestedName": "UserService"
    }
  ],
  "totalCount": 1,
  "highCount": 1,
  "mediumCount": 0,
  "lowCount": 0,
  "typeCount": 1,
  "methodCount": 0,
  "fieldCount": 0,
  "meaningCount": 0
}
```

### POST `/api/naming/upload`

지원 확장자:

- `.java`
- `.txt`

1차는 Java 중심이므로 시작은 단순하게 갑니다.

응답 예시:

```json
{
  "text": "public class UserService { }",
  "filename": "UserService.java"
}
```

## 19. 결과 정렬 기준

추천 정렬:

1. `HIGH`
2. `MEDIUM`
3. `LOW`
4. 같은 심각도면 라인 번호 오름차순

이렇게 하면 리뷰 순서가 자연스럽습니다.

## 20. 대시보드 / 사이드바 반영 방향

현재 도구 목록에 이미 `네이밍 컨벤션 검사` 카드가 있습니다.

구현 시 해야 할 일:

1. 카드 상태를 `COMING_SOON -> ACTIVE`로 변경
2. `href`를 `/naming`으로 연결
3. 대시보드 아이콘 분기 추가
4. 사이드바 아이콘 분기 추가

## 21. 2차 확장 아이디어

1차가 안정화되면 아래 확장이 가능합니다.

### 2차 후보

- JS / TS 규칙 강화
- React 컴포넌트 이름 검사
- 파일명과 클래스명 일치 여부
- DTO / VO / Entity 접미사 정책 검사
- 사내 약어 사전 등록

### 3차 후보

- 프로젝트 폴더 업로드
- Git diff 기준 검사
- PR 보조 API
- 자동 수정 제안

## 22. 구현 우선순위 제안

### 1단계

- 백엔드 패키지 생성
- 형식 규칙 4개 구현
- 검사 API

### 2단계

- 의미 품질 규칙 3~4개 구현
- 카테고리 집계 추가

### 3단계

- `naming.html` 화면 구현
- 파일 업로드
- 결과 필터 UI

### 4단계

- 도구 카드 `ACTIVE` 전환
- 대시보드 / 사이드바 연결

## 23. 최종 추천 결론

가장 좋은 1차 방향은 이겁니다.

- Java 전용
- 선언부 중심 분석
- 형식 규칙 + 의미 품질 규칙 혼합
- 심각도 + 카테고리 필터 제공
- lint 도구보다 리뷰 보조 도구에 가깝게 설계

이 방향이 지금 `JSU` 전체 흐름과 가장 잘 맞습니다.

---

다음 단계에서는 이 문서를 기준으로 바로

- 규칙 구현 순서 확정
- 백엔드 클래스 생성
- 화면 초안 구현

순으로 진행할 수 있습니다.
