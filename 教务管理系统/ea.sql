/*
 Navicat Premium Data Transfer

 Source Server         : isjhd
 Source Server Type    : MySQL
 Source Server Version : 50719
 Source Host           : localhost:3306
 Source Schema         : ea

 Target Server Type    : MySQL
 Target Server Version : 50719
 File Encoding         : 65001

 Date: 25/12/2023 09:45:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student`  (
  `stuid` int(11) NOT NULL AUTO_INCREMENT,
  `sname` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `gender` int(11) NOT NULL,
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `address` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `clsid` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `STATUS` int(11) NULL DEFAULT 1,
  PRIMARY KEY (`stuid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES (1, '忍野忍', 0, '18371317825', '日本', '1903', 0);
INSERT INTO `student` VALUES (2, '战场原黑仪', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (3, '羽川翼', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (4, '四宫辉夜', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (5, '藤原千花', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (6, '石上优', 1, '18371317825', '日本', '1904', 0);
INSERT INTO `student` VALUES (7, '神原骏河', 1, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (8, '八九寺真宵', 0, '18371317825', '日本', '1903', 0);
INSERT INTO `student` VALUES (9, '千石抚子', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (10, '阿良良木火怜', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (11, '阿良良木月火', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (12, '斧乃木余接', 0, '18371317825', '日本', '1903', 0);
INSERT INTO `student` VALUES (13, '忍野扇', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (14, '老仓育', 0, '18371317825', '日本', '1903', 1);
INSERT INTO `student` VALUES (15, '伊井野弥子', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (16, '子安燕', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (17, '早坂爱', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (18, '四条真妃', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (19, '白银圭', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (20, '柏木渚', 0, '18371317825', '日本', '1904', 1);
INSERT INTO `student` VALUES (21, '古河渚', 0, '18371317825', '日本', '1905', 1);
INSERT INTO `student` VALUES (22, '藤林杏', 0, '18371317825', '日本', '1905', 1);
INSERT INTO `student` VALUES (23, '藤林椋', 0, '18371317825', '日本', '1905', 1);
INSERT INTO `student` VALUES (24, '坂上智代', 0, '18371317825', '日本', '1905', 1);
INSERT INTO `student` VALUES (25, '伊吹风子', 0, '18371317825', '日本', '1905', 0);
INSERT INTO `student` VALUES (26, '一之濑琴美', 0, '18371317825', '日本', '1905', 1);
INSERT INTO `student` VALUES (27, '春原阳平', 1, '18371317825', '日本', '1905', 1);
INSERT INTO `student` VALUES (28, '北原伊织', 1, '18371317825', '日本', '无', 1);
INSERT INTO `student` VALUES (29, '今村耕平', 1, '18371317825', '日本', '无', 1);
INSERT INTO `student` VALUES (30, '托尔芬', 1, '18371317825', '日本', '无', 0);
INSERT INTO `student` VALUES (31, '夏目贵志', 1, '18371317825', '日本', '无', 1);
INSERT INTO `student` VALUES (32, '千叶敦子', 0, '18371317825', '日本', '无', 1);
INSERT INTO `student` VALUES (33, '斯派克·斯皮格尔', 1, '18371317825', '日本', '无', 0);
INSERT INTO `student` VALUES (34, '菲·瓦伦坦', 0, '18371317825', '日本', '无', 1);
INSERT INTO `student` VALUES (36, '埼玉', 1, '18371317825', '日本', '无', 1);

-- ----------------------------
-- Table structure for tbactivity
-- ----------------------------
DROP TABLE IF EXISTS `tbactivity`;
CREATE TABLE `tbactivity`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `SUBJECT` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `content` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `clsid` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `starttime` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tbactivity
-- ----------------------------
INSERT INTO `tbactivity` VALUES (1, 'FFF', '审判罪恶之人', '1903', '2023-12-22');
INSERT INTO `tbactivity` VALUES (2, '圣杯战争', '召唤英灵', '1903', '2023-12-22');
INSERT INTO `tbactivity` VALUES (3, '放学后茶会', '喝茶', '1903', '2023-12-22');
INSERT INTO `tbactivity` VALUES (4, '今天的风儿甚是喧嚣', '隔壁薯片半价', '1903', '2023-12-22');
INSERT INTO `tbactivity` VALUES (5, 'JoJo', '学习黄金精神', '1904', '2023-12-22');
INSERT INTO `tbactivity` VALUES (6, 'JoJo', '黑帮摇', '1904', '2023-12-22');
INSERT INTO `tbactivity` VALUES (7, '潜水社', '酒宴', '1904', '2023-12-22');
INSERT INTO `tbactivity` VALUES (8, '西游记', '西天取经', '1904', '2023-12-22');
INSERT INTO `tbactivity` VALUES (9, '明日之丈', '拳击比赛', '1904', '2023-12-22');
INSERT INTO `tbactivity` VALUES (10, 'jojo', ' 学习jojo立', '1904', '2023-12-22');
INSERT INTO `tbactivity` VALUES (11, 'made in Heaven', '吟唱24句密语', '1905', '2023-12-22');
INSERT INTO `tbactivity` VALUES (12, '混沌武士', '踏上旅途', '1905', '2023-12-22');
INSERT INTO `tbactivity` VALUES (13, '无间道', '参加卧底任务', '1905', '2023-12-22');
INSERT INTO `tbactivity` VALUES (14, '测验召唤系统', '通过召唤兽来战斗', '1905', '2023-12-22');
INSERT INTO `tbactivity` VALUES (15, '零之使魔', '召唤使魔', '1905', '2023-12-22');
INSERT INTO `tbactivity` VALUES (17, '跳舞', '白金disco', '1903', '2023-12-24');
INSERT INTO `tbactivity` VALUES (20, '胜利誓约之剑', '召唤亚瑟王', '1903', '2023-12-24');

-- ----------------------------
-- Table structure for tbclass
-- ----------------------------
DROP TABLE IF EXISTS `tbclass`;
CREATE TABLE `tbclass`  (
  `cid` int(11) NOT NULL AUTO_INCREMENT,
  `cname` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `cdesc` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `teacher` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `stucount` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`cid`) USING BTREE,
  UNIQUE INDEX `cname`(`cname`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tbclass
-- ----------------------------
INSERT INTO `tbclass` VALUES (1, '1903', '软件开发', '阿良良木历', 11);
INSERT INTO `tbclass` VALUES (2, '1904', '计算机', '会会长', 9);
INSERT INTO `tbclass` VALUES (3, '1905', '物联网', '冈崎朋也', 7);

-- ----------------------------
-- Table structure for tbresource
-- ----------------------------
DROP TABLE IF EXISTS `tbresource`;
CREATE TABLE `tbresource`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clsid` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `filename` varchar(15) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tbresource
-- ----------------------------
INSERT INTO `tbresource` VALUES (1, '1904', '提问的艺术', 'art.png');
INSERT INTO `tbresource` VALUES (2, '1905', '清理lastUpdated', 'clearLastUpdated.txt');
INSERT INTO `tbresource` VALUES (3, '1904', '爱的价格', 'love.png');
INSERT INTO `tbresource` VALUES (4, '1903', 'chatgpt催眠咒语', 'magic.txt');
INSERT INTO `tbresource` VALUES (5, '1904', 'PPT模板', 'ppt.txt');
INSERT INTO `tbresource` VALUES (6, '1903', '找壁纸网站', 'wallpaper.txt');
INSERT INTO `tbresource` VALUES (7, '1905', '动漫网站', 'Anime.txt');
INSERT INTO `tbresource` VALUES (8, '1903', '实用资源、工具网站', 'naturalResources.txt');
INSERT INTO `tbresource` VALUES (9, '1904', 'chatGBT网站', 'chatGBT.txt');
INSERT INTO `tbresource` VALUES (10, '1905', 'PPT制作网站', 'pptMake.txt');
INSERT INTO `tbresource` VALUES (11, '1903', '飞机场', 'plane.txt');
INSERT INTO `tbresource` VALUES (12, '1904', '程序员必备', 'programmer.txt');
INSERT INTO `tbresource` VALUES (13, '1905', '编程指南', 'ProgrammingGuide.txt');
INSERT INTO `tbresource` VALUES (17, '1903', '白金disco', 'b3b5abee-c01a-417f-9d3d-b3f20c82b78b.gif');

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `tname` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `photo` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '1.gif',
  `PASSWORD` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '123456',
  `gender` int(11) NULL DEFAULT 1,
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `address` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  PRIMARY KEY (`tid`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, '阿良良木历', '阿良良木君', '111919c4-25e3-407e-a7c7-31b93b48f5d9.gif', '123456', 1, '18371317825', '私立直江津高等学校');
INSERT INTO `teacher` VALUES (2, '白银御行', '会长', '45727488-b3e9-41d6-a68b-3c541342d42e.png', '123456', 1, '18371317825', '秀知院学园高中2年级');
INSERT INTO `teacher` VALUES (3, '冈崎朋也', '营养不良少年', '820bbce7-a7ef-4e29-bbf3-f8ffb749b4d8.gif', '123456', 1, '18371317825', '私立光坂高等学校');

SET FOREIGN_KEY_CHECKS = 1;
