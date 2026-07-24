# Aiven MySQL 준비 및 전환 체크리스트

이 문서는 Supabase Auth는 유지하고 애플리케이션 데이터베이스만 Aiven MySQL로 전환할 때 프로젝트 외부에서 수행해야 하는 작업을 정리한다.

## 현재 상태 (2026-07-24)

- Aiven 연결 및 Flyway V1 스키마 적용 완료
- Hibernate schema validation 완료
- JPA 한글/이모지 쓰기·조회 후 트랜잭션 롤백 검증 완료
- Supabase PostgreSQL에서 Aiven MySQL로 계정 9건, 채팅방 4건, 메시지 28건 이관 완료
- 양쪽 DB의 식별자, 본문, 소유자, 모델, UTC 시간 및 외래키 전체 비교 완료

아래 절차는 재현 및 운영 체크리스트로 유지한다. 현재 Aiven DB에는 데이터가 있으므로 빈 DB만 허용하는 일회성 이관 테스트를 다시 실행하지 않는다.

## 1. Aiven에서 준비할 항목

1. Aiven for MySQL 서비스를 생성한다.
2. 애플리케이션과 가까운 region을 선택한다.
3. Overview의 Connection information에서 다음 값을 확인한다.
   - Host
   - Port
   - Database
   - Username
   - Password
4. 네트워크 접근 제한을 사용하는 경우 애플리케이션 실행 환경의 outbound IP를 허용한다.
5. 운영에서는 기본 관리자 계정 대신 애플리케이션 전용 사용자를 생성하고 필요한 DB에만 권한을 부여한다.
6. CA Certificate를 안전한 위치에 내려받는다. 저장소에는 커밋하지 않는다.

## 2. 애플리케이션 환경변수

아래 값을 로컬 `.env` 또는 배포 플랫폼의 Secret/Environment 설정에 등록한다.

```dotenv
AIVEN_MYSQL_HOST=서비스-host
AIVEN_MYSQL_PORT=서비스-port
AIVEN_MYSQL_DATABASE=사용할-database
AIVEN_MYSQL_USER=애플리케이션-user
AIVEN_MYSQL_PASSWORD=비밀번호
AIVEN_MYSQL_SSL_MODE=REQUIRED
```

Supabase Auth는 계속 사용하므로 아래 값도 유지한다.

```dotenv
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
```

AI Provider 환경변수도 기존과 동일하게 필요하다.

```dotenv
GEMINI_API_KEY=
GROQ_API_KEY=
NIM_API_KEY=
```

주의:

- `.env`는 Spring Boot가 자동으로 읽는 파일이 아니다. IDE 실행 구성, 운영체제 환경변수 또는 배포 플랫폼 Secret으로 주입한다.
- 초기 연결 검증에는 `REQUIRED`를 사용할 수 있다.
- 운영에서 CA 검증을 적용하려면 Java truststore에 Aiven CA를 등록하고 `VERIFY_CA` 또는 `VERIFY_IDENTITY`를 사용한다.
- 비밀번호, Supabase key, AI key, CA/truststore를 Git에 커밋하지 않는다.

## 3. 첫 연결 순서

새 Aiven DB가 비어 있는 상태에서 다음 순서로 진행한다.

1. 위 환경변수를 설정한다.
2. `./mvnw spring-boot:run` 또는 패키징된 WAR를 실행한다.
3. Flyway가 `V1__create_initial_schema.sql`을 적용하는지 확인한다.
4. 다음 테이블이 생성됐는지 확인한다.
   - `flyway_schema_history`
   - `account`
   - `chat_rooms`
   - `chats`
5. 애플리케이션 로그에서 Hibernate schema validation 성공을 확인한다.
6. 로그인 후 `account` 행이 생성되는지 확인한다.
7. 방 생성과 메시지 저장을 staging 데이터로 확인한다.

Flyway 실행 전 테이블을 수동 생성하지 않는다. 이미 동일한 테이블이 있으면 migration이 실패할 수 있다.

## 4. 기존 Supabase PostgreSQL 데이터 이관

기존 데이터 이관은 `PostgresToAivenMigrationTest`를 통해 단일 대상 트랜잭션으로 수행했다. 이 테스트는 대상 테이블이 모두 비어 있을 때만 동작하며 `RUN_DATA_MIGRATION=true`를 명시해야 실행된다. 원본/대상 검증은 `DatabaseMigrationValidationTest`로 재실행할 수 있다.

필요 작업:

1. Supabase PostgreSQL을 백업한다.
2. `account`, `chat_rooms`, `chats`를 UTF-8 CSV로 export한다.
3. 아래 순서로 Aiven MySQL에 import한다.

```text
account -> chat_rooms -> chats
```

4. PostgreSQL 타입을 MySQL 타입으로 변환한다.
   - `TIMESTAMPTZ` -> UTC `DATETIME(6)`
   - `SERIAL` -> 기존 ID를 유지한 `BIGINT`
   - `chats.timestamp` 문자열 -> `sent_at DATETIME(6)`
5. 메시지의 쉼표, 따옴표, 줄바꿈, 한글, 이모지가 보존되는지 확인한다.
6. import 후 `chats`의 다음 `AUTO_INCREMENT`가 `MAX(id) + 1` 이상인지 확인한다.

`chats.timestamp` 파싱에 실패하는 행이 있으면 임의의 시간으로 대체하지 말고 별도 오류 파일로 분리한다.

## 5. 이관 전후 검증 SQL

Aiven MySQL에서 실행한다.

```sql
SELECT COUNT(*) FROM account;
SELECT COUNT(*) FROM chat_rooms;
SELECT COUNT(*) FROM chats;
SELECT MIN(id), MAX(id) FROM chats;

SELECT COUNT(*) AS orphan_rooms
FROM chat_rooms cr
LEFT JOIN account a ON a.user_id = cr.user_id
WHERE a.user_id IS NULL;

SELECT COUNT(*) AS orphan_chats
FROM chats c
LEFT JOIN chat_rooms cr ON cr.id = c.room_id
WHERE cr.id IS NULL;
```

Supabase PostgreSQL에서 기록한 기존 건수와 비교하고, 고아 데이터 결과는 0이어야 한다.

## 6. 컷오버 시 사용자 수행 항목

- [x] Supabase PostgreSQL 기준 데이터 이관
- [x] 테이블별 건수 및 고아 데이터 검증
- [x] 한글·이모지 메시지와 UTC 시간 검증
- [ ] Supabase PostgreSQL 최종 백업
- [ ] 기존 서비스 쓰기 중단 또는 maintenance window 시작
- [ ] 배포 환경에 Aiven 환경변수 등록
- [ ] 새 Spring Boot WAR 배포
- [ ] 로그인/회원가입 확인
- [ ] 채팅방 생성/수정/삭제 확인
- [ ] 사용자 메시지와 AI 응답 저장 확인
- [ ] Aiven connection 수와 애플리케이션 오류 로그 관찰

## 7. 사용자에게 필요한 정보

실제 Aiven 연결 검증을 이어서 수행하려면 아래 정보가 환경변수로 설정되어 있어야 한다. 채팅에 비밀번호를 붙여 넣을 필요는 없다.

- `AIVEN_MYSQL_HOST`
- `AIVEN_MYSQL_PORT`
- `AIVEN_MYSQL_DATABASE`
- `AIVEN_MYSQL_USER`
- `AIVEN_MYSQL_PASSWORD`
- 사용할 SSL mode

기존 데이터까지 옮길 경우에는 추가로 다음이 필요하다.

- Supabase PostgreSQL의 테이블별 CSV 또는 접근 가능한 migration 환경
- 각 테이블의 기준 row count
- `chats.timestamp` 변환 실패 행 처리 정책
- 허용 가능한 maintenance window

현재 데이터 이관은 완료됐다. 이후 기존 Supabase DB에 새 쓰기가 발생하면 자동 동기화되지 않으므로, 컷오버 전까지 쓰기를 중단하거나 차분 이관 절차를 별도로 마련해야 한다.
