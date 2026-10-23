# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20050
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 127.0.0.1 (MySQL 5.6.39)
# 数据库: openai
# 生成时间: 2023-10-08 01:32:51 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE database if NOT EXISTS `openai` default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use `openai`;

# 转储表 openai_order
# ------------------------------------------------------------

DROP TABLE IF EXISTS `openai_order`;

CREATE TABLE `openai_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(128) NOT NULL COMMENT '用户ID；微信分配的唯一ID编码',
  `product_id` int(4) NOT NULL COMMENT '商品ID',
  `product_name` varchar(32) NOT NULL COMMENT '商品名称',
  `product_quota` int(8) NOT NULL COMMENT '商品额度',
  `product_model_types` varchar(128) DEFAULT NULL COMMENT '可用模型；gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,gpt-4-32k',
  `order_id` varchar(12) NOT NULL COMMENT '订单编号',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `order_status` tinyint(1) NOT NULL COMMENT '订单状态；0-创建完成、1-等待发货、2-发货完成、3-系统关单',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `pay_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '支付方式；0-微信支付',
  `pay_url` varchar(128) DEFAULT NULL COMMENT '支付地址；创建支付后，获得的URL地址',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '支付金额；支付成功后，以回调信息更新金额',
  `transaction_id` varchar(32) DEFAULT NULL COMMENT '交易单号；支付成功后，回调信息的交易单号',
  `pay_status` tinyint(1) DEFAULT NULL COMMENT '支付状态；0-等待支付、1-支付完成、2-支付失败、3-放弃支付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_order_id` (`order_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_order_status_pay_status_order_time` (`order_time`,`order_status`,`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `openai_order` WRITE;
/*!40000 ALTER TABLE `openai_order` DISABLE KEYS */;

INSERT INTO `openai_order` (`id`, `openid`, `product_id`, `product_name`, `product_quota`, `product_model_types`, `order_id`, `order_time`, `order_status`, `total_amount`, `pay_type`, `pay_url`, `pay_amount`, `transaction_id`, `pay_status`, `pay_time`, `create_time`, `update_time`)
VALUES
	(1,'xfg',1001,'测试商品',100,NULL,'118845061424','2023-10-05 17:02:42',3,0.11,0,'weixin://wxpay/bizpayurl?pr=NFByxsizz',NULL,NULL,1,NULL,'2023-10-05 17:02:41','2023-10-07 19:03:40'),
	(2,'xfg',1001,'测试商品',100,NULL,'730807176035','2023-10-05 20:35:46',2,0.11,0,'weixin://wxpay/bizpayurl?pr=0ckFdM2zz',0.11,'4200001993202310054200967494',1,'2023-10-05 20:09:10','2023-10-05 20:08:46','2023-10-05 20:42:16'),
	(3,'xfg',1001,'测试商品',100,NULL,'957726718857','2023-10-05 21:10:37',3,0.11,0,'weixin://wxpay/bizpayurl?pr=5BWAAAezz',NULL,NULL,0,NULL,'2023-10-05 21:10:37','2023-10-06 10:48:07'),
	(4,'xfg',1001,'测试商品',100,NULL,'911886525135','2023-10-06 10:38:13',2,0.01,0,'weixin://wxpay/bizpayurl?pr=5VXNHs5zz',0.01,'4200002005202310065504004737',1,'2023-10-06 10:48:51','2023-10-06 10:48:13','2023-10-06 10:52:54'),
	(5,'xfg',1001,'OpenAi 测试商品(3.5)',100,NULL,'175715149006','2023-10-07 18:41:17',2,0.01,0,'weixin://wxpay/bizpayurl?pr=EMrrTSYzz',0.01,'4200002003202310079361212463',1,'2023-10-07 18:52:06','2023-10-07 18:51:17','2023-10-07 18:56:00'),
	(6,'xfg',1002,'OpenAi 测试商品(3.5)',200,NULL,'535514648535','2023-10-07 18:57:27',3,0.02,0,'weixin://wxpay/bizpayurl?pr=YlCE6Xszz',NULL,NULL,0,NULL,'2023-10-07 18:57:27','2023-10-07 19:03:38'),
	(7,'xfg',1003,'OpenAi 测试商品(3.5&4.0)',50,NULL,'845537169226','2023-10-07 18:57:35',3,10.00,0,'weixin://wxpay/bizpayurl?pr=A0SVuiXzz',NULL,NULL,0,NULL,'2023-10-07 18:57:34','2023-10-07 19:03:37'),
	(8,'xfg',1004,'OpenAi 测试商品(3.5&4.0)',100,NULL,'624271053503','2023-10-07 18:57:37',3,18.88,0,'weixin://wxpay/bizpayurl?pr=hHSv2M4zz',NULL,NULL,0,NULL,'2023-10-07 18:57:36','2023-10-07 19:03:36'),
	(9,'xfg',1001,'OpenAi 测试商品(3.5)',100,NULL,'829590724715','2023-10-07 19:42:00',3,0.01,0,'weixin://wxpay/bizpayurl?pr=YuhyFmOzz',NULL,NULL,0,NULL,'2023-10-07 19:41:59','2023-10-07 20:19:18'),
	(10,'xfg',1001,'OpenAi 测试商品(3.5)',100,NULL,'959500557346','2023-10-07 20:19:30',3,0.01,0,'因未配置支付渠道，所以暂时不能生成支付URL',NULL,NULL,0,NULL,'2023-10-07 20:19:29','2023-10-08 09:12:19'),
	(13,'xfg',1001,'OpenAi 测试商品(3.5)',100,NULL,'309388058761','2023-10-08 09:16:36',2,0.01,0,'weixin://wxpay/bizpayurl?pr=VmUfvQ0zz',0.01,'4200001978202310081985000180',1,'2023-10-08 09:24:07','2023-10-08 09:16:35','2023-10-08 09:25:30'),
	(14,'xfg-test02',1001,'OpenAi 测试商品(3.5)',100,NULL,'087139547716','2023-10-08 09:17:01',3,0.01,0,'因未配置支付渠道，所以暂时不能生成支付URL',NULL,NULL,0,NULL,'2023-10-08 09:17:00','2023-10-08 09:24:44'),
	(15,'xfg',1002,'OpenAi 测试商品(3.5)',200,NULL,'436646302440','2023-10-08 09:29:03',3,0.02,0,'weixin://wxpay/bizpayurl?pr=yFq9wJszz',NULL,NULL,0,NULL,'2023-10-08 09:29:03','2023-10-08 09:32:43'),
	(16,'xfg',1003,'OpenAi 测试商品(3.5&4.0)',50,NULL,'178799200110','2023-10-08 09:29:07',3,10.00,0,'weixin://wxpay/bizpayurl?pr=7DF8mmHzz',NULL,NULL,0,NULL,'2023-10-08 09:29:07','2023-10-08 09:32:44');

/*!40000 ALTER TABLE `openai_order` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 openai_product
# ------------------------------------------------------------

DROP TABLE IF EXISTS `openai_product`;

CREATE TABLE `openai_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `product_id` int(4) NOT NULL COMMENT '商品ID',
  `product_name` varchar(32) NOT NULL COMMENT '商品名称',
  `product_desc` varchar(128) NOT NULL COMMENT '商品描述',
  `product_model_types` varchar(128) DEFAULT NULL COMMENT '可用模型；gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,gpt-4-32k',
  `quota` int(8) NOT NULL COMMENT '额度次数',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `sort` int(4) NOT NULL COMMENT '商品排序',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效；0无效、1有效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id_enabled` (`product_id`,`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `openai_product` WRITE;
/*!40000 ALTER TABLE `openai_product` DISABLE KEYS */;

INSERT INTO `openai_product` (`id`, `product_id`, `product_name`, `product_desc`, `product_model_types`, `quota`, `price`, `sort`, `is_enabled`, `create_time`, `update_time`)
VALUES
	(1,1001,'OpenAi 测试商品(3.5)','测试商品请勿下单','gpt-3.5-turbo,gpt-3.5-turbo-16k',10,0.01,1,1,'2023-10-07 18:45:36','2023-10-07 18:45:36'),
	(2,1002,'OpenAi 测试商品(3.5)','测试商品请勿下单','gpt-3.5-turbo,gpt-3.5-turbo-16k',20,1.00,2,1,'2023-10-07 18:45:42','2023-10-07 18:45:42'),
	(3,1003,'OpenAi 测试商品(3.5&4.0)','测试商品请勿下单','gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4',50,9.99,3,1,'2023-10-07 18:46:41','2023-10-07 18:46:41'),
	(4,1004,'OpenAi 测试商品(3.5&4.0)','测试商品请勿下单','gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4',100,18.88,3,0,'2023-10-08 09:28:47','2023-10-08 09:28:47');

/*!40000 ALTER TABLE `openai_product` ENABLE KEYS */;
UNLOCK TABLES;


# 转储表 user_account
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(64) NOT NULL COMMENT '用户ID；这里用的是微信ID作为唯一ID，你也可以给用户创建唯一ID，之后绑定微信ID',
  `total_quota` int(11) NOT NULL DEFAULT '0' COMMENT '总量额度；分配的总使用次数',
  `surplus_quota` int(11) NOT NULL DEFAULT '0' COMMENT '剩余额度；剩余的可使用次数',
  `model_types` varchar(128) NOT NULL COMMENT '可用模型；gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,gpt-4-32k',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '账户状态；0-可用、1-冻结',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_openid` (`openid`),
  KEY `idx_surplus_quota_status` (`surplus_quota`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `user_account` WRITE;
/*!40000 ALTER TABLE `user_account` DISABLE KEYS */;

INSERT INTO `user_account` (`id`, `openid`, `total_quota`, `surplus_quota`, `model_types`, `status`, `create_time`, `update_time`)
VALUES
	(1,'xfg',100,100,'gpt-3.5-turbo,gpt-3.5-turbo-16k',0,'2023-10-03 18:56:13','2023-10-08 09:25:30');

/*!40000 ALTER TABLE `user_account` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
