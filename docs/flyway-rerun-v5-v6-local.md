# Flyway 로컬 가이드: V5, V6만 다시 실행하기

이 문서는 로컬 개발 DB에서만 쓰는 절차입니다.

운영 DB나 여러 사람이 같이 쓰는 DB에서는 이 방법보다 새 마이그레이션 파일을 추가하는 쪽이 안전합니다.

## 언제 쓰는가

- `V5__seed_devlog.sql`, `V6__seed_devlog_refactor.sql` 내용을 다시 반영하고 싶을 때
- 이미 적용된 `V5`, `V6`을 로컬에서만 다시 돌려보고 싶을 때

## 핵심 원리

Flyway는 `flyway_schema_history` 테이블을 보고 어떤 버전이 이미 실행됐는지 판단합니다.

그래서 다시 실행하려면 아래 2가지를 같이 해야 합니다.

1. `flyway_schema_history`에서 해당 버전 기록 삭제
2. 해당 마이그레이션이 건드린 실제 테이블 상태도 되돌리기

이번 프로젝트에서 `V5`, `V6`은 `devlog` 데이터를 넣는 성격이므로, 실제 정리 대상은 `devlog` 테이블입니다.

## 실행 순서

### 1. 앱 종료

앱이 실행 중이면 먼저 종료합니다.

### 2. `devlog` 데이터 초기화

```sql
TRUNCATE TABLE devlog RESTART IDENTITY;
```

설명:

- `devlog` 데이터를 모두 비웁니다.
- ID 시퀀스도 처음부터 다시 시작합니다.

### 3. Flyway 이력에서 `V5`, `V6` 삭제

```sql
DELETE FROM flyway_schema_history
WHERE version IN ('5', '6');
```

설명:

- Flyway가 `V5`, `V6`를 아직 실행하지 않은 것으로 보게 만듭니다.
- `V6`가 아직 적용되지 않은 상태여도 이 SQL은 그대로 써도 됩니다.

### 4. 앱 재기동

앱을 다시 실행하면 Flyway가 `V5`부터 다시 실행합니다.

그 다음 `V6` 파일이 있으면 이어서 실행합니다.

## 확인용 SQL

### Flyway 이력 확인

```sql
SELECT installed_rank, version, script, checksum, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

### DevLog 데이터 확인

```sql
SELECT id, title, created_at, updated_at
FROM devlog
ORDER BY id;
```

## 한 번에 실행할 SQL

```sql
TRUNCATE TABLE devlog RESTART IDENTITY;

DELETE FROM flyway_schema_history
WHERE version IN ('5', '6');
```

그 후 앱을 다시 실행합니다.

## 주의사항

- 이 방법은 로컬 개발 DB에서만 권장합니다.
- `flyway_schema_history` 기록만 지우고 실제 테이블 데이터를 안 지우면 데이터가 꼬일 수 있습니다.
- 이미 적용된 마이그레이션 파일은 원칙적으로 수정하지 않는 것이 맞습니다.
- 계속 반영이 필요하면 다음부터는 `V7__...sql`처럼 새 버전 파일을 추가하는 쪽이 정석입니다.

## psql에서 한 번에 실행

아래 명령은 터미널에서 그대로 실행할 수 있습니다.

```bash
PGPASSWORD=jsu1234 psql -h localhost -U jsu -d jsudb -c "TRUNCATE TABLE devlog RESTART IDENTITY; DELETE FROM flyway_schema_history WHERE version IN ('5', '6');"
```

실행 후 이력 확인:

```bash
PGPASSWORD=jsu1234 psql -h localhost -U jsu -d jsudb -c "SELECT installed_rank, version, script, checksum, success FROM flyway_schema_history ORDER BY installed_rank;"
```

실행 후 `devlog` 확인:

```bash
PGPASSWORD=jsu1234 psql -h localhost -U jsu -d jsudb -c "SELECT id, title, created_at, updated_at FROM devlog ORDER BY id;"
```
