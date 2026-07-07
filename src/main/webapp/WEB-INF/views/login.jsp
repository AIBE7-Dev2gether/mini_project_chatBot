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
                        accent: '#9A3B3B'
                    },
                    fontFamily: {
                        sans: ['Pretendard', 'sans-serif'],
                        serif: ['"Noto Serif KR"', 'serif']
                    }
                }
            }
        }
    </script>
</head>
<body class="min-h-screen bg-paper text-ink font-sans antialiased flex items-center justify-center px-6">
<main class="w-full max-w-md">
    <section class="border border-ink/10 rounded-[28px] bg-white/60 backdrop-blur px-8 py-10 shadow-sm">
        <header class="mb-8 text-center">
            <p class="font-sans text-xs uppercase tracking-[0.3em] text-ink/45 mb-3">ArChat Access</p>
            <h1 class="font-serif text-3xl font-black">로그인</h1>
            <p class="mt-3 text-sm text-ink/60 leading-6">
                Supabase 인증 후 서버 세션으로 로그인 상태를 유지합니다.
            </p>
        </header>

        <% if (request.getParameter("error") != null) { %>
            <div class="mb-5 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                <%= request.getParameter("error") %>
            </div>
        <% } %>
        <% if ("1".equals(request.getParameter("expired"))) { %>
            <div class="mb-5 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
                세션이 만료되었습니다. 다시 로그인해 주세요.
            </div>
        <% } %>
        <% if ("1".equals(request.getParameter("logout"))) { %>
            <div class="mb-5 rounded-2xl border border-ink/10 bg-white px-4 py-3 text-sm text-ink/70">
                로그아웃되었습니다.
            </div>
        <% } %>

        <form action="<%= request.getContextPath() %>/login" method="post" class="space-y-5">
            <div>
                <label for="email" class="mb-2 block text-sm text-ink/70">이메일</label>
                <input
                        id="email"
                        name="email"
                        type="email"
                        required
                        autocomplete="email"
                        class="w-full rounded-2xl border border-ink/15 bg-white px-4 py-3 text-sm focus:border-accent"
                />
            </div>
            <div>
                <label for="password" class="mb-2 block text-sm text-ink/70">비밀번호</label>
                <input
                        id="password"
                        name="password"
                        type="password"
                        required
                        autocomplete="current-password"
                        class="w-full rounded-2xl border border-ink/15 bg-white px-4 py-3 text-sm focus:border-accent"
                />
            </div>
            <button
                    type="submit"
                    class="w-full rounded-2xl bg-ink px-4 py-3 font-serif text-base font-bold text-paper transition hover:bg-accent"
            >
                로그인
            </button>
        </form>

        <p class="mt-6 text-center text-sm text-ink/60">
            계정이 없으면
            <a href="<%= request.getContextPath() %>/signup" class="text-accent underline underline-offset-4">회원가입</a>
        </p>
    </section>
</main>
</body>
</html>
