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

package top.continew.admin.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 事务提交后回调测试
 *
 * @author SeeweWis
 * @since 5.0.0
 */
class TransactionUtilsTest {

    private final List<String> actions = new ArrayList<>();

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldRunImmediatelyWithoutActiveSynchronization() {
        TransactionUtils.afterCommit(() -> actions.add("done"));

        assertEquals(List.of("done"), actions);
    }

    @Test
    void shouldRunAfterCommitWhenSynchronizationIsActive() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionUtils.afterCommit(() -> actions.add("done"));

        assertTrue(actions.isEmpty());
        List<TransactionSynchronization> synchronizations =
            TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCommit();
        assertEquals(List.of("done"), actions);
    }
}
