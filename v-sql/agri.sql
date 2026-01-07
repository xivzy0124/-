/*
 Navicat Premium Dump SQL

 Source Server         : 农业大数据
 Source Server Type    : MySQL
 Source Server Version : 50738 (5.7.38)
 Source Host           : 47.117.34.210:3306
 Source Schema         : agri

 Target Server Type    : MySQL
 Target Server Version : 50738 (5.7.38)
 File Encoding         : 65001

 Date: 06/01/2026 09:34:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for calendar_data
-- ----------------------------
DROP TABLE IF EXISTS `calendar_data`;
CREATE TABLE `calendar_data`  (
  `provincename` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `one_level` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `two_level` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `varietyname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `total_export_volume` double NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for daily_vegetable_prices
-- ----------------------------
DROP TABLE IF EXISTS `daily_vegetable_prices`;
CREATE TABLE `daily_vegetable_prices`  (
  `dt` date NULL DEFAULT NULL,
  `week_of_month` int(11) NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `price` decimal(6, 2) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dish_price_statistics
-- ----------------------------
DROP TABLE IF EXISTS `dish_price_statistics`;
CREATE TABLE `dish_price_statistics`  (
  `year` int(11) NULL DEFAULT NULL,
  `quarter` int(11) NULL DEFAULT NULL,
  `dish_name` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `avg_max_price` double NULL DEFAULT NULL,
  `avg_min_price` double NULL DEFAULT NULL,
  `price_difference` double NULL DEFAULT NULL,
  `average_price` double NULL DEFAULT NULL,
  `provincename` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for export_top
-- ----------------------------
DROP TABLE IF EXISTS `export_top`;
CREATE TABLE `export_top`  (
  `varietyname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `marketname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `tradingVolume` double NULL DEFAULT NULL,
  `minimumPrice` double NULL DEFAULT NULL,
  `middlePrice` double NULL DEFAULT NULL,
  `highestPrice` double NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for price_breakdown_by_province
-- ----------------------------
DROP TABLE IF EXISTS `price_breakdown_by_province`;
CREATE TABLE `price_breakdown_by_province`  (
  `provincename` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `varietyname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `min_price` double NULL DEFAULT NULL,
  `max_price` double NULL DEFAULT NULL,
  `avg_price` double NULL DEFAULT NULL,
  `tradingvolume` double NULL DEFAULT NULL,
  `marketname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for price_forecasts
-- ----------------------------
DROP TABLE IF EXISTS `price_forecasts`;
CREATE TABLE `price_forecasts`  (
  `generate_date` date NOT NULL COMMENT '预测生成日期',
  `forecast_date` date NOT NULL COMMENT '预测目标日期',
  `province` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '省份名称',
  `product_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `predicted_price` decimal(5, 2) NOT NULL COMMENT '预测价格（元/斤）',
  `algorithm` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预测算法'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for processed_prices
-- ----------------------------
DROP TABLE IF EXISTS `processed_prices`;
CREATE TABLE `processed_prices`  (
  `marketname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `provincename` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `minimumprice` double NULL DEFAULT NULL,
  `middleprice` double NULL DEFAULT NULL,
  `highestprice` double NULL DEFAULT NULL,
  `tradingvolume` double NULL DEFAULT NULL,
  `varietyname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `reporttime` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `dt` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for scatter_data
-- ----------------------------
DROP TABLE IF EXISTS `scatter_data`;
CREATE TABLE `scatter_data`  (
  `provincename` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `marketname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `varietyname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `volume` double NULL DEFAULT NULL,
  `price_variance` double NULL DEFAULT NULL,
  `average_price` double NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for statistics
-- ----------------------------
DROP TABLE IF EXISTS `statistics`;
CREATE TABLE `statistics`  (
  `total` bigint(20) NOT NULL,
  `varietyname_ct` bigint(20) NOT NULL,
  `provincename_ct` bigint(20) NOT NULL,
  `areacode_ct` bigint(20) NOT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vaegettable_type_2
-- ----------------------------
DROP TABLE IF EXISTS `vaegettable_type_2`;
CREATE TABLE `vaegettable_type_2`  (
  `one_level` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '大类',
  `two_level` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '小类',
  `varietyname` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '菜名'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vegetable_type
-- ----------------------------
DROP TABLE IF EXISTS `vegetable_type`;
CREATE TABLE `vegetable_type`  (
  `one_level` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `two_level` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
