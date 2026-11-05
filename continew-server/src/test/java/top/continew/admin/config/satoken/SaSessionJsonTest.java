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

package top.continew.admin.config.satoken;

import static org.assertj.core.api.Assertions.assertThat;

import cn.dev33.satoken.json.SaJsonTemplateForJackson3;
import cn.dev33.satoken.session.SaSession;
import org.junit.jupiter.api.Test;
import top.continew.admin.common.context.RoleContext;
import top.continew.admin.common.context.UserContext;
import top.continew.admin.common.enums.DataScopeEnum;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Sa-Token session JSON compatibility.
 *
 * @author SeeweWis
 * @since 5.0.0
 */
class SaSessionJsonTest {

    @Test
    void shouldRestoreUserContextFromSession() {
        RoleContext role = new RoleContext(1L, "admin", DataScopeEnum.ALL);
        UserContext user = new UserContext(new HashSet<>(Set.of("system:user:list")),
            new HashSet<>(Set.of(role)), 90);
        user.setId(1L);
        user.setPwdResetTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        SaSession session = new SaSession("test-session");
        session.getDataMap().put(SaSession.USER, user);

        SaJsonTemplateForJackson3 json = new SaJsonTemplateForJackson3();
        SaSession restored = json.jsonToObject(json.objectToJson(session), SaSession.class);

        assertThat(restored.get(SaSession.USER)).isInstanceOf(UserContext.class);
        UserContext restoredUser = (UserContext) restored.get(SaSession.USER);
        assertThat(restoredUser.getId()).isEqualTo(user.getId());
        assertThat(restoredUser.getPermissions()).containsExactly("system:user:list");
        assertThat(restoredUser.getRoles()).containsExactly(role);
        assertThat(restoredUser.getPwdResetTime()).isEqualTo(user.getPwdResetTime());
    }
}
