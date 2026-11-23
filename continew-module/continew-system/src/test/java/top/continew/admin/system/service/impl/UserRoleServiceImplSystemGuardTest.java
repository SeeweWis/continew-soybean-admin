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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.system.mapper.UserRoleMapper;
import top.continew.admin.system.model.entity.UserRoleDO;
import top.continew.admin.system.model.entity.user.UserDO;
import top.continew.admin.system.service.RoleService;
import top.continew.admin.system.service.UserService;
import top.continew.admin.test.TestApplicationContext;
import top.continew.starter.core.exception.BusinessException;

import java.util.List;
import java.util.Set;

/**
 * 系统内置用户角色关联保护测试
 *
 * @author SeeweWis
 * @since 5.0.0
 */
class UserRoleServiceImplSystemGuardTest {

    private static final long SYSTEM_USER_ID = 1L;
    private static final long ROLE_RELATION_ID = 10L;
    private static final long ROLE_ID = 3L;

    @BeforeAll
    static void initializeApplicationContext() {
        TestApplicationContext.initialize();
    }

    @Test
    void shouldRejectRoleBatchAssignmentToSystemUser() {
        UserRoleMapper mapper = mock(UserRoleMapper.class);
        UserService userService = mock(UserService.class);
        UserRoleServiceImpl service = service(mapper, userService);
        when(userService.listByIds(Set.of(SYSTEM_USER_ID))).thenReturn(List.of(systemUser()));

        assertThrows(BusinessException.class,
            () -> service.assignRoleToUsers(ROLE_ID, List.of(SYSTEM_USER_ID)));

        verify(mapper, never()).insertBatch(anyList());
    }

    @Test
    void shouldRejectDeletingSystemUserRoleRelation() {
        UserRoleMapper mapper = mock(UserRoleMapper.class);
        UserService userService = mock(UserService.class);
        UserRoleServiceImpl service = service(mapper, userService);
        when(mapper.selectByIds(List.of(ROLE_RELATION_ID)))
            .thenReturn(List.of(new UserRoleDO(SYSTEM_USER_ID, ROLE_ID)));
        when(userService.listByIds(Set.of(SYSTEM_USER_ID))).thenReturn(List.of(systemUser()));

        assertThrows(BusinessException.class, () -> service.deleteByIds(List.of(ROLE_RELATION_ID)));

        verify(mapper, never()).deleteByIds(anyList());
    }

    @Test
    void shouldRejectSavingSystemUserRoleRelation() {
        UserRoleMapper mapper = mock(UserRoleMapper.class);
        UserService userService = mock(UserService.class);
        UserRoleServiceImpl service = service(mapper, userService);
        when(userService.listByIds(Set.of(SYSTEM_USER_ID))).thenReturn(List.of(systemUser()));

        assertThrows(BusinessException.class,
            () -> service.saveBatch(List.of(new UserRoleDO(SYSTEM_USER_ID, ROLE_ID))));

        verify(mapper, never()).insert(anyList());
    }

    private static UserRoleServiceImpl service(UserRoleMapper mapper, UserService userService) {
        UserRoleServiceImpl service = new UserRoleServiceImpl(mapper);
        ReflectionTestUtils.setField(service, "roleService", mock(RoleService.class));
        ReflectionTestUtils.setField(service, "userService", userService);
        return service;
    }

    private static UserDO systemUser() {
        UserDO user = new UserDO();
        user.setId(SYSTEM_USER_ID);
        user.setNickname("内置用户");
        user.setIsSystem(true);
        return user;
    }
}
