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

package org.flcit.springboot.commons.test.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.servlet.http.Part;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class PartResource implements Resource {

    private final Part part;

    /**
     * @param part
     */
    public PartResource(Part part) {
        Assert.notNull(part, "Part must not be null");
        this.part = part;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public URL getURL() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public URI getURI() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public File getFile() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public long contentLength() throws IOException {
        return part.getSize();
    }

    @Override
    public long lastModified() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public String getFilename() {
        return part.getSubmittedFileName();
    }

    @Override
    public String getDescription() {
        return "Part resource [" + this.part.getName() + "]";
    }

}
