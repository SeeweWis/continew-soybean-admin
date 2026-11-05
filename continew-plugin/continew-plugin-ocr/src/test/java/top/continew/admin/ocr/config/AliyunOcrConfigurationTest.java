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

package top.continew.admin.ocr.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import top.continew.admin.common.api.ocr.OcrApi;
import top.continew.admin.common.api.system.OptionApi;

/**
 * OCR 插件配置测试。
 *
 * @author seewe-yolo
 * @since 4.2.0
 */
class AliyunOcrConfigurationTest {

    @Test
    void shouldCreateOcrApiWithApplicationObjectMapper() {
        try (
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(OptionApi.class, () -> mock(OptionApi.class));
            context.registerBean(ObjectMapper.class, () -> new JsonMapper());
            context.register(AliyunOcrConfiguration.class);
            context.refresh();

            assertNotNull(context.getBean(OcrApi.class));
        }
    }
}
