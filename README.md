# Just Write

Java Servlet/JSP 기반의 AI 채팅 웹 애플리케이션입니다. 사용자는 Supabase Auth로 로그인/회원가입하고, 채팅 메시지와 대화방 목록은 Supabase PostgreSQL에 저장됩니다. AI 응답은 선택한 모델명에 따라 Gemini, Groq, NVIDIA NIM Provider 중 하나를 통해 생성됩니다. 클라이언트는 최신 Fetch API와 마크다운 렌더링, Optimistic UI를 적용하여 새로고침 없는 부드러운 사용자 경험을 제공합니다.

## 관련 문서

- [데이터 흐름 및 아키텍처](docs/dataFlow/data_flow.md)
- [데이터베이스 스키마](docs/database/database_schema.md)

## 프로젝트 구조

```text
.
├── docs/
├── src/main/java/com/example/archat/
│   ├── application/
│   │   ├── auth/
│   │   │   ├── AuthException.java
│   │   │   ├── AuthProvider.java
│   │   │   ├── AuthUseCase.java
│   │   │   └── DefaultAuthUseCase.java
│   │   └── chat/
│   │       ├── AIChatUseCase.java
│   │       ├── ChatProvider.java
│   │       └── ChatUseCase.java
│   ├── domain/
│   │   ├── auth/
│   │   │   ├── AccountRepository.java
│   │   │   └── AuthUser.java
│   │   └── chat/
│   │       ├── Chat.java
│   │       ├── ChatRepository.java
│   │       ├── ChatRoom.java
│   │       └── ChatRoomRepository.java
│   ├── infrastructure/
│   │   ├── auth/
│   │   │   ├── SupabaseAccountRepository.java
│   │   │   └── SupabaseAuthClient.java
│   │   ├── chat/
│   │   │   ├── GenAIChatProvider.java
│   │   │   ├── OpenAICompatibleProvider.java
│   │   │   ├── SupabaseChatRepository.java
│   │   │   ├── SupabaseChatRoomRepository.java
│   │   │   └── Configs...
│   │   ├── db/
│   │   │   └── DatabaseUtil.java
│   │   └── session/
│   │       ├── SessionCookieConfigListener.java
│   │       └── SessionManager.java
│   └── presentation/
│       ├── auth/
│       │   └── AuthController.java
│       ├── chat/
│       │   ├── ChatController.java
│       │   ├── ChatResponseDTO.java
│       │   └── ChatRoomResponseDTO.java
│       └── common/
│           ├── AuthFilter.java
│           ├── BaseController.java
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

- 프로젝트 전체가 `domain`, `application`, `presentation`, `infrastructure`의 4계층으로 나뉘며, **각 계층 내부는 도메인(기능) 단위인 `chat`, `auth`로 다시 분리**된 하이브리드 수직 슬라이스(Vertical Slice) 형태를 취합니다.
- `presentation`: Servlet Controller, Filter, JSP View와 DTO를 포함하며 비동기 AJAX(JSON) 및 폼 요청을 모두 유연하게 처리합니다.
- `application`: 인증/채팅 UseCase(서비스)를 구현하고 비즈니스 로직을 조합합니다.
- `domain`: 핵심 모델(Entity)과 Repository 인터페이스, UseCase 인터페이스를 둡니다.
- `infrastructure`: Supabase DB/Auth, AI API Provider, 세션, JDBC 연결 등 외부 기술의 구체적인 구현체를 둡니다.

## 주요 흐름

- 인증: `AuthController`가 폼/JSON 로그인 요청을 받고 `DefaultAuthUseCase`를 통해 인증을 수행합니다. Supabase Auth API를 통해 토큰을 발급받고 DB `account` 테이블을 갱신한 뒤, 세션을 생성합니다.
- 대화방 관리: 세션 기반으로 사용자의 전체 대화방 목록을 불러오며, 새 대화방 생성, 이름 변경, 삭제 등의 기능을 제공합니다.
- 실시간 채팅 (AJAX + Optimistic UI): `ChatController`는 사용자의 질문을 비동기(Fetch API)로 받아 즉각적으로 UI에 렌더링(Optimistic Update)합니다. 뒷단에서는 `AIChatUseCase`가 AI Provider에 질의한 뒤 응답을 DB에 저장하고 JSON 형식으로 브라우저에 반환하여, 화면 새로고침 없이 마크다운 기반의 답변을 화면에 그려냅니다.

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
