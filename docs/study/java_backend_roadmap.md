# Java 백엔드 학습 로드맵 (ArChat 기준)

이 문서는 ArChat 코드를 읽으면서 Java Servlet/JSP 백엔드의 핵심 개념을 학습하기 위한 가이드입니다.

## 1. 객체지향과 인터페이스

먼저 구현체가 아니라 계약을 기준으로 코드를 나누는 방식을 확인합니다.

- `domain.service.ChatService`: 채팅 유스케이스 계약
- `domain.repository.ChatRepository`: 채팅 저장소 계약
- `domain.repository.AccountRepository`: 계정 저장소 계약
- `application.service.AuthService`: 인증 유스케이스 계약
- `application.port.ChatProvider`: AI Provider 계약

학습 포인트:

- 인터페이스가 Controller와 Infrastructure 사이의 결합을 낮추는 방식
- `AIChatService`가 `ChatService`를 구현하고 `SupabaseChatRepository`를 사용하는 구조
- `GenAIChatProvider`, `GroqChatProvider`, `NimChatProvider`처럼 같은 역할의 구현체를 교체할 수 있는 구조

## 2. Servlet, JSP, HTTP

ArChat은 Spring MVC 없이 Jakarta Servlet과 JSP로 웹 요청을 처리합니다.

- `AuthController`: `/login`, `/signup`, `/logout`, `/api/auth/*`
- `ChatController`: `/chat`
- `BaseController`: JSP view prefix 공통화
- `chat.jsp`, `login.jsp`, `signup.jsp`: 서버 사이드 렌더링 View

학습 포인트:

- `doGet`, `doPost`의 역할 차이
- `RequestDispatcher.forward()`와 `sendRedirect()`의 차이
- POST 처리 후 redirect하는 PRG(Post-Redirect-Get) 패턴
- JSON API와 form submit을 같은 Controller에서 처리하는 방식

## 3. 세션과 필터

HTTP는 요청 간 상태를 유지하지 않으므로 로그인 상태는 서버 세션으로 관리합니다.

- `SessionManager`: 세션 생성, 로그인 사용자 저장, 로그아웃, 인증 확인
- `AuthFilter`: `/chat` 접근 전 인증 여부 확인
- `SessionCookieConfigListener`: 세션 쿠키 설정
- `EncodingFilter`: 요청/응답 UTF-8 인코딩 설정

학습 포인트:

- `HttpSession`에 저장되는 `authUserId`, `authUserEmail`
- 로그인 시 기존 세션을 무효화하고 새 세션을 만드는 이유
- 인증이 필요한 URL을 Filter에서 보호하는 방식

## 4. JDBC와 Repository

DB 접근은 Repository 구현체와 `DatabaseUtil`에 모여 있습니다.

- `DatabaseUtil`: `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD` 환경 변수로 JDBC 연결 생성
- `SupabaseChatRepository`: `chats` 테이블 저장/조회
- `SupabaseAccountRepository`: `account` 테이블 upsert

학습 포인트:

- `Connection`, `PreparedStatement`, `ResultSet`의 역할
- SQL Injection을 줄이기 위해 파라미터 바인딩을 사용하는 방식
- `try-with-resources`로 DB 리소스를 닫는 방식
- Repository 인터페이스와 구현체를 나누는 이유

## 5. 외부 API 연동

ArChat은 Supabase Auth와 여러 AI API를 HTTP/API Client로 호출합니다.

- `SupabaseAuthClient`: Supabase Auth 로그인/회원가입 API 호출
- `GenAIChatProvider`: Gemini 계열 모델 호출
- `GroqChatProvider`: Groq 모델 호출
- `NimChatProvider`: NVIDIA NIM 모델 호출
- `GenAIConfig`, `GroqConfig`, `NimConfig`: API 설정값 관리

학습 포인트:

- 환경 변수로 API Key를 주입하는 방식
- 외부 API 실패를 애플리케이션 예외로 변환하는 방식
- 모델명에 따라 Provider를 선택하는 분기(`AIChatService.save`)

## 6. 예외와 사용자 메시지

인증 오류는 `AuthException`으로 사용자에게 전달할 상태 코드, 에러 코드, 메시지를 함께 관리합니다.

학습 포인트:

- 기술적 오류와 사용자에게 보여줄 메시지를 분리하는 방식
- Controller에서 예외를 받아 redirect query 또는 JSON 응답으로 변환하는 방식
- 외부 API 응답을 내부 예외 모델로 매핑하는 방식

## 7. 추천 코드 읽기 순서

1. `presentation.controller.ChatController`
2. `application.service.AIChatService`
3. `infrastructure.repository.SupabaseChatRepository`
4. `presentation.controller.AuthController`
5. `application.service.SupabaseAuthService`
6. `infrastructure.api.SupabaseAuthClient`
7. `infrastructure.session.SessionManager`
8. `presentation.filter.AuthFilter`

이 순서로 읽으면 HTTP 요청이 Controller에서 시작해 Service, Repository, 외부 API, DB로 이어지는 흐름을 자연스럽게 따라갈 수 있습니다.
