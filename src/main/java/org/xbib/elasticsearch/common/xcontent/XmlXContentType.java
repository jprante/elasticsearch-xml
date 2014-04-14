

package org.xbib.elasticsearch.common.xcontent;

import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.xcontent.smile.SmileXContent;
import org.elasticsearch.common.xcontent.yaml.YamlXContent;

import org.xbib.elasticsearch.common.xcontent.xml.XmlXContent;

/**
 * The content type of {@link org.elasticsearch.common.xcontent.XContent}.
 */
public enum XmlXContentType {

    /**
     * A JSON based content type.
     */
    JSON(0) {
        @Override
        public String restContentType() {
            return "application/json; charset=UTF-8";
        }

        @Override
        public String shortName() {
            return "json";
        }

        @Override
        public XContent xContent() {
            return JsonXContent.jsonXContent;
        }
    },
    /**
     * The jackson based smile binary format. Fast and compact binary format.
     */
    SMILE(1) {
        @Override
        public String restContentType() {
            return "application/smile";
        }

        @Override
        public String shortName() {
            return "smile";
        }

        @Override
        public XContent xContent() {
            return SmileXContent.smileXContent;
        }
    },
    /**
     * The YAML format.
     */
    YAML(2) {
        @Override
        public String restContentType() {
            return "application/yaml";
        }

        @Override
        public String shortName() {
            return "yaml";
        }

        @Override
        public XContent xContent() {
            return YamlXContent.yamlXContent;
        }
    },
    /**
     * The XML format.
     */
    XML(3) {
        @Override
        public String restContentType() {
            return "application/xml";
        }

        @Override
        public String shortName() {
            return "xml";
        }

        @Override
        public XContent xContent() {
            return XmlXContent.xmlXContent();
        }
    };

    public static XmlXContentType fromRestContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        if ("application/json".equals(contentType) || "json".equalsIgnoreCase(contentType)) {
            return JSON;
        }
        if ("application/smile".equals(contentType) || "smile".equalsIgnoreCase(contentType)) {
            return SMILE;
        }
        if ("application/yaml".equals(contentType) || "yaml".equalsIgnoreCase(contentType)) {
            return YAML;
        }
        if ("application/xml".equals(contentType) || "xml".equalsIgnoreCase(contentType)) {
            return XML;
        }
        return null;
    }

    private int index;

    XmlXContentType(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }

    public abstract String restContentType();

    public abstract String shortName();

    public abstract XContent xContent();
}
