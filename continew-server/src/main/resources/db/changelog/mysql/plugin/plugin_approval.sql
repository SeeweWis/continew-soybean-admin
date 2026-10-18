-- liquibase formatted sql

-- changeset Charles7c:plugin_approval_1
-- comment: 创建审批插件相关表

-- 审批定义表
CREATE TABLE `approval_definition` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `code` VARCHAR(50) NOT NULL COMMENT '审批编码',
  `name` VARCHAR(100) NOT NULL COMMENT '审批名称',
  `category` VARCHAR(50) COMMENT '审批分类',
  `flow_config` JSON NOT NULL COMMENT '流程配置',
  `description` VARCHAR(500) COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态（1启用 0禁用）',
  `tenant_id` BIGINT COMMENT '租户ID',
  `create_user` BIGINT COMMENT '创建人',
  `create_time` DATETIME COMMENT '创建时间',
  `update_user` BIGINT COMMENT '修改人',
  `update_time` DATETIME COMMENT '修改时间',
  `deleted` BIGINT DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批定义表';

-- 审批实例表
CREATE TABLE `approval_instance` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `definition_code` VARCHAR(50) NOT NULL COMMENT '审批定义编码',
  `business_key` VARCHAR(100) NOT NULL COMMENT '业务主键',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  `title` VARCHAR(200) NOT NULL COMMENT '审批标题',
  `current_node_id` VARCHAR(50) COMMENT '当前节点ID',
  `status` VARCHAR(20) NOT NULL COMMENT '状态',
  `result` VARCHAR(20) COMMENT '审批结果',
  `form_data` JSON COMMENT '表单数据',
  `start_user` BIGINT NOT NULL COMMENT '发起人',
  `start_time` DATETIME NOT NULL COMMENT '发起时间',
  `end_time` DATETIME COMMENT '结束时间',
  `tenant_id` BIGINT COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_key`, `business_type`),
  KEY `idx_status` (`status`),
  KEY `idx_start_user` (`start_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

-- 审批任务表
CREATE TABLE `approval_task` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `instance_id` BIGINT NOT NULL COMMENT '审批实例ID',
  `node_id` VARCHAR(50) NOT NULL COMMENT '节点ID',
  `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
  `assignee` BIGINT NOT NULL COMMENT '审批人',
  `status` VARCHAR(20) NOT NULL COMMENT '状态',
  `opinion` VARCHAR(500) COMMENT '审批意见',
  `attachments` JSON COMMENT '附件列表',
  `claim_time` DATETIME COMMENT '签收时间',
  `complete_time` DATETIME COMMENT '完成时间',
  `tenant_id` BIGINT COMMENT '租户ID',
  `create_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_instance` (`instance_id`),
  KEY `idx_assignee_status` (`assignee`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- 审批历史表
CREATE TABLE `approval_history` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `instance_id` BIGINT NOT NULL COMMENT '审批实例ID',
  `task_id` BIGINT COMMENT '任务ID',
  `node_id` VARCHAR(50) COMMENT '节点ID',
  `node_name` VARCHAR(100) COMMENT '节点名称',
  `operator` BIGINT NOT NULL COMMENT '操作人',
  `action` VARCHAR(20) NOT NULL COMMENT '操作',
  `opinion` VARCHAR(500) COMMENT '意见',
  `attachments` JSON COMMENT '附件',
  `from_user` BIGINT COMMENT '转办来源人',
  `to_user` BIGINT COMMENT '转办目标人',
  `tenant_id` BIGINT COMMENT '租户ID',
  `create_time` DATETIME NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批历史表';

-- 审批消息表
CREATE TABLE `approval_message` (
  `id` BIGINT NOT NULL COMMENT 'ID',
  `instance_id` BIGINT NOT NULL COMMENT '审批实例ID',
  `recipient` BIGINT NOT NULL COMMENT '接收人',
  `message_type` VARCHAR(20) NOT NULL COMMENT '消息类型',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT COMMENT '消息内容',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
  `read_time` DATETIME COMMENT '阅读时间',
  `tenant_id` BIGINT COMMENT '租户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recipient_read` (`recipient`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批消息表';

-- changeset seewis:plugin_approval_2
-- comment: 初始化审批菜单和权限，按租户关联现有角色
INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(10000, '审批中心', 0, 1, '/approval', 'Approval', 'Layout', '/approval/start', 'mdi:clipboard-check-outline', b'0', b'0', b'0', NULL, 10, 1, 1, NOW()),
(10010, '审批定义', 10000, 2, '/approval/definition', 'ApprovalDefinition', 'approval/definition/index', NULL, 'mdi:file-tree-outline', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(10011, '列表', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:list', 1, 1, 1, NOW()),
(10012, '详情', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:get', 2, 1, 1, NOW()),
(10013, '新增', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:create', 3, 1, 1, NOW()),
(10014, '修改', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:update', 4, 1, 1, NOW()),
(10015, '删除', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:delete', 5, 1, 1, NOW()),
(10020, '发起审批', 10000, 2, '/approval/start', 'ApprovalStart', 'approval/start/index', NULL, 'mdi:send-check-outline', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(10021, '发起', 10020, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:start', 1, 1, 1, NOW()),
(10030, '我的申请', 10000, 2, '/approval/application', 'ApprovalApplication', 'approval/application/index', NULL, 'mdi:file-document-outline', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(10031, '列表', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:list', 1, 1, 1, NOW()),
(10032, '详情', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:get', 2, 1, 1, NOW()),
(10033, '撤销', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:cancel', 3, 1, 1, NOW()),
(10040, '我的待办', 10000, 2, '/approval/task', 'ApprovalTask', 'approval/task/index', NULL, 'mdi:clipboard-check-outline', b'0', b'0', b'0', NULL, 4, 1, 1, NOW()),
(10041, '列表', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:list', 1, 1, 1, NOW()),
(10042, '同意', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:approve', 2, 1, 1, NOW()),
(10043, '拒绝', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:reject', 3, 1, 1, NOW()),
(10044, '转办', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:transfer', 4, 1, 1, NOW());

-- 仅为默认租户现有系统管理员授权；其他租户通过套餐和角色授权，不扩大既有套餐。
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT r.tenant_id, r.id, m.id FROM sys_role r CROSS JOIN sys_menu m
WHERE r.tenant_id = 0 AND r.code = 'sys_admin' AND r.deleted = 0
AND m.id IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
    WHERE rm.tenant_id = r.tenant_id AND rm.role_id = r.id AND rm.menu_id = m.id);

-- 普通用户仅拥有发起与处理本人任务的权限，不授予定义管理。
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT r.tenant_id, r.id, m.id FROM sys_role r CROSS JOIN sys_menu m
WHERE r.tenant_id = 0 AND r.code = 'general' AND r.deleted = 0
AND m.id IN (10000, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044, 10011, 10012)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
    WHERE rm.tenant_id = r.tenant_id AND rm.role_id = r.id AND rm.menu_id = m.id);

-- rollback DELETE FROM `sys_role_menu` WHERE `menu_id` IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044);
-- rollback DELETE FROM `sys_menu` WHERE `id` IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044);

-- changeset seewis:plugin_approval_3
-- comment: 保存审批流程快照，定义编码按租户和逻辑删除标识唯一
ALTER TABLE `approval_instance` ADD COLUMN `flow_config` TEXT;
UPDATE approval_instance SET flow_config = (SELECT CAST(d.flow_config AS CHAR)
    FROM approval_definition d WHERE d.code = approval_instance.definition_code
    AND COALESCE(d.tenant_id, 0) = COALESCE(approval_instance.tenant_id, 0) AND d.deleted = 0);
UPDATE approval_definition SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_instance SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_task SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_history SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_message SET tenant_id = 0 WHERE tenant_id IS NULL;
ALTER TABLE `approval_definition` DROP INDEX `uk_code`,
    ADD UNIQUE INDEX `uk_approval_definition_tenant_code` (`tenant_id`, `code`, `deleted`);
-- rollback ALTER TABLE `approval_definition` DROP INDEX `uk_approval_definition_tenant_code`, ADD UNIQUE INDEX `uk_code` (`code`);
-- rollback ALTER TABLE `approval_instance` DROP COLUMN `flow_config`;
-- 回滚将恢复全局编码唯一约束；须先处理跨租户同编码数据，否则数据库拒绝回滚且保留隔离约束。

-- changeset seewis:plugin_approval_4
-- comment: 保存申请表单配置及实例表单快照
ALTER TABLE `approval_definition` ADD COLUMN `form_config` JSON COMMENT '申请表单配置';
ALTER TABLE `approval_instance` ADD COLUMN `form_config` JSON COMMENT '申请表单配置快照';
UPDATE `approval_definition` SET `form_config` = '{"version":1,"fields":[{"id":"reason","key":"reason","label":"申请说明","type":"TEXTAREA","required":true,"validation":{"maxLength":1000}}]}'
WHERE `form_config` IS NULL;
UPDATE `approval_instance` i INNER JOIN `approval_definition` d
    ON d.`code` = i.`definition_code`
    AND COALESCE(d.`tenant_id`, 0) = COALESCE(i.`tenant_id`, 0)
    AND d.`deleted` = 0
SET i.`form_config` = COALESCE(d.`form_config`, '{"version":1,"fields":[{"id":"reason","key":"reason","label":"申请说明","type":"TEXTAREA","required":true,"validation":{"maxLength":1000}}]}')
WHERE i.`form_config` IS NULL;
-- rollback ALTER TABLE `approval_instance` DROP COLUMN `form_config`;
-- rollback ALTER TABLE `approval_definition` DROP COLUMN `form_config`;

-- changeset seewis:plugin_approval_5
-- comment: 将发起审批权限并入我的申请，移除独立发起审批菜单
INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT rm.`tenant_id`, rm.`role_id`, 10021 FROM `sys_role_menu` rm
WHERE rm.`menu_id` = 10020
AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` existing
    WHERE existing.`tenant_id` = rm.`tenant_id`
    AND existing.`role_id` = rm.`role_id` AND existing.`menu_id` = 10021);
UPDATE `sys_menu` SET `parent_id` = 10030, `title` = '新建审批', `sort` = 4 WHERE `id` = 10021;
DELETE FROM `sys_role_menu` WHERE `menu_id` = 10020;
DELETE FROM `sys_menu` WHERE `id` = 10020;
-- rollback INSERT INTO `sys_menu` (`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`) VALUES (10020, '发起审批', 10000, 2, '/approval/start', 'ApprovalStart', 'approval/start/index', NULL, 'mdi:send-check-outline', b'0', b'0', b'0', NULL, 2, 1, 1, NOW());
-- rollback INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`) SELECT rm.`tenant_id`, rm.`role_id`, 10020 FROM `sys_role_menu` rm WHERE rm.`menu_id` = 10021 AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` existing WHERE existing.`tenant_id` = rm.`tenant_id` AND existing.`role_id` = rm.`role_id` AND existing.`menu_id` = 10020);
-- rollback UPDATE `sys_menu` SET `parent_id` = 10020, `title` = '发起', `sort` = 1 WHERE `id` = 10021;
