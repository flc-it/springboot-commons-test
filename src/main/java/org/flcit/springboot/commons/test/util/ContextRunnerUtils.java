/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flcit.springboot.commons.test.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.AbstractApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class ContextRunnerUtils {

    private ContextRunnerUtils() { }

    /**
     * @return
     */
    public static WebApplicationContextRunner getBaseContextRunnerForWebMvc() {
        return new WebApplicationContextRunner()
                .withConfiguration(
                    AutoConfigurations.of(
                            HttpMessageConvertersAutoConfiguration.class,
                            JacksonAutoConfiguration.class,
                            WebMvcAutoConfiguration.class
                        )
                )
                .withPropertyValues(
                    PropertyTestUtils.getValue("spring.jackson", "default-property-inclusion", "non_empty")
                );
    }

    /**
     * @param contextRunner
     */
    public static void assertHasFailed(final AbstractApplicationContextRunner<?, ?, AssertableWebApplicationContext> contextRunner) {
        contextRunner.run(context -> assertThat(context).hasFailed());
    }

    /**
     * @param contextRunner
     * @param types
     */
    public static void assertHasSingleBean(final AbstractApplicationContextRunner<?, ?, AssertableWebApplicationContext> contextRunner, Class<?>... types) {
        contextRunner.run(context -> assertHasSingleBean(context, types));
    }

    /**
     * @param context
     * @param types
     */
    public static void assertHasSingleBean(final AssertableWebApplicationContext context, Class<?>... types) {
        for (Class<?> type : types) {
            assertThat(context).hasSingleBean(type);
        }
    }

    /**
     * @param contextRunner
     * @param types
     */
    public static void assertDoesNotHaveBean(final AbstractApplicationContextRunner<?, ?, AssertableWebApplicationContext> contextRunner, Class<?>... types) {
        contextRunner.run(context -> {
            for (Class<?> type : types) {
                assertThat(context).doesNotHaveBean(type);
            }
        });
    }

    /**
     * @param contextRunner
     * @param beansType
     * @return
     */
    @SuppressWarnings({ "rawtypes", "java:S1452" })
    public static AbstractApplicationContextRunner<?, ConfigurableWebApplicationContext, AssertableWebApplicationContext> withMockBeansBuilder(AbstractApplicationContextRunner<?, ConfigurableWebApplicationContext, AssertableWebApplicationContext> contextRunner, Class... beansType) {
        return withMockBeans(contextRunner, beansType).getKey();
    }

    /**
     * @param contextRunner
     * @param beansType
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "java:S1452" })
    public static SimpleEntry<AbstractApplicationContextRunner<?, ConfigurableWebApplicationContext, AssertableWebApplicationContext>, Map<Class<Object>, Object>> withMockBeans(AbstractApplicationContextRunner<?, ConfigurableWebApplicationContext, AssertableWebApplicationContext> contextRunner, Class... beansType) {
        final Map<Class<Object>, Object> mockBeans = new HashMap<>(beansType.length);
        for (Class beanType : beansType) {
            final Object mocked = mock(beanType);
            mockBeans.put(beanType, mocked);
            contextRunner = contextRunner.withBean(beanType, () -> mocked);
        }
        return new SimpleEntry<>(contextRunner, mockBeans);
    }

}
