# Flyway 로컬 가이드: DB 초기화 후 V1부터 다시 적용하기

이 문서는 로컬 개발 DB를 처음부터 다시 만들고 싶을 때 쓰는 절차입니다.

가장 깔끔한 방법은 DB를 비운 뒤 `V1`부터 다시 실행하게 만드는 것입니다.

## 언제 쓰는가

- 로컬 데이터가 많이 꼬였을 때
- 어떤 마이그레이션이 어디까지 반영됐는지 헷갈릴 때
- 특정 버전만 억지로 다시 돌리기보다 처음부터 깨끗하게 맞추고 싶을 때

## 핵심 원리

아래 2가지를 모두 초기화하면 됩니다.

1. 실제 테이블/데이터 상태
2. `flyway_schema_history` 이력

그 뒤 앱을 다시 실행하면 Flyway가 `V1`부터 순서대로 다시 적용합니다.

## 방법 1. 스키마 전체를 날리고 처음부터 다시 만들기

로컬 DB를 가장 깨끗하게 되돌리는 방법입니다.

### 1. 앱 종료

앱이 실행 중이면 먼저 종료합니다.

### 2. `public` 스키마 초기화

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO jsu;
GRANT ALL ON SCHEMA public TO public;
```

설명:

- 현재 스키마 안의 테이블, 시퀀스, `flyway_schema_history`까지 모두 삭제됩니다.
- 이후 비어 있는 `public` 스키마를 다시 만듭니다.

### 3. 앱 재기동

앱을 다시 실행하면 Flyway가 `V1`부터 전부 다시 적용합니다.

## 방법 2. DB는 유지하고 Flyway 관련 테이블만 정리하기

이 방법은 상황을 잘 알고 있을 때만 권장합니다.

테이블 간 연관관계가 많으면 중간에 꼬일 수 있어서, 보통은 방법 1이 더 안전합니다.

예시:

```sql
DROP TABLE IF EXISTS devlog CASCADE;
DROP TABLE IF EXISTS qna_reply CASCADE;
DROP TABLE IF EXISTS qna_post CASCADE;
DROP TABLE IF EXISTS notice CASCADE;
DROP TABLE IF EXISTS tool CASCADE;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
```

그 후 앱을 다시 실행하면 `V1`부터 다시 적용됩니다.

## 확인용 SQL

앱 재기동 후:

```sql
SELECT installed_rank, version, script, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

필요하면 데이터도 확인합니다.

```sql
SELECT * FROM devlog ORDER BY id;
SELECT * FROM notice ORDER BY id;
SELECT * FROM tool ORDER BY sort_order;
```

## 어떤 방법이 더 좋은가

- 가장 안전함: 스키마 전체 초기화 후 재기동
- 부분 복구가 필요함: 특정 테이블과 이력만 정리

로컬 개발 DB에서는 보통 스키마 전체 초기화가 제일 단순하고 덜 헷갈립니다.

## 주의사항

- 이 문서는 로컬 개발 DB 기준입니다.
- 운영 DB나 공용 DB에서는 절대 그대로 쓰면 안 됩니다.
- 한 번 적용된 마이그레이션 파일은 가능하면 수정하지 말고, 새 버전 파일을 추가하는 습관이 가장 안전합니다.

## psql에서 한 번에 실행

### 방법 1. 스키마 전체 초기화

아래 명령은 터미널에서 그대로 실행할 수 있습니다.

```bash
PGPASSWORD=jsu1234 psql -h localhost -U jsu -d jsudb -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO jsu; GRANT ALL ON SCHEMA public TO public;"
```

그 후 앱을 다시 실행하면 `V1`부터 다시 적용됩니다.

### 방법 2. 주요 테이블과 Flyway 이력만 정리

```bash
PGPASSWORD=jsu1234 psql -h localhost -U jsu -d jsudb -c "DROP TABLE IF EXISTS devlog CASCADE; DROP TABLE IF EXISTS qna_reply CASCADE; DROP TABLE IF EXISTS qna_post CASCADE; DROP TABLE IF EXISTS notice CASCADE; DROP TABLE IF EXISTS tool CASCADE; DROP TABLE IF EXISTS flyway_schema_history CASCADE;"
```

그 후 앱을 다시 실행하면 `V1`부터 다시 적용됩니다.

재기동 후 이력 확인:

```bash
PGPASSWORD=jsu1234 psql -h localhost -U jsu -d jsudb -c "SELECT installed_rank, version, script, success FROM flyway_schema_history ORDER BY installed_rank;"
```
