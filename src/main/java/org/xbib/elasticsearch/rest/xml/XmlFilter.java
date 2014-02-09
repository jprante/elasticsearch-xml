/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.elasticsearch.rest.xml;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.StringRestResponse;

import org.xbib.elasticsearch.common.xcontent.XmlXContentBuilder;
import org.xbib.elasticsearch.common.xcontent.XmlXContentFactory;
import org.xbib.elasticsearch.common.xcontent.XmlXContentType;
import org.xbib.elasticsearch.common.xcontent.xml.XmlXParams;

import java.util.Map;

/**
 * XML filter for Elasticsearch REST requests and responses.
 *
 * XML requests must be declared with header Content-type: application/xml
 *
 * To receive XML responses, the header Accept: must contain the sequence application/xml
 */
public class XmlFilter extends RestFilter {

    private final ESLogger logger = ESLoggerFactory.getLogger(XmlFilter.class.getName());

    private final XmlXParams params;

    public XmlFilter() {
        this.params = new XmlXParams();
    }

    @Override
    public void process(RestRequest request, RestChannel channel, RestFilterChain filterChain) {
        filterChain.continueProcessing(new XmlRequest(request), new XmlChannel(request, channel));
    }

    /**
     * Unwraps an XML REST request to JSON if Content-type: header declares application/xml
     */
    class XmlRequest extends RestRequest {

        private RestRequest request;

        XmlRequest(RestRequest request) {
            this.request = request;
        }

        @Override
        public Method method() {
            return request.method();
        }

        @Override
        public String uri() {
            return request.uri();
        }

        @Override
        public String rawPath() {
            return request.rawPath();
        }

        @Override
        public boolean hasContent() {
            return request.hasContent();
        }

        @Override
        public boolean contentUnsafe() {
            return request.contentUnsafe();
        }

        @Override
        public BytesReference content() {
            if ("application/xml".equals(request.header("Content-type"))) {
                XContentParser parser = null;
                try {
                    BytesReference b = request.content();
                    parser = XmlXContentFactory.xContent(XmlXContentType.XML).createParser(b);
                    parser.nextToken();
                    XContentBuilder builder = XContentFactory.jsonBuilder();
                    builder.copyCurrentStructure(parser);
                    return builder.bytes();
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (parser != null) {
                        parser.close();
                    }
                }
            }
            return request.content();
        }

        @Override
        public String header(String name) {
            return request.header(name);
        }

        @Override
        public Iterable<Map.Entry<String, String>> headers() {
            return request.headers();
        }

        @Override
        public boolean hasParam(String key) {
            return false;
        }

        @Override
        public String param(String key) {
            return request.param(key);
        }

        @Override
        public String param(String key, String defaultValue) {
            return request.param(key, defaultValue);
        }

        @Override
        public Map<String, String> params() {
            return request.params();
        }
    }

    /**
     * Wraps a REST channel response into XML if Accept: header declares application/xml
     */
    class XmlChannel implements RestChannel {

        private final RestRequest request;

        private final RestChannel channel;

        XmlChannel(RestRequest request, RestChannel channel) {
            this.request = request;
            this.channel = channel;
        }

        @Override
        public void sendResponse(RestResponse response) {
            if (!response.status().equals(RestStatus.OK)) {
                channel.sendResponse(response);
            }
            String accept = request.header("Accept");
            if (accept != null && accept.contains("application/xml")) {
                XContentParser parser = null;
                try {
                    byte[] b = response.content();
                    XContentType xContentType = XContentFactory.xContentType(b);
                    parser = XContentFactory.xContent(xContentType).createParser(b);
                    parser.nextToken();
                    XmlXContentBuilder builder = XmlXContentFactory.xmlBuilder(params);
                    if (request.paramAsBoolean("pretty", false)) {
                        builder.prettyPrint();
                    }
                    builder.copyCurrentStructure(parser);
                    response.addHeader("Content-type", "application/xml");
                    channel.sendResponse(new StringRestResponse(RestStatus.OK, builder.string()));
                    return;
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    channel.sendResponse(new StringRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                    return;
                } finally {
                    if (parser != null) {
                        parser.close();
                    }
                }
            }
            channel.sendResponse(response);
        }
    }
}
