-- MySQL dump 10.13  Distrib 8.4.0, for Win64 (x86_64)
--
-- Host: 192.168.33.10    Database: Bilibili-test
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_auth_element_operation`
--

DROP TABLE IF EXISTS `t_auth_element_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_auth_element_operation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `element_name` varchar(255) DEFAULT NULL COMMENT '页面元素名称',
  `element_code` varchar(50) DEFAULT NULL COMMENT '页面元素唯一编码',
  `operation_type` varchar(5) DEFAULT NULL COMMENT '操作类型：0可点击，1可见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限控制--页面元素操作表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_auth_element_operation`
--

LOCK TABLES `t_auth_element_operation` WRITE;
/*!40000 ALTER TABLE `t_auth_element_operation` DISABLE KEYS */;
INSERT INTO `t_auth_element_operation` VALUES (1,'视频投稿按钮','postVideoButton','0','2023-01-24 09:37:06','2023-01-24 09:37:06');
/*!40000 ALTER TABLE `t_auth_element_operation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_auth_menu`
--

DROP TABLE IF EXISTS `t_auth_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_auth_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(255) DEFAULT NULL COMMENT '菜单项目名称',
  `code` varchar(50) DEFAULT NULL COMMENT '唯一编码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限控制--页面访问表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_auth_menu`
--

LOCK TABLES `t_auth_menu` WRITE;
/*!40000 ALTER TABLE `t_auth_menu` DISABLE KEYS */;
INSERT INTO `t_auth_menu` VALUES (1,'购买邀请码','buyInviteCode','2023-01-24 13:02:03','2023-01-24 13:02:03');
/*!40000 ALTER TABLE `t_auth_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_auth_role`
--

DROP TABLE IF EXISTS `t_auth_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_auth_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(255) DEFAULT NULL COMMENT '角色名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `code` varchar(50) DEFAULT NULL COMMENT '唯一编码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限控制--角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_auth_role`
--

LOCK TABLES `t_auth_role` WRITE;
/*!40000 ALTER TABLE `t_auth_role` DISABLE KEYS */;
INSERT INTO `t_auth_role` VALUES (1,'等级0','2023-01-24 09:34:56','2023-01-24 09:34:56','Lv0'),(2,'等级1','2023-01-24 09:35:08','2023-01-24 09:35:08','Lv1'),(3,'等级2','2023-01-24 09:35:19','2023-01-24 09:35:19','Lv2');
/*!40000 ALTER TABLE `t_auth_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_auth_role_element_operation`
--

DROP TABLE IF EXISTS `t_auth_role_element_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_auth_role_element_operation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `element_operation_id` bigint DEFAULT NULL COMMENT '元素操作id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限控制--角色与元素操作关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_auth_role_element_operation`
--

LOCK TABLES `t_auth_role_element_operation` WRITE;
/*!40000 ALTER TABLE `t_auth_role_element_operation` DISABLE KEYS */;
INSERT INTO `t_auth_role_element_operation` VALUES (1,2,1,'2023-01-24 09:38:22');
/*!40000 ALTER TABLE `t_auth_role_element_operation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_auth_role_menu`
--

DROP TABLE IF EXISTS `t_auth_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_auth_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `menu_id` bigint DEFAULT NULL COMMENT '页面菜单id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限控制--角色页面菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_auth_role_menu`
--

LOCK TABLES `t_auth_role_menu` WRITE;
/*!40000 ALTER TABLE `t_auth_role_menu` DISABLE KEYS */;
INSERT INTO `t_auth_role_menu` VALUES (1,2,1,'2023-01-24 13:03:54');
/*!40000 ALTER TABLE `t_auth_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_collection_group`
--

DROP TABLE IF EXISTS `t_collection_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_collection_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `name` varchar(50) DEFAULT NULL COMMENT '收藏分组名称',
  `type` varchar(5) DEFAULT NULL COMMENT '收藏分组类型：0默认收藏分组',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_collection_group`
--

LOCK TABLES `t_collection_group` WRITE;
/*!40000 ALTER TABLE `t_collection_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_collection_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_danmu`
--

DROP TABLE IF EXISTS `t_danmu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_danmu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `video_id` bigint DEFAULT NULL COMMENT '视频id',
  `content` text COMMENT '弹幕内容',
  `danmu_time` varchar(50) DEFAULT NULL COMMENT '弹幕出现时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='弹幕表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_danmu`
--

LOCK TABLES `t_danmu` WRITE;
/*!40000 ALTER TABLE `t_danmu` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_danmu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_file`
--

DROP TABLE IF EXISTS `t_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `url` varchar(500) DEFAULT NULL COMMENT '文件存储路径',
  `type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `md5` varchar(500) DEFAULT NULL COMMENT '文件MD5唯一标识',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上传文件信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_file`
--

LOCK TABLES `t_file` WRITE;
/*!40000 ALTER TABLE `t_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_following_group`
--

DROP TABLE IF EXISTS `t_following_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_following_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `name` varchar(50) DEFAULT NULL COMMENT '关注分组名称',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `type` varchar(5) DEFAULT NULL COMMENT '关注分组类型：0特别关注，1悄悄关注，2默认关注，3用户自定义关注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注分组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_following_group`
--

LOCK TABLES `t_following_group` WRITE;
/*!40000 ALTER TABLE `t_following_group` DISABLE KEYS */;
INSERT INTO `t_following_group` VALUES (1,NULL,'特别关注','2023-01-23 05:07:55','2023-01-23 05:07:55','0'),(2,NULL,'悄悄关注','2023-01-23 05:08:11','2023-01-23 05:08:11','1'),(3,NULL,'默认关注','2023-01-23 05:08:57','2023-01-23 05:08:57','2');
/*!40000 ALTER TABLE `t_following_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_interact_coin`
--

DROP TABLE IF EXISTS `t_interact_coin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_interact_coin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `biz_id` bigint NOT NULL,
  `biz_type` tinyint NOT NULL,
  `amount` int DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_biz` (`user_id`,`biz_id`,`biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='this file is to make record the coin with media type';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_interact_coin`
--

LOCK TABLES `t_interact_coin` WRITE;
/*!40000 ALTER TABLE `t_interact_coin` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_interact_coin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_refresh_token`
--

DROP TABLE IF EXISTS `t_refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_refresh_token` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `refresh_token` varchar(500) DEFAULT NULL COMMENT '刷新令牌',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='刷新令牌记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_refresh_token`
--

LOCK TABLES `t_refresh_token` WRITE;
/*!40000 ALTER TABLE `t_refresh_token` DISABLE KEYS */;
INSERT INTO `t_refresh_token` VALUES (2,2,'eyJraWQiOiIyIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiLnrb7lj5HogIUiLCJleHAiOjE2NzU0ODEyMTd9.A4Oi8_f-pOTZ6C1hJx6Md80GN6hAsEDGVzQoFKnWDz8LZNUOu_8r0mXt1bIotfjz9duMrTJGxBussp6ey1vBPPzFiK4JGL9bAIUDIH67eeBxS9s6xwU6pFNrHFGj3ks1HCGiFC44koDL0ddZL_YbFcQ1-sbjn_-fL_eNelCMeSo','2023-01-28 03:26:57');
/*!40000 ALTER TABLE `t_refresh_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_tag`
--

DROP TABLE IF EXISTS `t_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_tag`
--

LOCK TABLES `t_tag` WRITE;
/*!40000 ALTER TABLE `t_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `phone` varchar(100) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `salt` varchar(50) DEFAULT NULL COMMENT '盐值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(2) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (1,'13887654321',NULL,'62261c47d94c9986818709780e4eb6da','1674445314548','2023-01-23 03:41:54','2025-12-28 02:59:54','0'),(2,'13987654321',NULL,'9b603b98859fec10a5613ad0aa313ac2','1674540758877','2023-01-24 06:12:39','2025-12-28 02:59:57','0'),(3,'123','123','$2a$10$FzWZDHwkgOYFp1hc20TL4ep.0U/opnumuRuP9hvezQtxIr9YguE2G',NULL,'2025-12-28 10:58:06','2025-12-28 10:58:06','0');
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_coin`
--

DROP TABLE IF EXISTS `t_user_coin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_coin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `amount` bigint DEFAULT NULL COMMENT '硬币总数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户硬币数量表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_coin`
--

LOCK TABLES `t_user_coin` WRITE;
/*!40000 ALTER TABLE `t_user_coin` DISABLE KEYS */;
INSERT INTO `t_user_coin` VALUES (1,3,0,'2025-12-28 10:58:06','2025-12-28 10:58:06');
/*!40000 ALTER TABLE `t_user_coin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_following`
--

DROP TABLE IF EXISTS `t_user_following`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_following` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `following_id` bigint DEFAULT NULL COMMENT '关注用户id',
  `group_id` bigint DEFAULT NULL COMMENT '关注分组id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_following`
--

LOCK TABLES `t_user_following` WRITE;
/*!40000 ALTER TABLE `t_user_following` DISABLE KEYS */;
INSERT INTO `t_user_following` VALUES (2,1,2,3,'2023-01-24 06:13:14','2023-01-24 06:13:14');
/*!40000 ALTER TABLE `t_user_following` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_info`
--

DROP TABLE IF EXISTS `t_user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id（关联）',
  `nick` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(1024) DEFAULT NULL COMMENT '头像',
  `sign` text COMMENT '签名',
  `gender` varchar(2) DEFAULT NULL COMMENT '性别：0男，1女，2未知',
  `birth` varchar(20) DEFAULT NULL COMMENT '生日',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_info`
--

LOCK TABLES `t_user_info` WRITE;
/*!40000 ALTER TABLE `t_user_info` DISABLE KEYS */;
INSERT INTO `t_user_info` VALUES (1,1,'零零落落test',NULL,NULL,'1','2000-01-01','2023-01-23 03:41:54','2023-01-23 04:19:05'),(2,2,'萌新',NULL,NULL,'2','2000-01-01','2023-01-24 06:12:39','2023-01-24 06:12:39'),(3,3,'萌新',NULL,NULL,'2','2000-01-01','2025-12-28 10:58:06','2025-12-28 10:58:06');
/*!40000 ALTER TABLE `t_user_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_moments`
--

DROP TABLE IF EXISTS `t_user_moments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_moments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `type` varchar(5) DEFAULT NULL COMMENT '动态类型：0视频，1直播，2动态专栏',
  `content_id` bigint DEFAULT NULL COMMENT '内容详情id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户动态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_moments`
--

LOCK TABLES `t_user_moments` WRITE;
/*!40000 ALTER TABLE `t_user_moments` DISABLE KEYS */;
INSERT INTO `t_user_moments` VALUES (11,2,'0',2,'2023-01-24 13:58:05','2023-01-24 13:58:05'),(12,2,'0',2,'2023-01-24 13:58:47','2023-01-24 13:58:47');
/*!40000 ALTER TABLE `t_user_moments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_role`
--

DROP TABLE IF EXISTS `t_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `role_id` bigint DEFAULT NULL COMMENT '角色id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_role`
--

LOCK TABLES `t_user_role` WRITE;
/*!40000 ALTER TABLE `t_user_role` DISABLE KEYS */;
INSERT INTO `t_user_role` VALUES (1,1,1,'2023-01-24 09:39:07'),(2,2,2,'2023-01-24 13:41:45');
/*!40000 ALTER TABLE `t_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_coin`
--

DROP TABLE IF EXISTS `t_video_coin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_coin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `video_id` bigint DEFAULT NULL COMMENT '视频投稿id',
  `amount` int DEFAULT NULL COMMENT '投币数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频投币记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_coin`
--

LOCK TABLES `t_video_coin` WRITE;
/*!40000 ALTER TABLE `t_video_coin` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_coin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_collection`
--

DROP TABLE IF EXISTS `t_video_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `video_id` bigint DEFAULT NULL COMMENT '视频投稿id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `group_id` bigint DEFAULT NULL COMMENT '收藏分组',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_collection`
--

LOCK TABLES `t_video_collection` WRITE;
/*!40000 ALTER TABLE `t_video_collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_comment`
--

DROP TABLE IF EXISTS `t_video_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `video_id` bigint NOT NULL COMMENT '视频id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `comment` text NOT NULL COMMENT '评论',
  `reply_user_id` bigint DEFAULT NULL COMMENT '回复用户id',
  `root_id` bigint DEFAULT NULL COMMENT '根结点评论id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_comment`
--

LOCK TABLES `t_video_comment` WRITE;
/*!40000 ALTER TABLE `t_video_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_like`
--

DROP TABLE IF EXISTS `t_video_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `video_id` bigint NOT NULL COMMENT '视频投稿id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_like`
--

LOCK TABLES `t_video_like` WRITE;
/*!40000 ALTER TABLE `t_video_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_operation`
--

DROP TABLE IF EXISTS `t_video_operation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_operation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `video_id` bigint DEFAULT NULL COMMENT '视频id',
  `operation_type` varchar(5) DEFAULT NULL COMMENT '操作类型:0点赞，1收藏，2投币',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户操作表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_operation`
--

LOCK TABLES `t_video_operation` WRITE;
/*!40000 ALTER TABLE `t_video_operation` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_operation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_stats`
--

DROP TABLE IF EXISTS `t_video_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_stats` (
  `video_id` bigint NOT NULL,
  `view_count` bigint DEFAULT '0',
  `like_count` int DEFAULT '0',
  `comment_count` int DEFAULT '0',
  PRIMARY KEY (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_stats`
--

LOCK TABLES `t_video_stats` WRITE;
/*!40000 ALTER TABLE `t_video_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_tag`
--

DROP TABLE IF EXISTS `t_video_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `video_id` bigint NOT NULL COMMENT '视频id',
  `tag_id` bigint NOT NULL COMMENT '标签id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_tag`
--

LOCK TABLES `t_video_tag` WRITE;
/*!40000 ALTER TABLE `t_video_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_video_view`
--

DROP TABLE IF EXISTS `t_video_view`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_video_view` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `video_id` bigint NOT NULL COMMENT '视频id',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `client_id` varchar(500) DEFAULT NULL COMMENT '客户端id',
  `ip` varchar(50) DEFAULT NULL COMMENT 'ip',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视频观看记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_video_view`
--

LOCK TABLES `t_video_view` WRITE;
/*!40000 ALTER TABLE `t_video_view` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_video_view` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_videos`
--

DROP TABLE IF EXISTS `t_videos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_videos` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `area` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'beijing',
  `status` tinyint NOT NULL DEFAULT '0',
  `oss_bucket` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `oss_raw_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `oss_hls_manifest` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `duration` int DEFAULT '0',
  `cover_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_videos`
--

LOCK TABLES `t_videos` WRITE;
/*!40000 ALTER TABLE `t_videos` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_videos` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-29 19:13:29
