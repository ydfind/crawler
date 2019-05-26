/*
Navicat MySQL Data Transfer

Source Server         : localhost3306
Source Server Version : 50723
Source Host           : localhost:3306
Source Database       : yd_app

Target Server Type    : MYSQL
Target Server Version : 50723
File Encoding         : 65001

Date: 2019-05-26 20:45:17
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for yd_app_package
-- ----------------------------
DROP TABLE IF EXISTS `yd_app_package`;
CREATE TABLE `yd_app_package` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '系统ID',
  `APP_NAME` varchar(64) NOT NULL COMMENT '应用名称',
  `APP_TYPE` varchar(32) DEFAULT NULL COMMENT '应用类型ANDROID 采集，接入商户的APP',
  `PACKAGE_NAME` varchar(256) DEFAULT NULL COMMENT '包名',
  `INDUSTRY_TYPE` varchar(16) DEFAULT NULL COMMENT '行业大类',
  `SUB_INDUSTRY_TYPE` varchar(16) DEFAULT NULL COMMENT '行业细类',
  `USABLE_FLAG` varchar(8) NOT NULL COMMENT '可用标识',
  `CREATED_AT` timestamp NOT NULL COMMENT '创建时间',
  `CREATED_BY` varchar(32) NOT NULL COMMENT '创建人',
  `UPDATED_AT` timestamp NOT NULL COMMENT '更新时间',
  `UPDATED_BY` varchar(32) NOT NULL COMMENT '更新人',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UNIQUE_APP_NAME` (`PACKAGE_NAME`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=623130 DEFAULT CHARSET=utf8mb4 COMMENT='应用分类表';
