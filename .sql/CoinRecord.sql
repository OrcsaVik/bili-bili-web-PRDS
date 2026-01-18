-- Unified Coin Table (Transactional)
CREATE TABLE t_interact_coin (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 biz_id BIGINT NOT NULL,
                                 biz_type TINYINT NOT NULL,
                                 amount INT DEFAULT 1,
                                 UNIQUE KEY uk_user_biz (user_id, biz_id, biz_type)
);

-----------------------------------------

-- Unified Like Table
CREATE TABLE t_interact_like (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 biz_id BIGINT NOT NULL COMMENT 'The VideoID or ArticleID',
                                 biz_type TINYINT NOT NULL COMMENT '1:Video, 2:Article',
                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE KEY uk_user_biz (user_id, biz_id, biz_type) -- One like per item
);


-- Unified Collection/Favorite
CREATE TABLE t_interact_fav (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                biz_id BIGINT NOT NULL,
                                biz_type TINYINT NOT NULL,
                                folder_id BIGINT DEFAULT 0,
                                UNIQUE KEY uk_user_biz_folder (user_id, biz_id, biz_type, folder_id)
);