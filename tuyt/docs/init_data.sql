-- ============================================================
-- 太原市环境网格化综合管理系统 - 数据库初始化脚本
-- 数据库: tuyt_env_grid
-- 编码: UTF-8
-- ============================================================

CREATE DATABASE IF NOT EXISTS `tuyt_env_grid` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `tuyt_env_grid`;

-- ============================================================
-- 1. 系统组织表
-- ============================================================
DROP TABLE IF EXISTS `sys_organization`;
CREATE TABLE `sys_organization` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `org_name` varchar(100) NOT NULL COMMENT '组织名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父组织ID',
  `level` int DEFAULT 1 COMMENT '层级 1:市级 2:区级 3:街道',
  `sort` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统组织表';

INSERT INTO `sys_organization` VALUES
-- 市级
(1, '太原市生态环境局', NULL, 1, 1, NOW(), NOW(), 0),
-- 区级 (6个区)
(2, '尖草坪区环保局', 1, 2, 1, NOW(), NOW(), 0),
(3, '万柏林区环保局', 1, 2, 2, NOW(), NOW(), 0),
(4, '杏花岭区环保局', 1, 2, 3, NOW(), NOW(), 0),
(5, '迎泽区环保局',  1, 2, 4, NOW(), NOW(), 0),
(6, '晋源区环保局',  1, 2, 5, NOW(), NOW(), 0),
(7, '小店区环保局',  1, 2, 6, NOW(), NOW(), 0),
-- 街道级 (每区2~3个街道)
(8,  '尖草坪街道环保办',  2, 3, 1, NOW(), NOW(), 0),
(9,  '迎新街街道环保办',  2, 3, 2, NOW(), NOW(), 0),
(10, '古城街道环保办',    2, 3, 3, NOW(), NOW(), 0),
(11, '千峰街道环保办',    3, 3, 1, NOW(), NOW(), 0),
(12, '下元街道环保办',    3, 3, 2, NOW(), NOW(), 0),
(13, '敦化坊街道环保办',  4, 3, 1, NOW(), NOW(), 0),
(14, '涧河街道环保办',    4, 3, 2, NOW(), NOW(), 0),
(15, '柳巷街道环保办',    5, 3, 1, NOW(), NOW(), 0),
(16, '老军营街道环保办',  5, 3, 2, NOW(), NOW(), 0),
(17, '晋祠街道环保办',    6, 3, 1, NOW(), NOW(), 0),
(18, '义井街道环保办',    6, 3, 2, NOW(), NOW(), 0),
(19, '平阳路街道环保办',  7, 3, 1, NOW(), NOW(), 0),
(20, '坞城街道环保办',    7, 3, 2, NOW(), NOW(), 0);

-- ============================================================
-- 2. 系统角色表
-- ============================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

INSERT INTO `sys_role` VALUES
(1, '超级管理员', 'ADMIN',       '系统最高权限',                       NOW(), NOW(), 0),
(2, '网格长',     'GRID_LEADER', '负责整个网格管理工作',               NOW(), NOW(), 0),
(3, '巡查员',     'INSPECTOR',   '日常巡查和问题上报',                 NOW(), NOW(), 0),
(4, '调度员',     'DISPATCHER',  '任务调度分配',                       NOW(), NOW(), 0),
(5, '数据分析员', 'ANALYST',     '查看统计报表和数据分析，仅读权限',   NOW(), NOW(), 0);

-- ============================================================
-- 3. 系统用户表
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(加密)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `status` tinyint DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 密码均为 123456 的MD5加密值 (e10adc3949ba59abbe56e057f20f883e)
INSERT INTO `sys_user` VALUES
-- 超级管理员
(1,  'admin',     'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800000000', 'admin@taiyuan.gov.cn',  NULL, 1,  1, NOW(), NOW(), 0),
-- 网格长 (每区1人)
(2,  'zhangag',   'e10adc3949ba59abbe56e057f20f883e', '张爱国',   '13800000001', 'zhangag@taiyuan.gov.cn',   NULL, 2,  1, NOW(), NOW(), 0),
(3,  'liweimin',  'e10adc3949ba59abbe56e057f20f883e', '李为民',   '13800000002', NULL, NULL, 3,  1, NOW(), NOW(), 0),
(4,  'wangshzh',  'e10adc3949ba59abbe56e057f20f883e', '王守正',   '13800000003', NULL, NULL, 4,  1, NOW(), NOW(), 0),
(5,  'zhaozq',    'e10adc3949ba59abbe56e057f20f883e', '赵志强',   '13800000004', NULL, NULL, 5,  1, NOW(), NOW(), 0),
(6,  'sunjx',     'e10adc3949ba59abbe56e057f20f883e', '孙建新',   '13800000005', NULL, NULL, 6,  1, NOW(), NOW(), 0),
(7,  'zhoumd',    'e10adc3949ba59abbe56e057f20f883e', '周明达',   '13800000006', NULL, NULL, 7,  1, NOW(), NOW(), 0),
-- 巡查员 (每街道1人)
(8,  'liuqiang',  'e10adc3949ba59abbe56e057f20f883e', '刘强',     '13800000010', NULL, NULL, 8,  1, NOW(), NOW(), 0),
(9,  'maxiaoyan', 'e10adc3949ba59abbe56e057f20f883e', '马晓燕',   '13800000011', NULL, NULL, 9,  1, NOW(), NOW(), 0),
(10, 'chenwei',   'e10adc3949ba59abbe56e057f20f883e', '陈伟',     '13800000012', NULL, NULL, 11, 1, NOW(), NOW(), 0),
(11, 'guojing',   'e10adc3949ba59abbe56e057f20f883e', '郭静',     '13800000013', NULL, NULL, 12, 1, NOW(), NOW(), 0),
(12, 'huangyi',   'e10adc3949ba59abbe56e057f20f883e', '黄毅',     '13800000014', NULL, NULL, 13, 1, NOW(), NOW(), 0),
(13, 'yangli',    'e10adc3949ba59abbe56e057f20f883e', '杨丽',     '13800000015', NULL, NULL, 14, 1, NOW(), NOW(), 0),
(14, 'lintao',    'e10adc3949ba59abbe56e057f20f883e', '林涛',     '13800000016', NULL, NULL, 15, 1, NOW(), NOW(), 0),
(15, 'hefang',    'e10adc3949ba59abbe56e057f20f883e', '何芳',     '13800000017', NULL, NULL, 16, 1, NOW(), NOW(), 0),
(16, 'xuguang',   'e10adc3949ba59abbe56e057f20f883e', '徐光',     '13800000018', NULL, NULL, 17, 1, NOW(), NOW(), 0),
(17, 'songmei',   'e10adc3949ba59abbe56e057f20f883e', '宋梅',     '13800000019', NULL, NULL, 18, 1, NOW(), NOW(), 0),
(18, 'dengfei',   'e10adc3949ba59abbe56e057f20f883e', '邓飞',     '13800000020', NULL, NULL, 19, 1, NOW(), NOW(), 0),
(19, 'wuxia',     'e10adc3949ba59abbe56e057f20f883e', '吴霞',     '13800000021', NULL, NULL, 20, 1, NOW(), NOW(), 0),
-- 调度员
(20, 'tinapeng',  'e10adc3949ba59abbe56e057f20f883e', '彭丽娜',   '13800000030', 'tinapeng@taiyuan.gov.cn',  NULL, 1,  1, NOW(), NOW(), 0),
(21, 'huxin',     'e10adc3949ba59abbe56e057f20f883e', '胡鑫',     '13800000031', 'huxin@taiyuan.gov.cn',     NULL, 1,  1, NOW(), NOW(), 0),
-- 数据分析员
(22, 'jiangyi',   'e10adc3949ba59abbe56e057f20f883e', '江怡',     '13800000032', 'jiangyi@taiyuan.gov.cn',   NULL, 1,  1, NOW(), NOW(), 0);

-- ============================================================
-- 3b. 用户-角色关联表
-- ============================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

INSERT INTO `sys_user_role` VALUES
(1,  1,  1, NOW()), (2,  1,  4, NOW()),   -- admin: 管理员+调度员
(3,  2,  2, NOW()), (4,  3,  2, NOW()),   -- 网格长
(5,  4,  2, NOW()), (6,  5,  2, NOW()),
(7,  6,  2, NOW()), (8,  7,  2, NOW()),
(9,  8,  3, NOW()), (10, 9,  3, NOW()),  -- 巡查员
(11, 10, 3, NOW()), (12, 11, 3, NOW()),
(13, 12, 3, NOW()), (14, 13, 3, NOW()),
(15, 14, 3, NOW()), (16, 15, 3, NOW()),
(17, 16, 3, NOW()), (18, 17, 3, NOW()),
(19, 18, 3, NOW()), (20, 19, 3, NOW()),
(21, 20, 4, NOW()), (22, 21, 4, NOW()),  -- 调度员
(23, 22, 5, NOW());                        -- 数据分析员

-- ============================================================
-- 4. 网格信息表
-- ============================================================
DROP TABLE IF EXISTS `grid_info`;
CREATE TABLE `grid_info` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `grid_name` varchar(100) NOT NULL COMMENT '网格名称',
  `grid_level` int NOT NULL DEFAULT 1 COMMENT '网格层级 1:市级 2:区级 3:街道',
  `parent_id` bigint DEFAULT NULL COMMENT '父网格ID',
  `org_id` bigint DEFAULT NULL COMMENT '所属组织ID',
  `leader` varchar(50) DEFAULT NULL COMMENT '网格长',
  `responsible_person` varchar(50) DEFAULT NULL COMMENT '责任人',
  `responsible_phone` varchar(20) DEFAULT NULL COMMENT '责任人电话',
  `polygon_data` text COMMENT '网格边界多边形数据(GeoJSON)',
  `status` tinyint DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网格信息表';

-- 市级网格
INSERT INTO `grid_info` VALUES
(100, '太原市', 1, NULL, 1, '张爱国', '张爱国', '13800000001', '[[112.0,37.5],[113.2,37.5],[113.2,38.2],[112.0,38.2]]', 1, NOW(), NOW(), 0);

-- 区级网格 (6个)
INSERT INTO `grid_info` VALUES
(1, '小店区',   2, 100, 7, '周明达', '周明达', '13800000006',
 '[[112.536,37.706],[112.596,37.706],[112.596,37.766],[112.536,37.766]]', 1, NOW(), NOW(), 0),
(2, '迎泽区',   2, 100, 5, '赵志强', '赵志强', '13800000004',
 '[[112.540,37.838],[112.600,37.838],[112.600,37.898],[112.540,37.898]]', 1, NOW(), NOW(), 0),
(3, '杏花岭区', 2, 100, 4, '王守正', '王守正', '13800000003',
 '[[112.545,37.866],[112.605,37.866],[112.605,37.926],[112.545,37.926]]', 1, NOW(), NOW(), 0),
(4, '尖草坪区', 2, 100, 2, '张爱国', '张爱国', '13800000001',
 '[[112.471,37.916],[112.531,37.916],[112.531,37.976],[112.471,37.976]]', 1, NOW(), NOW(), 0),
(5, '万柏林区', 2, 100, 3, '李为民', '李为民', '13800000002',
 '[[112.474,37.829],[112.534,37.829],[112.534,37.889],[112.474,37.889]]', 1, NOW(), NOW(), 0),
(6, '晋源区',   2, 100, 6, '孙建新', '孙建新', '13800000005',
 '[[112.448,37.690],[112.508,37.690],[112.508,37.750],[112.448,37.750]]', 1, NOW(), NOW(), 0);

-- 街道级网格 (每区 2 个 = 12个)
INSERT INTO `grid_info` VALUES
(11, '尖草坪街道网格', 3, 4, 8,  '刘强',   '刘强',   '13800000010',
 '[[112.571,37.916],[112.591,37.916],[112.591,37.936],[112.571,37.936]]', 1, NOW(), NOW(), 0),
(12, '迎新街街道网格', 3, 4, 9,  '马晓燕', '马晓燕', '13800000011',
 '[[112.531,37.946],[112.551,37.946],[112.551,37.966],[112.531,37.966]]', 1, NOW(), NOW(), 0),
(13, '千峰街道网格',   3, 5, 11, '陈伟',   '陈伟',   '13800000012',
 '[[112.504,37.839],[112.524,37.839],[112.524,37.859],[112.504,37.859]]', 1, NOW(), NOW(), 0),
(14, '下元街道网格',   3, 5, 12, '郭静',   '郭静',   '13800000013',
 '[[112.484,37.829],[112.504,37.829],[112.504,37.849],[112.484,37.849]]', 1, NOW(), NOW(), 0),
(15, '敦化坊街道网格', 3, 3, 13, '黄毅',   '黄毅',   '13800000014',
 '[[112.565,37.876],[112.585,37.876],[112.585,37.896],[112.565,37.896]]', 1, NOW(), NOW(), 0),
(16, '涧河街道网格',   3, 3, 14, '杨丽',   '杨丽',   '13800000015',
 '[[112.580,37.896],[112.600,37.896],[112.600,37.916],[112.580,37.916]]', 1, NOW(), NOW(), 0),
(17, '柳巷街道网格',   3, 2, 15, '林涛',   '林涛',   '13800000016',
 '[[112.560,37.858],[112.580,37.858],[112.580,37.878],[112.560,37.878]]', 1, NOW(), NOW(), 0),
(18, '老军营街道网格', 3, 2, 16, '何芳',   '何芳',   '13800000017',
 '[[112.540,37.828],[112.560,37.828],[112.560,37.848],[112.540,37.848]]', 1, NOW(), NOW(), 0),
(19, '晋祠街道网格',   3, 6, 17, '徐光',   '徐光',   '13800000018',
 '[[112.458,37.700],[112.478,37.700],[112.478,37.720],[112.458,37.720]]', 1, NOW(), NOW(), 0),
(20, '义井街道网格',   3, 6, 18, '宋梅',   '宋梅',   '13800000019',
 '[[112.478,37.730],[112.498,37.730],[112.498,37.750],[112.478,37.750]]', 1, NOW(), NOW(), 0),
(21, '平阳路街道网格', 3, 1, 19, '邓飞',   '邓飞',   '13800000020',
 '[[112.546,37.716],[112.566,37.716],[112.566,37.736],[112.546,37.736]]', 1, NOW(), NOW(), 0),
(22, '坞城街道网格',   3, 1, 20, '吴霞',   '吴霞',   '13800000021',
 '[[112.546,37.796],[112.566,37.796],[112.566,37.816],[112.546,37.816]]', 1, NOW(), NOW(), 0);

-- ============================================================
-- 4b. 网格-企业关联表
-- ============================================================
DROP TABLE IF EXISTS `grid_enterprise`;
CREATE TABLE `grid_enterprise` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `grid_id` bigint NOT NULL COMMENT '网格ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `inspector_id` bigint DEFAULT NULL COMMENT '巡查人员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grid_enterprise` (`grid_id`, `enterprise_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网格企业关联表';

INSERT INTO `grid_enterprise` VALUES
(1,  4,  1,  8,  NOW()), (2,  4,  10, 9,  NOW()),
(3,  5,  2,  10, NOW()), (4,  5,  4,  11, NOW()), (5,  5,  11, 10, NOW()),
(6,  3,  7,  12, NOW()), (7,  3,  12, 13, NOW()),
(8,  6,  3,  16, NOW()), (9,  6,  6,  17, NOW()),
(10, 1,  5,  18, NOW()), (11, 1,  8,  19, NOW()),
(12, 2,  9,  14, NOW());

-- ============================================================
-- 5. 企业表
-- ============================================================
DROP TABLE IF EXISTS `enterprise`;
CREATE TABLE `enterprise` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `enterprise_code` varchar(50) DEFAULT NULL COMMENT '企业编号',
  `enterprise_name` varchar(100) NOT NULL COMMENT '企业名称',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `pollution_type` varchar(50) DEFAULT NULL COMMENT '污染类型',
  `supervise_type` varchar(20) DEFAULT NULL COMMENT '监管等级 I/II/III',
  `enterprise_type` varchar(50) DEFAULT NULL COMMENT '企业类型',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `legal_phone` varchar(20) DEFAULT NULL COMMENT '法人电话',
  `credit_level` varchar(10) DEFAULT NULL COMMENT '信用等级 A/B/C/D',
  `production_status` int DEFAULT 1 COMMENT '生产状态 0:停产 1:正常 2:限产',
  `source_type` varchar(20) DEFAULT NULL COMMENT '来源类型',
  `has_video` tinyint DEFAULT 0 COMMENT '是否有视频监控',
  `has_monitor` tinyint DEFAULT 0 COMMENT '是否有在线监测',
  `has_working` tinyint DEFAULT 0 COMMENT '是否在产',
  `status` tinyint DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_enterprise_name` (`enterprise_name`),
  KEY `idx_supervise_type` (`supervise_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业表';

INSERT INTO `enterprise` VALUES
(1, 'ENT-20260001', '太原钢铁集团',    '尖草坪区尖草坪街2号',   112.580400, 37.926400, '大气污染,水污染',      'I',   '钢铁冶炼', '高祥明', '0351-3012345', 'A', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(2, 'ENT-20260002', '太重集团',        '万柏林区玉河街53号',    112.516700, 37.863200, '大气污染,噪声',        'I',   '机械制造', '韩珍堂', '0351-6361000', 'A', 1, '重点源', 1, 0, 1, 1, NOW(), NOW(), 0),
(3, 'ENT-20260003', '太原化工集团',    '晋源区化工路1号',       112.494200, 37.730800, '水污染,大气污染,危废',  'I',   '化工',     '王强',   '0351-6085000', 'B', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(4, 'ENT-20260004', '山西焦煤集团',    '万柏林区西矿街388号',   112.504400, 37.858900, '大气污染,固废',        'I',   '煤炭开采', '赵建泽', '0351-8305000', 'A', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(5, 'ENT-20260005', '太原污水处理厂',  '小店区汾东南路88号',    112.563800, 37.740500, '水污染',                'II',  '污水处理', '刘志刚', '0351-7598000', 'B', 1, '一般源', 0, 1, 1, 1, NOW(), NOW(), 0),
(6, 'ENT-20260006', '太原第一热电厂',  '晋源区晋祠路三段59号',  112.482500, 37.757300, '大气污染',              'I',   '电力',     '李明',   '0351-6321000', 'B', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(7, 'ENT-20260007', '太原水泥厂',      '杏花岭区涧河路85号',    112.587800, 37.904300, '大气污染,粉尘',        'II',  '建材',     '陈大力', '0351-3087000', 'C', 1, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0),
(8, 'ENT-20260008', '山西制药厂',      '小店区学府街102号',     112.551700, 37.803300, '水污染,危废',          'II',  '制药',     '张华',   '0351-7012000', 'B', 1, '一般源', 0, 1, 1, 1, NOW(), NOW(), 0),
(9, 'ENT-20260009', '太原食品加工厂',  '迎泽区五一路168号',     112.570300, 37.867900, '水污染',                'III', '食品加工', '赵伟',   '0351-2023000', 'B', 1, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0),
(10,'ENT-20260010', '太原第二热电厂',  '尖草坪区迎新街20号',    112.535600, 37.948100, '大气污染',              'I',   '电力',     '孙涛',   '0351-3056000', 'B', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(11,'ENT-20260011', '太原印染厂',      '万柏林区和平南路99号',  112.514300, 37.848600, '水污染',                'II',  '纺织印染', '周明',   '0351-6074000', 'C', 1, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0),
(12,'ENT-20260012', '太原电镀厂',      '杏花岭区胜利街56号',    112.567800, 37.886300, '水污染,重金属',        'III', '电镀',     '吴志远', '0351-3091000', 'D', 1, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0),
(13,'ENT-20260013', '太原玻璃厂',      '迎泽区建设南路200号',   112.582500, 37.855600, '大气污染,粉尘',        'II',  '建材',     '郑建军', '0351-2022000', 'C', 1, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0),
(14,'ENT-20260014', '太原化肥厂',      '晋源区晋祠路88号',      112.468300, 37.715400, '水污染,大气污染',      'I',   '化肥',     '马永强', '0351-6938000', 'B', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(15,'ENT-20260015', '太原造纸厂',      '小店区龙城大街55号',    112.572100, 37.778200, '水污染',                'II',  '造纸',     '许文龙', '0351-7871000', 'C', 1, '一般源', 0, 1, 1, 1, NOW(), NOW(), 0),
(16,'ENT-20260016', '太原合金材料厂',  '尖草坪区钢园路33号',    112.560900, 37.935400, '大气污染,重金属',      'II',  '合金制造', '段刚',   '0351-3345000', 'C', 2, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0),
(17,'ENT-20260017', '太原电子废弃物处理中心', '万柏林区迎泽西大街88号', 112.495200, 37.842500, '固废,危废',        'I',   '危废处理', '钱卫华', '0351-6189000', 'B', 1, '重点源', 1, 1, 1, 1, NOW(), NOW(), 0),
(18,'ENT-20260018', '太原燃气公司储配站',    '杏花岭区北大街120号',   112.558200, 37.891700, '大气污染',            'II',  '燃气',     '丁建国', '0351-3034000', 'B', 1, '一般源', 0, 1, 1, 1, NOW(), NOW(), 0),
(19,'ENT-20260019', '太原塑料制品厂',  '迎泽区朝阳街47号',     112.592300, 37.850200, '大气污染,固废',        'III', '塑料加工', '叶军',   '0351-4065000', 'D', 0, '一般源', 0, 0, 0, 1, NOW(), NOW(), 0),
(20,'ENT-20260020', '太原酿酒厂',      '小店区平阳南路12号',    112.558600, 37.762500, '水污染',                'III', '酿酒',     '潘志华', '0351-7523000', 'C', 1, '一般源', 0, 0, 1, 1, NOW(), NOW(), 0);

-- ============================================================
-- 6. 环境问题表
-- ============================================================
DROP TABLE IF EXISTS `env_problem`;
CREATE TABLE `env_problem` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `problem_no` varchar(50) DEFAULT NULL COMMENT '问题编号',
  `problem_level` varchar(10) DEFAULT NULL COMMENT '问题级别 I/II/III',
  `alarm_time` datetime DEFAULT NULL COMMENT '报警时间',
  `problem_source` varchar(50) DEFAULT NULL COMMENT '问题来源 巡查发现/在线监测/群众举报/上级交办',
  `problem_source_detail` varchar(255) DEFAULT NULL COMMENT '来源详情',
  `problem_type` varchar(50) DEFAULT NULL COMMENT '问题类型 水污染/大气污染/固废污染/噪声污染',
  `pollution_type` varchar(50) DEFAULT NULL COMMENT '污染类别',
  `problem_desc` varchar(500) DEFAULT NULL COMMENT '问题描述',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `enterprise_id` bigint DEFAULT NULL COMMENT '关联企业ID',
  `area_code` varchar(20) DEFAULT NULL COMMENT '行政区划代码',
  `area_name` varchar(50) DEFAULT NULL COMMENT '区域名称',
  `merge_id` bigint DEFAULT NULL COMMENT '合并问题ID',
  `close_reason` varchar(500) DEFAULT NULL COMMENT '关闭原因',
  `handle_status` varchar(20) DEFAULT '待处理' COMMENT '处理状态 待处理/处理中/已关闭',
  `penalty_status` varchar(20) DEFAULT NULL COMMENT '处罚状态',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_problem_no` (`problem_no`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境问题表';

INSERT INTO `env_problem` VALUES
(1,  'PB-20260521-001', 'I',   '2026-05-21 09:00:00', '在线监测', '汾河断面自动监测站数据异常', '水污染',   '工业废水', '工业废水排放超标，汾河水质异常，COD在线监测值达120mg/L，远超标准值60mg/L', '尖草坪区汾河东路',   112.540400, 37.886400, 1,  '140108', '尖草坪区', NULL, NULL,                                                            '待处理', NULL,     2, NOW(), NOW(), 0),
(2,  'PB-20260522-001', 'I',   '2026-05-22 10:00:00', '巡查发现', '网格员日常巡查中发现',       '大气污染', '扬尘',     '建筑工地扬尘严重，PM10浓度超标，周边居民多次投诉空气中有大量粉尘',             '万柏林区玉河街',     112.576700, 37.853200, 2,  '140109', '万柏林区', NULL, NULL,                                                            '处理中', NULL,     3, NOW(), NOW(), 0),
(3,  'PB-20260523-001', 'II',  '2026-05-23 14:30:00', '群众举报', '12369环保热线投诉',          '固废污染', '生活垃圾', '露天焚烧垃圾，产生大量浓烟，造成空气污染，周边有刺鼻气味',                 '晋源区化工路附近',   112.514200, 37.930800, 3,  '140110', '晋源区',   NULL, '已责令当场扑灭，并对责任人进行教育处罚',                                    '已关闭', '已处罚', 4, NOW(), NOW(), 0),
(4,  'PB-20260524-001', 'I',   '2026-05-24 22:00:00', '巡查发现', '夜间突击检查发现',           '大气污染', '偷排废气', '企业夜间偷排废气，烟囱排烟颜色异常，疑似未开启废气处理设备',                 '杏花岭区涧河路',     112.564400, 37.818900, 7,  '140107', '杏花岭区', NULL, NULL,                                                            '待处理', NULL,     5, NOW(), NOW(), 0),
(5,  'PB-20260525-001', 'III', '2026-05-25 08:00:00', '群众举报', '市民热线12345转办',          '水污染',   '河道垃圾', '汾河河道漂浮垃圾堆积，影响市容环境和水质，长度约50米',                     '迎泽区汾河公园段',   112.523800, 37.790500, NULL, '140106', '迎泽区',   NULL, '已组织环卫清理完毕',                                                        '已关闭', NULL,     6, NOW(), NOW(), 0),
(6,  'PB-20260525-002', 'II',  '2026-05-25 11:30:00', '群众举报', '居民小区微信举报',           '大气污染', '餐饮油烟', '餐饮店铺油烟直排，未安装油烟净化设备，居民多次投诉，影响正常生活',           '小店区学府街',       112.542500, 37.867300, 8,  '140105', '小店区',   NULL, NULL,                                                            '处理中', NULL,     2, NOW(), NOW(), 0),
(7,  'PB-20260526-001', 'I',   '2026-05-26 15:00:00', '巡查发现', '专项检查中发现',             '危化品',   '化工泄漏', '化工厂储罐区疑似存在泄漏风险，现场有轻微异味，需立即排查',                 '晋源区晋祠路',       112.597800, 37.924300, 3,  '140110', '晋源区',   NULL, NULL,                                                            '待处理', NULL,     3, NOW(), NOW(), 0),
(8,  'PB-20260526-002', 'II',  '2026-05-26 16:00:00', '巡查发现', '月度巡查中发现',             '大气污染', '矿区扬尘', '矿区开采面扬尘未有效覆盖，风大时扬尘扩散至周边居民区',                   '万柏林区西矿街',     112.541700, 37.833300, 4,  '140109', '万柏林区', NULL, NULL,                                                            '处理中', NULL,     4, NOW(), NOW(), 0),
(9,  'PB-20260527-001', 'II',  '2026-05-27 10:00:00', '群众举报', '12369热线投诉',              '噪声污染', '工业噪声', '太原印染厂夜间生产噪声扰民，周边居民多次反映无法正常休息',               '万柏林区和平南路',   112.514300, 37.848600, 11, '140109', '万柏林区', NULL, NULL,                                                            '待处理', NULL,     10, NOW(), NOW(), 0),
(10, 'PB-20260527-002', 'I',   '2026-05-27 14:00:00', '在线监测', '废气在线监测超标报警',       '大气污染', 'SO2超标',  '太原第一热电厂SO2排放浓度在线监测值连续3小时超标，峰值达280mg/m³',         '晋源区晋祠路三段',   112.482500, 37.757300, 6,  '140110', '晋源区',   NULL, NULL,                                                            '处理中', NULL,     16, NOW(), NOW(), 0),
(11, 'PB-20260528-001', 'III', '2026-05-28 08:30:00', '巡查发现', '日常巡查中发现',             '固废污染', '建筑垃圾', '建筑工地附近空地堆放大量建筑垃圾，未按要求覆盖防尘网',                   '小店区龙城大街',     112.572100, 37.778200, 15, '140105', '小店区',   NULL, NULL,                                                            '待处理', NULL,     18, NOW(), NOW(), 0),
(12, 'PB-20260528-002', 'II',  '2026-05-28 11:00:00', '上级交办', '省环保督察组交办',           '大气污染', '异味扰民', '太原合金材料厂附近居民反映有刺鼻异味，疑似企业生产废气泄漏',             '尖草坪区钢园路',     112.560900, 37.935400, 16, '140108', '尖草坪区', NULL, NULL,                                                            '处理中', NULL,     8,  NOW(), NOW(), 0),
(13, 'PB-20260529-001', 'III', '2026-05-29 09:00:00', '巡查发现', '日常巡查中发现',             '水污染',   '废水直排', '太原食品加工厂废水处理设施疑似未正常运行，排污口水质浑浊',               '迎泽区五一路',       112.570300, 37.867900, 9,  '140106', '迎泽区',   NULL, NULL,                                                            '待处理', NULL,     14, NOW(), NOW(), 0),
(14, 'PB-20260529-002', 'I',   '2026-05-29 15:00:00', '巡查发现', '专项执法检查中发现',         '危废',     '危废违规','电子废弃物处理中心危废仓库标识不清，危废分类存放不规范',                   '万柏林区迎泽西大街', 112.495200, 37.842500, 17, '140109', '万柏林区', NULL, NULL,                                                            '待处理', NULL,     11, NOW(), NOW(), 0),
(15, 'PB-20260530-001', 'II',  '2026-05-30 10:30:00', '群众举报', '12369热线投诉',              '水污染',   '油污泄漏', '太原造纸厂厂区外排水沟发现黑色含油污水，已流入附近农田',                 '小店区龙城大街',     112.572100, 37.778200, 15, '140105', '小店区',   NULL, NULL,                                                            '待处理', NULL,     19, NOW(), NOW(), 0);

-- ============================================================
-- 6b. 环境问题动态日志表
-- ============================================================
DROP TABLE IF EXISTS `env_problem_log`;
CREATE TABLE `env_problem_log` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `problem_id` bigint NOT NULL COMMENT '问题ID',
  `operation_type` varchar(30) NOT NULL COMMENT '操作类型 warn/edit/dispatch/process/close/merge',
  `content` text COMMENT '操作内容',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_problem_id` (`problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境问题动态日志表';

INSERT INTO `env_problem_log` VALUES
(1,  1,  'warn',     '在线监测系统自动报警：COD值120mg/L超标',                                        1,  '2026-05-21 09:00:00'),
(2,  1,  'dispatch', '调度员已将问题派发给尖草坪区网格长张爱国处理',                                 20, '2026-05-21 09:15:00'),
(3,  2,  'warn',     '网格员李为民上报：万柏林区玉河街扬尘问题',                                      3,  '2026-05-22 10:00:00'),
(4,  2,  'dispatch', '调度员将问题派发给万柏林区网格长处理',                                          20, '2026-05-22 10:20:00'),
(5,  3,  'close',    '已责令当场扑灭露天焚烧，并对责任人处以罚款500元',                                4,  '2026-05-23 16:00:00'),
(6,  5,  'close',    '环卫部门已清理完毕，共清理垃圾约3吨',                                            6,  '2026-05-25 17:00:00'),
(7,  10, 'warn',     'SO2在线监测连续3小时超标，系统自动报警',                                         1,  '2026-05-27 14:00:00'),
(8,  10, 'dispatch', '调度员将问题交给晋源区网格员徐光处理',                                          21, '2026-05-27 14:30:00'),
(9,  10, 'process',  '徐光已赴现场核查，确认在线监测设备正常，企业脱硫设施疑似故障',                  16, '2026-05-27 16:00:00'),
(10, 12, 'edit',     '陈伟更新问题等级为II级，协调环保执法队联合处理',                                10, '2026-05-28 14:00:00');

-- ============================================================
-- 7. 任务信息表
-- ============================================================
DROP TABLE IF EXISTS `task_info`;
CREATE TABLE `task_info` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_no` varchar(50) DEFAULT NULL COMMENT '任务编号',
  `task_title` varchar(200) NOT NULL COMMENT '任务标题',
  `task_type` varchar(50) DEFAULT NULL COMMENT '任务类型 日常巡查/专项检查/投诉处理/整改通知/复查验收',
  `urgency` varchar(20) DEFAULT NULL COMMENT '紧急程度 一般/紧急/非常紧急',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `deadline` datetime DEFAULT NULL COMMENT '截止时间',
  `dispatch_time` datetime DEFAULT NULL COMMENT '调度时间',
  `initiator_id` bigint DEFAULT NULL COMMENT '发起人ID',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handler_unit_id` bigint DEFAULT NULL COMMENT '处理单位ID',
  `grid_id` bigint DEFAULT NULL COMMENT '所属网格ID',
  `enterprise_id` bigint DEFAULT NULL COMMENT '关联企业ID',
  `problem_id` bigint DEFAULT NULL COMMENT '关联问题ID',
  `check_template_id` bigint DEFAULT NULL COMMENT '检查模板ID',
  `task_content` text COMMENT '任务内容',
  `cc_users` varchar(255) DEFAULT NULL COMMENT '抄送人',
  `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态 DRAFT/DISPATCHED/RECEIVED/PROCESSING/COMPLETED/RETURNED',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_task_no` (`task_no`),
  KEY `idx_status` (`status`),
  KEY `idx_handler_id` (`handler_id`),
  KEY `idx_grid_id` (`grid_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务信息表';

INSERT INTO `task_info` VALUES
(1, 'TK-20260520', '排查尖草坪区企业排污情况',    '日常巡查', '紧急',    '2026-05-20 08:00:00', '2026-06-01 18:00:00', '2026-05-20 09:00:00', 1,  2,  2,  4,  1,  1,  1, '针对尖草坪区重点排污企业进行全面排查，重点检查废水处理设施运行情况，核实在线监测数据准确性。',                                  'admin',  'DISPATCHED', NULL,                    NOW(), NOW(), 0),
(2, 'TK-20260521', '复核万柏林区扬尘治理进度',    '专项检查', '一般',    '2026-05-21 08:00:00', '2026-06-06 18:00:00', '2026-05-21 10:00:00', 1,  3,  3,  5,  2,  2,  2, '对万柏林区建筑工地和工业企业扬尘治理情况进行专项复核，确认PM10监测数据是否达标。',                                              'admin',  'DISPATCHED', NULL,                    NOW(), NOW(), 0),
(3, 'TK-20260522', '处理晋源区河道污染投诉',      '投诉处理', '非常紧急','2026-05-22 08:00:00', '2026-06-11 18:00:00', '2026-05-22 09:00:00', 1,  4,  6,  6,  3,  NULL, NULL,'立即前往晋源区化工路河道实地查看污染情况，采样检测水质，核实污染源头，48小时内提交初步调查报告。',                                'admin',  'RECEIVED',   NULL,                    NOW(), NOW(), 0),
(4, 'TK-20260523', '日常巡查杏花岭区重点企业',    '日常巡查', '一般',    '2026-05-23 08:00:00', '2026-06-16 18:00:00', '2026-05-23 09:00:00', 1,  5,  4,  3,  7,  4,  1, '对杏花岭区I、II类重点监管企业进行日常巡查，检查废气废水排放达标情况和在线设备运行状态。',                                      'admin',  'DISPATCHED', NULL,                    NOW(), NOW(), 0),
(5, 'TK-20260524', '整改迎泽区餐饮油烟问题',      '整改通知', '紧急',    '2026-05-24 08:00:00', '2026-06-21 18:00:00', '2026-05-24 10:00:00', 1,  6,  5,  2,  NULL, 6,  NULL,'针对市民投诉的迎泽区五一路餐饮油烟直排问题，下发整改通知书，限期15日内安装油烟净化设备并验收。',                                  'admin',  'RECEIVED',   NULL,                    NOW(), NOW(), 0),
(6, 'TK-20260525', '复查小店区污水处理设施',      '复查验收', '一般',    '2026-05-25 08:00:00', '2026-06-26 18:00:00', '2026-05-25 09:00:00', 1,  7,  7,  1,  5,  NULL, NULL,'对太原污水处理厂上次整改情况进行复查验收，确认污水处理设施运行是否恢复正常，出水水质是否达标。',                                  'admin',  'COMPLETED',  '2026-05-25 17:00:00',   NOW(), NOW(), 0),
(7, 'TK-20260527', '核查太原第一热电厂SO2超标',    '投诉处理', '非常紧急','2026-05-27 15:00:00', '2026-05-30 18:00:00', '2026-05-27 15:30:00', 20, 16, 17, 6,  6,  10, NULL,'在线监测显示太原第一热电厂SO2持续超标，立即赴现场核查脱硫设施运行状况，对超标原因进行分析并出具检测报告。',                      'admin',  'RECEIVED',   NULL,                    NOW(), NOW(), 0),
(8, 'TK-20260528', '调查合金材料厂异味扰民投诉',  '投诉处理', '紧急',    '2026-05-28 13:00:00', '2026-06-04 18:00:00', '2026-05-28 14:00:00', 20, 8,  8,  4,  16, 12, NULL,'省环保督察组交办：尖草坪区钢园路附近居民反映有刺鼻异味，调查太原合金材料厂废气排放和异味来源。',                                'admin',  'PROCESSING', NULL,                    NOW(), NOW(), 0),
(9, 'TK-20260529', '专项检查危废规范化管理',      '专项检查', '一般',    '2026-05-29 08:00:00', '2026-06-12 18:00:00', '2026-05-29 09:00:00', 1,  10, 11, 5,  17, 14, NULL,'对万柏林区涉危废企业进行专项检查，重点核查危废分类存放、标识标签、台账管理和转移联单制度执行情况。',                              'admin',  'DISPATCHED', NULL,                    NOW(), NOW(), 0),
(10,'TK-20260530', '处理造纸厂油污泄漏事件',      '投诉处理', '紧急',    '2026-05-30 11:00:00', '2026-06-06 18:00:00', '2026-05-30 11:30:00', 21, 19, 19, 1,  15, 15, NULL,'接群众举报：小店区太原造纸厂厂区外排水沟发现黑色含油污水，已流入农田。立即赴现场采样取证，责令停排并启动应急处置。',            'admin',  'DISPATCHED', NULL,                    NOW(), NOW(), 0);

-- ============================================================
-- 8. 任务处理表
-- ============================================================
DROP TABLE IF EXISTS `task_process`;
CREATE TABLE `task_process` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `process_date` date DEFAULT NULL COMMENT '处理日期',
  `conclusion` varchar(500) DEFAULT NULL COMMENT '处理结论',
  `suggestion` varchar(500) DEFAULT NULL COMMENT '处理建议',
  `rectification` varchar(500) DEFAULT NULL COMMENT '整改措施',
  `rectification_deadline` datetime DEFAULT NULL COMMENT '整改期限',
  `production_status` varchar(20) DEFAULT NULL COMMENT '生产状态',
  `is_signin` tinyint DEFAULT 0 COMMENT '是否签到 0:否 1:是',
  `signin_time` datetime DEFAULT NULL COMMENT '签到时间',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务处理表';

INSERT INTO `task_process` VALUES
(1, 6, '2026-05-25', '污水处理设施已恢复正常运行，出水水质各项指标均达标',          '建议加强日常运维管理，定期校准在线监测设备',                       '已更换损坏的曝气设备，清理了沉淀池',           '2026-06-10 18:00:00', '正常', 1, '2026-05-25 08:30:00', 7,  NOW()),
(2, 3, '2026-05-22', '现场核查发现河道确实存在工业废水排放痕迹，已取样送检',        '建议对上游化工厂进行全面排查，锁定污染源',                         '已设置临时围堰防止污染扩散',                   '2026-05-30 18:00:00', '正常', 1, '2026-05-22 09:00:00', 4,  NOW()),
(3, 1, '2026-05-21', '初步排查完成，发现2家企业在线监测设备数据偏低',             '建议约谈企业负责人，限期整改并处罚',                               '已要求企业校准设备并提交整改方案',             '2026-06-05 18:00:00', '正常', 1, '2026-05-21 08:00:00', 2,  NOW()),
(4, 7, '2026-05-27', '现场核查确认脱硫设施喷淋系统故障，导致SO2超标排放',          '建议责令企业立即停产检修，并对超标时段进行经济处罚',              '企业已启动脱硫设施应急检修预案',               '2026-05-30 18:00:00', '限产', 1, '2026-05-27 16:30:00', 16, NOW()),
(5, 8, '2026-05-28', '现场排查发现企业生产车间废气收集罩破损，导致废气泄漏',       '建议责令企业修复收集罩，增加车间通风，对周边居民进行回访',        '已下达限期整改通知书，要求5日内修复',          '2026-06-04 18:00:00', '限产', 1, '2026-05-28 15:00:00', 8,  NOW()),
(6, 5, '2026-05-25', '走访餐饮店铺确认为多家共用一条排烟管道，均未安装净化设备',   '建议联合市场监管部门进行联合执法，督促商户统一安装油烟净化器',    '已下发联合整改通知，10日内完成安装',           '2026-06-05 18:00:00', '正常', 1, '2026-05-25 10:00:00', 6,  NOW());

-- ============================================================
-- 9. 任务转办表
-- ============================================================
DROP TABLE IF EXISTS `task_transfer`;
CREATE TABLE `task_transfer` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `transfer_type` varchar(20) DEFAULT NULL COMMENT '转办类型',
  `from_user_id` bigint DEFAULT NULL COMMENT '转出人ID',
  `to_user_id` bigint DEFAULT NULL COMMENT '接收人ID',
  `reason` varchar(500) DEFAULT NULL COMMENT '转办原因',
  `suggest_handler` varchar(50) DEFAULT NULL COMMENT '建议处理人',
  `suggest_unit` varchar(100) DEFAULT NULL COMMENT '建议处理单位',
  `audit_result` varchar(20) DEFAULT NULL COMMENT '审核结果',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务转办表';

INSERT INTO `task_transfer` VALUES
(1, 2, '转办', 3,  5,  '万柏林区扬尘治理涉及机械制造企业，需杏花岭区协助',                       '赵志强', '尖草坪区环保局', '已通过', NOW()),
(2, 1, '转办', 2,  8,  '尖草坪区排查任务增加对太原钢铁集团在线监测设备专项核查',                '刘强',   '尖草坪街道环保办','已通过', NOW()),
(3, 8, '督办', 20, 8,  '合金材料厂异味投诉涉及省环保督察组，请加快处理进度，48小时内报送进展',    NULL,     NULL,             '已通过', NOW());

-- ============================================================
-- 10. 检查项表
-- ============================================================
DROP TABLE IF EXISTS `check_item`;
CREATE TABLE `check_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `item_name` varchar(100) NOT NULL COMMENT '检查项名称',
  `item_type` varchar(50) DEFAULT NULL COMMENT '检查类别',
  `monitor_type` varchar(50) DEFAULT NULL COMMENT '监测类型 水污染/大气污染/噪声',
  `input_type` varchar(20) DEFAULT '数字输入' COMMENT '输入类型 数字输入/文本描述/单选/多选',
  `status` tinyint DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查项表';

INSERT INTO `check_item` VALUES
(1,  '废水排放口pH值',     '排污检查', '水污染',   '数字输入', 1, NOW(), NOW(), 0),
(2,  'COD在线监测',        '排污检查', '水污染',   '数字输入', 1, NOW(), NOW(), 0),
(3,  '氨氮浓度检测',       '排污检查', '水污染',   '数字输入', 1, NOW(), NOW(), 0),
(4,  '废气SO2浓度',        '排污检查', '大气污染', '数字输入', 1, NOW(), NOW(), 0),
(5,  '烟尘排放浓度',       '排污检查', '大气污染', '文本描述', 1, NOW(), NOW(), 0),
(6,  '噪声分贝检测',       '排污检查', '噪声',     '数字输入', 1, NOW(), NOW(), 0),
(7,  '总磷浓度检测',       '排污检查', '水污染',   '数字输入', 1, NOW(), NOW(), 0),
(8,  'NOx浓度检测',        '排污检查', '大气污染', '数字输入', 1, NOW(), NOW(), 0),
(9,  '危废台账检查',       '台账检查', '固废',     '单选',     1, NOW(), NOW(), 0),
(10, '排污许可证核查',     '资质检查', '综合',     '文本描述', 1, NOW(), NOW(), 0),
(11, '在线监测设备运行状态','设备检查', '综合',     '单选',     1, NOW(), NOW(), 0),
(12, '厂界无组织排放检测', '排污检查', '大气污染', '数字输入', 1, NOW(), NOW(), 0);

-- ============================================================
-- 11. 检查模板表
-- ============================================================
DROP TABLE IF EXISTS `check_template`;
CREATE TABLE `check_template` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `input_type` varchar(20) DEFAULT 'RADIO' COMMENT '判断类型 RADIO/CHECKBOX',
  `is_normal` tinyint DEFAULT 1 COMMENT '正常选项 0:否 1:是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检查模板表';

INSERT INTO `check_template` VALUES
(1, '废水排放达标', 'RADIO', 1, NOW(), NOW(), 0),
(2, '废气排放达标', 'CHECKBOX', 1, NOW(), NOW(), 0),
(3, '固体废物规范存放', 'RADIO', 0, NOW(), NOW(), 0),
(4, '噪声达标', 'RADIO', 0, NOW(), NOW(), 0),
(5, '在线设备正常运行', 'CHECKBOX', 1, NOW(), NOW(), 0);

-- ============================================================
-- 12. 考评指标表
-- ============================================================
DROP TABLE IF EXISTS `assess_indicator`;
CREATE TABLE `assess_indicator` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `indicator_name` varchar(100) NOT NULL COMMENT '指标名称',
  `assess_type` varchar(20) DEFAULT NULL COMMENT '考评类型 月度/半年/年度',
  `is_valid` tinyint DEFAULT 1 COMMENT '是否有效 0:无效 1:有效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考评指标表';

INSERT INTO `assess_indicator` VALUES
(1, '企业排污达标率', '月度', 1, NOW(), NOW(), 0),
(2, '巡查任务完成率', '月度', 1, NOW(), NOW(), 0),
(3, '问题整改及时率', '半年', 1, NOW(), NOW(), 0),
(4, '公众投诉处理率', '半年', 1, NOW(), NOW(), 0),
(5, '在线监测有效率', '年度', 1, NOW(), NOW(), 0);

-- ============================================================
-- 13. 考评规则表
-- ============================================================
DROP TABLE IF EXISTS `assess_rule`;
CREATE TABLE `assess_rule` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `version` varchar(20) DEFAULT NULL COMMENT '版本号',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考评规则表';

INSERT INTO `assess_rule` VALUES
(1, '月度考评规则v2.0', '2026-01', '污染管控', NOW(), NOW(), 0),
(2, '半年考评规则v1.5', '2026-01', '任务完成', NOW(), NOW(), 0),
(3, '年度考评规则v3.0', '2026-01', '综合考评', NOW(), NOW(), 0);

-- ============================================================
-- 14. 考评结果表
-- ============================================================
DROP TABLE IF EXISTS `assess_result`;
CREATE TABLE `assess_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `grid_id` bigint DEFAULT NULL COMMENT '网格ID',
  `grid_name` varchar(100) DEFAULT NULL COMMENT '网格名称',
  `rule_id` bigint DEFAULT NULL COMMENT '规则ID',
  `score` decimal(5,1) DEFAULT NULL COMMENT '得分',
  `level` varchar(5) DEFAULT NULL COMMENT '评级 A/B/C/D/E',
  `assess_period` varchar(20) DEFAULT NULL COMMENT '考评周期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考评结果表';

INSERT INTO `assess_result` VALUES
(1, 1, '尖草坪区', 1, 92.0, 'A', '2026-05', NOW(), NOW(), 0),
(2, 2, '万柏林区', 1, 89.0, 'A', '2026-05', NOW(), NOW(), 0),
(3, 3, '晋源区', 1, 78.0, 'B', '2026-05', NOW(), NOW(), 0),
(4, 4, '杏花岭区', 1, 76.0, 'B', '2026-05', NOW(), NOW(), 0),
(5, 5, '迎泽区', 1, 75.0, 'B', '2026-05', NOW(), NOW(), 0),
(6, 6, '小店区', 1, 65.0, 'C', '2026-05', NOW(), NOW(), 0);

-- ============================================================
-- 15. 联系人表
-- ============================================================
DROP TABLE IF EXISTS `contact`;
CREATE TABLE `contact` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `contact_type` varchar(20) NOT NULL COMMENT '联系人类型 PERSON/ORG',
  `name` varchar(100) NOT NULL COMMENT '姓名或单位名称',
  `phone` varchar(20) NOT NULL COMMENT '联系电话',
  `org_id` bigint DEFAULT NULL COMMENT '组织ID',
  `org_name` varchar(100) DEFAULT NULL COMMENT '组织名称',
  `position` varchar(50) DEFAULT NULL COMMENT '职务',
  `photo` varchar(255) DEFAULT NULL COMMENT '照片',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系人表';

INSERT INTO `contact` VALUES
(1,  'PERSON', '张爱国',  '13800000001', 2,  '尖草坪区环保局', '网格长',    NULL, NOW(), NOW(), 0),
(2,  'PERSON', '李为民',  '13800000002', 3,  '万柏林区环保局', '网格长',    NULL, NOW(), NOW(), 0),
(3,  'PERSON', '王守正',  '13800000003', 4,  '杏花岭区环保局', '网格长',    NULL, NOW(), NOW(), 0),
(4,  'PERSON', '赵志强',  '13800000004', 5,  '迎泽区环保局',  '网格长',    NULL, NOW(), NOW(), 0),
(5,  'PERSON', '孙建新',  '13800000005', 6,  '晋源区环保局',  '网格长',    NULL, NOW(), NOW(), 0),
(6,  'PERSON', '周明达',  '13800000006', 7,  '小店区环保局',  '网格长',    NULL, NOW(), NOW(), 0),
(7,  'ORG',   '尖草坪区环保局', '0351-5678001', 2,  '尖草坪区环保局', '主管部门', NULL, NOW(), NOW(), 0),
(8,  'ORG',   '万柏林区环保局', '0351-5678002', 3,  '万柏林区环保局', '主管部门', NULL, NOW(), NOW(), 0),
(9,  'ORG',   '杏花岭区环保局', '0351-5678003', 4,  '杏花岭区环保局', '主管部门', NULL, NOW(), NOW(), 0),
(10, 'ORG',   '迎泽区环保局',   '0351-5678004', 5,  '迎泽区环保局',  '主管部门', NULL, NOW(), NOW(), 0),
(11, 'ORG',   '晋源区环保局',   '0351-5678005', 6,  '晋源区环保局',  '主管部门', NULL, NOW(), NOW(), 0),
(12, 'ORG',   '小店区环保局',   '0351-5678006', 7,  '小店区环保局',  '主管部门', NULL, NOW(), NOW(), 0),
(13, 'PERSON', '彭丽娜',  '13800000030', 1,  '太原市生态环境局', '调度员',    NULL, NOW(), NOW(), 0),
(14, 'PERSON', '刘强',    '13800000010', 8,  '尖草坪街道环保办', '巡查员',    NULL, NOW(), NOW(), 0),
(15, 'PERSON', '陈伟',    '13800000012', 11, '千峰街道环保办',   '巡查员',    NULL, NOW(), NOW(), 0),
(16, 'PERSON', '徐光',    '13800000018', 17, '晋祠街道环保办',   '巡查员',    NULL, NOW(), NOW(), 0);

-- ============================================================
-- 17. 字典类型表
-- ============================================================
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_code` varchar(100) NOT NULL COMMENT '字典编码',
  `status` tinyint DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

INSERT INTO `sys_dict_type` VALUES
(1, '任务类型',     'task_type',      1, NOW(), NOW(), 0),
(2, '紧急程度',     'urgency',        1, NOW(), NOW(), 0),
(3, '问题级别',     'problem_level',  1, NOW(), NOW(), 0),
(4, '污染类型',     'pollution_type', 1, NOW(), NOW(), 0),
(5, '问题来源',     'problem_source', 1, NOW(), NOW(), 0),
(6, '监管等级',     'supervise_type', 1, NOW(), NOW(), 0),
(7, '处理状态',     'handle_status',  1, NOW(), NOW(), 0),
(8, '生产状态',     'production_status', 1, NOW(), NOW(), 0),
(9, '巡查周期',     'patrol_cycle',   1, NOW(), NOW(), 0),
(10,'任务状态',     'task_status',    1, NOW(), NOW(), 0);

-- ============================================================
-- 18. 字典数据表
-- ============================================================
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(100) NOT NULL COMMENT '字典值',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

INSERT INTO `sys_dict_data` VALUES
-- 任务类型
(1,  1, '日常巡查', 'PATROL',      1, 1, NOW(), NOW(), 0),
(2,  1, '专项检查', 'SPECIAL',     2, 1, NOW(), NOW(), 0),
(3,  1, '投诉处理', 'COMPLAINT',   3, 1, NOW(), NOW(), 0),
(4,  1, '整改通知', 'RECTIFY',     4, 1, NOW(), NOW(), 0),
(5,  1, '复查验收', 'RECHECK',     5, 1, NOW(), NOW(), 0),
-- 紧急程度
(6,  2, '一般',     'NORMAL',      1, 1, NOW(), NOW(), 0),
(7,  2, '紧急',     'URGENT',      2, 1, NOW(), NOW(), 0),
(8,  2, '非常紧急', 'CRITICAL',    3, 1, NOW(), NOW(), 0),
-- 问题级别
(9,  3, '严重',     'I',           1, 1, NOW(), NOW(), 0),
(10, 3, '较严重',   'II',          2, 1, NOW(), NOW(), 0),
(11, 3, '一般',     'III',         3, 1, NOW(), NOW(), 0),
-- 污染类型
(12, 4, '水污染',   'WASTE_WATER', 1, 1, NOW(), NOW(), 0),
(13, 4, '大气污染', 'WASTE_GAS',   2, 1, NOW(), NOW(), 0),
(14, 4, '噪声污染', 'NOISE',       3, 1, NOW(), NOW(), 0),
(15, 4, '固废污染', 'SOLID_WASTE', 4, 1, NOW(), NOW(), 0),
(16, 4, '危化品',   'HAZARDOUS',   5, 1, NOW(), NOW(), 0),
-- 问题来源
(17, 5, '巡查发现', 'PATROL',      1, 1, NOW(), NOW(), 0),
(18, 5, '在线监测', 'MONITOR',     2, 1, NOW(), NOW(), 0),
(19, 5, '群众举报', 'COMPLAINT',   3, 1, NOW(), NOW(), 0),
(20, 5, '上级交办', 'SUPERIOR',    4, 1, NOW(), NOW(), 0),
-- 监管等级
(21, 6, 'I级',      'I',           1, 1, NOW(), NOW(), 0),
(22, 6, 'II级',     'II',          2, 1, NOW(), NOW(), 0),
(23, 6, 'III级',    'III',         3, 1, NOW(), NOW(), 0),
-- 处理状态
(24, 7, '待处理',   'PENDING',     1, 1, NOW(), NOW(), 0),
(25, 7, '处理中',   'PROCESSING',  2, 1, NOW(), NOW(), 0),
(26, 7, '已关闭',   'CLOSED',      3, 1, NOW(), NOW(), 0),
-- 生产状态
(27, 8, '正常',     'NORMAL',      1, 1, NOW(), NOW(), 0),
(28, 8, '限产',     'LIMITED',     2, 1, NOW(), NOW(), 0),
(29, 8, '停产',     'STOPPED',     3, 1, NOW(), NOW(), 0),
-- 巡查周期
(30, 9, '每周',     'WEEKLY',      1, 1, NOW(), NOW(), 0),
(31, 9, '每月',     'MONTHLY',     2, 1, NOW(), NOW(), 0),
(32, 9, '每季度',   'QUARTERLY',   3, 1, NOW(), NOW(), 0),
(33, 9, '不定期',   'IRREGULAR',   4, 1, NOW(), NOW(), 0),
-- 任务状态
(34, 10,'已拟定',   'DRAFT',       1, 1, NOW(), NOW(), 0),
(35, 10,'已派发',   'DISPATCHED',  2, 1, NOW(), NOW(), 0),
(36, 10,'已签收',   'RECEIVED',    3, 1, NOW(), NOW(), 0),
(37, 10,'处理中',   'PROCESSING',  4, 1, NOW(), NOW(), 0),
(38, 10,'已完成',   'COMPLETED',   5, 1, NOW(), NOW(), 0),
(39, 10,'已退回',   'RETURNED',    6, 1, NOW(), NOW(), 0);

-- ============================================================
-- 完成
-- ============================================================
SELECT '数据库初始化完成！共创建 20 张表，已插入样本数据。
  - 组织: 20 条(市/区/街道三级)
  - 角色: 5 条
  - 用户: 22 条
  - 用户角色关联: 26 条
  - 网格: 19 条(市/区/街道三级)
  - 网格企业关联: 12 条
  - 企业: 20 条
  - 环境问题: 15 条
  - 问题日志: 10 条
  - 任务: 10 条
  - 任务处理: 6 条
  - 任务转办: 3 条
  - 检查项: 12 条
  - 检查模板: 5 条
  - 考评指标: 5 条
  - 考评规则: 3 条
  - 考评结果: 6 条
  - 联系人: 16 条
  - 巡查计划: 4 条
  - 字典类型: 10 条
  - 字典数据: 39 条
所有用户密码: 123456' AS message;
