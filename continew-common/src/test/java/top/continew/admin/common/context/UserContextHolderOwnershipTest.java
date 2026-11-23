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

package top.continew.admin.common.context;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * 用户上下文缓存归属测试
 *
 * @author SeeweWis
 * @since 5.0.0
 */
class UserContextHolderOwnershipTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.clearContext();
    }

    @Test
    void shouldReloadContextWhenCachedContextBelongsToAnotherUser() {
        UserContext cached = context(1L);
        UserContext sessionContext = context(2L);
        SaSession session = mock(SaSession.class);
        when(session.getModel(SaSession.USER, UserContext.class)).thenReturn(sessionContext);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(2L);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            UserContextHolder.setContext(cached, false);

            assertSame(sessionContext, UserContextHolder.getContext());
        }
    }

    @Test
    void shouldKeepTransmittedContextWhenRequestContextIsUnavailable() {
        UserContext context = context(1L);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdDefaultNull)
                .thenThrow(new SaTokenContextException("request context unavailable"));
            UserContextHolder.setContext(context, false);

            assertSame(context, UserContextHolder.getContext());
        }
    }

    @Test
    void shouldUpdateTargetSessionWithoutChangingCurrentThreadContext() {
        UserContext current = context(1L);
        UserContext target = context(2L);
        SaSession targetSession = mock(SaSession.class);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.getSessionByLoginId(2L, false)).thenReturn(targetSession);
            stpUtil.when(StpUtil::getLoginIdDefaultNull).thenReturn(1L);
            UserContextHolder.setContext(current, false);

            UserContextHolder.updateSessionContext(target);

            verify(targetSession).set(SaSession.USER, target);
            assertSame(current, UserContextHolder.getContext());
        }
    }

    private static UserContext context(Long userId) {
        UserContext context = new UserContext();
        context.setId(userId);
        return context;
    }
}
