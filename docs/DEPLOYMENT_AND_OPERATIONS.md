# Render · Aiven 배포 및 운영 가이드

## 1. 현재 운영 구조

```text
GitHub
  ↓ Docker deploy
Render Web Service (Spring Boot executable WAR)
  ├── Aiven MySQL: account/chat_rooms/chats
  ├── Supabase Auth: login/signup
  └── Gemini/Groq/NVIDIA NIM: AI response
```

- 배포 URL: <https://justwrite-gwf7.onrender.com>
- Spring Boot: 4.1.0
- Java: 17
- DB 스키마: Flyway V1
- Supabase PostgreSQL → Aiven MySQL 데이터 이관: 2026-07-24 완료
- 이관 검증 결과: account 9건, chat_rooms 4건, chats 28건, 고아 데이터 0건

## 2. Render 환경 변수

Render Dashboard의 해당 Web Service → Environment에 등록합니다. 로컬 `.env`는 Render가 읽지 않습니다.

```dotenv
# Aiven MySQL
AIVEN_MYSQL_HOST=
AIVEN_MYSQL_PORT=
AIVEN_MYSQL_DATABASE=
AIVEN_MYSQL_USER=
AIVEN_MYSQL_PASSWORD=
AIVEN_MYSQL_SSL_MODE=REQUIRED

# Supabase Auth
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=

# AI Providers
GEMINI_API_KEY=
GROQ_API_KEY=
NIM_API_KEY=

# Runtime
APP_COOKIE_SECURE=true
SESSION_TIMEOUT_MINUTES=30
```

Render가 `PORT`를 제공하며 애플리케이션은 `${PORT:10000}`을 사용합니다. 비밀번호, Supabase key, AI key와 인증서는 Git에 커밋하지 않습니다.

환경 변수 변경 후 `Save and deploy` 또는 `Save, rebuild, and deploy`를 선택해야 실행 인스턴스에 반영됩니다.

## 3. 배포 절차

1. 배포 대상 Git 브랜치와 커밋을 확인합니다.
2. `mvnw package`가 통과하는지 확인합니다.
3. Render Environment에 필수 값을 등록합니다.
4. Docker deploy를 실행합니다.
5. 로그에서 다음 순서를 확인합니다.
   - Aiven DataSource 연결
   - Flyway schema version 확인
   - Hibernate schema validation
   - `Tomcat started on port ...`
   - `Started ArchatApplication`
6. `/login`에 접속하여 HTTP 200을 확인합니다.
7. 실제 Supabase 사용자로 로그인합니다.
8. 기존 방/메시지 조회, 방 관리, AI 메시지 전송을 확인합니다.

`Dockerfile`은 multi-stage build로 WAR를 만들고 JRE 이미지에서 다음 명령으로 실행합니다.

```text
java -jar /app/archat.war
```

## 4. Smoke test

- [ ] `/login` 화면 표시
- [ ] 비로그인 `/chat`이 `/login`으로 이동
- [ ] 기존 Supabase 사용자 로그인
- [ ] Aiven `account` upsert
- [ ] 기존 대화방과 메시지 표시
- [ ] 방 생성·이름 변경·삭제
- [ ] 한글과 이모지 메시지 저장
- [ ] Gemini/Groq/NIM 중 실제 사용하는 Provider 응답
- [ ] 로그에 credential 또는 token이 출력되지 않음

## 5. Render cold start와 health check

Render Free Web Service는 유휴 상태에서 중지될 수 있어 첫 접속이 느릴 수 있습니다. 지속적인 응답 시간이 필요하면 always-on 유료 instance를 검토합니다.

현재 기본 TCP health check는 포트만 확인합니다. 애플리케이션 수준 readiness가 필요하면 DB·AI API를 호출하지 않는 `/health` endpoint를 추가하고 Render Health Check Path로 지정합니다. `/login`은 JSP 세션을 만들 수 있으므로 장기적인 health endpoint로는 권장하지 않습니다.

진단 시 endpoint별로 분리합니다.

```text
/login 실패       → Render 라우팅/JVM/JSP 점검
로그인 실패       → Supabase Auth 설정 점검
/chat 조회 실패   → Aiven 연결/JPA 점검
메시지 전송 실패 → AI Provider 및 Aiven 저장 점검
```

## 6. Aiven 연결과 TLS

JDBC URL은 `application.yml`이 개별 환경 변수로 조립합니다. Render에 placeholder가 포함된 `SPRING_DATASOURCE_URL`을 별도로 등록하지 않습니다.

```text
jdbc:mysql://HOST:PORT/DATABASE
  ?sslMode=REQUIRED
  &serverTimezone=UTC
  &characterEncoding=UTF-8
```

`REQUIRED`는 전송 암호화를 강제하지만 서버 신원까지 검증하지 않습니다. 운영에서는 Aiven CA를 Java truststore에 등록하고 `VERIFY_CA` 또는 `VERIFY_IDENTITY` 적용을 검토합니다.

다음 오류는 Render에 Aiven 변수가 누락됐거나 값이 placeholder 그대로 등록됐다는 의미입니다.

```text
Failed to parse the host:port pair
'${AIVEN_MYSQL_HOST}:${AIVEN_MYSQL_PORT}'
```

이 경우 `AIVEN_MYSQL_*` 값, Environment Group 연결, 중복된 `SPRING_DATASOURCE_*` 변수를 확인합니다.

## 7. Flyway 운영 규칙

- 기존 `V1__create_initial_schema.sql`은 적용 후 수정하지 않습니다.
- 새 변경은 `V2__description.sql`처럼 별도 파일로 추가합니다.
- 운영에서 Hibernate `ddl-auto`는 `validate`를 유지합니다.
- Flyway 실행 전 테이블을 수동으로 변경하지 않습니다.
- 배포 전 신규 migration을 백업 DB 또는 staging에서 검증합니다.

## 8. 백업과 롤백

안정화 기간에는 기존 Supabase PostgreSQL 백업을 보존합니다. 두 DB를 동시에 쓰는 dual-write는 구현되어 있지 않습니다.

롤백이 필요한 경우:

1. 새 애플리케이션 쓰기를 중단합니다.
2. 컷오버 이후 Aiven에 추가된 데이터를 별도 export합니다.
3. 이전 애플리케이션과 Supabase PostgreSQL 설정으로 복구합니다.
4. 서비스 복구 후 Aiven 전용 데이터를 재이관할 계획을 수립합니다.

원본 Supabase DB에 컷오버 이후 쓰기가 발생했다면 자동으로 Aiven에 동기화되지 않습니다.

## 9. 일회성 이관 테스트

다음 테스트는 운영 애플리케이션의 일반 실행 경로가 아닙니다.

- `DatabaseMigrationPreflightTest`: 원본과 대상 사전 점검
- `PostgresToAivenMigrationTest`: 빈 대상 DB로 일회성 이관
- `DatabaseMigrationValidationTest`: 원본/대상 전체 비교
- `AivenMySqlSmokeTest`: 실제 Aiven JPA 저장·조회·rollback

`PostgresToAivenMigrationTest`는 대상 테이블이 모두 비어 있을 때만 실행되며 `RUN_DATA_MIGRATION=true`가 필요합니다. 현재 Aiven DB에는 이관 데이터가 있으므로 다시 실행하지 않습니다.

롤백 보존 기간이 끝나면 배포 환경에서 `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`를 제거합니다. 테스트 도구까지 폐기하기로 결정하면 PostgreSQL test dependency와 일회성 migration test도 별도 정리합니다.

## 10. 비용과 사용량

애플리케이션 코드는 별도 사용료를 발생시키지 않지만 연결 서비스의 실제 플랜에 따라 비용이 생길 수 있습니다.

- Render instance type
- Aiven MySQL service plan
- Supabase organization plan
- Gemini/Groq API billing tier
- NVIDIA NIM 운영 라이선스

공개 회원가입 후 AI API를 호출할 수 있으므로 Provider별 quota·budget alert를 설정하고 사용자별 rate limit 도입을 검토합니다.

## 11. 모니터링 항목

- Render restart, memory, response time, cold start
- Aiven connection 수, CPU, storage, slow query
- Flyway/Hibernate startup failure
- Supabase Auth 오류율
- AI Provider latency, 429/5xx, token usage
- 로그인·채팅 실패 로그의 개인정보/credential 노출 여부
