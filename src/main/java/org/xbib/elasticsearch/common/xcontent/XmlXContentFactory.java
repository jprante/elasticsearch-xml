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
package org.xbib.elasticsearch.common.xcontent;

import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.jackson.dataformat.smile.SmileConstants;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.xcontent.smile.SmileXContent;
import org.elasticsearch.common.xcontent.yaml.YamlXContent;

import org.xbib.elasticsearch.common.xcontent.xml.XmlXContent;
import org.xbib.elasticsearch.common.xcontent.xml.XmlXParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A one stop to use {@link org.elasticsearch.common.xcontent.XContent} and {@link XmlXContentBuilder}.
 */
public class XmlXContentFactory {

    private static int GUESS_HEADER_LENGTH = 20;

    /**
     * Returns a content builder using JSON format ({@link XmlXContentType#JSON}.
     */
    public static XmlXContentBuilder jsonBuilder() throws IOException {
        return contentBuilder(XmlXContentType.JSON);
    }

    /**
     * Constructs a new json builder that will output the result into the provided output stream.
     */
    public static XmlXContentBuilder jsonBuilder(OutputStream os) throws IOException {
        return new XmlXContentBuilder(JsonXContent.jsonXContent, os);
    }

    /**
     * Returns a content builder using SMILE format ({@link XmlXContentType#SMILE}.
     */
    public static XmlXContentBuilder smileBuilder() throws IOException {
        return contentBuilder(XmlXContentType.SMILE);
    }

    /**
     * Constructs a new json builder that will output the result into the provided output stream.
     */
    public static XmlXContentBuilder smileBuilder(OutputStream os) throws IOException {
        return new XmlXContentBuilder(SmileXContent.smileXContent, os);
    }

    /**
     * Constructs a new yaml builder that will output the result into the provided output stream.
     */
    public static XmlXContentBuilder yamlBuilder(OutputStream os) throws IOException {
        return new XmlXContentBuilder(YamlXContent.yamlXContent, os);
    }

    /**
     * Constructs a new xml builder using XML.
     */
    public static XmlXContentBuilder xmlBuilder() throws IOException {
        XmlXParams params = new XmlXParams();
        return XmlXContent.contentBuilder(params);
    }

    /**
     * Constructs a new xml builder using XML.
     */
    public static XmlXContentBuilder xmlBuilder(XmlXParams params) throws IOException {
        return XmlXContent.contentBuilder(params);
    }

    /**
     * Constructs a new xml builder that will output the result into the provided output stream.
     */
    public static XmlXContentBuilder xmlBuilder(OutputStream os) throws IOException {
        return new XmlXContentBuilder(XmlXContent.xmlXContent(), os);
    }

    /**
     * Constructs a xcontent builder that will output the result into the provided output stream.
     */
    public static XmlXContentBuilder contentBuilder(XmlXContentType type, OutputStream outputStream) throws IOException {
        if (type == XmlXContentType.JSON) {
            return jsonBuilder(outputStream);
        } else if (type == XmlXContentType.SMILE) {
            return smileBuilder(outputStream);
        } else if (type == XmlXContentType.YAML) {
            return yamlBuilder(outputStream);
        } else if (type == XmlXContentType.XML) {
            return xmlBuilder(outputStream);
        }
        throw new ElasticsearchIllegalArgumentException("No matching content type for " + type);
    }

    /**
     * Returns a binary content builder for the provided content type.
     */
    public static XmlXContentBuilder contentBuilder(XmlXContentType type) throws IOException {
        if (type == XmlXContentType.XML) {
            XmlXParams params = new XmlXParams();
            return XmlXContent.contentBuilder(params);
        }
        throw new ElasticsearchIllegalArgumentException("No matching content type for " + type);
    }

    /**
     * Returns the {@link org.elasticsearch.common.xcontent.XContent} for the provided content type.
     */
    public static XContent xContent(XmlXContentType type) {
        return type.xContent();
    }

    /**
     * Guesses the content type based on the provided char sequence.
     */
    public static XmlXContentType xContentType(CharSequence content) {
        int length = content.length() < GUESS_HEADER_LENGTH ? content.length() : GUESS_HEADER_LENGTH;
        if (length == 0) {
            return null;
        }
        char first = content.charAt(0);
        if (first == '{') {
            return XmlXContentType.JSON;
        }
        // Should we throw a failure here? Smile idea is to use it in bytes....
        if (length > 2 && first == SmileConstants.HEADER_BYTE_1 && content.charAt(1) == SmileConstants.HEADER_BYTE_2 && content.charAt(2) == SmileConstants.HEADER_BYTE_3) {
            return XmlXContentType.SMILE;
        }
        if (length > 2 && first == '-' && content.charAt(1) == '-' && content.charAt(2) == '-') {
            return XmlXContentType.YAML;
        }
        for (int i = 0; i < length; i++) {
            char c = content.charAt(i);
            if (c == '{') {
                return XmlXContentType.JSON;
            }
            if (c == '<') {
                return XmlXContentType.XML;
            }
        }
        return null;
    }

    /**
     * Guesses the content (type) based on the provided char sequence.
     */
    public static XContent xContent(CharSequence content) {
        XmlXContentType type = xContentType(content);
        if (type == null) {
            throw new ElasticsearchParseException("Failed to derive xcontent from " + content);
        }
        return xContent(type);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContent xContent(byte[] data) {
        return xContent(data, 0, data.length);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XContent xContent(byte[] data, int offset, int length) {
        XmlXContentType type = xContentType(data, offset, length);
        if (type == null) {
            throw new ElasticsearchParseException("Failed to derive xcontent from (offset=" + offset + ", length=" + length + "): " + Arrays.toString(data));
        }
        return xContent(type);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XmlXContentType xContentType(byte[] data) {
        return xContentType(data, 0, data.length);
    }

    /**
     * Guesses the content type based on the provided input stream.
     */
    public static XmlXContentType xContentType(InputStream si) throws IOException {
        int first = si.read();
        if (first == -1) {
            return null;
        }
        int second = si.read();
        if (second == -1) {
            return null;
        }
        if (first == SmileConstants.HEADER_BYTE_1 && second == SmileConstants.HEADER_BYTE_2) {
            int third = si.read();
            if (third == SmileConstants.HEADER_BYTE_3) {
                return XmlXContentType.SMILE;
            }
        }
        if (first == '{' || second == '{') {
            return XmlXContentType.JSON;
        }
        if (first == '-' && second == '-') {
            int third = si.read();
            if (third == '-') {
                return XmlXContentType.YAML;
            }
        }
        if (first == '<' && second == '?') {
            int third = si.read();
            if (third == 'x') {
                return XmlXContentType.XML;
            }
        }
        for (int i = 2; i < GUESS_HEADER_LENGTH; i++) {
            int val = si.read();
            if (val == -1) {
                return null;
            }
            if (val == '{') {
                return XmlXContentType.JSON;
            }
        }
        return null;
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XmlXContentType xContentType(byte[] data, int offset, int length) {
        return xContentType(new BytesArray(data, offset, length));
    }

    public static XContent xContent(BytesReference bytes) {
        XmlXContentType type = xContentType(bytes);
        if (type == null) {
            throw new ElasticsearchParseException("Failed to derive xcontent from " + bytes);
        }
        return xContent(type);
    }

    /**
     * Guesses the content type based on the provided bytes.
     */
    public static XmlXContentType xContentType(BytesReference bytes) {
        int length = bytes.length() < GUESS_HEADER_LENGTH ? bytes.length() : GUESS_HEADER_LENGTH;
        if (length == 0) {
            return null;
        }
        byte first = bytes.get(0);
        if (first == '{') {
            return XmlXContentType.JSON;
        }
        if (length > 2 && first == SmileConstants.HEADER_BYTE_1 && bytes.get(1) == SmileConstants.HEADER_BYTE_2 && bytes.get(2) == SmileConstants.HEADER_BYTE_3) {
            return XmlXContentType.SMILE;
        }
        if (length > 2 && first == '-' && bytes.get(1) == '-' && bytes.get(2) == '-') {
            return XmlXContentType.YAML;
        }
        if (length > 2 && first == '<' && bytes.get(1) == '?' && bytes.get(2) == 'x') {
            return XmlXContentType.XML;
        }
        for (int i = 0; i < length; i++) {
            if (bytes.get(i) == '{') {
                return XmlXContentType.JSON;
            }
        }
        return null;
    }
}
