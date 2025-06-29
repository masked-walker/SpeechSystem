CREATE TABLE IF NOT EXISTS `room` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '会议室名称',
  `capacity` int(11) NOT NULL COMMENT '容纳人数',
  `location` varchar(100) NOT NULL COMMENT '位置',
  `equipment` json DEFAULT NULL COMMENT '设备配置',
  `status` varchar(20) NOT NULL DEFAULT 'available' COMMENT '状态：available-可用，maintenance-维护中',
  `description` text COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表'; 