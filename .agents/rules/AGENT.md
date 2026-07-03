# AI Agent Guidelines

## 1. 📝 README.md 업데이트 규칙
새로운 기능이 추가되거나 기존 코드의 비즈니스 로직(데이터 구조, 아키텍처 흐름, 주요 기능 등)에 변경이 발생한 경우, **반드시 `README.md`를 함께 업데이트**해야 합니다.
- **문서 동기화:** 로직 변경이 이루어졌다면 작업 완료 전에 프로젝트 루트에 있는 `README.md` (또는 `docs/` 디렉토리 내의 관련 문서)를 갱신합니다.
- 반영 내용: 아키텍처 및 패키지 구조 변경 사항, 데이터 흐름/DB 스키마 내역, 최근 업데이트 내역.

## 2. 🏗️ 아키텍처 무결성 유지 (Architecture Constraints)
현재 프로젝트의 헥사고날(Port & Adapter) / 계층형 구조의 원칙을 반드시 준수합니다.
- **Domain 의존성 금지:** `domain` 계층 내의 코드는 순수 자바 코드로 작성하며, 외부 계층(DB, API 호출, 서블릿 등)을 절대 참조(import)하지 마세요.
- **인터페이스 기반 개발:** 외부 서비스(AI, DB 등)를 연동할 때는 반드시 `application.port` 혹은 `domain.repository`에 인터페이스를 먼저 정의하고, `infrastructure` 계층에서 이를 구현(Adapter)하세요.

## 3. 🧩 의존성 주입 및 프레임워크 제약 (No-Spring)
이 프로젝트는 Spring Framework 같은 DI 컨테이너를 사용하지 않는 순수 Servlet/JSP 프로젝트입니다.
- **수동 싱글톤 관리:** Service나 Repository 객체를 생성할 때는 애노테이션(`@Service`, `@Autowired` 등)을 사용하지 말고, **수동 싱글톤 패턴(`getInstance()`)**을 사용하여 인스턴스를 관리하세요.
- **책임 분리:** `Controller`에서는 비즈니스 로직을 처리하지 말고, 반드시 `Service` 클래스에 위임하세요.

## 4. 🔒 보안 및 환경 변수 처리 (Security & Config)
코드 내 보안 위협을 방지하기 위한 필수 규칙입니다.
- **하드코딩 금지:** Supabase 접속 정보(URL, DB User, Password), Gemini API 키, Groq API 키 등은 절대 코드 내에 하드코딩하지 마세요.
- **환경 변수 활용:** 모든 민감한 정보는 `System.getenv()` 등을 통해 서버 환경 변수로 주입받도록 구성해야 하며, 새로운 키가 추가될 경우 템플릿 파일인 `.env.sample`을 반드시 업데이트하세요.

## 5. 🌐 PRG (Post-Redirect-Get) 패턴 준수
안정적인 웹 애플리케이션 동작을 위한 규칙입니다.
- `ChatController` 등에서 HTTP POST 요청을 통해 데이터베이스나 상태를 변경한 후에는, **반드시 Redirect(예: `resp.sendRedirect()`)**로 화면을 전환하여 새로고침 시 데이터가 중복 전송되는 문제를 방지하세요.
