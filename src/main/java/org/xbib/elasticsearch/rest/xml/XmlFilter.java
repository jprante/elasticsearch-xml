package org.xbib.elasticsearch.rest.xml;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.http.HttpChannel;
import org.elasticsearch.http.HttpRequest;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestFilter;
import org.elasticsearch.rest.RestFilterChain;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;

import org.xbib.elasticsearch.common.xcontent.XmlXContentBuilder;
import org.xbib.elasticsearch.common.xcontent.XmlXContentFactory;
import org.xbib.elasticsearch.common.xcontent.XmlXContentType;
import org.xbib.elasticsearch.common.xcontent.xml.XmlXParams;

import java.util.Map;

/**
 * XML filter for Elasticsearch REST requests and responses.
 *
 * To receive XML responses, the request header Accept: must contain "application/xml"
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

    private boolean isXml(RestRequest request) {
        return "application/xml".equals(request.header("Accept"))
                || request.hasParam("xml");
    }

    /**
     * Unwraps an XML REST request to JSON if Content-type: header declares application/xml.
     * We must extend HttpRequest because this will get used in a casting in the HTTP controller.
     */
    class XmlRequest extends HttpRequest {

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
        public BytesReference content() {
            if (isXml(request)) {
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
     * Wraps a REST channel response into XML if Accept: header declares application/xml.
     * This must extend HttpChannel because this will get used in a casting in the HTTP controller.
     */
    class XmlChannel extends HttpChannel {

        private final RestChannel channel;

        XmlChannel(RestRequest request, RestChannel channel) {
            super(request, true);
            this.channel = channel;
        }

        @Override
        public void sendResponse(RestResponse response) {
            if (!response.status().equals(RestStatus.OK)) {
                channel.sendResponse(response);
            }
            if (isXml(request)) {
                XContentParser parser = null;
                try {
                    String string = response.content().toUtf8(); // takes some space ... :(
                    XContentType xContentType = XContentFactory.xContentType(string);
                    parser = XContentFactory.xContent(xContentType).createParser(string);
                    parser.nextToken();
                    XmlXContentBuilder builder = XmlXContentFactory.xmlBuilder(params);
                    if (request.paramAsBoolean("pretty", false)) {
                        builder.prettyPrint();
                    }
                    builder.copyCurrentStructure(parser);
                    BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/xml; charset=UTF-8", builder.bytes());
                    channel.sendResponse(restResponse);
                    return;
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
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
