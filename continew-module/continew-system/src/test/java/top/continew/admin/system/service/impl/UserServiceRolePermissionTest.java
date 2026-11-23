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

package top.continew.admin.system.service.impl;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

/**
 * 用户新增与修改的角色权限校验测试
 *
 * @author SeeweWis
 * @since 5.0.0
 */
class UserServiceRolePermissionTest {

    @Test
    void shouldRequireRolePermissionWhenRoleSetChanges() {
        UserServiceImpl service = mock(UserServiceImpl.class, CALLS_REAL_METHODS);

        try (MockedStatic<StpUtil> permissions = mockStatic(StpUtil.class)) {
            service.checkAssignRolePermission(List.of(2L), List.of(1L));

            permissions.verify(() -> StpUtil.checkPermission("system:user:updateRole"));
        }
    }

    @Test
    void shouldNotRequireRolePermissionWhenRoleSetIsUnchanged() {
        UserServiceImpl service = mock(UserServiceImpl.class, CALLS_REAL_METHODS);

        try (MockedStatic<StpUtil> permissions = mockStatic(StpUtil.class)) {
            service.checkAssignRolePermission(List.of(1L, 2L), List.of(2L, 1L));

            permissions.verifyNoInteractions();
        }
    }
}
