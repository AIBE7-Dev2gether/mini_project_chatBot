<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;700;900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.css">
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        paper: '#FAF9F4',
                        ink: '#2A2A2A',
                        meta: '#888888',
                        accent: '#9A3B3B',
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
        *:focus { outline: none; }
    </style>
</head>
<body class="min-h-screen bg-paper text-ink font-sans antialiased flex flex-col items-center justify-center selection:bg-accent selection:text-white">
    <div class="w-full max-w-sm mx-auto px-6 -mt-10">
        <!-- 헤더 -->
        <header class="mb-12 text-center animate-fade-in-up opacity-0" style="animation-delay: 0.1s;">
            <h1 class="font-serif text-4xl font-black tracking-tight text-ink">로그인</h1>
        </header>

        <main class="animate-fade-in-up opacity-0" style="animation-delay: 0.2s;">
            <% if (request.getParameter("error") != null) { %>
                <div class="mb-8 text-center text-sm font-serif italic text-accent">
                    * <%= request.getParameter("error") %>
                </div>
            <% } %>

            <% if ("1".equals(request.getParameter("logout"))) { %>
                <div class="mb-8 text-center text-sm font-serif italic text-meta/80">
                    로그아웃되었습니다.
                </div>
            <% } %>

            <form action="<%= request.getContextPath() %>/login" method="post" class="space-y-10">
                <!-- 이메일 입력 (밑줄) -->
                <div class="relative group border-b border-ink/20 pb-2 transition-colors focus-within:border-accent">
                    <label for="email" class="absolute -top-5 left-0 text-[0.65rem] uppercase tracking-widest text-meta/70 transition-colors group-focus-within:text-accent">E-Mail</label>
                    <input
                            id="email"
                            name="email"
                            type="email"
                            required
                            autocomplete="email"
                            placeholder="your@email.com"
                            class="w-full bg-transparent border-none p-0 text-ink font-serif text-lg placeholder:text-meta/30 placeholder:italic focus:ring-0"
                    />
                </div>

                <!-- 비밀번호 입력 (밑줄) -->
                <div class="relative group border-b border-ink/20 pb-2 transition-colors focus-within:border-accent">
                    <label for="password" class="absolute -top-5 left-0 text-[0.65rem] uppercase tracking-widest text-meta/70 transition-colors group-focus-within:text-accent">Password</label>
                    <input
                            id="password"
                            name="password"
                            type="password"
                            required
                            autocomplete="current-password"
                            placeholder="••••••••"
                            class="w-full bg-transparent border-none p-0 text-ink font-serif text-lg placeholder:text-meta/30 placeholder:italic focus:ring-0 tracking-[0.2em]"
                    />
                </div>

                <!-- 블록 형태 버튼 -->
                <div class="pt-4">
                    <button
                            type="submit"
                            class="w-full bg-ink text-paper py-3.5 rounded-sm font-sans font-bold text-[0.85rem] tracking-[0.2em] uppercase transition-colors hover:bg-ink/80 focus:ring-2 focus:ring-offset-2 focus:ring-offset-paper focus:ring-ink"
                    >
                        Log In
                    </button>
                </div>
            </form>

            <p class="mt-12 text-center text-[0.8rem] text-meta/70 tracking-wide font-sans">
                아직 빈 페이지가 필요하신가요?<br>
                <a href="<%= request.getContextPath() %>/signup" class="inline-block mt-2 text-ink font-bold border-b border-ink hover:text-accent hover:border-accent transition-colors pb-0.5">회원가입</a>
            </p>
        </main>
    </div>
</body>
</html>
