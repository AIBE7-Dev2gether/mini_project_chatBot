-- 1. 대화방(chat_rooms) 테이블 생성
CREATE TABLE chat_rooms (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. 기존 chats 테이블에 room_id 컬럼 추가
ALTER TABLE chats ADD COLUMN room_id VARCHAR(255);
