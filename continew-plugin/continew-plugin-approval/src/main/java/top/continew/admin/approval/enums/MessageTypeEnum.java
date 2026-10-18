/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.approval.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 审批消息类型枚举
 *
 * @author Charles7c
 * @since 4.2.0
 */
@Getter
@RequiredArgsConstructor
public enum MessageTypeEnum {

    /**
     * 任务分配
     */
    TASK_ASSIGNED("待办提醒"),

    /**
     * 审批通过
     */
    APPROVED("审批通过"),

    /**
     * 审批拒绝
     */
    REJECTED("审批拒绝"),

    /**
     * 审批转办
     */
    TRANSFERRED("审批转办"),

    /**
     * 审批撤销
     */
    CANCELLED("审批撤销");

    private final String description;
}
