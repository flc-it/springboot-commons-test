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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.util.ReflectionUtils;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class ReflectionTestUtils {

    private ReflectionTestUtils() { }

    /**
     * @param obj
     * @param classType
     * @param name
     * @param parameterTypes
     * @param args
     * @return
     */
    public static Object invokeMethod(Object obj, Class<?> classType, String name, Class<?>[] parameterTypes, Object[] args) {
        final Method method = getMethod(classType, name, parameterTypes);
        ReflectionUtils.makeAccessible(method);
        try {
            return method.invoke(obj, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method getMethod(Class<?> classType, String name, Class<?>... parameterTypes) {
        do {
            try {
                return classType.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                // DO NOTHING
            }
        }
        while ((classType = classType.getSuperclass()) != Object.class);
        throw new IllegalStateException(new NoSuchMethodException(name));
    }

    /**
     * @param obj
     * @param name
     * @return
     */
    public static Object getFieldValue(Object obj, String name) {
        if (obj == null) {
            return null;
        }
        final Field field = getField(obj.getClass(), name);
        ReflectionUtils.makeAccessible(field);
        return ReflectionUtils.getField(field, obj);
    }

    /**
     * @param obj
     * @param name
     * @param value
     */
    public static void setFieldValue(Object obj, String name, Object value) {
        if (obj == null) {
            return;
        }
        final Field field = getField(obj.getClass(), name);
        makeFinalAccessible(field);
        ReflectionUtils.setField(field, obj, value);
    }

    private static void makeFinalAccessible(Field field) {
        ReflectionUtils.makeAccessible(field);
        if (Modifier.isFinal(field.getModifiers())) {
            try {
                final Field modifiersField = Field.class.getDeclaredField("modifiers");
                ReflectionUtils.makeAccessible(modifiersField);
                ReflectionUtils.setField(modifiersField, field, field.getModifiers() & ~Modifier.FINAL);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static Field getField(Class<?> classType, String name) {
        do {
            try {
                return classType.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // DO NOTHING
            }
        }
        while ((classType = classType.getSuperclass()) != Object.class);
        throw new IllegalStateException(new NoSuchFieldException(name));
    }

}
