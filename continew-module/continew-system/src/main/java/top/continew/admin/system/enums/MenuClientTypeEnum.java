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

package top.continew.admin.system.enums;

import top.continew.starter.core.enums.BaseEnum;

/**
 * 菜单终端类型枚举（取值对齐字典 client_type 及 sys_client.client_type）
 *
 * @author seewe-yolo
 * @since 5.0.0
 */
public enum MenuClientTypeEnum implements BaseEnum<String> {

    /**
     * 桌面端
     */
    PC("PC", "桌面端"),

    /**
     * 小程序
     */
    XCX("XCX", "小程序"),

    /**
     * 安卓
     */
    ANDROID("ANDROID", "安卓");

    private final String value;
    private final String description;

    MenuClientTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
