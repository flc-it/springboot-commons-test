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

package org.flcit.springboot.commons.test.validation;

import java.util.List;
import java.util.Objects;

import org.springframework.validation.AbstractBindingResult;
import org.springframework.validation.ObjectError;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class SimpleBindingResult extends AbstractBindingResult {

    private static final long serialVersionUID = 1L;

    private final List<ObjectError> errors;

    /**
     * @param errors
     */
    @SuppressWarnings("java:S4449")
    public SimpleBindingResult(List<ObjectError> errors) {
        super(null);
        this.errors = errors;
    }

    @Override
    public List<ObjectError> getAllErrors() {
        return this.errors;
    }

    @Override
    public Object getTarget() {
        throw new IllegalStateException();
    }

    @Override
    protected Object getActualFieldValue(String field) {
        throw new IllegalStateException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(errors);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleBindingResult other = (SimpleBindingResult) obj;
        return Objects.equals(errors, other.errors);
    }

}
