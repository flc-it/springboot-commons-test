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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.flcit.commons.core.util.IterableUtils;
import org.flcit.springboot.commons.test.multipart.PartResource;
import org.flcit.springboot.commons.test.security.SecurityTestUtils;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class MvcUtils {

    private MvcUtils() { }

    /**
     * @param context
     * @param paths
     */
    public static void assertGetAsyncResponsesTimeout(AssertableWebApplicationContext context, String... paths) {
        assertAsyncResponsesTimeout(context, HttpMethod.GET, paths);
    }

    private static void assertAsyncResponsesTimeout(AssertableWebApplicationContext context, HttpMethod method, String... paths) {
        for (String path : paths) {
            assertThrows(IllegalStateException.class, () -> assertAsyncResponse(context, MockMvcRequestBuilders.request(method, path)).getAsyncResult());
        }
    }

    /**
     * @param context
     * @param expectedResponse
     * @param paths
     */
    public static void assertGetJsonAsyncResponses(AssertableWebApplicationContext context, Object expectedResponse, String... paths) {
        for (String path : paths) {
            assertGetJsonResponse(context, path, expectedResponse, true);
        }
    }

    /**
     * @param context
     * @param path
     * @param expectedResponse
     * @return
     */
    public static ResultActions assertGetJsonAsyncResponse(AssertableWebApplicationContext context, String path, Object expectedResponse) {
        return assertGetJsonResponse(context, path, expectedResponse, true);
    }

    /**
     * @param context
     * @param path
     * @return
     */
    public static ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path) {
        return assertGetJsonResponse(context, path, null);
    }

    /**
     * @param context
     * @param path
     * @param expectedResponse
     * @return
     */
    public static ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, Object expectedResponse) {
        return assertGetJsonResponse(context, path, expectedResponse, false);
    }

    private static ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, Object expectedResponse, boolean async) {
        return assertGetJsonResponse(context, path, null, expectedResponse, true, async, status().isOk());
    }

    /**
     * @param context
     * @param path
     * @param expectedStatus
     * @param expectedResponse
     * @return
     */
    public static ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, HttpStatus expectedStatus, Object expectedResponse) {
        return assertGetJsonResponse(context, path, null, expectedResponse, true, false, status().is(expectedStatus.value()));
    }

    /**
     * @param context
     * @param path
     * @param expectedStatus
     * @param expectedResponse
     * @param strict
     * @return
     */
    public static ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, HttpStatus expectedStatus, Object expectedResponse, boolean strict) {
        return assertGetJsonResponse(context, path, null, expectedResponse, strict, false, status().is(expectedStatus.value()));
    }

    private static ResultActions assertGetJsonResponse(AssertableWebApplicationContext context, String path, Object[] uriVariables, Object expectedResponse, boolean strict, boolean async, ResultMatcher... expected) {
        expected = ObjectUtils.addObjectToArray(expected, content().contentType(MediaType.APPLICATION_JSON));
        if (expectedResponse != null) {
            expected = ObjectUtils.addObjectToArray(expected, content().json(writeValueAsString(context, expectedResponse), strict));
        }
        return assertGetResponseIntern(context, path, uriVariables, new MediaType[] { MediaType.APPLICATION_JSON }, async, expected);
    }

    /**
     * @param context
     * @param paths
     */
    public static void assertGetResponseStatus(AssertableWebApplicationContext context, String... paths) {
        assertGetResponseStatus(context, HttpStatus.OK, paths);
    }

    /**
     * @param context
     * @param expectedStatus
     * @param paths
     */
    public static void assertGetResponseStatus(AssertableWebApplicationContext context, HttpStatus expectedStatus, String... paths) {
        for (String path: paths) {
            assertGetResponse(context, path, expectedStatus);
        }
    }

    /**
     * @param context
     * @param path
     * @param expectedStatus
     * @param uriVariables
     * @return
     */
    public static ResultActions assertGetResponse(AssertableWebApplicationContext context, String path, HttpStatus expectedStatus, Object... uriVariables) {
        return assertGetResponseIntern(context, path, uriVariables, null, false, status().is(expectedStatus.value()));
    }

    /**
     * @param context
     * @param path
     * @param uriVariables
     * @return
     */
    public static ResultActions assertGetResponse(AssertableWebApplicationContext context, String path, Object... uriVariables) {
        return assertGetResponseIntern(context, path, uriVariables, null, false, status().isOk());
    }

    private static ResultActions assertGetResponseIntern(AssertableWebApplicationContext context, String path, Object[] uriVariables, MediaType[] mediaTypes, boolean async, ResultMatcher... expected) {
        return assertResponse(context, uriVariables != null ? MockMvcRequestBuilders.get(path, uriVariables) : MockMvcRequestBuilders.get(path), mediaTypes, async, expected);
    }

    /**
     * @param context
     * @param path
     * @param uriVariables
     * @param body
     * @return
     */
    public static ResultActions assertPostJsonResponse(AssertableWebApplicationContext context, String path, Object[] uriVariables, Object body) {
        return assertPostJsonResponse(context, path, uriVariables, body, null, false);
    }

    /**
     * @param context
     * @param path
     * @param uriVariables
     * @param body
     * @param expectedResponse
     * @param strict
     * @return
     */
    public static ResultActions assertPostJsonResponse(AssertableWebApplicationContext context, String path, Object[] uriVariables, Object body, Object expectedResponse, boolean strict) {
        return assertPostJsonResponse(context, path, uriVariables, body, expectedResponse, strict, false);
    }

    /**
     * @param context
     * @param path
     * @param uriVariables
     * @param body
     * @param expectedResponse
     * @param strict
     * @param mockMultipart
     * @return
     */
    public static ResultActions assertPostJsonResponse(AssertableWebApplicationContext context, String path, Object[] uriVariables, Object body, Object expectedResponse, boolean strict, boolean mockMultipart) {
        ResultMatcher[] expected = new ResultMatcher[] { status().isOk() };
        if (expectedResponse != null) {
            expected = ObjectUtils.addObjectToArray(expected, content().contentType(MediaType.APPLICATION_JSON));
            expected = ObjectUtils.addObjectToArray(expected, content().json(writeValueAsString(context, expectedResponse), strict));
        }
        return assertResponse(context, getMockMvcRequestBuilders(context, path, uriVariables, body, mockMultipart), new MediaType[] { MediaType.APPLICATION_JSON }, false, expected);
    }

    /**
     * @param context
     * @param path
     * @param body
     * @param expectedException
     */
    public static void assertPostThrows(AssertableWebApplicationContext context, String path, Object body, Class<? extends Throwable> expectedException) {
        final Executable executable = () -> assertPostResponse(context, path, null, body);
        AssertUtils.assertThrowsCause(expectedException, executable);
    }

    /**
     * @param context
     * @param path
     * @param body
     * @return
     */
    public static ResultActions assertPostResponse(AssertableWebApplicationContext context, String path, Object body) {
        return assertPostResponse(context, path, null, body);
    }

    /**
     * @param context
     * @param path
     * @param body
     * @param expectedStatus
     * @return
     */
    public static ResultActions assertPostResponseStatus(AssertableWebApplicationContext context, String path, Object body, HttpStatus expectedStatus) {
        return assertPostResponse(context, path, null, body, expectedStatus);
    }

    /**
     * @param context
     * @param path
     * @param uriVariables
     * @param body
     * @return
     */
    public static ResultActions assertPostResponse(AssertableWebApplicationContext context, String path, Object[] uriVariables, Object body) {
        return assertResponse(context, getMockMvcRequestBuilders(context, path, uriVariables, body), null, false, status().isOk());
    }

    /**
     * @param context
     * @param path
     * @param uriVariables
     * @param body
     * @param expectedStatus
     * @return
     */
    public static ResultActions assertPostResponse(AssertableWebApplicationContext context, String path, Object[] uriVariables, Object body, HttpStatus expectedStatus) {
        return assertResponse(context, getMockMvcRequestBuilders(context, path, uriVariables, body), null, false, status().is(expectedStatus.value()));
    }

    private static MockHttpServletRequestBuilder getMockMvcRequestBuilders(WebApplicationContext context, String path, Object[] uriVariables, Object body) {
        return getMockMvcRequestBuilders(context, path, uriVariables, body, false);
    }

    private static MockHttpServletRequestBuilder getMockMvcRequestBuilders(WebApplicationContext context, String path, Object[] uriVariables, Object body, boolean mockMultipart) {
        final boolean multipart = isMultipart(body);
        MockHttpServletRequestBuilder requestBuilder = null;
        if (multipart) {
            requestBuilder = getMockMvcRequestBuildersMultipart(path, uriVariables, body, mockMultipart);
        } else {
            requestBuilder = getPostMockHttpServletRequestBuilder(path, uriVariables, body != null ? MediaType.APPLICATION_JSON : null, body != null ? writeValueAsBytes(context, body) : null);
        }
        return requestBuilder;
    }

    @SuppressWarnings("unchecked")
    private static MockHttpServletRequestBuilder getMockMvcRequestBuildersMultipart(String path, Object[] uriVariables, Object body, boolean mockMultipart) {
        final MockMultipartHttpServletRequestBuilder multipartRequestBuilder = uriVariables != null ? MockMvcRequestBuilders.multipart(path, uriVariables) : MockMvcRequestBuilders.multipart(path);
        if (body instanceof MockMultipartFile) {
            multipartRequestBuilder.file((MockMultipartFile) body);
        } else {
            final Iterable<MockMultipartFile> it = body.getClass().isArray() ? Arrays.asList((MockMultipartFile[]) body) : (Iterable<MockMultipartFile>) body;
            for (MockMultipartFile file : it) {
                multipartRequestBuilder.file(file);
            }
        }
        return mockMultipart ? multipartRequestBuilder : getMockHttpServletRequestBuilderMultipartFormData(multipartRequestBuilder, path, uriVariables);
    }

    private static MockHttpServletRequestBuilder getMockHttpServletRequestBuilderMultipartFormData(MockMultipartHttpServletRequestBuilder builder, String path, Object[] uriVariables) {
        final FormHttpMessageConverter converter = new FormHttpMessageConverter();
        final MockMultipartHttpServletRequest request = (MockMultipartHttpServletRequest) builder.buildRequest(new MockServletContext());
        final MultiValueMap<String, HttpEntity<Resource>> parts = new LinkedMultiValueMap<>();
        for (Entry<String, List<MultipartFile>> files : request.getMultiFileMap().entrySet()) {
            for (MultipartFile file : files.getValue()) {
                parts.add(files.getKey(), getPart(file));
            }
        }
        final MockHttpOutputMessage httpOutputMessage = new MockHttpOutputMessage();
        try {
            for (Part part : request.getParts()) {
                parts.add(part.getName(), getPart(part));
            }
            converter.write(parts, MediaType.MULTIPART_FORM_DATA, httpOutputMessage);
        } catch (IOException | ServletException e) {
            throw new IllegalStateException(e);
        }
        return getPostMockHttpServletRequestBuilder(path, uriVariables, httpOutputMessage.getHeaders().getContentType(), httpOutputMessage.getBodyAsBytes());
    }

    private static HttpEntity<Resource> getPart(MultipartFile file) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, file.getContentType());
        return new HttpEntity<>(file.getResource(), headers);
    }

    private static HttpEntity<Resource> getPart(Part part) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, part.getContentType());
        return new HttpEntity<>(new PartResource(part), headers);
    }

    private static MockHttpServletRequestBuilder getPostMockHttpServletRequestBuilder(String path, Object[] uriVariables, MediaType contentType, byte[] content) {
        final MockHttpServletRequestBuilder requestBuilder = uriVariables != null ? MockMvcRequestBuilders.post(path, uriVariables) : MockMvcRequestBuilders.post(path);
        if (contentType != null) {
            requestBuilder.contentType(contentType);
        }
        if (content != null) {
            requestBuilder.content(content);
        }
        return requestBuilder;
    }

    private static boolean isMultipart(Object body) {
        if (body == null) {
            return false;
        }
        if (body instanceof MockMultipartFile) {
            return true;
        }
        if (body.getClass().isArray()) {
            return !ObjectUtils.isEmpty((Object[]) body) && MockMultipartFile.class.isAssignableFrom(body.getClass().getComponentType());
        }
        if (Iterable.class.isAssignableFrom(body.getClass())) {
            return !CollectionUtils.isEmpty((List<?>) body) && isMultipart(IterableUtils.getFirst((Iterable<?>) body));
        }
        return false;
    }

    private static ResultActions assertResponse(WebApplicationContext context, MockHttpServletRequestBuilder requestBuilder, MediaType[] mediaTypes, boolean async, ResultMatcher... expected) {
        final MockMvc mockMvc = getMockMvc(context);
        if (mediaTypes != null) {
            requestBuilder.accept(mediaTypes);
        }
        return assertResponse(mockMvc, async ? asyncDispatch(assertAsyncResponse(mockMvc, requestBuilder, null)) : requestBuilder, expected);
    }

    private static MvcResult assertAsyncResponse(WebApplicationContext context, MockHttpServletRequestBuilder requestBuilder, ResultMatcher... expected) {
        return assertAsyncResponse(getMockMvc(context), requestBuilder, null, expected);
    }

    private static MvcResult assertAsyncResponse(MockMvc mockMvc, MockHttpServletRequestBuilder requestBuilder, Object expectedResult, ResultMatcher... expected) {
        if (expectedResult != null) {
            expected = ObjectUtils.addObjectToArray(expected, request().asyncResult(expectedResult));
        }
        if (ObjectUtils.isEmpty(expected)) {
            expected = new ResultMatcher[] { request().asyncStarted() };
        }
        return assertResponse(mockMvc, requestBuilder, expected).andReturn();
    }

    private static ResultActions assertResponse(MockMvc mockMvc, RequestBuilder requestBuilder, ResultMatcher... expected) {
        try {
            final ResultActions result = mockMvc.perform(requestBuilder);
            if (expected != null) {
                result.andExpectAll(expected);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static HttpStatus assertGetResponseStatus(AssertableWebApplicationContext context, String path, User user) {
        final FilterChainProxy filterChainProxy = context.getBean(FilterChainProxy.class);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockServletContext servletContext = new MockServletContext();
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
        final MockHttpServletRequest request = MockMvcRequestBuilders.get(path).buildRequest(servletContext);
        if (user != null) {
            SecurityTestUtils.addSecurityContextUser(filterChainProxy.getFilterChains().get(0), user);
        }
        try {
            filterChainProxy.doFilter(request, response, new MockFilterChain());
        } catch (IOException | ServletException e) {
            throw new IllegalStateException(e);
        }
        return HttpStatus.valueOf(response.getStatus());
    }

    private static HttpStatus assertGetResponseStatusNoMvc(AssertableWebApplicationContext context, String path) {
        return assertGetResponseStatus(context, path, null);
    }

    private static HttpStatus assertGetResponseStatus(AssertableWebApplicationContext context, String[] roles, String path) {
        return assertGetResponseStatus(context, path, SecurityTestUtils.getUser(roles, null));
    }

    /**
     * @param context
     * @param paths
     */
    public static void assertGetResponseStatusNoMvc(AssertableWebApplicationContext context, String... paths) {
        assertGetResponseStatusNoMvc(context, HttpStatus.OK, paths);
    }

    /**
     * @param context
     * @param expectedStatus
     * @param paths
     */
    public static void assertGetResponseStatusNoMvc(AssertableWebApplicationContext context, HttpStatus expectedStatus, String... paths) {
        for (String path : paths) {
            assertEquals(expectedStatus, assertGetResponseStatusNoMvc(context, path));
        }
    }

    /**
     * @param context
     * @param roles
     * @param expectedStatus
     * @param paths
     */
    public static void assertGetResponseStatus(AssertableWebApplicationContext context, String[] roles, HttpStatus expectedStatus, String... paths) {
        for (String path : paths) {
            assertEquals(expectedStatus, assertGetResponseStatus(context, roles, path));
        }
    }

    /**
     * @param <T>
     * @param context
     * @param result
     * @param responseType
     * @return
     */
    public static <T> T convert(final WebApplicationContext context, final ResultActions result, Class<T> responseType) {
        try {
            return getObjectMapper(context).readValue(result.andReturn().getResponse().getContentAsByteArray(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String writeValueAsString(final WebApplicationContext context, final Object body) {
        try {
            return getObjectMapper(context).writeValueAsString(body);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] writeValueAsBytes(final WebApplicationContext context, final Object body) {
        try {
            return getObjectMapper(context).writeValueAsBytes(body);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ObjectMapper getObjectMapper(final WebApplicationContext context) {
        return context.getBean(ObjectMapper.class);
    }

    private static MockMvc getMockMvc(WebApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

}
