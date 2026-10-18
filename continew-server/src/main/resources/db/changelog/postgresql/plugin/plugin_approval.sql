-- liquibase formatted sql

-- changeset Charles7c:plugin_approval_1
-- comment: 创建审批插件相关表

-- 审批定义表
CREATE TABLE "approval_definition" (
  "id" BIGINT NOT NULL,
  "code" VARCHAR(50) NOT NULL,
  "name" VARCHAR(100) NOT NULL,
  "category" VARCHAR(50),
  "flow_config" JSONB NOT NULL,
  "description" VARCHAR(500),
  "status" SMALLINT DEFAULT 1,
  "tenant_id" BIGINT,
  "create_user" BIGINT,
  "create_time" TIMESTAMP,
  "update_user" BIGINT,
  "update_time" TIMESTAMP,
  "deleted" BIGINT DEFAULT 0,
  PRIMARY KEY ("id"),
  CONSTRAINT "uk_code" UNIQUE ("code")
);

COMMENT ON TABLE "approval_definition" IS '审批定义表';
COMMENT ON COLUMN "approval_definition"."id" IS 'ID';
COMMENT ON COLUMN "approval_definition"."code" IS '审批编码';
COMMENT ON COLUMN "approval_definition"."name" IS '审批名称';
COMMENT ON COLUMN "approval_definition"."category" IS '审批分类';
COMMENT ON COLUMN "approval_definition"."flow_config" IS '流程配置';
COMMENT ON COLUMN "approval_definition"."description" IS '描述';
COMMENT ON COLUMN "approval_definition"."status" IS '状态（1启用 0禁用）';
COMMENT ON COLUMN "approval_definition"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "approval_definition"."create_user" IS '创建人';
COMMENT ON COLUMN "approval_definition"."create_time" IS '创建时间';
COMMENT ON COLUMN "approval_definition"."update_user" IS '修改人';
COMMENT ON COLUMN "approval_definition"."update_time" IS '修改时间';
COMMENT ON COLUMN "approval_definition"."deleted" IS '是否已删除';

-- 审批实例表
CREATE TABLE "approval_instance" (
  "id" BIGINT NOT NULL,
  "definition_code" VARCHAR(50) NOT NULL,
  "business_key" VARCHAR(100) NOT NULL,
  "business_type" VARCHAR(50) NOT NULL,
  "title" VARCHAR(200) NOT NULL,
  "current_node_id" VARCHAR(50),
  "status" VARCHAR(20) NOT NULL,
  "result" VARCHAR(20),
  "form_data" JSONB,
  "start_user" BIGINT NOT NULL,
  "start_time" TIMESTAMP NOT NULL,
  "end_time" TIMESTAMP,
  "tenant_id" BIGINT,
  PRIMARY KEY ("id")
);

CREATE INDEX "idx_business" ON "approval_instance" ("business_key", "business_type");
CREATE INDEX "idx_status" ON "approval_instance" ("status");
CREATE INDEX "idx_start_user" ON "approval_instance" ("start_user");

COMMENT ON TABLE "approval_instance" IS '审批实例表';
COMMENT ON COLUMN "approval_instance"."id" IS 'ID';
COMMENT ON COLUMN "approval_instance"."definition_code" IS '审批定义编码';
COMMENT ON COLUMN "approval_instance"."business_key" IS '业务主键';
COMMENT ON COLUMN "approval_instance"."business_type" IS '业务类型';
COMMENT ON COLUMN "approval_instance"."title" IS '审批标题';
COMMENT ON COLUMN "approval_instance"."current_node_id" IS '当前节点ID';
COMMENT ON COLUMN "approval_instance"."status" IS '状态';
COMMENT ON COLUMN "approval_instance"."result" IS '审批结果';
COMMENT ON COLUMN "approval_instance"."form_data" IS '表单数据';
COMMENT ON COLUMN "approval_instance"."start_user" IS '发起人';
COMMENT ON COLUMN "approval_instance"."start_time" IS '发起时间';
COMMENT ON COLUMN "approval_instance"."end_time" IS '结束时间';
COMMENT ON COLUMN "approval_instance"."tenant_id" IS '租户ID';

-- 审批任务表
CREATE TABLE "approval_task" (
  "id" BIGINT NOT NULL,
  "instance_id" BIGINT NOT NULL,
  "node_id" VARCHAR(50) NOT NULL,
  "node_name" VARCHAR(100) NOT NULL,
  "assignee" BIGINT NOT NULL,
  "status" VARCHAR(20) NOT NULL,
  "opinion" VARCHAR(500),
  "attachments" JSONB,
  "claim_time" TIMESTAMP,
  "complete_time" TIMESTAMP,
  "tenant_id" BIGINT,
  "create_time" TIMESTAMP,
  PRIMARY KEY ("id")
);

CREATE INDEX "idx_approval_task_instance" ON "approval_task" ("instance_id");
CREATE INDEX "idx_assignee_status" ON "approval_task" ("assignee", "status");

COMMENT ON TABLE "approval_task" IS '审批任务表';
COMMENT ON COLUMN "approval_task"."id" IS 'ID';
COMMENT ON COLUMN "approval_task"."instance_id" IS '审批实例ID';
COMMENT ON COLUMN "approval_task"."node_id" IS '节点ID';
COMMENT ON COLUMN "approval_task"."node_name" IS '节点名称';
COMMENT ON COLUMN "approval_task"."assignee" IS '审批人';
COMMENT ON COLUMN "approval_task"."status" IS '状态';
COMMENT ON COLUMN "approval_task"."opinion" IS '审批意见';
COMMENT ON COLUMN "approval_task"."attachments" IS '附件列表';
COMMENT ON COLUMN "approval_task"."claim_time" IS '签收时间';
COMMENT ON COLUMN "approval_task"."complete_time" IS '完成时间';
COMMENT ON COLUMN "approval_task"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "approval_task"."create_time" IS '创建时间';

-- 审批历史表
CREATE TABLE "approval_history" (
  "id" BIGINT NOT NULL,
  "instance_id" BIGINT NOT NULL,
  "task_id" BIGINT,
  "node_id" VARCHAR(50),
  "node_name" VARCHAR(100),
  "operator" BIGINT NOT NULL,
  "action" VARCHAR(20) NOT NULL,
  "opinion" VARCHAR(500),
  "attachments" JSONB,
  "from_user" BIGINT,
  "to_user" BIGINT,
  "tenant_id" BIGINT,
  "create_time" TIMESTAMP NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX "idx_approval_history_instance" ON "approval_history" ("instance_id");

COMMENT ON TABLE "approval_history" IS '审批历史表';
COMMENT ON COLUMN "approval_history"."id" IS 'ID';
COMMENT ON COLUMN "approval_history"."instance_id" IS '审批实例ID';
COMMENT ON COLUMN "approval_history"."task_id" IS '任务ID';
COMMENT ON COLUMN "approval_history"."node_id" IS '节点ID';
COMMENT ON COLUMN "approval_history"."node_name" IS '节点名称';
COMMENT ON COLUMN "approval_history"."operator" IS '操作人';
COMMENT ON COLUMN "approval_history"."action" IS '操作';
COMMENT ON COLUMN "approval_history"."opinion" IS '意见';
COMMENT ON COLUMN "approval_history"."attachments" IS '附件';
COMMENT ON COLUMN "approval_history"."from_user" IS '转办来源人';
COMMENT ON COLUMN "approval_history"."to_user" IS '转办目标人';
COMMENT ON COLUMN "approval_history"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "approval_history"."create_time" IS '操作时间';

-- 审批消息表
CREATE TABLE "approval_message" (
  "id" BIGINT NOT NULL,
  "instance_id" BIGINT NOT NULL,
  "recipient" BIGINT NOT NULL,
  "message_type" VARCHAR(20) NOT NULL,
  "title" VARCHAR(200) NOT NULL,
  "content" TEXT,
  "is_read" SMALLINT DEFAULT 0,
  "read_time" TIMESTAMP,
  "tenant_id" BIGINT,
  "create_time" TIMESTAMP NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX "idx_recipient_read" ON "approval_message" ("recipient", "is_read");

COMMENT ON TABLE "approval_message" IS '审批消息表';
COMMENT ON COLUMN "approval_message"."id" IS 'ID';
COMMENT ON COLUMN "approval_message"."instance_id" IS '审批实例ID';
COMMENT ON COLUMN "approval_message"."recipient" IS '接收人';
COMMENT ON COLUMN "approval_message"."message_type" IS '消息类型';
COMMENT ON COLUMN "approval_message"."title" IS '消息标题';
COMMENT ON COLUMN "approval_message"."content" IS '消息内容';
COMMENT ON COLUMN "approval_message"."is_read" IS '是否已读';
COMMENT ON COLUMN "approval_message"."read_time" IS '阅读时间';
COMMENT ON COLUMN "approval_message"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "approval_message"."create_time" IS '创建时间';

-- changeset seewis:plugin_approval_2
-- comment: 初始化审批菜单和权限，按租户关联现有角色
INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(10000, '审批中心', 0, 1, '/approval', 'Approval', 'Layout', '/approval/start', 'mdi:clipboard-check-outline', false, false, false, NULL, 10, 1, 1, NOW()),
(10010, '审批定义', 10000, 2, '/approval/definition', 'ApprovalDefinition', 'approval/definition/index', NULL, 'mdi:file-tree-outline', false, false, false, NULL, 1, 1, 1, NOW()),
(10011, '列表', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:list', 1, 1, 1, NOW()),
(10012, '详情', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:get', 2, 1, 1, NOW()),
(10013, '新增', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:create', 3, 1, 1, NOW()),
(10014, '修改', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:update', 4, 1, 1, NOW()),
(10015, '删除', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:delete', 5, 1, 1, NOW()),
(10020, '发起审批', 10000, 2, '/approval/start', 'ApprovalStart', 'approval/start/index', NULL, 'mdi:send-check-outline', false, false, false, NULL, 2, 1, 1, NOW()),
(10021, '发起', 10020, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:start', 1, 1, 1, NOW()),
(10030, '我的申请', 10000, 2, '/approval/application', 'ApprovalApplication', 'approval/application/index', NULL, 'mdi:file-document-outline', false, false, false, NULL, 3, 1, 1, NOW()),
(10031, '列表', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:list', 1, 1, 1, NOW()),
(10032, '详情', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:get', 2, 1, 1, NOW()),
(10033, '撤销', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:cancel', 3, 1, 1, NOW()),
(10040, '我的待办', 10000, 2, '/approval/task', 'ApprovalTask', 'approval/task/index', NULL, 'mdi:clipboard-check-outline', false, false, false, NULL, 4, 1, 1, NOW()),
(10041, '列表', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:list', 1, 1, 1, NOW()),
(10042, '同意', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:approve', 2, 1, 1, NOW()),
(10043, '拒绝', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:reject', 3, 1, 1, NOW()),
(10044, '转办', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:transfer', 4, 1, 1, NOW());

-- 仅为默认租户现有系统管理员授权；其他租户通过套餐和角色授权，不扩大既有套餐。
INSERT INTO "sys_role_menu" ("tenant_id", "role_id", "menu_id")
SELECT r.tenant_id, r.id, m.id FROM sys_role r CROSS JOIN sys_menu m
WHERE r.tenant_id = 0 AND r.code = 'sys_admin' AND r.deleted = 0
AND m.id IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
    WHERE rm.tenant_id = r.tenant_id AND rm.role_id = r.id AND rm.menu_id = m.id);

-- 普通用户仅拥有发起与处理本人任务的权限，不授予定义管理。
INSERT INTO "sys_role_menu" ("tenant_id", "role_id", "menu_id")
SELECT r.tenant_id, r.id, m.id FROM sys_role r CROSS JOIN sys_menu m
WHERE r.tenant_id = 0 AND r.code = 'general' AND r.deleted = 0
AND m.id IN (10000, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044, 10011, 10012)
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
    WHERE rm.tenant_id = r.tenant_id AND rm.role_id = r.id AND rm.menu_id = m.id);

-- rollback DELETE FROM "sys_role_menu" WHERE "menu_id" IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044);
-- rollback DELETE FROM "sys_menu" WHERE "id" IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10020, 10021, 10030, 10031, 10032, 10033, 10040, 10041, 10042, 10043, 10044);

-- changeset seewis:plugin_approval_3
-- comment: 保存审批流程快照，定义编码按租户和逻辑删除标识唯一
ALTER TABLE "approval_instance" ADD COLUMN "flow_config" TEXT;
UPDATE approval_instance SET flow_config = (SELECT d.flow_config::text
    FROM approval_definition d WHERE d.code = approval_instance.definition_code
    AND COALESCE(d.tenant_id, 0) = COALESCE(approval_instance.tenant_id, 0) AND d.deleted = 0);
UPDATE approval_definition SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_instance SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_task SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_history SET tenant_id = 0 WHERE tenant_id IS NULL;
UPDATE approval_message SET tenant_id = 0 WHERE tenant_id IS NULL;
ALTER TABLE "approval_definition" DROP CONSTRAINT "uk_code",
    ADD CONSTRAINT "uk_approval_definition_tenant_code" UNIQUE ("tenant_id", "code", "deleted");
-- rollback ALTER TABLE "approval_definition" DROP CONSTRAINT "uk_approval_definition_tenant_code", ADD CONSTRAINT "uk_code" UNIQUE ("code");
-- rollback ALTER TABLE "approval_instance" DROP COLUMN "flow_config";
-- 回滚将恢复全局编码唯一约束；须先处理跨租户同编码数据，否则数据库拒绝回滚且保留隔离约束。

-- changeset seewis:plugin_approval_4
-- comment: 保存申请表单配置及实例表单快照
ALTER TABLE "approval_definition" ADD COLUMN "form_config" JSONB;
ALTER TABLE "approval_instance" ADD COLUMN "form_config" JSONB;
UPDATE "approval_definition" SET "form_config" = '{"version":1,"fields":[{"id":"reason","key":"reason","label":"申请说明","type":"TEXTAREA","required":true,"validation":{"maxLength":1000}}]}'::jsonb
WHERE "form_config" IS NULL;
UPDATE "approval_instance" i SET "form_config" = COALESCE(d."form_config", '{"version":1,"fields":[{"id":"reason","key":"reason","label":"申请说明","type":"TEXTAREA","required":true,"validation":{"maxLength":1000}}]}'::jsonb)
FROM "approval_definition" d
WHERE i."form_config" IS NULL
  AND d."code" = i."definition_code"
  AND COALESCE(d."tenant_id", 0) = COALESCE(i."tenant_id", 0)
  AND d."deleted" = 0;
COMMENT ON COLUMN "approval_definition"."form_config" IS '申请表单配置';
COMMENT ON COLUMN "approval_instance"."form_config" IS '申请表单配置快照';
-- rollback ALTER TABLE "approval_instance" DROP COLUMN "form_config";
-- rollback ALTER TABLE "approval_definition" DROP COLUMN "form_config";

-- changeset seewis:plugin_approval_5
-- comment: 将发起审批权限并入我的申请，移除独立发起审批菜单
INSERT INTO "sys_role_menu" ("tenant_id", "role_id", "menu_id")
SELECT rm."tenant_id", rm."role_id", 10021 FROM "sys_role_menu" rm
WHERE rm."menu_id" = 10020
AND NOT EXISTS (SELECT 1 FROM "sys_role_menu" existing
    WHERE existing."tenant_id" = rm."tenant_id"
    AND existing."role_id" = rm."role_id" AND existing."menu_id" = 10021);
UPDATE "sys_menu" SET "parent_id" = 10030, "title" = '新建审批', "sort" = 4 WHERE "id" = 10021;
DELETE FROM "sys_role_menu" WHERE "menu_id" = 10020;
DELETE FROM "sys_menu" WHERE "id" = 10020;
-- rollback INSERT INTO "sys_menu" ("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time") VALUES (10020, '发起审批', 10000, 2, '/approval/start', 'ApprovalStart', 'approval/start/index', NULL, 'mdi:send-check-outline', false, false, false, NULL, 2, 1, 1, NOW());
-- rollback INSERT INTO "sys_role_menu" ("tenant_id", "role_id", "menu_id") SELECT rm."tenant_id", rm."role_id", 10020 FROM "sys_role_menu" rm WHERE rm."menu_id" = 10021 AND NOT EXISTS (SELECT 1 FROM "sys_role_menu" existing WHERE existing."tenant_id" = rm."tenant_id" AND existing."role_id" = rm."role_id" AND existing."menu_id" = 10020);
-- rollback UPDATE "sys_menu" SET "parent_id" = 10020, "title" = '发起', "sort" = 1 WHERE "id" = 10021;
