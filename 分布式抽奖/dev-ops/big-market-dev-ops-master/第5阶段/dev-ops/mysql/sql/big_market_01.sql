# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: big_market_01
# 生成时间: 2024-10-20 10:30:38 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `big_market_01` default character set utf8mb4 collate utf8mb4_0900_ai_ci;

use `big_market_01`;

# 转储表 raffle_activity_account
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_account`;

CREATE TABLE `raffle_activity_account` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `total_count` int(8) NOT NULL COMMENT '总次数',
  `total_count_surplus` int(8) NOT NULL COMMENT '总次数-剩余',
  `day_count` int(8) NOT NULL COMMENT '日次数',
  `day_count_surplus` int(8) NOT NULL COMMENT '日次数-剩余',
  `month_count` int(8) NOT NULL COMMENT '月次数',
  `month_count_surplus` int(8) NOT NULL COMMENT '月次数-剩余',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动账户表';

LOCK TABLES `raffle_activity_account` WRITE;
/*!40000 ALTER TABLE `raffle_activity_account` DISABLE KEYS */;

INSERT INTO `raffle_activity_account` (`id`, `user_id`, `activity_id`, `total_count`, `total_count_surplus`, `day_count`, `day_count_surplus`, `month_count`, `month_count_surplus`, `create_time`, `update_time`)
VALUES
	(3,'xiaofuge',100301,1188,1078,1188,1078,1188,1078,'2024-03-23 16:38:57','2024-10-20 15:09:49'),
	(4,'12345',100301,10,10,10,10,10,10,'2024-05-01 15:28:50','2024-05-01 15:28:50'),
	(5,'liergou',100301,20,6,20,6,20,6,'2024-05-04 15:30:21','2024-05-04 15:34:10'),
	(6,'liergou2',100301,100,86,100,86,100,86,'2024-05-04 15:35:52','2024-05-04 15:37:37'),
	(7,'user003',100301,100,98,100,98,100,98,'2024-05-25 10:52:19','2024-05-25 10:54:30'),
	(8,'xiaofuge26',100301,1,1,1,1,1,1,'2024-06-22 10:22:35','2024-06-22 10:22:35'),
	(9,'xfg',100401,102,96,102,96,102,96,'2024-10-06 12:22:14','2024-10-20 16:38:45'),
	(10,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,110,105,110,105,110,105,'2024-10-18 22:42:30','2024-10-20 10:34:28'),
	(11,'xfg-test02',100401,2,2,2,2,2,2,'2024-10-20 16:48:11','2024-10-20 18:15:52');

/*!40000 ALTER TABLE `raffle_activity_account` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_account_day
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_account_day`;

CREATE TABLE `raffle_activity_account_day` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `day` varchar(10) NOT NULL COMMENT '日期（yyyy-mm-dd）',
  `day_count` int(8) NOT NULL COMMENT '日次数',
  `day_count_surplus` int(8) NOT NULL COMMENT '日次数-剩余',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_id_activity_id_day` (`user_id`,`activity_id`,`day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动账户表-日次数';

LOCK TABLES `raffle_activity_account_day` WRITE;
/*!40000 ALTER TABLE `raffle_activity_account_day` DISABLE KEYS */;

INSERT INTO `raffle_activity_account_day` (`id`, `user_id`, `activity_id`, `day`, `day_count`, `day_count_surplus`, `create_time`, `update_time`)
VALUES
	(2,'xiaofuge',100301,'2024-04-05',45,44,'2024-04-05 17:10:31','2024-04-05 17:10:31'),
	(3,'xiaofuge',100301,'2024-04-08',45,44,'2024-04-08 22:52:47','2024-04-08 22:52:47'),
	(4,'xiaofuge',100301,'2024-04-13',45,23,'2024-04-13 11:44:10','2024-04-20 10:51:09'),
	(7,'xiaofuge',100301,'2024-04-20',45,13,'2024-04-20 16:50:38','2024-04-20 16:50:38'),
	(11,'xiaofuge',100301,'2024-05-01',60,40,'2024-05-01 14:51:45','2024-05-01 17:45:10'),
	(12,'xiaofuge',100301,'2024-05-03',90,86,'2024-05-03 09:00:28','2024-05-03 13:28:42'),
	(13,'xiaofuge',100301,'2024-05-04',160,131,'2024-05-04 09:32:02','2024-05-04 15:29:56'),
	(14,'liergou',100301,'2024-05-04',20,6,'2024-05-04 15:30:36','2024-05-04 15:34:10'),
	(15,'liergou2',100301,'2024-05-04',100,86,'2024-05-04 15:35:56','2024-05-04 15:37:37'),
	(16,'user003',100301,'2024-05-25',100,98,'2024-05-25 10:53:19','2024-05-25 10:54:30'),
	(17,'xiaofuge',100301,'2024-05-30',160,154,'2024-05-30 07:24:01','2024-05-30 08:02:18'),
	(18,'xiaofuge',100301,'2024-06-09',660,659,'2024-06-09 16:39:17','2024-06-09 18:44:43'),
	(19,'xiaofuge',100301,'2024-06-10',660,637,'2024-06-10 08:16:55','2024-06-10 09:36:26'),
	(20,'xiaofuge',100301,'2024-06-15',1160,1157,'2024-06-15 20:02:16','2024-06-15 20:19:36'),
	(21,'xiaofuge',100301,'2024-06-29',1188,1173,'2024-06-29 15:01:04','2024-06-29 17:49:09'),
	(22,'xiaofuge',100301,'2024-06-30',1188,1143,'2024-06-30 12:03:48','2024-06-30 21:10:16'),
	(23,'xiaofuge',100301,'2024-07-06',1188,1184,'2024-07-06 11:53:19','2024-07-06 18:43:07'),
	(24,'xiaofuge',100301,'2024-07-13',1188,1186,'2024-07-13 09:17:44','2024-07-13 12:06:25'),
	(25,'xiaofuge',100301,'2024-07-20',1188,1130,'2024-07-20 09:19:56','2024-07-20 14:24:18'),
	(26,'xfg',100401,'2024-10-06',100,94,'2024-10-06 12:26:27','2024-10-06 16:10:22'),
	(27,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'2024-10-19',104,103,'2024-10-19 09:35:55','2024-10-19 16:15:17'),
	(28,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'2024-10-20',110,106,'2024-10-20 10:33:26','2024-10-20 10:34:28'),
	(29,'xiaofuge',100301,'2024-10-20',1188,1187,'2024-10-20 15:09:49','2024-10-20 15:09:49');

/*!40000 ALTER TABLE `raffle_activity_account_day` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_account_month
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_account_month`;

CREATE TABLE `raffle_activity_account_month` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `month` varchar(7) NOT NULL COMMENT '月（yyyy-mm）',
  `month_count` int(8) NOT NULL COMMENT '月次数',
  `month_count_surplus` int(8) NOT NULL COMMENT '月次数-剩余',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_id_activity_id_month` (`user_id`,`activity_id`,`month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动账户表-月次数';

LOCK TABLES `raffle_activity_account_month` WRITE;
/*!40000 ALTER TABLE `raffle_activity_account_month` DISABLE KEYS */;

INSERT INTO `raffle_activity_account_month` (`id`, `user_id`, `activity_id`, `month`, `month_count`, `month_count_surplus`, `create_time`, `update_time`)
VALUES
	(7,'xiaofuge',100301,'2024-05',140,79,'2024-05-01 14:51:45','2024-05-30 08:02:18'),
	(8,'liergou',100301,'2024-05',20,6,'2024-05-04 15:30:36','2024-05-04 15:34:10'),
	(9,'liergou2',100301,'2024-05',100,86,'2024-05-04 15:35:56','2024-05-04 15:37:37'),
	(10,'user003',100301,'2024-05',100,98,'2024-05-25 10:53:19','2024-05-25 10:54:30'),
	(11,'xiaofuge',100301,'2024-06',1188,1143,'2024-06-09 16:39:17','2024-06-30 21:10:16'),
	(12,'xiaofuge',100301,'2024-07',1188,1124,'2024-07-06 11:53:19','2024-07-20 14:24:18'),
	(13,'xfg',100401,'2024-10',102,96,'2024-10-06 12:26:27','2024-10-20 16:38:45'),
	(14,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'2024-10',110,105,'2024-10-19 09:35:55','2024-10-20 10:34:28'),
	(15,'xiaofuge',100301,'2024-10',1188,1187,'2024-10-20 15:09:49','2024-10-20 15:09:49');

/*!40000 ALTER TABLE `raffle_activity_account_month` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_order_000
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_order_000`;

CREATE TABLE `raffle_activity_order_000` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `sku` bigint(12) NOT NULL COMMENT '商品sku',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `total_count` int(8) NOT NULL COMMENT '总次数',
  `day_count` int(8) NOT NULL COMMENT '日次数',
  `month_count` int(8) NOT NULL COMMENT '月次数',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额【积分】',
  `state` varchar(16) NOT NULL DEFAULT 'complete' COMMENT '订单状态（complete）',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传的，确保幂等',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`,`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动单';

LOCK TABLES `raffle_activity_order_000` WRITE;
/*!40000 ALTER TABLE `raffle_activity_order_000` DISABLE KEYS */;

INSERT INTO `raffle_activity_order_000` (`id`, `user_id`, `sku`, `activity_id`, `activity_name`, `strategy_id`, `order_id`, `order_time`, `total_count`, `day_count`, `month_count`, `pay_amount`, `state`, `out_business_no`, `create_time`, `update_time`)
VALUES
	(1,'xfg',9014,100301,'测试活动',100006,'641340633741','2024-10-06 12:22:14',1,1,1,0.00,'completed','xfg_sku_20241006','2024-10-06 12:22:14','2024-10-06 12:22:14'),
	(2,'xfg',9901,100401,'OpenAi抽奖活动',10007,'709187540625','2024-10-20 16:11:53',1,1,1,0.00,'completed','xfg_sku_100101001010001','2024-10-20 16:11:52','2024-10-20 16:11:52'),
	(3,'xfg',9901,100401,'OpenAi抽奖活动',10007,'165680275659','2024-10-20 16:38:45',1,1,1,0.00,'completed','xfg_sku_178799200110','2024-10-20 16:38:45','2024-10-20 16:38:45');

/*!40000 ALTER TABLE `raffle_activity_order_000` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_order_001
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_order_001`;

CREATE TABLE `raffle_activity_order_001` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `sku` bigint(12) NOT NULL COMMENT '商品sku',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `total_count` int(8) NOT NULL COMMENT '总次数',
  `day_count` int(8) NOT NULL COMMENT '日次数',
  `month_count` int(8) NOT NULL COMMENT '月次数',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额【积分】',
  `state` varchar(16) NOT NULL DEFAULT 'complete' COMMENT '订单状态（complete）',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传的，确保幂等',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`,`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动单';



# 转储表 raffle_activity_order_002
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_order_002`;

CREATE TABLE `raffle_activity_order_002` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `sku` bigint(12) NOT NULL COMMENT '商品sku',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `total_count` int(8) NOT NULL COMMENT '总次数',
  `day_count` int(8) NOT NULL COMMENT '日次数',
  `month_count` int(8) NOT NULL COMMENT '月次数',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额【积分】',
  `state` varchar(16) NOT NULL DEFAULT 'complete' COMMENT '订单状态（complete）',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传的，确保幂等',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`,`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动单';

LOCK TABLES `raffle_activity_order_002` WRITE;
/*!40000 ALTER TABLE `raffle_activity_order_002` DISABLE KEYS */;

INSERT INTO `raffle_activity_order_002` (`id`, `user_id`, `sku`, `activity_id`, `activity_name`, `strategy_id`, `order_id`, `order_time`, `total_count`, `day_count`, `month_count`, `pay_amount`, `state`, `out_business_no`, `create_time`, `update_time`)
VALUES
	(1,'liergou2',9011,100301,'测试活动',100006,'987026967898','2024-05-04 07:35:53',100,100,100,0.00,'completed','liergou2_sku_20240504','2024-05-04 15:35:52','2024-05-04 15:35:52'),
	(2,'user003',9011,100301,'测试活动',100006,'700446814309','2024-05-25 02:52:20',100,100,100,0.00,'completed','user003_sku_20240525','2024-05-25 10:52:19','2024-05-25 10:52:19'),
	(3,'xfg-test02',9901,100401,'OpenAi抽奖活动',10007,'520229673615','2024-10-20 16:48:12',1,1,1,0.00,'completed','xfg-test02_sku_418301185073','2024-10-20 16:48:11','2024-10-20 16:48:11'),
	(4,'xfg-test02',9901,100401,'OpenAi抽奖活动',10007,'569665612535','2024-10-20 18:15:52',1,1,1,0.00,'completed','xfg-test02_sku_515019067766','2024-10-20 18:15:52','2024-10-20 18:15:52');

/*!40000 ALTER TABLE `raffle_activity_order_002` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 raffle_activity_order_003
# ------------------------------------------------------------

DROP TABLE IF EXISTS `raffle_activity_order_003`;

CREATE TABLE `raffle_activity_order_003` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `sku` bigint(12) NOT NULL COMMENT '商品sku',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `total_count` int(8) NOT NULL COMMENT '总次数',
  `day_count` int(8) NOT NULL COMMENT '日次数',
  `month_count` int(8) NOT NULL COMMENT '月次数',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额【积分】',
  `state` varchar(16) NOT NULL DEFAULT 'complete' COMMENT '订单状态（complete）',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传的，确保幂等',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`,`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖活动单';

LOCK TABLES `raffle_activity_order_003` WRITE;
/*!40000 ALTER TABLE `raffle_activity_order_003` DISABLE KEYS */;

INSERT INTO `raffle_activity_order_003` (`id`, `user_id`, `sku`, `activity_id`, `activity_name`, `strategy_id`, `order_id`, `order_time`, `total_count`, `day_count`, `month_count`, `pay_amount`, `state`, `out_business_no`, `create_time`, `update_time`)
VALUES
	(1,'xiaofuge26',9014,100301,'测试活动',100006,'197003210793','2024-06-22 02:22:35',1,1,1,0.00,'completed','xiaofuge26_sku_20240622','2024-06-22 10:22:35','2024-06-22 10:22:35'),
	(2,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9901,100401,'OpenAi抽奖活动',10007,'793594691258','2024-10-18 22:42:30',1,1,1,0.00,'completed','oxfA9w8-23yvwTmo2ombz0E4zJv4_sku_20241018','2024-10-18 22:42:30','2024-10-18 22:42:30'),
	(3,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9901,100401,'OpenAi抽奖活动',10007,'620969716231','2024-10-19 14:44:55',1,1,1,5.00,'completed','749423786782','2024-10-19 14:44:54','2024-10-19 14:44:55'),
	(4,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9901,100401,'OpenAi抽奖活动',10007,'142497723232','2024-10-19 14:45:11',1,1,1,5.00,'completed','289961143515','2024-10-19 14:45:10','2024-10-19 14:45:10'),
	(5,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9901,100401,'OpenAi抽奖活动',10007,'247433845736','2024-10-19 16:15:16',1,1,1,5.00,'completed','736734564403','2024-10-19 16:15:15','2024-10-19 16:15:15'),
	(6,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9901,100401,'OpenAi抽奖活动',10007,'663435095325','2024-10-19 16:15:17',1,1,1,5.00,'completed','445087851198','2024-10-19 16:15:17','2024-10-19 16:15:17'),
	(7,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9901,100401,'OpenAi抽奖活动',10007,'323709392245','2024-10-20 10:32:50',1,1,1,0.00,'completed','oxfA9w8-23yvwTmo2ombz0E4zJv4_sku_20241020','2024-10-20 10:32:50','2024-10-20 10:32:50'),
	(8,'oxfA9w8-23yvwTmo2ombz0E4zJv4',9902,100401,'OpenAi抽奖活动',10007,'460928984147','2024-10-20 10:33:05',5,5,5,10.00,'completed','021052884467','2024-10-20 10:33:04','2024-10-20 10:33:04');

/*!40000 ALTER TABLE `raffle_activity_order_003` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 task
# ------------------------------------------------------------

DROP TABLE IF EXISTS `task`;

CREATE TABLE `task` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `topic` varchar(32) NOT NULL COMMENT '消息主题',
  `message_id` varchar(11) DEFAULT NULL COMMENT '消息编号',
  `message` varchar(512) NOT NULL COMMENT '消息主体',
  `state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '任务状态；create-创建、completed-完成、fail-失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_message_id` (`message_id`),
  KEY `idx_state` (`state`),
  KEY `idx_create_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表，发送MQ';

LOCK TABLES `task` WRITE;
/*!40000 ALTER TABLE `task` DISABLE KEYS */;

INSERT INTO `task` (`id`, `user_id`, `topic`, `message_id`, `message`, `state`, `create_time`, `update_time`)
VALUES
	(21,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_rebate','85209475237','{\"data\":{\"bizId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241018\",\"rebateConfig\":\"10\",\"rebateType\":\"integral\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"85209475237\",\"timestamp\":1729262455309}','completed','2024-10-18 22:40:55','2024-10-18 22:40:55'),
	(22,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_rebate','53592463262','{\"data\":{\"bizId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4_sku_20241018\",\"rebateConfig\":\"9901\",\"rebateType\":\"sku\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"53592463262\",\"timestamp\":1729262455313}','completed','2024-10-18 22:40:55','2024-10-18 22:40:55'),
	(23,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','19850980202','{\"data\":{\"amount\":10,\"orderId\":\"157409343333\",\"outBusinessNo\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241018\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"19850980202\",\"timestamp\":1729262456320}','completed','2024-10-18 22:40:56','2024-10-18 22:40:56'),
	(24,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','04244520328','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"629553450287\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"04244520328\",\"timestamp\":1729301756922}','completed','2024-10-19 09:35:57','2024-10-19 09:35:57'),
	(25,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','86765017098','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"229072233917\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"86765017098\",\"timestamp\":1729302118539}','completed','2024-10-19 09:41:58','2024-10-19 09:41:58'),
	(26,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','92597467501','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"989222182178\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"92597467501\",\"timestamp\":1729302156966}','completed','2024-10-19 09:42:36','2024-10-19 09:42:36'),
	(27,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','42527045371','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"841654853533\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"42527045371\",\"timestamp\":1729302296132}','completed','2024-10-19 09:44:56','2024-10-19 09:44:56'),
	(28,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','64829644000','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"436056352579\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"64829644000\",\"timestamp\":1729302648400}','completed','2024-10-19 09:50:48','2024-10-19 09:50:48'),
	(29,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','35782344332','{\"data\":{\"amount\":-5.00,\"orderId\":\"355569749986\",\"outBusinessNo\":\"749423786782\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"35782344332\",\"timestamp\":1729320294971}','completed','2024-10-19 14:44:55','2024-10-19 14:44:55'),
	(30,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','24879141620','{\"data\":{\"amount\":-5.00,\"orderId\":\"748397938640\",\"outBusinessNo\":\"289961143515\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"24879141620\",\"timestamp\":1729320310797}','completed','2024-10-19 14:45:10','2024-10-19 14:45:10'),
	(31,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','11295816186','{\"data\":{\"awardId\":101,\"awardTitle\":\"随机积分\",\"orderId\":\"577222874492\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"11295816186\",\"timestamp\":1729320315608}','completed','2024-10-19 14:45:15','2024-10-19 14:45:15'),
	(32,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','73809992581','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"757423953290\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"73809992581\",\"timestamp\":1729325665363}','completed','2024-10-19 16:14:25','2024-10-19 16:14:25'),
	(33,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','88857490297','{\"data\":{\"amount\":-5.00,\"orderId\":\"841275706069\",\"outBusinessNo\":\"736734564403\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"88857490297\",\"timestamp\":1729325715772}','completed','2024-10-19 16:15:15','2024-10-19 16:15:15'),
	(34,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','11645829506','{\"data\":{\"amount\":-5.00,\"orderId\":\"473846116324\",\"outBusinessNo\":\"445087851198\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"11645829506\",\"timestamp\":1729325717496}','completed','2024-10-19 16:15:17','2024-10-19 16:15:17'),
	(35,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_rebate','13795287103','{\"data\":{\"bizId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241020\",\"rebateConfig\":\"10\",\"rebateType\":\"integral\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"13795287103\",\"timestamp\":1729391569376}','completed','2024-10-20 10:32:49','2024-10-20 10:32:49'),
	(36,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_rebate','72572755062','{\"data\":{\"bizId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4_sku_20241020\",\"rebateConfig\":\"9901\",\"rebateType\":\"sku\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"72572755062\",\"timestamp\":1729391569387}','completed','2024-10-20 10:32:49','2024-10-20 10:32:49'),
	(37,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','64642770064','{\"data\":{\"amount\":10,\"orderId\":\"132991772778\",\"outBusinessNo\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241020\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"64642770064\",\"timestamp\":1729391569673}','completed','2024-10-20 10:32:49','2024-10-20 10:32:49'),
	(38,'oxfA9w8-23yvwTmo2ombz0E4zJv4','credit_adjust_success','69273186440','{\"data\":{\"amount\":-10.00,\"orderId\":\"737934197183\",\"outBusinessNo\":\"021052884467\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"69273186440\",\"timestamp\":1729391584647}','completed','2024-10-20 10:33:04','2024-10-20 10:33:04'),
	(39,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','84719144444','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"138774860660\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"84719144444\",\"timestamp\":1729391606968}','completed','2024-10-20 10:33:27','2024-10-20 10:33:27'),
	(40,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','72631930357','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"214610372888\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"72631930357\",\"timestamp\":1729391631984}','completed','2024-10-20 10:33:51','2024-10-20 10:33:52'),
	(41,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','65469493545','{\"data\":{\"awardId\":101,\"awardTitle\":\"随机积分\",\"orderId\":\"853795415263\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"65469493545\",\"timestamp\":1729391644113}','completed','2024-10-20 10:34:04','2024-10-20 10:34:04'),
	(42,'oxfA9w8-23yvwTmo2ombz0E4zJv4','send_award','88795866190','{\"data\":{\"awardId\":110,\"awardTitle\":\"1次使用次数\",\"orderId\":\"589498615977\",\"userId\":\"oxfA9w8-23yvwTmo2ombz0E4zJv4\"},\"id\":\"88795866190\",\"timestamp\":1729391668090}','completed','2024-10-20 10:34:28','2024-10-20 10:34:28'),
	(51,'xfg','send_rebate','15775317066','{\"data\":{\"bizId\":\"xfg_sku_100101001010001\",\"rebateConfig\":\"9901\",\"rebateType\":\"sku\",\"userId\":\"xfg\"},\"id\":\"15775317066\",\"timestamp\":1729411910781}','completed','2024-10-20 16:11:52','2024-10-20 16:11:52'),
	(52,'xfg','send_rebate','15529886244','{\"data\":{\"bizId\":\"xfg_integral_100101001010001\",\"rebateConfig\":\"10\",\"rebateType\":\"integral\",\"userId\":\"xfg\"},\"id\":\"15529886244\",\"timestamp\":1729411912443}','completed','2024-10-20 16:11:52','2024-10-20 16:11:52'),
	(53,'xfg','credit_adjust_success','71434254130','{\"data\":{\"amount\":10,\"orderId\":\"110272427822\",\"outBusinessNo\":\"xfg_integral_100101001010001\",\"userId\":\"xfg\"},\"id\":\"71434254130\",\"timestamp\":1729411912964}','completed','2024-10-20 16:11:53','2024-10-20 16:11:53'),
	(54,'xfg','send_rebate','01641823679','{\"data\":{\"bizId\":\"xfg_sku_178799200110\",\"rebateConfig\":\"9901\",\"rebateType\":\"sku\",\"userId\":\"xfg\"},\"id\":\"01641823679\",\"timestamp\":1729413525000}','completed','2024-10-20 16:38:45','2024-10-20 16:38:45'),
	(55,'xfg','send_rebate','08483972228','{\"data\":{\"bizId\":\"xfg_integral_178799200110\",\"rebateConfig\":\"10\",\"rebateType\":\"integral\",\"userId\":\"xfg\"},\"id\":\"08483972228\",\"timestamp\":1729413525000}','completed','2024-10-20 16:38:45','2024-10-20 16:38:45'),
	(56,'xfg','credit_adjust_success','46115202584','{\"data\":{\"amount\":10,\"orderId\":\"485156147723\",\"outBusinessNo\":\"xfg_integral_178799200110\",\"userId\":\"xfg\"},\"id\":\"46115202584\",\"timestamp\":1729413525232}','completed','2024-10-20 16:38:45','2024-10-20 16:38:45'),
	(57,'xfg-test02','send_rebate','54199517948','{\"data\":{\"bizId\":\"xfg-test02_sku_418301185073\",\"rebateConfig\":\"9901\",\"rebateType\":\"sku\",\"userId\":\"xfg-test02\"},\"id\":\"54199517948\",\"timestamp\":1729414091402}','completed','2024-10-20 16:48:11','2024-10-20 16:48:11'),
	(58,'xfg-test02','send_rebate','39061262183','{\"data\":{\"bizId\":\"xfg-test02_integral_418301185073\",\"rebateConfig\":\"10\",\"rebateType\":\"integral\",\"userId\":\"xfg-test02\"},\"id\":\"39061262183\",\"timestamp\":1729414091406}','completed','2024-10-20 16:48:11','2024-10-20 16:48:11'),
	(59,'xfg-test02','credit_adjust_success','69377786529','{\"data\":{\"amount\":10,\"orderId\":\"481224690406\",\"outBusinessNo\":\"xfg-test02_integral_418301185073\",\"userId\":\"xfg-test02\"},\"id\":\"69377786529\",\"timestamp\":1729414092167}','completed','2024-10-20 16:48:12','2024-10-20 16:48:12'),
	(60,'xfg-test02','send_rebate','14845028991','{\"data\":{\"bizId\":\"xfg-test02_sku_515019067766\",\"rebateConfig\":\"9901\",\"rebateType\":\"sku\",\"userId\":\"xfg-test02\"},\"id\":\"14845028991\",\"timestamp\":1729419351537}','completed','2024-10-20 18:15:51','2024-10-20 18:15:51'),
	(61,'xfg-test02','send_rebate','35445982237','{\"data\":{\"bizId\":\"xfg-test02_integral_515019067766\",\"rebateConfig\":\"10\",\"rebateType\":\"integral\",\"userId\":\"xfg-test02\"},\"id\":\"35445982237\",\"timestamp\":1729419351547}','completed','2024-10-20 18:15:51','2024-10-20 18:15:51'),
	(62,'xfg-test02','credit_adjust_success','17094429783','{\"data\":{\"amount\":10,\"orderId\":\"182616870509\",\"outBusinessNo\":\"xfg-test02_integral_515019067766\",\"userId\":\"xfg-test02\"},\"id\":\"17094429783\",\"timestamp\":1729419352298}','completed','2024-10-20 18:15:52','2024-10-20 18:15:52');

/*!40000 ALTER TABLE `task` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_award_record_000
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_award_record_000`;

CREATE TABLE `user_award_record_000` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '抽奖订单ID【作为幂等使用】',
  `award_id` int(11) NOT NULL COMMENT '奖品ID',
  `award_title` varchar(128) NOT NULL COMMENT '奖品标题（名称）',
  `award_time` datetime NOT NULL COMMENT '中奖时间',
  `award_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '奖品状态；create-创建、completed-发奖完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_award_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户中奖记录表';

LOCK TABLES `user_award_record_000` WRITE;
/*!40000 ALTER TABLE `user_award_record_000` DISABLE KEYS */;

INSERT INTO `user_award_record_000` (`id`, `user_id`, `activity_id`, `strategy_id`, `order_id`, `award_id`, `award_title`, `award_time`, `award_state`, `create_time`, `update_time`)
VALUES
	(1,'xfg',100401,10007,'896971572454',110,'1次使用次数','2024-10-06 12:26:28','create','2024-10-06 12:26:28','2024-10-06 12:26:28'),
	(2,'xfg',100401,10007,'726058573393',101,'随机积分','2024-10-06 12:27:32','completed','2024-10-06 12:27:32','2024-10-06 12:27:32'),
	(3,'xfg',100401,10007,'255567446036',101,'随机积分','2024-10-06 12:28:07','completed','2024-10-06 12:28:07','2024-10-06 12:28:07'),
	(4,'xfg',100401,10007,'907702562339',101,'随机积分','2024-10-06 12:30:31','completed','2024-10-06 12:30:31','2024-10-06 12:30:31'),
	(5,'xfg',100401,10007,'919655980330',110,'1次使用次数','2024-10-06 12:31:10','create','2024-10-06 12:31:10','2024-10-06 12:31:10'),
	(6,'xfg',100401,10007,'052515846031',110,'1次使用次数','2024-10-06 16:10:23','create','2024-10-06 16:10:23','2024-10-06 16:10:23');

/*!40000 ALTER TABLE `user_award_record_000` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_award_record_001
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_award_record_001`;

CREATE TABLE `user_award_record_001` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '抽奖订单ID【作为幂等使用】',
  `award_id` int(11) NOT NULL COMMENT '奖品ID',
  `award_title` varchar(128) NOT NULL COMMENT '奖品标题（名称）',
  `award_time` datetime NOT NULL COMMENT '中奖时间',
  `award_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '奖品状态；create-创建、completed-发奖完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_award_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户中奖记录表';



# 转储表 user_award_record_002
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_award_record_002`;

CREATE TABLE `user_award_record_002` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '抽奖订单ID【作为幂等使用】',
  `award_id` int(11) NOT NULL COMMENT '奖品ID',
  `award_title` varchar(128) NOT NULL COMMENT '奖品标题（名称）',
  `award_time` datetime NOT NULL COMMENT '中奖时间',
  `award_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '奖品状态；create-创建、completed-发奖完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_award_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户中奖记录表';



# 转储表 user_award_record_003
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_award_record_003`;

CREATE TABLE `user_award_record_003` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '抽奖订单ID【作为幂等使用】',
  `award_id` int(11) NOT NULL COMMENT '奖品ID',
  `award_title` varchar(128) NOT NULL COMMENT '奖品标题（名称）',
  `award_time` datetime NOT NULL COMMENT '中奖时间',
  `award_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '奖品状态；create-创建、completed-发奖完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_award_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户中奖记录表';

LOCK TABLES `user_award_record_003` WRITE;
/*!40000 ALTER TABLE `user_award_record_003` DISABLE KEYS */;

INSERT INTO `user_award_record_003` (`id`, `user_id`, `activity_id`, `strategy_id`, `order_id`, `award_id`, `award_title`, `award_time`, `award_state`, `create_time`, `update_time`)
VALUES
	(1,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'629553450287',110,'1次使用次数','2024-10-19 09:35:57','create','2024-10-19 09:35:56','2024-10-19 09:35:56'),
	(2,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'229072233917',110,'1次使用次数','2024-10-19 09:41:59','create','2024-10-19 09:41:58','2024-10-19 09:41:58'),
	(3,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'989222182178',110,'1次使用次数','2024-10-19 09:42:37','create','2024-10-19 09:42:36','2024-10-19 09:42:36'),
	(4,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'841654853533',110,'1次使用次数','2024-10-19 09:44:56','create','2024-10-19 09:44:56','2024-10-19 09:44:56'),
	(5,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'436056352579',110,'1次使用次数','2024-10-19 09:50:48','create','2024-10-19 09:50:48','2024-10-19 09:50:48'),
	(6,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'577222874492',101,'随机积分','2024-10-19 14:45:16','completed','2024-10-19 14:45:15','2024-10-19 14:45:15'),
	(7,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'757423953290',110,'1次使用次数','2024-10-19 16:14:25','create','2024-10-19 16:14:25','2024-10-19 16:14:25'),
	(8,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'138774860660',110,'1次使用次数','2024-10-20 10:33:27','create','2024-10-20 10:33:26','2024-10-20 10:33:26'),
	(9,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'214610372888',110,'1次使用次数','2024-10-20 10:33:52','create','2024-10-20 10:33:51','2024-10-20 10:33:51'),
	(10,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'853795415263',101,'随机积分','2024-10-20 10:34:04','completed','2024-10-20 10:34:04','2024-10-20 10:34:04'),
	(11,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,10007,'589498615977',110,'1次使用次数','2024-10-20 10:34:28','create','2024-10-20 10:34:28','2024-10-20 10:34:28');

/*!40000 ALTER TABLE `user_award_record_003` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_behavior_rebate_order_000
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_behavior_rebate_order_000`;

CREATE TABLE `user_behavior_rebate_order_000` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `behavior_type` varchar(16) NOT NULL COMMENT '行为类型（sign 签到、openai_pay 支付）',
  `rebate_desc` varchar(128) NOT NULL COMMENT '返利描述',
  `rebate_type` varchar(16) NOT NULL COMMENT '返利类型（sku 活动库存充值商品、integral 用户活动积分）',
  `rebate_config` varchar(32) NOT NULL COMMENT '返利配置【sku值，积分值】',
  `out_business_no` varchar(128) NOT NULL COMMENT '业务仿重ID - 外部透传，方便查询使用',
  `biz_id` varchar(128) NOT NULL COMMENT '业务ID - 拼接的唯一值。拼接 out_business_no + 自身枚举',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_biz_id` (`biz_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为返利流水订单表';

LOCK TABLES `user_behavior_rebate_order_000` WRITE;
/*!40000 ALTER TABLE `user_behavior_rebate_order_000` DISABLE KEYS */;

INSERT INTO `user_behavior_rebate_order_000` (`id`, `user_id`, `order_id`, `behavior_type`, `rebate_desc`, `rebate_type`, `rebate_config`, `out_business_no`, `biz_id`, `create_time`, `update_time`)
VALUES
	(1,'xfg','732169662186','sign','签到返利-sku额度','sku','9014','20241006','xfg_sku_20241006','2024-10-06 12:22:13','2024-10-06 12:22:13'),
	(2,'xfg','611402618750','sign','签到返利-积分','integral','10','20241006','xfg_integral_20241006','2024-10-06 12:22:13','2024-10-06 12:22:13'),
	(16,'xfg','280474605865','openai_pay','支付返利-积分','sku','9901','100101001010001','xfg_sku_100101001010001','2024-10-20 16:11:52','2024-10-20 16:11:52'),
	(19,'xfg','982644429798','openai_pay','支付返利-sku额度','integral','10','100101001010001','xfg_integral_100101001010001','2024-10-20 16:11:52','2024-10-20 16:11:52'),
	(20,'xfg','331758537999','openai_pay','支付返利-积分','sku','9901','178799200110','xfg_sku_178799200110','2024-10-20 16:38:45','2024-10-20 16:38:45'),
	(21,'xfg','366453933498','openai_pay','支付返利-sku额度','integral','10','178799200110','xfg_integral_178799200110','2024-10-20 16:38:45','2024-10-20 16:38:45');

/*!40000 ALTER TABLE `user_behavior_rebate_order_000` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_behavior_rebate_order_001
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_behavior_rebate_order_001`;

CREATE TABLE `user_behavior_rebate_order_001` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `behavior_type` varchar(16) NOT NULL COMMENT '行为类型（sign 签到、openai_pay 支付）',
  `rebate_desc` varchar(128) NOT NULL COMMENT '返利描述',
  `rebate_type` varchar(16) NOT NULL COMMENT '返利类型（sku 活动库存充值商品、integral 用户活动积分）',
  `rebate_config` varchar(32) NOT NULL COMMENT '返利配置【sku值，积分值】',
  `out_business_no` varchar(128) NOT NULL COMMENT '业务仿重ID - 外部透传，方便查询使用',
  `biz_id` varchar(128) NOT NULL COMMENT '业务ID - 拼接的唯一值。拼接 out_business_no + 自身枚举',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_biz_id` (`biz_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为返利流水订单表';

LOCK TABLES `user_behavior_rebate_order_001` WRITE;
/*!40000 ALTER TABLE `user_behavior_rebate_order_001` DISABLE KEYS */;

INSERT INTO `user_behavior_rebate_order_001` (`id`, `user_id`, `order_id`, `behavior_type`, `rebate_desc`, `rebate_type`, `rebate_config`, `out_business_no`, `biz_id`, `create_time`, `update_time`)
VALUES
	(5,'xiaofuge','630841674684','sign','签到返利-sku额度','sku','9011','20240503','xiaofuge_sku_20240503','2024-05-03 13:28:42','2024-05-03 13:28:42'),
	(6,'xiaofuge','552413408368','sign','签到返利-积分','integral','10','20240503','xiaofuge_integral_20240503','2024-05-03 13:28:42','2024-05-03 13:28:42'),
	(33,'liergou','658660043956','sign','签到返利-sku额度','sku','9011','20240504','liergou_sku_20240504','2024-05-04 15:32:25','2024-05-04 15:32:25'),
	(34,'liergou','659440313972','sign','签到返利-积分','integral','10','20240504','liergou_integral_20240504','2024-05-04 15:32:25','2024-05-04 15:32:25'),
	(35,'xiaofuge','577047817885','sign','签到返利-积分','integral','10','20240601001','xiaofuge_integral_20240601001','2024-06-01 10:49:00','2024-06-01 10:49:00'),
	(37,'xiaofuge','060366280132','sign','签到返利-积分','integral','10','20240601002','xiaofuge_integral_20240601002','2024-06-01 10:51:58','2024-06-01 10:51:58'),
	(38,'xiaofuge','735771087688','sign','签到返利-积分','integral','10','20240601003','xiaofuge_integral_20240601003','2024-06-01 10:52:52','2024-06-01 10:52:52'),
	(39,'xiaofuge','890916856671','sign','签到返利-积分','integral','10','20240601004','xiaofuge_integral_20240601004','2024-06-01 10:54:30','2024-06-01 10:54:30'),
	(40,'xiaofuge','881155696490','sign','签到返利-积分','integral','10','20240601005','xiaofuge_integral_20240601005','2024-06-01 10:57:09','2024-06-01 10:57:09'),
	(41,'xiaofuge','213871507610','sign','签到返利-积分','integral','10','20240601006','xiaofuge_integral_20240601006','2024-06-01 11:00:27','2024-06-01 11:00:27'),
	(43,'xiaofuge','031074415283','sign','签到返利-积分','integral','10','20240601101','xiaofuge_integral_20240601101','2024-06-01 14:02:47','2024-06-01 14:02:47'),
	(44,'xiaofuge','361978465199','sign','签到返利-积分','integral','10','20240621','xiaofuge_integral_20240621','2024-06-21 21:50:08','2024-06-21 21:50:08'),
	(45,'xiaofuge','494235657121','sign','签到返利-积分','integral','10','20240622','xiaofuge_integral_20240622','2024-06-22 08:53:26','2024-06-22 08:53:26');

/*!40000 ALTER TABLE `user_behavior_rebate_order_001` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_behavior_rebate_order_002
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_behavior_rebate_order_002`;

CREATE TABLE `user_behavior_rebate_order_002` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `behavior_type` varchar(16) NOT NULL COMMENT '行为类型（sign 签到、openai_pay 支付）',
  `rebate_desc` varchar(128) NOT NULL COMMENT '返利描述',
  `rebate_type` varchar(16) NOT NULL COMMENT '返利类型（sku 活动库存充值商品、integral 用户活动积分）',
  `rebate_config` varchar(32) NOT NULL COMMENT '返利配置【sku值，积分值】',
  `out_business_no` varchar(128) NOT NULL COMMENT '业务仿重ID - 外部透传，方便查询使用',
  `biz_id` varchar(128) NOT NULL COMMENT '业务ID - 拼接的唯一值。拼接 out_business_no + 自身枚举',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_biz_id` (`biz_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为返利流水订单表';

LOCK TABLES `user_behavior_rebate_order_002` WRITE;
/*!40000 ALTER TABLE `user_behavior_rebate_order_002` DISABLE KEYS */;

INSERT INTO `user_behavior_rebate_order_002` (`id`, `user_id`, `order_id`, `behavior_type`, `rebate_desc`, `rebate_type`, `rebate_config`, `out_business_no`, `biz_id`, `create_time`, `update_time`)
VALUES
	(5,'liergou2','274252392446','sign','签到返利-sku额度','sku','9011','20240504','liergou2_sku_20240504','2024-05-04 15:35:52','2024-05-04 15:35:52'),
	(6,'liergou2','687741770429','sign','签到返利-积分','integral','10','20240504','liergou2_integral_20240504','2024-05-04 15:35:52','2024-05-04 15:35:52'),
	(7,'user003','317965139211','sign','签到返利-sku额度','sku','9011','20240525','user003_sku_20240525','2024-05-25 10:52:18','2024-05-25 10:52:18'),
	(8,'user003','429627541291','sign','签到返利-积分','integral','10','20240525','user003_integral_20240525','2024-05-25 10:52:19','2024-05-25 10:52:19'),
	(9,'xfg-test02','902127021986','openai_pay','支付返利-积分','sku','9901','418301185073','xfg-test02_sku_418301185073','2024-10-20 16:48:11','2024-10-20 16:48:11'),
	(10,'xfg-test02','706520752751','openai_pay','支付返利-sku额度','integral','10','418301185073','xfg-test02_integral_418301185073','2024-10-20 16:48:11','2024-10-20 16:48:11'),
	(11,'xfg-test02','033153182361','openai_pay','支付返利-积分','sku','9901','515019067766','xfg-test02_sku_515019067766','2024-10-20 18:15:51','2024-10-20 18:15:51'),
	(13,'xfg-test02','348124238678','openai_pay','支付返利-sku额度','integral','10','515019067766','xfg-test02_integral_515019067766','2024-10-20 18:15:51','2024-10-20 18:15:51');

/*!40000 ALTER TABLE `user_behavior_rebate_order_002` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_behavior_rebate_order_003
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_behavior_rebate_order_003`;

CREATE TABLE `user_behavior_rebate_order_003` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `behavior_type` varchar(16) NOT NULL COMMENT '行为类型（sign 签到、openai_pay 支付）',
  `rebate_desc` varchar(128) NOT NULL COMMENT '返利描述',
  `rebate_type` varchar(16) NOT NULL COMMENT '返利类型（sku 活动库存充值商品、integral 用户活动积分）',
  `rebate_config` varchar(32) NOT NULL COMMENT '返利配置【sku值，积分值】',
  `out_business_no` varchar(128) NOT NULL COMMENT '业务仿重ID - 外部透传，方便查询使用',
  `biz_id` varchar(128) NOT NULL COMMENT '业务ID - 拼接的唯一值。拼接 out_business_no + 自身枚举',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_biz_id` (`biz_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为返利流水订单表';

LOCK TABLES `user_behavior_rebate_order_003` WRITE;
/*!40000 ALTER TABLE `user_behavior_rebate_order_003` DISABLE KEYS */;

INSERT INTO `user_behavior_rebate_order_003` (`id`, `user_id`, `order_id`, `behavior_type`, `rebate_desc`, `rebate_type`, `rebate_config`, `out_business_no`, `biz_id`, `create_time`, `update_time`)
VALUES
	(10,'oxfA9w8-23yvwTmo2ombz0E4zJv4','469739034536','sign','签到返利-积分','integral','10','20241018','oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241018','2024-10-18 22:40:55','2024-10-18 22:40:55'),
	(11,'oxfA9w8-23yvwTmo2ombz0E4zJv4','280866238839','sign','签到返利-sku额度','sku','9901','20241018','oxfA9w8-23yvwTmo2ombz0E4zJv4_sku_20241018','2024-10-18 22:40:55','2024-10-18 22:40:55'),
	(13,'oxfA9w8-23yvwTmo2ombz0E4zJv4','424759136381','sign','签到返利-积分','integral','10','20241020','oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241020','2024-10-20 10:32:49','2024-10-20 10:32:49'),
	(14,'oxfA9w8-23yvwTmo2ombz0E4zJv4','738958642933','sign','签到返利-sku额度','sku','9901','20241020','oxfA9w8-23yvwTmo2ombz0E4zJv4_sku_20241020','2024-10-20 10:32:49','2024-10-20 10:32:49');

/*!40000 ALTER TABLE `user_behavior_rebate_order_003` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_credit_account
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_credit_account`;

CREATE TABLE `user_credit_account` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总积分，显示总账户值，记得一个人获得的总积分',
  `available_amount` decimal(10,2) NOT NULL COMMENT '可用积分，每次扣减的值',
  `account_status` varchar(8) NOT NULL COMMENT '账户状态【open - 可用，close - 冻结】',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分账户';

LOCK TABLES `user_credit_account` WRITE;
/*!40000 ALTER TABLE `user_credit_account` DISABLE KEYS */;

INSERT INTO `user_credit_account` (`id`, `user_id`, `total_amount`, `available_amount`, `account_status`, `create_time`, `update_time`)
VALUES
	(1,'xiaofuge',207.21,187.21,'open','2024-05-24 22:11:59','2024-07-06 18:41:28'),
	(2,'user003',0.96,0.96,'open','2024-05-25 10:53:20','2024-05-25 10:54:31'),
	(3,'xiaofuge26',10.00,10.00,'open','2024-06-22 10:22:35','2024-06-22 10:22:35'),
	(4,'xfg',197.80,197.80,'open','2024-10-06 12:22:14','2024-10-20 16:38:45'),
	(5,'oxfA9w8-23yvwTmo2ombz0E4zJv4',157.90,127.90,'open','2024-10-18 22:40:56','2024-10-20 10:34:04'),
	(6,'xfg-test02',20.00,20.00,'open','2024-10-20 16:48:12','2024-10-20 18:15:52');

/*!40000 ALTER TABLE `user_credit_account` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_credit_order_000
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_credit_order_000`;

CREATE TABLE `user_credit_order_000` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `trade_name` varchar(32) NOT NULL COMMENT '交易名称',
  `trade_type` varchar(8) NOT NULL DEFAULT 'forward' COMMENT '交易类型；forward-正向、reverse-逆向',
  `trade_amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传。返利、行为等唯一标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分订单记录';

LOCK TABLES `user_credit_order_000` WRITE;
/*!40000 ALTER TABLE `user_credit_order_000` DISABLE KEYS */;

INSERT INTO `user_credit_order_000` (`id`, `user_id`, `order_id`, `trade_name`, `trade_type`, `trade_amount`, `out_business_no`, `create_time`, `update_time`)
VALUES
	(1,'xfg','584627990145','行为返利','forward',10.00,'xfg_integral_20241006','2024-10-06 12:22:14','2024-10-06 12:22:14'),
	(2,'xfg','110272427822','行为返利','forward',10.00,'xfg_integral_100101001010001','2024-10-20 16:11:53','2024-10-20 16:11:53'),
	(3,'xfg','485156147723','行为返利','forward',10.00,'xfg_integral_178799200110','2024-10-20 16:38:45','2024-10-20 16:38:45');

/*!40000 ALTER TABLE `user_credit_order_000` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_credit_order_001
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_credit_order_001`;

CREATE TABLE `user_credit_order_001` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `trade_name` varchar(32) NOT NULL COMMENT '交易名称',
  `trade_type` varchar(8) NOT NULL DEFAULT 'forward' COMMENT '交易类型；forward-正向、reverse-逆向',
  `trade_amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传。返利、行为等唯一标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分订单记录';



# 转储表 user_credit_order_002
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_credit_order_002`;

CREATE TABLE `user_credit_order_002` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `trade_name` varchar(32) NOT NULL COMMENT '交易名称',
  `trade_type` varchar(8) NOT NULL DEFAULT 'forward' COMMENT '交易类型；forward-正向、reverse-逆向',
  `trade_amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传。返利、行为等唯一标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分订单记录';

LOCK TABLES `user_credit_order_002` WRITE;
/*!40000 ALTER TABLE `user_credit_order_002` DISABLE KEYS */;

INSERT INTO `user_credit_order_002` (`id`, `user_id`, `order_id`, `trade_name`, `trade_type`, `trade_amount`, `out_business_no`, `create_time`, `update_time`)
VALUES
	(1,'xfg-test02','481224690406','行为返利','forward',10.00,'xfg-test02_integral_418301185073','2024-10-20 16:48:12','2024-10-20 16:48:12'),
	(2,'xfg-test02','182616870509','行为返利','forward',10.00,'xfg-test02_integral_515019067766','2024-10-20 18:15:52','2024-10-20 18:15:52');

/*!40000 ALTER TABLE `user_credit_order_002` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_credit_order_003
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_credit_order_003`;

CREATE TABLE `user_credit_order_003` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `trade_name` varchar(32) NOT NULL COMMENT '交易名称',
  `trade_type` varchar(8) NOT NULL DEFAULT 'forward' COMMENT '交易类型；forward-正向、reverse-逆向',
  `trade_amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `out_business_no` varchar(64) NOT NULL COMMENT '业务仿重ID - 外部透传。返利、行为等唯一标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  UNIQUE KEY `uq_out_business_no` (`out_business_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分订单记录';

LOCK TABLES `user_credit_order_003` WRITE;
/*!40000 ALTER TABLE `user_credit_order_003` DISABLE KEYS */;

INSERT INTO `user_credit_order_003` (`id`, `user_id`, `order_id`, `trade_name`, `trade_type`, `trade_amount`, `out_business_no`, `create_time`, `update_time`)
VALUES
	(1,'xiaofuge26','633312779415','行为返利','forward',10.00,'xiaofuge26_integral_20240622','2024-06-22 10:22:35','2024-06-22 10:22:35'),
	(2,'oxfA9w8-23yvwTmo2ombz0E4zJv4','157409343333','行为返利','forward',10.00,'oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241018','2024-10-18 22:40:56','2024-10-18 22:40:56'),
	(3,'oxfA9w8-23yvwTmo2ombz0E4zJv4','355569749986','兑换抽奖','reverse',-5.00,'749423786782','2024-10-19 14:44:55','2024-10-19 14:44:55'),
	(4,'oxfA9w8-23yvwTmo2ombz0E4zJv4','748397938640','兑换抽奖','reverse',-5.00,'289961143515','2024-10-19 14:45:10','2024-10-19 14:45:10'),
	(5,'oxfA9w8-23yvwTmo2ombz0E4zJv4','841275706069','兑换抽奖','reverse',-5.00,'736734564403','2024-10-19 16:15:15','2024-10-19 16:15:15'),
	(6,'oxfA9w8-23yvwTmo2ombz0E4zJv4','473846116324','兑换抽奖','reverse',-5.00,'445087851198','2024-10-19 16:15:17','2024-10-19 16:15:17'),
	(7,'oxfA9w8-23yvwTmo2ombz0E4zJv4','132991772778','行为返利','forward',10.00,'oxfA9w8-23yvwTmo2ombz0E4zJv4_integral_20241020','2024-10-20 10:32:49','2024-10-20 10:32:49'),
	(8,'oxfA9w8-23yvwTmo2ombz0E4zJv4','737934197183','兑换抽奖','reverse',-10.00,'021052884467','2024-10-20 10:33:04','2024-10-20 10:33:04');

/*!40000 ALTER TABLE `user_credit_order_003` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_raffle_order_000
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_raffle_order_000`;

CREATE TABLE `user_raffle_order_000` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `order_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '订单状态；create-创建、used-已使用、cancel-已作废',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户抽奖订单表';

LOCK TABLES `user_raffle_order_000` WRITE;
/*!40000 ALTER TABLE `user_raffle_order_000` DISABLE KEYS */;

INSERT INTO `user_raffle_order_000` (`id`, `user_id`, `activity_id`, `activity_name`, `strategy_id`, `order_id`, `order_time`, `order_state`, `create_time`, `update_time`)
VALUES
	(1,'xfg',100401,'OpenAi抽奖活动',10007,'896971572454','2024-10-06 12:26:27','used','2024-10-06 12:26:27','2024-10-06 12:26:28'),
	(2,'xfg',100401,'OpenAi抽奖活动',10007,'726058573393','2024-10-06 12:27:31','used','2024-10-06 12:27:31','2024-10-06 12:27:32'),
	(3,'xfg',100401,'OpenAi抽奖活动',10007,'255567446036','2024-10-06 12:28:06','used','2024-10-06 12:28:06','2024-10-06 12:28:07'),
	(4,'xfg',100401,'OpenAi抽奖活动',10007,'907702562339','2024-10-06 12:30:30','used','2024-10-06 12:30:30','2024-10-06 12:30:31'),
	(5,'xfg',100401,'OpenAi抽奖活动',10007,'919655980330','2024-10-06 12:31:09','used','2024-10-06 12:31:09','2024-10-06 12:31:10'),
	(6,'xfg',100401,'OpenAi抽奖活动',10007,'052515846031','2024-10-06 16:10:22','used','2024-10-06 16:10:22','2024-10-06 16:10:23');

/*!40000 ALTER TABLE `user_raffle_order_000` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_raffle_order_001
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_raffle_order_001`;

CREATE TABLE `user_raffle_order_001` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `order_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '订单状态；create-创建、used-已使用、cancel-已作废',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户抽奖订单表';

LOCK TABLES `user_raffle_order_001` WRITE;
/*!40000 ALTER TABLE `user_raffle_order_001` DISABLE KEYS */;

INSERT INTO `user_raffle_order_001` (`id`, `user_id`, `activity_id`, `activity_name`, `strategy_id`, `order_id`, `order_time`, `order_state`, `create_time`, `update_time`)
VALUES
	(1,'xiaofuge',100301,'测试活动',100006,'145154543201','2024-10-20 15:09:49','create','2024-10-20 15:09:49','2024-10-20 15:09:49');

/*!40000 ALTER TABLE `user_raffle_order_001` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_raffle_order_002
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_raffle_order_002`;

CREATE TABLE `user_raffle_order_002` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `order_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '订单状态；create-创建、used-已使用、cancel-已作废',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户抽奖订单表';



# 转储表 user_raffle_order_003
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_raffle_order_003`;

CREATE TABLE `user_raffle_order_003` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL COMMENT '用户ID',
  `activity_id` bigint(12) NOT NULL COMMENT '活动ID',
  `activity_name` varchar(64) NOT NULL COMMENT '活动名称',
  `strategy_id` bigint(8) NOT NULL COMMENT '抽奖策略ID',
  `order_id` varchar(12) NOT NULL COMMENT '订单ID',
  `order_time` datetime NOT NULL COMMENT '下单时间',
  `order_state` varchar(16) NOT NULL DEFAULT 'create' COMMENT '订单状态；create-创建、used-已使用、cancel-已作废',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_user_id_activity_id` (`user_id`,`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户抽奖订单表';

LOCK TABLES `user_raffle_order_003` WRITE;
/*!40000 ALTER TABLE `user_raffle_order_003` DISABLE KEYS */;

INSERT INTO `user_raffle_order_003` (`id`, `user_id`, `activity_id`, `activity_name`, `strategy_id`, `order_id`, `order_time`, `order_state`, `create_time`, `update_time`)
VALUES
	(1,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'629553450287','2024-10-19 09:35:55','used','2024-10-19 09:35:55','2024-10-19 09:35:57'),
	(2,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'229072233917','2024-10-19 09:41:58','used','2024-10-19 09:41:58','2024-10-19 09:41:58'),
	(3,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'989222182178','2024-10-19 09:42:37','used','2024-10-19 09:42:36','2024-10-19 09:42:36'),
	(4,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'841654853533','2024-10-19 09:44:56','used','2024-10-19 09:44:56','2024-10-19 09:44:56'),
	(5,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'436056352579','2024-10-19 09:50:48','used','2024-10-19 09:50:48','2024-10-19 09:50:48'),
	(6,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'577222874492','2024-10-19 14:45:15','used','2024-10-19 14:45:15','2024-10-19 14:45:15'),
	(7,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'757423953290','2024-10-19 16:14:25','used','2024-10-19 16:14:24','2024-10-19 16:14:25'),
	(8,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'138774860660','2024-10-20 10:33:27','used','2024-10-20 10:33:26','2024-10-20 10:33:27'),
	(9,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'214610372888','2024-10-20 10:33:52','used','2024-10-20 10:33:51','2024-10-20 10:33:51'),
	(10,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'853795415263','2024-10-20 10:34:04','used','2024-10-20 10:34:04','2024-10-20 10:34:04'),
	(11,'oxfA9w8-23yvwTmo2ombz0E4zJv4',100401,'OpenAi抽奖活动',10007,'589498615977','2024-10-20 10:34:28','used','2024-10-20 10:34:28','2024-10-20 10:34:28');

/*!40000 ALTER TABLE `user_raffle_order_003` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
