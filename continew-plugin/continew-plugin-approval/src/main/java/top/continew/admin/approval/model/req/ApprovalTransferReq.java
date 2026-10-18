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

package top.continew.admin.approval.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * 审批转办请求参数
 *
 * @author Charles7c
 * @since 4.2.0
 */
@Data
@Schema(description = "审批转办请求参数")
public class ApprovalTransferReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID
     */
    @Schema(description = "目标用户ID", example = "10001")
    @NotNull(message = "目标用户ID不能为空")
    private Long toUserId;

    /**
     * 转办原因
     */
    @Schema(description = "转办原因", example = "临时外出，请帮忙处理")
    @Length(max = 500, message = "转办原因长度不能超过 {max} 个字符")
    private String reason;
}
