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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class AssertUtils {

    private AssertUtils() { }

    /**
     * @param expectedType
     * @param executable
     */
    public static void assertThrowsCause(Class<? extends Throwable> expectedType, Executable executable) {
        assertThrows(expectedType, () -> {
            try {
                executable.execute();
            } catch (Throwable t) {
                throw getException(t, expectedType);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T getException(Throwable t, Class<? extends Throwable> expectedType) {
        Throwable current = t;
        if (expectedType.isAssignableFrom(current.getClass())) {
            return (T) current;
        }
        while ((current = current.getCause()) != null) {
            if (expectedType.isAssignableFrom(current.getClass())) {
                return (T) current;
            }
        }
        return (T) t;
    }

}
