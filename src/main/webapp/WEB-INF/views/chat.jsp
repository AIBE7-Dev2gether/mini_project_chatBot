<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>대화의 기록</title>
    
    <!-- Markdown Parser & DOMPurify -->
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/dompurify/3.0.6/purify.min.js"></script>

    <jsp:include page="/WEB-INF/views/includes/head.jsp" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/chat.css">
</head>
<body class="bg-paper text-ink font-sans antialiased min-h-screen flex selection:bg-accent selection:text-white">

    <!-- 사이드바 -->
    <aside class="w-64 border-r border-ink/5 bg-paper flex flex-col pt-8 pb-6 h-screen sticky top-0 hidden md:flex shrink-0 font-handwriting">
        <!-- 새 방 만들기 버튼 -->
        <div class="px-6 mb-8">
            <form action="<c:url value='/chat'/>" method="post">
                <input type="hidden" name="action" value="createRoom">
                <button type="submit" class="w-full flex items-center justify-center py-2.5 bg-ink text-paper rounded-sm hover:bg-ink/80 transition-colors font-bold text-[0.85rem] tracking-wide">
                    + 새 대화방
                </button>
            </form>
        </div>
        
        <!-- 대화방 목록 -->
        <nav class="flex-1 overflow-y-auto px-4 flex flex-col gap-1" aria-label="대화방 목록">
            <c:forEach var="room" items="${rooms}">
                <c:set var="isActive" value="${room.id == currentRoomId}" />
                <div class="group flex items-center justify-between px-3 py-2.5 rounded-sm transition-colors ${isActive ? 'bg-ink/5' : 'hover:bg-ink/5'}">
                    <a href="<c:url value='/chat'/>?roomId=${room.id}" class="flex-1 truncate text-sm ${isActive ? 'font-bold text-ink' : 'text-ink/70 hover:text-ink'} transition-colors">
                        ${room.title}
                    </a>
                    <div class="opacity-0 group-hover:opacity-100 focus-within:opacity-100 transition-opacity ml-2 shrink-0 flex items-center">
                        <form id="renameForm-${room.id}" action="<c:url value='/chat'/>" method="post" class="hidden">
                            <input type="hidden" name="action" value="renameRoom">
                            <input type="hidden" name="roomId" value="${room.id}">
                            <input type="hidden" name="newTitle" id="newTitle-${room.id}" value="">
                        </form>
                        <button type="button" class="text-meta/40 hover:text-ink text-sm px-1.5 py-1 leading-none" title="이름 변경" onclick="renameRoom('${room.id}', this.closest('.group').querySelector('a').innerText.trim())">
                            ✎
                        </button>
                        <form action="<c:url value='/chat'/>" method="post" class="inline-block">
                            <input type="hidden" name="action" value="deleteRoom">
                            <input type="hidden" name="roomId" value="${room.id}">
                            <button type="submit" class="text-meta/40 hover:text-accent text-sm px-1.5 py-1 leading-none" title="삭제" onclick="return confirm('이 대화방을 정말 삭제할까요?');">
                                ✕
                            </button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </nav>
        
        <!-- 하단: 유저 정보 및 로그아웃 -->
        <div class="mt-auto px-6 pt-6 border-t border-ink/5">
            <div class="text-base text-meta/70 truncate mb-3">${currentUserEmail}</div>
            <form action="<c:url value='/logout'/>" method="post">
                <button type="submit" class="text-[0.9rem] uppercase tracking-[0.1em] text-accent hover:text-ink transition-colors">로그아웃</button>
            </form>
        </div>
    </aside>

    <!-- 오른쪽 본문 영역 -->
    <div class="flex-1 flex flex-col min-h-screen w-full relative">
        <!-- 헤더 -->
        <header class="pt-16 pb-12 text-center border-b border-ink/5 mx-auto w-full max-w-2xl px-6">
            <h1 class="font-serif text-3xl font-black tracking-tight text-ink mb-2">대화의 기록</h1>
        </header>

        <!-- 대화 본문 영역 -->
        <main id="chatHistory" class="flex-1 w-full max-w-2xl mx-auto px-6 py-12 flex flex-col overflow-y-auto scroll-smooth" aria-live="polite">
            <c:if test="${empty chats}">
                <div class="m-auto text-center animate-fade-in-up opacity-0" style="animation-delay: 0.2s;">
                    <p class="font-serif text-xl text-ink/70 leading-relaxed text-balance">
                        아직 쓰여지지 않은 페이지입니다.<br>
                        하단에서 첫 질문을 던져 이야기를 시작해 보세요.
                    </p>
                </div>
            </c:if>

            <c:forEach var="chat" items="${chats}">
                <c:set var="isUser" value="${chat.owner == 'user' || chat.owner == 'USER' || chat.owner == 'User'}" />
                <article class="mb-10 animate-fade-in-up opacity-0 chat-article flex flex-col ${isUser ? 'items-end' : 'items-start'}" style="animation-delay: 0.1s;">
                    <header class="mb-2 flex items-baseline gap-x-2 gap-y-1 ${isUser ? 'flex-row-reverse' : ''}">
                        <span class="font-serif font-bold text-base ${isUser ? 'text-ink/80' : 'text-accent'}">
                            ${isUser ? 'Q.' : 'A.'}
                        </span>
                        <time datetime="${chat.timestamp}" class="font-sans text-xs text-meta/60 tabular-nums">
                            ${chat.timestamp}
                        </time>
                    </header>
                    <c:choose>
                        <c:when test="${isUser}">
                            <div class="px-5 py-3.5 max-w-[85%] sm:max-w-[75%] bg-ink text-paper rounded-2xl rounded-tr-sm markdown-body-user">
                                <div class="raw-content hidden"><c:out value="${chat.message}" /></div>
                                <div class="parsed-content"></div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="w-full pt-1 markdown-body text-ink/90">
                                <div class="raw-content hidden"><c:out value="${chat.message}" /></div>
                                <div class="parsed-content"></div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </article>
            </c:forEach>
        </main>

        <!-- 입력 폼 영역 -->
        <footer class="bg-paper pb-12 pt-6 sticky bottom-0">
            <div class="w-full h-12 bg-gradient-to-t from-paper to-transparent absolute bottom-full left-0 pointer-events-none"></div>
            <div class="max-w-2xl mx-auto px-6">
                <form id="chatForm" action="<c:url value="/chat"/>" method="post" class="flex flex-col gap-4">
                    <input type="hidden" name="roomId" value="${currentRoomId}">
                    
                    <div class="flex items-center gap-4 border-b border-ink/20 pb-3 transition-colors focus-within:border-accent">
                        <label for="message-input" class="sr-only">메시지 입력</label>
                        <input
                            type="text"
                            id="message-input"
                            name="message"
                            class="flex-1 bg-transparent border-none p-0 text-ink font-serif text-lg 
                                   placeholder:text-meta/40 placeholder:italic focus:ring-0"
                            placeholder="이곳에 당신의 생각을 적어주세요..."
                            required
                            autocomplete="off"
                            spellcheck="false"
                        />
                        
                        <button type="submit" id="sendBtn" aria-label="기록하기"
                                class="font-serif font-bold text-accent/80 hover:text-accent transition-colors group flex items-center gap-1 disabled:opacity-50">
                            <span id="sendText">기록하기</span>
                            <span id="sendArrow" class="inline-block transition-transform group-hover:translate-x-1">→</span>
                            <div id="loadingSpinner" class="hidden w-4 h-4 border-2 border-accent/20 border-t-accent rounded-full animate-spin"></div>
                        </button>
                    </div>

                    <!-- 모델 선택 -->
                    <div class="self-end relative" id="custom-select-container">
                        <label for="model-select-hidden" class="sr-only">모델 선택</label>
                        <input type="hidden" name="model" id="model-select-hidden" value="gemini-3.1-flash-lite">
                        
                        <button type="button" id="custom-select-btn" class="flex items-center gap-1.5 bg-transparent border-none text-xs text-meta/70 font-sans cursor-pointer hover:text-ink transition-colors focus:ring-0 group">
                            <span id="custom-select-text">Gemini 3.1 Flash Lite</span>
                            <span class="text-[0.6rem] transition-transform duration-200" id="custom-select-arrow">▼</span>
                        </button>

                        <div id="custom-select-dropdown" class="absolute bottom-full right-0 mb-2 w-44 bg-paper border border-ink/10 shadow-sm rounded-sm opacity-0 invisible transition-all duration-200 origin-bottom-right transform scale-95 z-50">
                            <ul class="py-1 flex flex-col font-sans text-xs">
                                <li>
                                    <button type="button" class="w-full text-left px-3 py-2 text-ink/70 hover:text-ink hover:bg-ink/5 transition-colors" data-value="gemma-4-26b-a4b-it">Gemma 4 (26B)</button>
                                </li>
                                <li>
                                    <button type="button" class="w-full text-left px-3 py-2 text-ink/70 hover:text-ink hover:bg-ink/5 transition-colors" data-value="gemma-4-31b-it">Gemma 4 (31B)</button>
                                </li>
                                <li>
                                    <button type="button" class="w-full text-left px-3 py-2 text-ink/70 hover:text-ink hover:bg-ink/5 transition-colors font-bold" data-value="gemini-3.1-flash-lite">Gemini 3.1 Flash Lite</button>
                                </li>
                                <li>
                                    <button type="button" class="w-full text-left px-3 py-2 text-ink/70 hover:text-ink hover:bg-ink/5 transition-colors" data-value="openai/gpt-oss-120b">GPT OSS 120B (Groq)</button>
                                </li>
                                <li>
                                    <button type="button" class="w-full text-left px-3 py-2 text-ink/70 hover:text-ink hover:bg-ink/5 transition-colors" data-value="meta/llama-3.1-8b-instruct">Llama 3.1 8B (NIM)</button>
                                </li>
                            </ul>
                        </div>
                    </div>
                </form>
            </div>
        </footer>
    </div>

    <!-- 이름 변경 모달 -->
    <div id="renameModal" class="fixed inset-0 z-50 flex items-center justify-center opacity-0 pointer-events-none transition-opacity duration-300">
        <!-- 배경 딤(Dim) -->
        <div class="absolute inset-0 bg-ink/10 backdrop-blur-sm transition-opacity" onclick="closeRenameModal()"></div>
        
        <!-- 모달 창 -->
        <div class="relative bg-paper border border-ink/10 shadow-lg p-8 w-full max-w-sm transform scale-95 transition-transform duration-300 mx-4" id="renameModalContent">
            <h2 class="font-serif text-xl font-bold text-ink mb-6">대화방 이름 수정</h2>
            
            <div class="relative group border-b border-ink/20 pb-2 mb-8 transition-colors focus-within:border-accent">
                <input
                    type="text"
                    id="renameModalInput"
                    class="w-full bg-transparent border-none p-0 text-ink font-serif text-lg placeholder:text-meta/30 focus:ring-0"
                    autocomplete="off"
                    spellcheck="false"
                />
            </div>
            
            <div class="flex items-center justify-end gap-5 font-sans text-xs tracking-widest uppercase">
                <button type="button" onclick="closeRenameModal()" class="text-meta/60 hover:text-ink transition-colors">
                    취소
                </button>
                <button type="button" onclick="submitRename()" class="text-ink font-bold hover:text-accent transition-colors">
                    확인
                </button>
            </div>
        </div>
    </div>

    <!-- 자바스크립트 로직 -->
<script src="${pageContext.request.contextPath}/js/chat.js"></script>
</body>
</html>
