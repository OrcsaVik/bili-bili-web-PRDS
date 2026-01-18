CREATE TABLE videos (
                        id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, -- or Snowflake ID
                        user_id BIGINT NOT NULL,                       -- Owner
                        title VARCHAR(255) NOT NULL,
                        description TEXT,

                        area VARCHAR(64) NOT NULL DEFAULT "beijing",

    -- STATE MACHINE (The most important column)
                        status TINYINT NOT NULL DEFAULT 0,
    -- 0: Created (Pre-upload)
    -- 1: Uploaded (Raw file in OSS, waiting for queue)
    -- 2: Processing (Transcoding in progress)
    -- 3: Published (Ready to stream)
    -- 4: Failed (Transcoding error)
    -- 5: Banned (Policy violation)

    -- STORAGE LINKS
                        oss_bucket VARCHAR(64) NOT NULL,
                        oss_raw_key VARCHAR(255) NOT NULL,  -- Path to original MP4 upload
                        oss_hls_manifest VARCHAR(255),      -- Path to the master .m3u8 (NULL until processed)

    -- METADATA
                        duration INT DEFAULT 0,             -- Seconds
                        cover_url VARCHAR(255),             -- Thumbnail path

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        INDEX idx_user (user_id),
                        INDEX idx_status (status), -- For finding "Processing" or "Published" videos
                        INDEX idx_created (created_at)
);