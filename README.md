# ArChat (아카이브 챗)

Java Servlet과 JSP를 기반으로 작성된 챗봇 웹 애플리케이션입니다. 구글의 Gemini AI API를 활용하여 사용자의 메시지에 자동으로 답변하는 기능을 제공합니다.

## 프로젝트 구조 (Layered Architecture)

본 프로젝트는 비즈니스 로직과 데이터 접근을 분리하기 위해 고전적인 MVC 패턴에 Service와 Repository 계층을 추가하여 구성되어 있습니다.

```text
archat/
├── src/main/java/com/example/archat/
│   ├── config/
│   │   └── GenAIConfig.java             # Gemini API 클라이언트 설정 및 시스템 프롬프트 관리
│   ├── controller/
│   │   ├── dto/
│   │   │   └── ChatResponseDTO.java     # View(JSP)로 데이터를 전달하기 위한 DTO
│   │   ├── BaseController.java          # 공통 뷰 경로 등 서블릿 공통 로직 관리
│   │   └── ChatController.java          # /chat 경로 요청 처리 및 뷰 포워딩
│   ├── model/
│   │   ├── repository/
│   │   │   ├── ChatRepository.java        # 채팅 데이터 저장소 인터페이스
│   │   │   └── InMemoryChatRepository.java# ConcurrentHashMap을 이용한 메모리 저장소 구현체
│   │   └── Chat.java                    # 채팅 메시지의 도메인 모델 (Record)
│   └── service/
│       └── ChatService.java             # 메시지 저장, AI 호출(useAI) 등 핵심 비즈니스 로직
├── src/main/webapp/
│   ├── WEB-INF/
│   │   ├── views/
│   │   │   └── chat.jsp                 # 채팅 내역을 렌더링하는 뷰 페이지
│   │   └── web.xml                      # 웹 애플리케이션 설정 파일
└── pom.xml                              # Maven 의존성 관리 파일 (google-genai, jstl 등)
```

## 핵심 동작 흐름 (Data Flow)

1. **클라이언트 요청**: 브라우저에서 `/chat` 으로 접속 (또는 메시지 전송 POST 요청).
2. **Controller (컨트롤러)**: `ChatController`가 요청을 받아 세션 ID를 식별합니다.
3. **Service (서비스)**: `ChatService`에 비즈니스 로직 처리를 위임합니다.
   - 메시지 조회: `ChatService.readHistory(sessionId)`
   - 메시지 전송: 사용자 메시지 저장 -> `GenAIConfig`를 통한 Gemini API 호출 -> AI 응답 저장
4. **Repository (레포지토리)**: `InMemoryChatRepository`가 메모리(Map)상에 데이터를 Read/Write 합니다.
5. **DTO 변환 및 View**: 반환된 도메인 모델(`Chat`)을 `ChatResponseDTO`로 변환하여 JSP로 넘기고, `chat.jsp`가 최종 HTML을 렌더링하여 클라이언트에게 응답합니다.

## 실행 방법 및 환경 변수 설정

본 프로젝트는 Gemini AI를 사용하므로, 실행 환경(Tomcat)에 다음 환경 변수가 반드시 등록되어야 합니다.

- `GEMINI_API_KEY` : 발급받은 Google Gemini API 키 값

**IntelliJ에서 실행 시 설정 방법:**
1. Tomcat Run/Debug Configurations 창 오픈
2. Server 탭 -> `Environment variables` 항목 수정
3. `GEMINI_API_KEY=자신의_API_키` 추가 후 서버 재시작
