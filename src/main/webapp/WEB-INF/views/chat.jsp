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
<body class="bg-paper text-ink font-sans antialiased min-h-screen flex selection:bg-accent selection:text-white">

    <!-- 사이드바 -->
    <aside class="w-64 border-r border-ink/5 bg-paper flex flex-col pt-8 pb-6 h-screen sticky top-0 hidden md:flex shrink-0">
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
            <div class="text-xs text-meta/70 truncate mb-3">${currentUserEmail}</div>
            <form action="<c:url value='/logout'/>" method="post">
                <button type="submit" class="font-sans text-[0.65rem] uppercase tracking-[0.2em] text-accent hover:text-ink transition-colors">로그아웃</button>
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
                <article class="mb-14 animate-fade-in-up opacity-0" style="animation-delay: 0.1s;">
                    <header class="mb-3 flex items-baseline flex-wrap gap-x-3 gap-y-1">
                        <span class="font-serif font-bold text-lg ${isUser ? 'text-ink' : 'text-accent'}">
                            ${isUser ? 'Q.' : 'A.'}
                        </span>
                        <time datetime="${chat.timestamp}" class="font-sans text-xs text-meta/70 tabular-nums">
                            ${chat.timestamp}
                        </time>
                    </header>
                    <div class="px-5 py-4 border ${isUser ? 'border-ink/10 rounded-2xl rounded-tl-sm' : 'border-accent/15 rounded-2xl rounded-tr-sm bg-accent/5'} font-sans text-[1.05rem] leading-[1.8] text-ink/90 whitespace-pre-wrap break-words">${chat.message}</div>
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
            <h2 class="font-serif text-xl font-bold text-ink mb-6">대화의 주제를 다시 쓰기</h2>
            
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
    <script>
        // 대화방 이름 변경 모달 로직
        let currentRenameRoomId = null;

        function renameRoom(roomId, currentTitle) {
            currentRenameRoomId = roomId;
            const modal = document.getElementById('renameModal');
            const modalContent = document.getElementById('renameModalContent');
            const input = document.getElementById('renameModalInput');
            
            input.value = currentTitle;
            
            // 모달 열기
            modal.classList.remove('opacity-0', 'pointer-events-none');
            modalContent.classList.remove('scale-95');
            modalContent.classList.add('scale-100');
            
            // 포커스 이동
            setTimeout(() => {
                input.focus();
                input.setSelectionRange(input.value.length, input.value.length);
            }, 100);
            
            // Enter 및 Esc 키 지원
            input.onkeydown = function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    submitRename();
                }
                if (e.key === 'Escape') {
                    closeRenameModal();
                }
            };
        }

        function closeRenameModal() {
            const modal = document.getElementById('renameModal');
            const modalContent = document.getElementById('renameModalContent');
            
            modal.classList.add('opacity-0', 'pointer-events-none');
            modalContent.classList.remove('scale-100');
            modalContent.classList.add('scale-95');
            
            currentRenameRoomId = null;
        }

        function submitRename() {
            if (!currentRenameRoomId) return;
            const input = document.getElementById('renameModalInput');
            const newTitle = input.value.trim();
            
            if (newTitle !== '') {
                document.getElementById('newTitle-' + currentRenameRoomId).value = newTitle;
                document.getElementById('renameForm-' + currentRenameRoomId).submit();
            } else {
                closeRenameModal();
            }
        }

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
                    localStorage.setItem('selectedModel', selectHidden.value);

                    selectOptions.forEach(function(o) { o.classList.remove('font-bold'); });
                    e.target.classList.add('font-bold');
                    
                    closeDropdown();
                });
            });

            // 저장된 모델 복원 (페이지 리로드 후 선택 유지)
            (function restoreModel() {
                var saved = localStorage.getItem('selectedModel');
                if (!saved) return;
                selectOptions.forEach(function(o) {
                    if (o.getAttribute('data-value') === saved) {
                        selectHidden.value = saved;
                        selectText.textContent = o.textContent;
                        selectOptions.forEach(function(x) { x.classList.remove('font-bold'); });
                        o.classList.add('font-bold');
                    }
                });
            })();

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
                    var message = messageInput.value.trim();
                    if(message === '') {
                        e.preventDefault();
                        return;
                    }
                    e.preventDefault();

                    // 1. 사용자 질문을 즉각적으로 화면에 렌더링 (Optimistic Update)
                    var emptyState = chatHistory.querySelector('.m-auto.text-center');
                    if (emptyState) emptyState.remove();

                    var userRow = document.createElement('article');
                    userRow.className = 'mb-14 animate-fade-in-up';
                    userRow.innerHTML = 
                        '<header class="mb-3 flex items-baseline flex-wrap gap-x-3 gap-y-1">' +
                            '<span class="font-serif font-bold text-lg text-ink">Q.</span>' +
                            '<time class="font-sans text-xs text-meta/70 tabular-nums">방금 전</time>' +
                        '</header>' +
                        '<div class="px-5 py-4 border border-ink/10 rounded-2xl rounded-tl-sm font-sans text-[1.05rem] leading-[1.8] text-ink/90 whitespace-pre-wrap break-words">' + escapeHtml(message) + '</div>';
                    
                    chatHistory.appendChild(userRow);
                    chatHistory.scrollTop = chatHistory.scrollHeight;

                    // 입력창 초기화 및 버튼 비활성화
                    messageInput.value = '';
                    messageInput.disabled = true;
                    sendBtn.disabled = true;
                    sendText.classList.add('hidden');
                    sendArrow.classList.add('hidden');
                    loadingSpinner.classList.remove('hidden');

                    // 2. 에디토리얼 무드에 맞는 AI 로딩(작성 중) 요소 추가
                    var loadingRow = document.createElement('article');
                    loadingRow.id = 'ai-loading';
                    loadingRow.className = 'mb-14 animate-fade-in-up';
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

                    // 3. 서버로 전송 (Fetch API)
                    var formData = new URLSearchParams();
                    formData.append('message', message);
                    formData.append('model', selectHidden.value);
                    formData.append('roomId', document.querySelector('input[name="roomId"]').value);

                    fetch(chatForm.action, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'Accept': 'application/json',
                            'X-Requested-With': 'XMLHttpRequest'
                        },
                        body: formData.toString()
                    })
                    .then(function(response) {
                        if (response.redirected) {
                            window.location.href = response.url; // 새 방이 생성되어 리다이렉트 된 경우
                            return null;
                        }
                        if (!response.ok) throw new Error('서버 통신 오류');
                        return response.json();
                    })
                    .then(function(data) {
                        if (!data) return; // 리다이렉트 진행 중

                        var loadingNode = document.getElementById('ai-loading');
                        if (loadingNode) loadingNode.remove();

                        // 날짜 포맷 (선택사항)
                        var formattedTime = '방금 전';
                        try {
                            var d = new Date(data.timestamp.replace(/\[.*\]$/, ''));
                            if(!isNaN(d.getTime())) {
                                formattedTime = d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
                            }
                        } catch(e) {}

                        var aiRow = document.createElement('article');
                        aiRow.className = 'mb-14 animate-fade-in-up';
                        aiRow.innerHTML = 
                            '<header class="mb-3 flex items-baseline flex-wrap gap-x-3 gap-y-1">' +
                                '<span class="font-serif font-bold text-lg text-accent">A.</span>' +
                                '<time class="font-sans text-xs text-meta/70 tabular-nums">' + formattedTime + '</time>' +
                            '</header>' +
                            '<div class="px-5 py-4 border border-accent/15 rounded-2xl rounded-tr-sm bg-accent/5 font-sans text-[1.05rem] leading-[1.8] text-ink/90 whitespace-pre-wrap break-words">' + escapeHtml(data.message) + '</div>';
                        
                        chatHistory.appendChild(aiRow);
                        chatHistory.scrollTop = chatHistory.scrollHeight;

                        restoreInput();
                    })
                    .catch(function(error) {
                        console.error('Error:', error);
                        var loadingNode = document.getElementById('ai-loading');
                        if (loadingNode) {
                            loadingNode.querySelector('div').innerHTML = '<span class="text-accent not-italic">오류가 발생했습니다. 잠시 후 다시 시도해주세요.</span>';
                        }
                        restoreInput();
                    });
                });
            }

            function restoreInput() {
                messageInput.disabled = false;
                sendBtn.disabled = false;
                sendText.classList.remove('hidden');
                sendArrow.classList.remove('hidden');
                loadingSpinner.classList.add('hidden');
                messageInput.focus();
            }

            function escapeHtml(unsafe) {
                return unsafe
                     .replace(/&/g, "&amp;")
                     .replace(/</g, "&lt;")
                     .replace(/>/g, "&gt;")
                     .replace(/"/g, "&quot;")
                     .replace(/'/g, "&#039;");
            }
        });
    </script>
</body>
</html>
