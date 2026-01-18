CREATE TABLE video_stats (
                             video_id BIGINT PRIMARY KEY,
                             view_count BIGINT DEFAULT 0,
                             like_count INT DEFAULT 0,
                             comment_count INT DEFAULT 0
);