<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>대화의 기록</title>
    
    <!-- 웹 폰트 (Pretendard & Noto Serif KR) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;700;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.css">

    <!-- Tailwind CSS v3 CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        paper: '#FAF9F4',     // 따뜻한 아이보리 종이 느낌
                        ink: '#2A2A2A',       // 완전한 검은색이 아닌 짙은 먹색
                        meta: '#888888',      // 메타 정보용 회색
                        accent: '#9A3B3B',    // 벽돌색(Terracotta) 액센트
                    },
                    fontFamily: {
                        sans: ['Pretendard', 'sans-serif'],
                        serif: ['"Noto Serif KR"', 'serif'],
                    },
                    keyframes: {
                        fadeInUp: {
                            '0%': { opacity: '0', transform: 'translateY(15px)' },
                            '100%': { opacity: '1', transform: 'translateY(0)' },
                        }
                    },
                    animation: {
                        'fade-in-up': 'fadeInUp 0.6s ease-out forwards',
                    }
                }
            }
        }
    </script>
    
    <style>
        body { background-color: #FAF9F4; }
        
        /* 스크롤바마저도 숨기거나 극도로 얇게 처리하여 문서 느낌 강조 */
        ::-webkit-scrollbar { width: 4px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #E0DDD0; }
        
        /* 포커스시 기본 아웃라인 제거 (Tailwind border로 대체) */
        *:focus { outline: none; }
    </style>
</head>
<body class="bg-paper text-ink font-sans antialiased min-h-screen flex flex-col selection:bg-accent selection:text-white">

    <!-- 헤더: 여백을 많이 주고 세리프 폰트로 제목만 툭 던져둠 -->
    <header class="pt-16 pb-12 text-center border-b border-ink/5 mx-auto w-full max-w-2xl px-6">
        <h1 class="font-serif text-3xl font-black tracking-tight text-ink mb-3">대화의 기록</h1>
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
            
            <!-- 말풍선이나 테두리 없이, 단락(Paragraph)과 여백만으로 대화를 구분 -->
            <article class="mb-14 animate-fade-in-up opacity-0" style="animation-delay: 0.1s;">
                <!-- 발화자 및 시간 정보 -->
                <header class="mb-3 flex items-baseline flex-wrap gap-x-3 gap-y-1">
                    <span class="font-serif font-bold text-lg ${isUser ? 'text-ink' : 'text-accent'}">
                        ${isUser ? 'Q.' : 'A.'}
                    </span>
                    
                    <time datetime="${chat.timestamp}" class="font-sans text-xs text-meta/70 tabular-nums">
                        ${chat.timestamp}
                    </time>
                </header>
                
                <!-- 대화 내용: 본문용 폰트(Pretendard)로 가독성을 극대화하고 자간/행간을 넓게 씀 -->
                <div class="px-5 py-4 border ${isUser ? 'border-ink/10 rounded-2xl rounded-tl-sm' : 'border-accent/15 rounded-2xl rounded-tr-sm bg-accent/5'} font-sans text-[1.05rem] leading-[1.8] text-ink/90 whitespace-pre-wrap break-words">${chat.message}</div>
            </article>
        </c:forEach>
    </main>

    <!-- 입력 폼 영역: 버튼이나 네모난 박스 없이 밑줄 하나로 처리 -->
    <footer class="bg-paper pb-12 pt-6 sticky bottom-0">
        <div class="w-full h-12 bg-gradient-to-t from-paper to-transparent absolute bottom-full left-0 pointer-events-none"></div>
        <div class="max-w-2xl mx-auto px-6">
            <form id="chatForm" action="<c:url value="/chat"/>" method="post" class="flex flex-col gap-4">
                
                <!-- 대화 입력 밑줄 UI -->
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
                        <!-- 로딩 스피너 (초기 숨김) -->
                        <div id="loadingSpinner" class="hidden w-4 h-4 border-2 border-accent/20 border-t-accent rounded-full animate-spin"></div>
                    </button>
                </div>

                <!-- 모델 선택 (우측 하단 작게 배치) -->
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
                        </ul>
                    </div>
                </div>
            </form>
        </div>
    </footer>

    <!-- 자바스크립트 로직 (이전의 타이핑 애니메이션을 에디토리얼 무드에 맞게 이식) -->
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            // 타임스탬프 포맷팅 (긴 서버 날짜를 "오후 5:09" 형태로 깔끔하게 변환)
            document.querySelectorAll('time').forEach(function(timeEl) {
                var rawDate = timeEl.getAttribute('datetime');
                if(rawDate) {
                    try {
                        var d = new Date(rawDate.replace(/\[.*\]$/, '')); // [Asia/Seoul] 등 제거
                        if(!isNaN(d.getTime())) {
                            timeEl.textContent = d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
                        }
                    } catch(e) {}
                }
            });

            var chatHistory = document.getElementById('chatHistory');
            var chatForm = document.getElementById('chatForm');
            var sendBtn = document.getElementById('sendBtn');
            var sendText = document.getElementById('sendText');
            var sendArrow = document.getElementById('sendArrow');
            var loadingSpinner = document.getElementById('loadingSpinner');
            var messageInput = document.getElementById('message-input');

            // 커스텀 모델 선택 드롭다운 로직
            var selectBtn = document.getElementById('custom-select-btn');
            var selectDropdown = document.getElementById('custom-select-dropdown');
            var selectArrow = document.getElementById('custom-select-arrow');
            var selectHidden = document.getElementById('model-select-hidden');
            var selectText = document.getElementById('custom-select-text');
            var selectOptions = selectDropdown.querySelectorAll('button[data-value]');

            function openDropdown() {
                selectDropdown.classList.remove('opacity-0', 'invisible', 'scale-95');
                selectDropdown.classList.add('opacity-100', 'visible', 'scale-100');
                selectArrow.style.transform = 'rotate(180deg)';
            }

            function closeDropdown() {
                selectDropdown.classList.add('opacity-0', 'invisible', 'scale-95');
                selectDropdown.classList.remove('opacity-100', 'visible', 'scale-100');
                selectArrow.style.transform = 'rotate(0deg)';
            }

            selectBtn.addEventListener('click', function(e) {
                e.preventDefault();
                var isExpanded = selectDropdown.classList.contains('opacity-100');
                if (isExpanded) {
                    closeDropdown();
                } else {
                    openDropdown();
                }
            });

            selectOptions.forEach(function(opt) {
                opt.addEventListener('click', function(e) {
                    e.preventDefault();
                    selectHidden.value = e.target.getAttribute('data-value');
                    selectText.textContent = e.target.textContent;
                    
                    selectOptions.forEach(function(o) { o.classList.remove('font-bold'); });
                    e.target.classList.add('font-bold');
                    
                    closeDropdown();
                });
            });

            document.addEventListener('click', function(e) {
                if (!selectBtn.contains(e.target) && !selectDropdown.contains(e.target)) {
                    closeDropdown();
                }
            });

            // 스크롤 최하단 이동
            if (chatHistory) {
                chatHistory.scrollTop = chatHistory.scrollHeight;
            }

            if(chatForm) {
                chatForm.addEventListener('submit', function(e) {
                    if(messageInput.value.trim() === '') {
                        e.preventDefault();
                        return;
                    }

                    // 전송 버튼 상태 변경
                    sendBtn.disabled = true;
                    sendText.classList.add('hidden');
                    sendArrow.classList.add('hidden');
                    loadingSpinner.classList.remove('hidden');

                    // 에디토리얼 무드에 맞는 AI 로딩(작성 중) 요소 추가
                    var loadingRow = document.createElement('article');
                    loadingRow.className = 'mb-14 animate-fade-in-up opacity-0';
                    loadingRow.style.animationDelay = '0.1s';
                    loadingRow.innerHTML = 
                        '<header class="mb-3 flex items-baseline gap-3">' +
                            '<span class="font-serif font-bold text-lg text-accent">A.</span>' +
                        '</header>' +
                        '<div class="px-5 py-4 border border-accent/15 rounded-2xl rounded-tr-sm bg-accent/5 font-sans text-[1.05rem] leading-[1.8] text-ink/50 italic flex gap-1 items-center">' +
                            '문장을 가다듬는 중' +
                            '<span class="flex gap-0.5 ml-1">' +
                                '<span class="w-1 h-1 bg-ink/50 rounded-full animate-bounce" style="animation-delay: -0.32s"></span>' +
                                '<span class="w-1 h-1 bg-ink/50 rounded-full animate-bounce" style="animation-delay: -0.16s"></span>' +
                                '<span class="w-1 h-1 bg-ink/50 rounded-full animate-bounce"></span>' +
                            '</span>' +
                        '</div>';
                    
                    chatHistory.appendChild(loadingRow);
                    chatHistory.scrollTop = chatHistory.scrollHeight;
                });
            }
        });
    </script>
</body>
</html>
