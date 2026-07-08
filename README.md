# 필담

Java Servlet/JSP 기반의 AI 채팅 웹 애플리케이션입니다. 사용자는 Supabase Auth로 로그인/회원가입하고, 채팅 메시지는 Supabase PostgreSQL에 저장됩니다. AI 응답은 선택한 모델명에 따라 Gemini, Groq, NVIDIA NIM Provider 중 하나를 통해 생성됩니다.

## 관련 문서

- [데이터 흐름 및 아키텍처](docs/dataFlow/data_flow.md)
- [데이터베이스 스키마](docs/database/database_schema.md)
- [계정 테이블 SQL](docs/database/account_table.sql)

## 프로젝트 구조

```text
.
├── docs/
│   ├── dataFlow/
│   │   └── data_flow.md
│   ├── database/
│   │   ├── account_table.sql
│   │   └── database_schema.md
│   └── study/
│       └── java_backend_roadmap.md
├── src/main/java/com/example/archat/
│   ├── application/
│   │   ├── port/
│   │   │   ├── ChatProvider.java
│   │   │   └── ChatPublisher.java
│   │   └── service/
│   │       ├── AIChatService.java
│   │       ├── AuthException.java
│   │       ├── AuthService.java
│   │       ├── GeminiChatService.java
│   │       └── SupabaseAuthService.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── AuthUser.java
│   │   │   └── Chat.java
│   │   ├── repository/
│   │   │   ├── AccountRepository.java
│   │   │   └── ChatRepository.java
│   │   └── service/
│   │       └── ChatService.java
│   ├── infrastructure/
│   │   ├── api/
│   │   │   ├── GenAIChatProvider.java
│   │   │   ├── GenAIConfig.java
│   │   │   ├── GroqChatProvider.java
│   │   │   ├── GroqConfig.java
│   │   │   ├── NimChatProvider.java
│   │   │   ├── NimConfig.java
│   │   │   └── SupabaseAuthClient.java
│   │   ├── db/
│   │   │   └── DatabaseUtil.java
│   │   ├── repository/
│   │   │   ├── SupabaseAccountRepository.java
│   │   │   └── SupabaseChatRepository.java
│   │   └── session/
│   │       ├── SessionCookieConfigListener.java
│   │       └── SessionManager.java
│   └── presentation/
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── BaseController.java
│       │   └── ChatController.java
│       ├── dto/
│       │   └── ChatResponseDTO.java
│       └── filter/
│           ├── AuthFilter.java
│           └── EncodingFilter.java
├── src/main/webapp/
│   └── WEB-INF/
│       ├── views/
│       │   ├── chat.jsp
│       │   ├── login.jsp
│       │   └── signup.jsp
│       └── web.xml
├── Dockerfile
├── mvnw / mvnw.cmd
└── pom.xml
```

## 아키텍처 요약

- `presentation`: Servlet Controller, Filter, JSP View와 DTO를 포함합니다.
- `application`: 인증/채팅 유스케이스를 구현하고 외부 Provider 사용 방식을 조합합니다.
- `domain`: 핵심 모델, Repository 인터페이스, 서비스 인터페이스를 둡니다.
- `infrastructure`: Supabase DB/Auth, AI API Provider, 세션, JDBC 연결처럼 외부 기술과 맞닿은 구현을 둡니다.

## 주요 흐름

- 인증: `AuthController`가 로그인/회원가입 요청을 받고 `SupabaseAuthService`가 `SupabaseAuthClient`로 Supabase Auth API를 호출합니다. 성공 시 `SupabaseAccountRepository`가 `account` 테이블을 upsert하고 `SessionManager`가 세션에 사용자 정보를 저장합니다.
- 채팅 조회: `ChatController#doGet`이 세션 사용자 ID로 `AIChatService.findAllByUserId()`를 호출하고, `SupabaseChatRepository`에서 `chats` 테이블을 조회한 뒤 `chat.jsp`로 전달합니다.
- 메시지 전송: `ChatController#doPost`가 사용자 메시지를 `Chat`으로 만들고 `AIChatService.save()`를 호출합니다. 서비스는 사용자 메시지를 저장하고 채팅 이력을 조회한 뒤 모델명에 따라 `GenAIChatProvider`, `GroqChatProvider`, `NimChatProvider` 중 하나를 호출해 AI 응답도 저장합니다.

## 환경 변수

- `GEMINI_API_KEY`: Google Gemini API Key
- `GROQ_API_KEY`: Groq API Key
- `NIM_API_KEY`: NVIDIA NIM API Key
- `SUPABASE_DB_URL`: Supabase PostgreSQL JDBC URL
- `SUPABASE_DB_USER`: Supabase PostgreSQL 사용자
- `SUPABASE_DB_PASSWORD`: Supabase PostgreSQL 비밀번호
- `SUPABASE_URL`: Supabase 프로젝트 URL
- `SUPABASE_ANON_KEY`: Supabase Auth 로그인용 anon key
- `SUPABASE_SERVICE_ROLE_KEY`: Supabase Auth 관리자 API 회원가입용 service role key
