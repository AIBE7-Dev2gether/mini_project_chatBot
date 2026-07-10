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
    // marked 옵션 설정
    if (typeof marked !== 'undefined') {
        marked.setOptions({
            breaks: true,
            gfm: true
        });
    }

    // 기존 채팅 데이터 마크다운 파싱
    document.querySelectorAll('.chat-article').forEach(function(article) {
        var rawNode = article.querySelector('.raw-content');
        var parsedNode = article.querySelector('.parsed-content');
        if (rawNode && parsedNode && typeof marked !== 'undefined' && typeof DOMPurify !== 'undefined') {
            parsedNode.innerHTML = DOMPurify.sanitize(marked.parse(rawNode.textContent));
        }
    });

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
            userRow.className = 'mb-10 animate-fade-in-up chat-article flex flex-col items-end';
            
            var userParsedMessage = (typeof marked !== 'undefined' && typeof DOMPurify !== 'undefined') 
                ? DOMPurify.sanitize(marked.parse(message)) 
                : escapeHtml(message);

            userRow.innerHTML = 
                '<header class="mb-2 flex items-baseline flex-row-reverse gap-x-2 gap-y-1">' +
                    '<span class="font-serif font-bold text-base text-ink/80">Q.</span>' +
                    '<time class="font-sans text-xs text-meta/60 tabular-nums">방금 전</time>' +
                '</header>' +
                '<div class="px-5 py-3.5 max-w-[85%] sm:max-w-[75%] bg-ink text-paper rounded-2xl rounded-tr-sm markdown-body-user">' +
                    '<div class="parsed-content">' + userParsedMessage + '</div>' +
                '</div>';
            
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
            loadingRow.className = 'mb-10 animate-fade-in-up flex flex-col items-start';
            loadingRow.innerHTML = 
                '<header class="mb-2 flex items-baseline gap-2">' +
                    '<span class="font-serif font-bold text-base text-accent">A.</span>' +
                '</header>' +
                '<div class="w-full pt-1 font-sans text-base text-ink/50 italic flex gap-1 items-center">' +
                    '문장을 가다듬는 중' +
                    '<span class="flex gap-0.5 ml-1">' +
                        '<span class="w-1 h-1 bg-ink/40 rounded-full animate-bounce" style="animation-delay: -0.32s"></span>' +
                        '<span class="w-1 h-1 bg-ink/40 rounded-full animate-bounce" style="animation-delay: -0.16s"></span>' +
                        '<span class="w-1 h-1 bg-ink/40 rounded-full animate-bounce"></span>' +
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

                var aiParsedMessage = (typeof marked !== 'undefined' && typeof DOMPurify !== 'undefined') 
                    ? DOMPurify.sanitize(marked.parse(data.message)) 
                    : escapeHtml(data.message);

                var aiRow = document.createElement('article');
                aiRow.className = 'mb-10 animate-fade-in-up chat-article flex flex-col items-start';
                aiRow.innerHTML = 
                    '<header class="mb-2 flex items-baseline gap-x-2 gap-y-1">' +
                        '<span class="font-serif font-bold text-base text-accent">A.</span>' +
                        '<time class="font-sans text-xs text-meta/60 tabular-nums">' + formattedTime + '</time>' +
                    '</header>' +
                    '<div class="w-full pt-1 markdown-body text-ink/90">' +
                        '<div class="parsed-content">' + aiParsedMessage + '</div>' +
                    '</div>';
                
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
