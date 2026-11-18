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

package top.continew.admin.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import top.continew.admin.system.enums.MenuTypeEnum;
import top.continew.admin.system.model.resp.MenuResp;

import java.util.List;
import java.util.Set;

/**
 * 小程序菜单与按钮联合授权规则。
 *
 * @author Elizabeth Davis
 * @since 5.0.0
 */
class AuthServiceImplTest {

    @Test
    void buttonAloneDoesNotExposeMenuOrEmptyDirectory() {
        MenuResp root = menu(1L, 0L, MenuTypeEnum.DIR);
        MenuResp button = menu(3L, 1L, MenuTypeEnum.BUTTON);

        assertEquals(List.of(),
            AuthServiceImpl.filterVisibleMiniProgramMenus(Set.of(root, button), List.of(root)));
    }

    @Test
    void menuAloneDoesNotExposeRoute() {
        MenuResp root = menu(1L, 0L, MenuTypeEnum.DIR);
        MenuResp page = menu(2L, 1L, MenuTypeEnum.MENU);

        assertEquals(List.of(),
            AuthServiceImpl.filterVisibleMiniProgramMenus(Set.of(root, page), List.of(root, page)));
    }

    @Test
    void menuAndButtonExposeOnlyTheirDirectoryBranch() {
        MenuResp root = menu(1L, 0L, MenuTypeEnum.DIR);
        MenuResp approval = menu(2L, 1L, MenuTypeEnum.DIR);
        MenuResp application = menu(3L, 2L, MenuTypeEnum.MENU);
        MenuResp applicationList = menu(4L, 3L, MenuTypeEnum.BUTTON);
        MenuResp task = menu(5L, 2L, MenuTypeEnum.MENU);
        MenuResp otherRoot = menu(6L, 0L, MenuTypeEnum.DIR);

        List<MenuResp> visible = AuthServiceImpl.filterVisibleMiniProgramMenus(
            Set.of(root, approval, application, applicationList, task, otherRoot),
            List.of(root, approval, application, task, otherRoot));

        assertEquals(List.of(root, approval, application), visible);
    }

    private static MenuResp menu(Long id, Long parentId, MenuTypeEnum type) {
        MenuResp menu = new MenuResp();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setType(type);
        return menu;
    }
}
