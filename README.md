# ArChat (아카이브 챗)

Java Servlet과 JSP를 기반으로 작성된 챗봇 웹 애플리케이션입니다. 구글의 Gemini AI API를 활용하여 사용자의 메시지에 자동으로 답변하며, Supabase(PostgreSQL)를 연동하여 채팅 내역을 안전하게 영구 저장합니다.

## 📁 관련 문서 (Docs)
프로젝트의 세부 설계 및 데이터베이스 구조는 `docs/` 디렉토리 내의 마크다운 문서를 참고하세요.
* [데이터 흐름도 (Data Flow)](docs/dataFlow/data_flow.md)
* [데이터베이스 스키마 및 ERD (Database Schema)](docs/database/database_schema.md)

## 🏗️ 프로젝트 구조 (Layered/Hexagonal Architecture)

본 프로젝트는 유지보수성과 확장성을 높이기 위해 도메인, 애플리케이션, 인프라스트럭처, 프레젠테이션 계층으로 역할을 명확히 분리하여 설계되었습니다.

```text
archat/
├── docs/                                    # 프로젝트 관련 문서 디렉토리
│   ├── data_flow.md                         # 데이터 흐름도
│   └── database_schema.md                   # DB 구조 및 ERD
├── src/main/java/com/example/archat/
│   ├── application/                         # 비즈니스 로직 및 유즈케이스 계층
│   │   └── service/
│   │       └── AIChatService.java           # 채팅 저장 및 AI API 호출 통합 서비스 로직
│   ├── domain/                              # 핵심 도메인 모델 계층
│   │   ├── model/
│   │   │   └── Chat.java                    # 채팅 메시지 모델 (Record)
│   │   └── repository/
│   │       └── ChatRepository.java          # 데이터 저장소 인터페이스
│   ├── infrastructure/                      # 외부 API 통신 및 DB 연결 계층
│   │   ├── api/
│   │   │   ├── GenAIChatProvider.java       # Gemini API 호출 구현체
│   │   │   └── GroqChatProvider.java        # Groq 등 기타 AI 호출 구현체
│   │   ├── db/
│   │   │   └── DatabaseUtil.java            # Supabase(PostgreSQL) JDBC 연결 관리
│   │   └── repository/
│   │       └── SupabaseChatRepository.java  # JDBC를 이용한 Supabase 기반 채팅 저장소 구현체
│   └── presentation/                        # 사용자 요청 처리 계층 (Controller)
│       └── controller/
│           └── ChatController.java          # HTTP 요청 처리 및 뷰(JSP) 포워딩
├── src/main/webapp/
│   ├── WEB-INF/
│   │   └── views/
│   │       └── chat.jsp                     # 채팅 화면을 렌더링하는 JSP 페이지
└── pom.xml                                  # Maven 의존성 관리 (google-genai, postgresql 등)
```

## 🚀 환경 변수 설정 및 실행 방법

이 프로젝트는 외부 API(Gemini)와 외부 데이터베이스(Supabase)를 사용합니다. Tomcat 서버를 실행하기 전 시스템 환경 변수(또는 톰캣 플러그인을 통한 `.env` 연동)에 다음 값들이 반드시 설정되어야 합니다.

**필수 환경 변수:**
- `GEMINI_API_KEY` : Google Gemini API 키
- `GROQ_API_KEY` : GROQ API 키
- `NIM_API_KEY` : NIM API 키
- `SUPABASE_DB_URL` : Supabase 데이터베이스 JDBC 연결 풀러 주소 (예: `jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres`)
- `SUPABASE_DB_USER` : Supabase 데이터베이스 접속 유저 (예: `postgres.[project-ref]`)
- `SUPABASE_DB_PASSWORD` : Supabase 데이터베이스 비밀번호

> ⚠️ 보안 권고사항: GitHub 저장소에 비밀번호나 API 키가 포함된 `.env` 파일이 올라가지 않도록 주의하세요. 설정 가이드를 제공하기 위해 비밀값이 비워진 `.env.sample` 파일을 활용하시기 바랍니다.

## 🔧 최근 업데이트 내역
- AI 에이전트 작업 표준화 및 아키텍처 규칙 강제를 위한 가이드라인(`AGENT.md`) 추가
- 메모리 기반 저장소(`InMemoryChatRepository`)에서 **Supabase 기반 영구 저장소(`SupabaseChatRepository`)**로 마이그레이션 완료
- `docs/` 폴더 분리를 통한 문서화(ERD 다이어그램 포함) 구조 개선
- 계층형 아키텍처 패턴 적용에 따른 패키지 구조 전면 개편
