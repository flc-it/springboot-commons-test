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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.ResultActions;

import org.flcit.commons.core.util.StringUtils;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class ResultActionsUtils {

    private ResultActionsUtils() { }

    /**
     * @param result
     * @param fields
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ResultActions assertEmptyOrNull(final ResultActions result, final String... fields) throws Exception {
        return assertAllOf(result, new Matcher[] { emptyOrNullString() } , fields);
    }

    /**
     * @param result
     * @param notExpectedValue
     * @param fields
     * @return
     * @throws Exception
     */
    public static ResultActions assertNotEmptyOrNullAndNotEqual(final ResultActions result, final String notExpectedValue, final String... fields) throws Exception {
        return assertNotEmptyOrNullAndNotEqualIntern(result, emptyOrNullString(), hasToString(notExpectedValue), fields);
    }

    /**
     * @param result
     * @param notExpectedValue
     * @param fields
     * @return
     * @throws Exception
     */
    public static ResultActions assertNotEmptyOrNullAndNotEqual(final ResultActions result, final Object notExpectedValue, final String... fields) throws Exception {
        return assertNotEmptyOrNullAndNotEqualIntern(result, notExpectedValue instanceof CharSequence ? emptyOrNullString() : empty(), equalTo(notExpectedValue), fields);
    }

    /**
     * @param result
     * @param regex
     * @param fields
     * @return
     * @throws Exception
     */
    public static ResultActions assertNotEmptyOrNullAndMatchRegex(final ResultActions result, final String regex, final String... fields) throws Exception {
        return assertNotEmptyOrNullAndMatchRegex(result, matchesRegex(regex), fields);
    }

    /**
     * @param result
     * @param regex
     * @param fields
     * @return
     * @throws Exception
     */
    public static ResultActions assertNotEmptyOrNullAndMatchRegex(final ResultActions result, final Pattern regex, final String... fields) throws Exception {
        return assertNotEmptyOrNullAndMatchRegex(result, matchesRegex(regex), fields);
    }

    @SuppressWarnings("unchecked")
    private static ResultActions assertNotEmptyOrNullAndMatchRegex(final ResultActions result, final Matcher<?> matchesRegex, final String... fields) throws Exception {
        return assertAllOf(result, new Matcher[] { not(emptyOrNullString()), matchesRegex }, fields);
    }

    @SuppressWarnings("unchecked")
    private static ResultActions assertNotEmptyOrNullAndNotEqualIntern(final ResultActions result, final Matcher<?> emptyMatcher, final Matcher<?> equalMatcher, final String... fields) throws Exception {
        return assertAllOf(result, new Matcher[] { not(emptyMatcher), not(equalMatcher) }, fields);
    }

    private static <T> ResultActions assertAllOf(final ResultActions result, final Matcher<? super T>[] matchers, final String... fields) throws Exception {
        for (String field : fields) {
            result.andExpect(jsonPath(getExpression(field), allOf(matchers)));
        }
        return result;
    }

    /**
     * @param result
     * @param fields
     * @return
     * @throws Exception
     */
    public static ResultActions assertArrayNotEmpty(final ResultActions result, final String... fields) throws Exception {
        for (String field : fields) {
            result.andExpect(jsonPath(getExpression(field), not(empty())));
        }
        return result;
    }

    /**
     * @param result
     * @param field
     * @param size
     * @return
     * @throws Exception
     */
    public static ResultActions assertArrayHasSize(final ResultActions result, final String field, final int size) throws Exception {
        return result.andExpect(jsonPath(getExpression(field), hasSize(size)));
    }

    /**
     * @param result
     * @param field
     * @param maxSize
     * @return
     * @throws Exception
     */
    public static ResultActions assertArrayHasSizeLessOrEqual(final ResultActions result, final String field, final int maxSize) throws Exception {
        return result.andExpect(jsonPath(getExpression(field), hasSize(lessThanOrEqualTo(maxSize))));
    }

    private static String getExpression(final String field) {
        return StringUtils.prefixIfMissing("$.", field);
    }

}
