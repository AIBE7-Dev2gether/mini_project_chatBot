// tailwind.config.js
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
                handwriting: ['"KyoboHandwriting2021Seongjiyoung"', 'cursive'],
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
