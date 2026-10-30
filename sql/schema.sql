CREATE DATABASE IF NOT EXISTS elderly_care_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elderly_care_system;
DROP TABLE IF EXISTS `fall_record`;
DROP TABLE IF EXISTS `follow_up_plan`;
DROP TABLE IF EXISTS `visit_record`;
DROP TABLE IF EXISTS `order_form`;
DROP TABLE IF EXISTS `help_event`;
DROP TABLE IF EXISTS `device`;
DROP TABLE IF EXISTS `community_staff`;
DROP TABLE IF EXISTS `doctor`;
DROP TABLE IF EXISTS `elderly`;
DROP TABLE IF EXISTS `community`;
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '登录密码（明文，管理员可在后台查看/修改）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` ENUM('community_worker', 'system_admin', 'doctor') NOT NULL COMMENT '角色',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';
CREATE TABLE `community` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '社区名称',
    `address` VARCHAR(200) COMMENT '社区地址',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区表';
CREATE TABLE `elderly` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender` VARCHAR(10) NOT NULL COMMENT '性别',
    `age` INT COMMENT '年龄',
    `id_card` VARCHAR(20) COMMENT '身份证号',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(200) COMMENT '住址',
    `emergency_contact` VARCHAR(50) COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(20) COMMENT '紧急联系电话',
    `chronic_diseases` VARCHAR(500) COMMENT '既往病史',
    `allergies` VARCHAR(500) COMMENT '过敏史',
    `current_illness` VARCHAR(500) COMMENT '现病史(医生可编辑)',
    `medications` VARCHAR(500) COMMENT '用药情况(医生可编辑)',
    `body_status` VARCHAR(500) COMMENT '身体现状(医生可编辑)',
    `community_id` INT NULL COMMENT '所属社区ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`community_id`) REFERENCES `community`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人档案表';
CREATE TABLE `doctor` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL COMMENT '关联用户表ID',
    `name` VARCHAR(50) NOT NULL COMMENT '医生姓名',
    `gender` VARCHAR(10) COMMENT '性别',
    `phone` VARCHAR(20),
    `specialization` VARCHAR(100) COMMENT '专长',
    `hospital` VARCHAR(100) COMMENT '所属医院',
    `community_id` INT NULL COMMENT '所属社区ID（一人一社区）',
    `status` VARCHAR(20) DEFAULT '离线' COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`community_id`) REFERENCES `community`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';
CREATE TABLE `device` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `device_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型：跌倒监测/SOS求助',
    `install_time` DATE COMMENT '安装时间',
    `status` VARCHAR(20) DEFAULT '正常' COMMENT '设备状态：正常/离线/故障',
    `elderly_id` INT NULL COMMENT '绑定老人ID（可空，未绑定）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监护设备表';
CREATE TABLE `help_event` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `device_id` INT NOT NULL COMMENT '触发设备ID',
    `elderly_id` INT NOT NULL COMMENT '老人ID',
    `event_type` VARCHAR(20) NOT NULL COMMENT '事件类型：摔倒告警/手动呼救',
    `event_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    `location` VARCHAR(200) COMMENT '发生位置',
    `status` VARCHAR(20) DEFAULT '待处理' COMMENT '状态：待处理/已派单/已处理',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`device_id`) REFERENCES `device`(`id`),
    FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求助事件表';
CREATE TABLE `order_form` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号',
    `elderly_id` INT NOT NULL COMMENT '老人ID',
    `doctor_id` INT NULL COMMENT '指派医生ID（待分配医生状态时为空）',
    `creator_id` INT NOT NULL COMMENT '创建人ID(社区人员)',
    `event_id` INT NULL COMMENT '关联求助事件ID（设备告警自动生成时才有）',
    `visit_date` DATE COMMENT '计划巡访日期',
    `reason` VARCHAR(500) COMMENT '巡访事由',
    `status` VARCHAR(20) DEFAULT '待接收' COMMENT '工单状态：待分配医生/待接收/进行中/巡访中/已完成/已退回',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`),
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`),
    FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`event_id`) REFERENCES `help_event`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='派医工单表';
CREATE TABLE `visit_record` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL COMMENT '关联工单ID',
    `elderly_id` INT NOT NULL COMMENT '老人ID',
    `doctor_id` INT NOT NULL COMMENT '填写医生ID',
    `vital_signs` VARCHAR(500) COMMENT '体征数据汇总(血压/心率/血糖)',
    `bp_systolic` INT COMMENT '收缩压(mmHg)',
    `bp_diastolic` INT COMMENT '舒张压(mmHg)',
    `heart_rate` INT COMMENT '心率(次/分)',
    `blood_glucose` DECIMAL(4,1) COMMENT '血糖(mmol/L)',
    `body_condition` VARCHAR(500) COMMENT '身体状态',
    `daily_life` VARCHAR(500) COMMENT '生活起居',
    `medication_situation` VARCHAR(500) COMMENT '用药情况',
    `subjective_symptoms` VARCHAR(500) COMMENT '老人主观症状',
    `ai_advice` VARCHAR(500) COMMENT 'AI辅助诊断建议',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`order_id`) REFERENCES `order_form`(`id`),
    FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`),
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡访记录表';
CREATE TABLE `community_staff` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `community_id` INT NOT NULL COMMENT '社区ID',
    `user_id` INT NOT NULL COMMENT '工作人员登录账号ID',
    `start_date` DATE COMMENT '任职时间',
    `position` VARCHAR(50) COMMENT '岗位',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`community_id`) REFERENCES `community`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    UNIQUE KEY `uk_staff_user` (`user_id`) COMMENT '一人只能归属一个社区'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区工作人员表（一人一社区）';
CREATE TABLE `follow_up_plan` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `elderly_id` INT NOT NULL COMMENT '老人ID',
    `community_id` INT NULL COMMENT '所属社区ID',
    `creator_id` INT NOT NULL COMMENT '创建人ID(社区人员)',
    `plan_type` VARCHAR(50) COMMENT '计划类型：周期巡访/健康随访',
    `cycle_days` INT COMMENT '周期天数',
    `start_date` DATE COMMENT '开始日期',
    `next_visit_date` DATE COMMENT '下次随访日期',
    `status` VARCHAR(20) DEFAULT '进行中' COMMENT '状态：进行中/已结束',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`),
    FOREIGN KEY (`community_id`) REFERENCES `community`(`id`),
    FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访计划表';
CREATE TABLE `fall_record` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL COMMENT '关联工单ID',
    `event_id` INT NOT NULL COMMENT '关联求助事件ID（设备告警）',
    `elderly_id` INT NOT NULL COMMENT '老人ID',
    `doctor_id` INT NOT NULL COMMENT '处置医生ID',
    `fall_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '摔倒/呼救时间',
    `site_condition` VARCHAR(500) COMMENT '现场情况/事发经过',
    `injury_site` VARCHAR(100) COMMENT '受伤部位',
    `injury_severity` VARCHAR(20) COMMENT '伤情程度：无/轻/中/重',
    `vital_signs` VARCHAR(500) COMMENT '生命体征汇总(血压/心率/血糖)',
    `bp_systolic` INT COMMENT '收缩压(mmHg)',
    `bp_diastolic` INT COMMENT '舒张压(mmHg)',
    `heart_rate` INT COMMENT '心率(次/分)',
    `blood_glucose` DECIMAL(4,1) COMMENT '血糖(mmol/L)',
    `disposal_measure` VARCHAR(500) COMMENT '处置措施',
    `need_hospitalization` VARCHAR(10) COMMENT '是否送医：是/否',
    `hospital` VARCHAR(100) COMMENT '送医医院',
    `result` VARCHAR(500) COMMENT '处置结果与后续建议',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`order_id`) REFERENCES `order_form`(`id`),
    FOREIGN KEY (`event_id`) REFERENCES `help_event`(`id`),
    FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`),
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摔倒/紧急处置记录表';
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `phone`) VALUES
('wangfang', 'wangfang123', '王芳', 'community_worker', '13800000001'),
('liminghui', 'admin2026', '李明辉', 'system_admin', '13800000002'),
('zhangwei', 'zhangwei2026', '张伟', 'doctor', '13800000003'),
('lijing', 'lijing2026', '李静', 'doctor', '13800000004'),
('zhaomin', 'zhaomin123', '赵敏', 'community_worker', '13800000005'),
('qianli', 'qianli789', '钱丽', 'community_worker', '13800000006'),
('wangmin', 'wangmin789', '王敏', 'doctor', '13800000007'),
('sunxiufang', 'sunxiufang789', '孙秀芳', 'community_worker', '13800000008'),
('zhouwen', 'zhouwen789', '周文', 'doctor', '13800000009');
INSERT INTO `community` (`name`, `address`, `phone`) VALUES
('幸福社区', '长春市朝阳区幸福路1号', '0431-88000001'),
('安康社区', '长春市南关区安康街88号', '0431-88000002'),
('和平社区', '长春市宽城区和平大街12号', '0431-88000003'),
('康乐社区', '长春市二道区康乐路66号', '0431-88000004');
INSERT INTO `elderly` (`name`, `gender`, `age`, `phone`, `address`, `emergency_contact`, `emergency_phone`, `chronic_diseases`, `allergies`, `current_illness`, `medications`, `body_status`, `community_id`) VALUES
('陈建国', '男', 75, '13900000001', '幸福社区A栋1单元101', '陈晓明', '13900000011', '高血压、糖尿病', '无', '血压偏高', '降压药、降糖药', '一般', 1),
('张桂兰', '女', 72, '13900000002', '幸福社区B栋2单元302', '张小红', '13900000012', '无', '青霉素', '健康', '无', '良好', 1),
('王德福', '男', 80, '13900000003', '幸福社区C栋3单元501', '王刚', '13900000013', '心脏病、高血压', '无', '心率异常', '心脏药、降压药', '较差', 2),
('李秀英', '女', 70, '13900000004', '幸福社区A栋2单元201', '李芳', '13900000014', '无', '无', '健康', '无', '健康', 1),
('周国栋', '男', 74, '13900000005', '幸福社区B栋1单元402', '周华', '13900000015', '关节炎', '无', '关节疼痛', '止痛药', '一般', 2),
('刘桂芳', '女', 78, '13900000016', '和平社区3栋2单元301', '刘刚', '13900000026', '高血压、骨质疏松', '无', '偶有关节痛', '钙片、降压药', '一般', 3),
('孙德林', '男', 81, '13900000017', '和平社区1栋1单元502', '孙丽', '13900000027', '冠心病', '无', '心慌，需随访', '硝酸甘油、阿司匹林', '较差', 3),
('郑秀兰', '女', 76, '13900000018', '康乐社区2栋3单元201', '郑军', '13900000028', '糖尿病、白内障', '无', '血糖控制一般', '二甲双胍、降糖药', '一般', 4),
('韩德山', '男', 79, '13900000019', '康乐社区5栋1单元102', '韩雪', '13900000029', '高血压、慢阻肺', '无', '气短', '沙丁胺醇、降压药', '较差', 4);
INSERT INTO `doctor` (`user_id`, `name`, `gender`, `phone`, `specialization`, `hospital`, `community_id`, `status`) VALUES
(3, '张伟', '男', '13811111111', '内科', '社区医院', 1, '在线'),
(4, '李静', '女', '13811111112', '老年科', '社区医院', 2, '在线'),
(7, '王敏', '女', '13811111113', '内科', '社区医院', 3, '在线'),
(9, '周文', '男', '13811111114', '全科', '社区医院', 4, '在线');
INSERT INTO `community_staff` (`community_id`, `user_id`, `start_date`, `position`) VALUES
(1, 1, '2024-03-01', '社区主管'),
(2, 5, '2024-06-01', '社区主管'),
(3, 6, '2025-01-10', '社区主管'),
(4, 8, '2025-03-15', '社区主管');
INSERT INTO `device` (`device_no`, `device_type`, `install_time`, `status`, `elderly_id`) VALUES
('DEV001', '跌倒监测', '2026-05-10', '正常', 3),
('DEV002', 'SOS求助',  '2026-05-12', '正常', 1),
('DEV003', '跌倒监测', '2026-05-15', '离线', 5),
('DEV004', 'SOS求助',  '2026-06-01', '正常', 2),
('DEV005', '跌倒监测', '2026-06-10', '故障', 4),
('DEV006', '跌倒监测', '2026-06-20', '正常', 6);
INSERT INTO `help_event` (`id`, `device_id`, `elderly_id`, `event_type`, `event_time`, `location`, `status`) VALUES
(1, 1, 3, '摔倒告警', '2026-08-19 08:30:00', '安康社区3栋501室', '已派单'),
(2, 2, 1, '手动呼救', '2026-08-19 09:10:00', '幸福社区A栋1单元101', '待处理'),
(4, 6, 6, '摔倒告警', '2026-08-19 10:20:00', '和平社区3栋2单元301', '待处理');
INSERT INTO `order_form` (`order_no`, `elderly_id`, `doctor_id`, `creator_id`, `visit_date`, `reason`, `status`) VALUES
('ORD20260818001', 1, 1, 1, '2026-08-19', '血压偏高，需要上门检查', '待接收'),
('ORD20260818002', 3, 2, 1, '2026-08-19', '心率异常，需紧急巡访', '待接收'),
('ORD20260817003', 2, 1, 1, '2026-08-18', '常规复查', '已完成');
INSERT INTO `order_form` (`id`, `order_no`, `elderly_id`, `doctor_id`, `creator_id`, `event_id`, `visit_date`, `reason`, `status`, `create_time`) VALUES
(7, 'ORD20260819007', 3, 2, 1, 1, '2026-08-19', '摔倒告警（设备告警自动生成）', '待接收', '2026-08-19 08:30:00'),
(8, 'ORD20260819008', 1, NULL, 1, 2, '2026-08-19', '手动呼救（设备告警自动生成）', '待分配医生', '2026-08-19 09:10:00'),
(10, 'ORD202608191010', 6, NULL, 6, 4, '2026-08-19', '摔倒告警（设备告警自动生成）', '待分配医生', '2026-08-19 10:20:00');
INSERT INTO `visit_record` (`order_id`, `elderly_id`, `doctor_id`, `vital_signs`, `bp_systolic`, `bp_diastolic`, `heart_rate`, `blood_glucose`, `body_condition`, `daily_life`, `medication_situation`, `subjective_symptoms`, `ai_advice`) VALUES
(3, 2, 1, '血压125/80，心率72，血糖6.5', 125, 80, 72, 6.5, '良好', '自理', '按时服药', '无不适', '状态良好，建议继续保持');
INSERT INTO `help_event` (`id`, `device_id`, `elderly_id`, `event_type`, `event_time`, `location`, `status`) VALUES
(3, 1, 3, '摔倒告警', '2026-08-19 07:50:00', '安康社区3栋501室', '已处理');
INSERT INTO `order_form` (`id`, `order_no`, `elderly_id`, `doctor_id`, `creator_id`, `event_id`, `visit_date`, `reason`, `status`, `create_time`) VALUES
(9, 'ORD20260819009', 3, 2, 1, 3, '2026-08-19', '摔倒告警（设备告警自动生成）', '已完成', '2026-08-19 07:50:00');
INSERT INTO `fall_record` (`id`, `order_id`, `event_id`, `elderly_id`, `doctor_id`, `fall_time`, `site_condition`, `injury_site`, `injury_severity`, `vital_signs`, `bp_systolic`, `bp_diastolic`, `heart_rate`, `blood_glucose`, `disposal_measure`, `need_hospitalization`, `hospital`, `result`, `create_time`) VALUES
(1, 9, 3, 3, 2, '2026-08-19 07:52:00', '老人晨起在家门口摔倒，家属发现后通过手环呼救', '右手腕、左膝', '轻', '血压138/86，心率88，血糖5.9', 138, 86, 88, 5.9, '现场初步检查无骨折迹象，腕部冷敷包扎', '是', '市第一人民医院', '生命体征平稳，建议腕部拍片排除骨折，留观24小时', '2026-08-19 08:20:00');
INSERT INTO `follow_up_plan` (`elderly_id`, `community_id`, `creator_id`, `plan_type`, `cycle_days`, `start_date`, `next_visit_date`, `status`) VALUES
(3, 2, 1, '周期巡访', 30, '2026-08-20', '2026-09-19', '进行中'),
(1, 1, 1, '健康随访', 15, '2026-08-20', '2026-09-04', '进行中'),
(5, 2, 1, '周期巡访', 30, '2026-08-01', '2026-08-20', '已结束');
ALTER TABLE `user`        AUTO_INCREMENT = 10;
ALTER TABLE `community`   AUTO_INCREMENT = 5;
ALTER TABLE `elderly`     AUTO_INCREMENT = 10;
ALTER TABLE `doctor`      AUTO_INCREMENT = 5;
ALTER TABLE `device`      AUTO_INCREMENT = 7;
ALTER TABLE `help_event`  AUTO_INCREMENT = 5;
ALTER TABLE `order_form`  AUTO_INCREMENT = 11;
ALTER TABLE `visit_record` AUTO_INCREMENT = 2;
ALTER TABLE `community_staff` AUTO_INCREMENT = 6;
ALTER TABLE `follow_up_plan`  AUTO_INCREMENT = 4;
ALTER TABLE `fall_record`  AUTO_INCREMENT = 2;
